package com.example.elukapo.wifidirect;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TransferService extends IntentService {

    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_FILE = "com.example.elukapo.wifidirect.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";
    public TransferService(String name) {
        super(name);
    }
    public TransferService() {
        super("TransferService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(MainActivity.TAG, "1");

        Context context = getApplicationContext();
        Log.d(MainActivity.TAG, "2");

        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            Log.d(MainActivity.TAG, "3");

            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
            Log.d(MainActivity.TAG, "4");

            Socket socket = new Socket();
            Log.d(MainActivity.TAG, "5");

            try {
                Log.d(MainActivity.TAG, "Opening client socket - ");
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
                Log.d(MainActivity.TAG, "Client socket - " + socket.isConnected());
                OutputStream stream = socket.getOutputStream();
//                DataOutputStream mDataOutputStream = new DataOutputStream(socket.getOutputStream());
//                mDataOutputStream.writeUTF("ala ma kota");
//                mDataOutputStream.flush();
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                String przyszloOdServera = inputStream.readUTF();
                Log.d(MainActivity.TAG, "Przyszlo od servera " + przyszloOdServera);

/*                Toast.makeText(context,
                        przyszloOdServera, Toast.LENGTH_SHORT).show();*/
                ContentResolver cr = context.getContentResolver();
                InputStream is = null;
                try {
                    is = cr.openInputStream(Uri.parse(fileUri));
                } catch (FileNotFoundException e) {
                    Log.d(MainActivity.TAG, e.toString());
                }
                copyFile(is, stream);
                Log.d(MainActivity.TAG, "Client: Data written");
            } catch (IOException e) {
                Log.e(MainActivity.TAG, e.getMessage());
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(MainActivity.TAG, e.toString());
            return false;
        }
        return true;
    }
}
