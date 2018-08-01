package com.example.elukapo.wifidirect;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends AsyncTask<Void, Void, String> {
private OutputStream outToServer=null;
private InputStream inFromServer=null;
private DataInputStream inputStream = new DataInputStream(inFromServer);
private Socket clientSocket=null;
private int numberOfTry=0;
private String serverIP=null;
private int serverPort=0;
        Handler myHandler=null;

// constructor. need to specify the ip and port.
// also need a handler instance to be able to show message on UI thread
public Client(String serverIP,int serverPort,Handler myHandler){
        this.serverIP=serverIP;
        this.serverPort=serverPort;
        this.myHandler=myHandler;
        }

private boolean initializeClient(String serverIP,int serverPort){
        try{
            Thread.sleep(1000);
        //create socket and connect to server. get input/ouput streams
        //try several times in case client start first.
        numberOfTry++;
        clientSocket=new Socket(serverIP,serverPort);
        outToServer=clientSocket.getOutputStream();
        inFromServer=clientSocket.getInputStream();
        return true;
        }catch(UnknownHostException e){
        if(numberOfTry<10)initializeClient(serverIP,serverPort);
        else showMessage("Client Error: Cannot Connect to the Server after 10 attempts");
        return false;
        }catch(IOException e){
        showMessage("Client Error: IO Error.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    return false;

}

    private void showMessage(String str){
        //str.replace('\n', '\0');
        Message msg=new Message();
        msg.obj=(Object)str;
        myHandler.sendMessage(msg);
        return;
    }

    @Override
    protected String doInBackground(Void... voids) {
        if(!initializeClient(serverIP, serverPort)){
            Log.d(MainActivity.TAG, "Client Error");
            showMessage("Client Error.");
            return null;
        }

        while(true){
            try {
                if(inputStream != null) {
                    String przyszloOdServera = inputStream.readUTF();
                    Log.d(MainActivity.TAG, "Przyszlo od servera " + przyszloOdServera);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }
}