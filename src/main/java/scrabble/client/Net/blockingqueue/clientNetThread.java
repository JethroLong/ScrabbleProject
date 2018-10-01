package scrabble.client.Net.blockingqueue;

import scrabble.protocols.Pack;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Hashtable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class clientNetThread implements Runnable {
    private Socket server;
    private Hashtable clientDataHash;
    private Hashtable clientNameHash;
    private boolean isClientClosed = false;
    private final BlockingQueue<Pack> toNetPutMsg;

    public clientNetThread(Socket server, BlockingQueue toNetPutMsg) {
        this.server = server;
        this.toNetPutMsg = toNetPutMsg;
    }

    @Override
    public void run() {

        BufferedReader inputStream;
        try {

            inputStream = new BufferedReader(new InputStreamReader(server.getInputStream()));
            while (true){
                String message = inputStream.readLine();
                Pack msg = new Pack(-1,message);
                System.out.println(message);
                toNetPutMsg.put(msg);
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (!isClientClosed){
                closeClient();
            }
        }
    }
    private void closeClient(){}
}
