package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    //colori
    String colNormale = "\u001B[0m";

    String colGiallo = "\u001B[33m";
    String colNero = "\u001B[30m";
    String colRosso = "\u001B[31m";
    String colVerde = "\u001B[32m";
    String colBlu = "\u001B[34m";
    String colMagenta = "\u001B[35m";
    String colCiano = "\u001B[36m";
    String colBianco = "\u001B[37m";
    String colViola =  "\u001B[35m";
    String colViolaAcceso = "\u001B[95m";


    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;


    public Server(){
        connections = new ArrayList<>();
        done = false;
    }

    //definire il codice che può essere eseguito da più thread 
    @Override 
    public void run() {
        // Codice che verrà eseguito quando il thread verrà creato
        try {
            //server si mette in ascolto sulla porta 9999
            server = new ServerSocket(9999);
            //creo una pool che gestisce i thread
            pool = Executors.newCachedThreadPool();
            while (!done) {
                //stabilisco connessione tra client e server
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                //aggiungo connessione appena stabilita alla lista
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e) { // Gestisci le eccezioni di tipo IOException separatamente
            shutdown();
        }
    }
    public void broadcast(String message){
        for(ConnectionHandler ch: connections){
            if(ch != null){
                ch.sendMessage(message);
            }
        }
    }

    public void shutdown(){
        try{
            done = true;
            if(!server.isClosed()){
                server.close();
            }
            for(ConnectionHandler ch: connections){
                ch.shutdown();
            }
        
        }catch(IOException e){

        }
    }

    class ConnectionHandler implements Runnable{

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;
        public ConnectionHandler(Socket client){
            this.client = client;


        }

        @Override
        public void run(){
            try{
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader((client.getInputStream())));
                
                // SET USERNAME CLIENT
                out.println("[-] - USERNAME: ");
                nickname = in.readLine();
                System.out.println(colGiallo +"[-] - " + nickname + " CONNECTED");
                // AVVISO JOIN CLIENT
                broadcast(colGiallo + "[-] - " + nickname +  " JOINED THE CHAT"+colNormale);
                String message;
                while((message = in.readLine()) != null){
                        // COMMAND CAMBIO NOME
                    if(message.startsWith("/nick")){
                        String[] messageSplit = message.split(" ", 2);
                        if(messageSplit.length == 2){
                            broadcast(colGiallo + "[-] - " + nickname + " RENAMED TO " + messageSplit[1] + colNormale);
                            System.out.println(colRosso + "[!] - " + nickname + " RENAMED TO " + messageSplit[1]+ colNormale);
                            nickname = messageSplit[1];
                            out.println(colGiallo + "[-] - SUCCESSFULLY CHANGED NAME TO " + nickname + colNormale);
                        }else{
                            out.println(colRosso + "[!] - NO NICKNAME PROVIDED" + colNormale);
                        }
                    }else if(message.startsWith("/quit")){
                        // COMMAND QUIT CHAT
                        broadcast(colRosso + "[!] - " + nickname + " LEFT THE CHAT" + colNormale);
                        System.out.println(colRosso +"[!] - " + nickname + " LEFT THE CHAT");
                        shutdown();
                    }else if(message.startsWith("/userlist")){
                        out.println(colGiallo + "[-] - USERLIST " + colNormale);
                        for(ConnectionHandler ch: connections){
                            out.println("      @" + ch.nickname);
                        }
                        out.println("\n______________");
                    }else if(message.startsWith("/help")){
                        out.println(colGiallo + "\n[-] - SLASH COMMANDS\n " + colNormale);
                        out.println("- /userlist\n- /nickname (modify username)\n- /quit\n" );
                        out.println(colGiallo + "\n[-] - PRIVATE MESSAGE\n " + colNormale);
                        out.println("- " + colRosso + "@" + colNormale + "USERNAME message\n______________" );
                    }else if(message.startsWith("@")){
                        // PRIVATE MESSAGE
                        String prvMsg[] = message.split(" ", 2);
                        String dstName = prvMsg[0];
                        dstName = dstName.replaceAll("@", "");
                        System.out.println(dstName);
                        for(ConnectionHandler ch: connections){
                            if(dstName.equals(ch.nickname)){
                                ch.sendMessage("[PRV] @" + nickname + " : " + prvMsg[1]);
                            }    
                        }
                    }else{
                        //MESSAGGIO STANDARD A TUTTI
                        broadcast("["+nickname +"] : " + message);
                    }
                }
            }catch(IOException e){
                shutdown();
            }
            
        }

        public void sendMessage(String message){
            out.println(message);
        }

        public void shutdown(){
            try{
                done = true;
                pool.shutdown();
                in.close();
                out.close();
                if(!client.isClosed()){
                    client.close();
                }
            }catch(IOException e){
                //ignore
            }
        }

    }

    public static void main(String[] args){
        Server server = new Server();
        server.run();
    }
}
