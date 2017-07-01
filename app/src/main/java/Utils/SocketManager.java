package Utils;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by xcaluser on 28/6/17.
 */

public class SocketManager {



    private Socket socket = null;

    private DataInputStream inputStream = null;

    private DataOutputStream outputStream = null;

    public SocketManager(String host, int port) {
        try {

            Log.e("SOCKETMANAGER" , "assiging host and port " + host + " " + port);
            socket = new Socket(host,port);

            inputStream = new DataInputStream(socket.getInputStream());

            outputStream = new DataOutputStream(socket.getOutputStream());
        }catch(Exception e){


            Log.e("SOCKETMANAGER" , "Error : " + e.fillInStackTrace() + " host " + host + " " + port);
        }
    }

    public void writeMessage(String message) throws IOException {


        if (message != null) {
            Log.e("SOCKETMANAGER" , "writing message  ");
            outputStream.writeUTF(message);
        }
    }

    public String readMessage() throws IOException {

        String response = inputStream.readUTF();
        return response;
    }

}
