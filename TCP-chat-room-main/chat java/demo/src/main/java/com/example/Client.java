package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable{
    
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


    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;

    @Override
    public void run(){
        try{
            client = new Socket("localhost", 9999);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);
            t.start();

            String inMessage;
            while((inMessage = in.readLine()) != null){
                System.out.println(inMessage);
            }
        }catch(IOException e){
            shutdown();
        }
    }
    
    public void shutdown(){
        done = true;

        try{
            in.close();
            out.close();
            if(!client.isClosed()){
                client.close();
            }
        } catch(IOException e){

        }
    }


    class InputHandler implements Runnable {
        @Override
        public void run(){
            try{
                BufferedReader inReader = new BufferedReader((new InputStreamReader(System.in)));
                while(!done){
                    String message = inReader.readLine();
                    if(message.equals("/quit")){
                        out.println(message);
                        inReader.close();
                        shutdown();
                    }else{
                        out.println(message);
                    }

                }
            }catch(IOException e){
                shutdown();
            }
        }

    }
 public static void main(String[] args) {
    Client client = new Client();
    client.run();
 }
}
