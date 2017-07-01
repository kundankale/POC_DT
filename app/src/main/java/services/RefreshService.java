package services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.wifidirect_datatransfer.GlobalApplication;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import Utils.RxBusApi;
import Utils.SocketManager;
import rx.Observer;

import static Utils.Constants.COMMAND_1;
import static Utils.Constants.COMMAND_2;
import static Utils.Constants.RECEIVER_HOST;
import static Utils.Constants.RECEIVER_PORT;
import static com.wifidirect_datatransfer.GlobalApplication.ContactPort;
import static com.wifidirect_datatransfer.GlobalApplication.DocumentPort;
import static com.wifidirect_datatransfer.GlobalApplication.FilePathJson;
import static com.wifidirect_datatransfer.GlobalApplication.ImagePort;
import static com.wifidirect_datatransfer.GlobalApplication._rxBus;
import static com.wifidirect_datatransfer.GlobalApplication.feedbackHost;
import static com.wifidirect_datatransfer.GlobalApplication.feedbackPort;


/**
 * Created by xcaluser on 14/6/17.
 */

public class RefreshService extends Service {

    public final static String SENDMESAGGE = "passMessage";
    public static final String TAG = "BACKSERVICE";

    private ConnectionListenerFeedBacksocketManager connListenerFeedBackSocketManager;
    public String response="NA";

    public String messageToSend = "NA";


    SocketManager mSocketManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)  {

        Log.e(TAG,"Service on startcommand");






        if(_rxBus==null){

            _rxBus = new RxBusApi();
            _rxBus.toObserverable().startWith("Bus Started").subscribe(myObserver);

        }






        return START_STICKY;
    }


    Observer<Object> myObserver = new Observer<Object>() {
        @Override
        public void onCompleted() {

            Log.e(TAG,"DataManager Observer OnComplete " );



        }

        @Override
        public void onError(Throwable e) {


            Log.e(TAG,"DataManager Observer on error " );
        }

        @Override
        public void onNext(Object s) {

            Log.e(TAG,"DataManager Observer OnNext " + s.toString() );



            if(s.toString().equals(COMMAND_1)){
                mSocketManager= null;
                messageToSend = createCommandJson();
                new AssignTask().execute();








                new SendCommandOne().execute();


            }
            if(s.toString().equals(COMMAND_2)){
                Log.e("SOCKETMANAGER" , "sending command 2 data");

                mSocketManager= null;

                new AssignTask().execute();
                messageToSend = FilePathJson;


                new SendCommandOne().execute();


            }





        }
    };


    public class AssignTask extends AsyncTask<Void,Void,Void> {


        @Override
        protected Void doInBackground(Void... params) {

            mSocketManager = new SocketManager(RECEIVER_HOST,RECEIVER_PORT);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

               if(connListenerFeedBackSocketManager!=null) {
            if (connListenerFeedBackSocketManager.isAlive()) {
                connListenerFeedBackSocketManager.interrupt();
                connListenerFeedBackSocketManager.tearDown();
                connListenerFeedBackSocketManager = null;
            }
        }

        connListenerFeedBackSocketManager = new ConnectionListenerFeedBacksocketManager(getApplicationContext(), RECEIVER_PORT );
        connListenerFeedBackSocketManager.start();
        }
    }

    private String createCommandJson() {


        String strJson = "NA";
        try {
            JSONObject json = new JSONObject();
            JSONObject manJson = new JSONObject();
            manJson.put("CommandKey", COMMAND_1);
            manJson.put("SenderType", "Android");


            json.put("Command", manJson);

            strJson = json.toString();
        } catch (Exception e) {

        }



        return strJson;


    }


    public class SendCommandOne extends AsyncTask<Void,Void,Void> {


        @Override
        protected Void doInBackground(Void... params) {



           // sendCommandData(messageToSend);

            Log.e("SOCKETMANAGER" , "SendCommandOne ");
            try {
                mSocketManager.writeMessage(messageToSend);
            }catch(Exception e){

                Log.e("SOCKETMANAGER" , "write message error " + e.getMessage());
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);



        }
    }

    private void sendCommandData(String messageToSend) {



        Log.e("SENDFEEDBACKPORT", "sendCommandData messageToSend " + messageToSend);


        Log.e("SENDFEEDBACKPORT", "sendCommandData sendMessageFile host " + RECEIVER_HOST);
        Log.e("SENDFEEDBACKPORT", "sendCommandData sendMessageFile port " + RECEIVER_PORT);

        Socket socket = null;


        DataInputStream dataInputStreamFeedback = null;
        DataOutputStream dataOutputStreamFeedback = null;
        try {
            socket = new Socket(RECEIVER_HOST, RECEIVER_PORT);


            dataOutputStreamFeedback = new DataOutputStream(
                    socket.getOutputStream());
            dataInputStreamFeedback = new DataInputStream(socket.getInputStream());


            dataOutputStreamFeedback.writeUTF(messageToSend);








            response  = dataInputStreamFeedback.readUTF();


            Log.e(TAG, "Response of command " + response);



        } catch (IOException e) {

            Log.e("SENDFEEDBACKPORT", "sendCommandData error " + e.getMessage());
            e.printStackTrace();




        } finally {
            if (dataInputStreamFeedback != null) {
                try {
                    dataInputStreamFeedback.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (dataOutputStreamFeedback != null) {
                try {
                    dataOutputStreamFeedback.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }



    private String createJson() {

        String strJson = "NA";
        try {
            JSONObject json = new JSONObject();
            JSONObject manJson = new JSONObject();
            manJson.put("SenderHost", feedbackHost);
            manJson.put("SenderPort", feedbackPort);

            json.put("Command", manJson);

            strJson = json.toString();
        } catch (Exception e) {

        }



        return strJson;

    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"Service onDestroy");


        _rxBus.complete();

        _rxBus = null;

        try {


            if(connListenerFeedBackSocketManager!=null) {
                if (connListenerFeedBackSocketManager.isAlive()) {
                    connListenerFeedBackSocketManager.interrupt();
                    connListenerFeedBackSocketManager.tearDown();
                    connListenerFeedBackSocketManager = null;
                }
            }



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void passMessageToActivity(String message){
        Intent intent = new Intent();
        intent.setAction(SENDMESAGGE);
        intent.putExtra("message",message);
        sendBroadcast(intent);
    }
    private void passMessageToActivityDataCount(String message){
        Intent intent = new Intent();
        intent.setAction(SENDMESAGGE);
        intent.putExtra("messagecount",message);
        sendBroadcast(intent);
    }

    public class ConnectionListenerFeedBacksocketManager extends Thread {

        int mPort;

        private boolean acceptRequests = true;

        public ConnectionListenerFeedBacksocketManager(Context context, int port) {
            Log.d(TAG, "ConnectionListenerFeedBack constructor : ");
            mPort = port;
        }

        @Override
        public void run() {

            while(true) {
                try {

                    response="";

                    response = mSocketManager.readMessage();

                    Log.e(TAG, "message from receiver " + response);


                    if(response.contains("ArtifectPortInfo")) {
                        try {

                            GlobalApplication.mode = "ANDROID";

                            JSONObject reader = new JSONObject(response);

                            JSONObject sys = reader.getJSONObject("ArtifectPortInfo");
                            ContactPort = Integer.parseInt(sys.getString("ContactPort"));
                            ImagePort = Integer.parseInt(sys.getString("ImagePort"));
                            DocumentPort = Integer.parseInt(sys.getString("DocumentPort"));
                            Log.e("KKKKK", " parsing json ContactPort" + ContactPort);
                            Log.e("KKKKK", " parsing json ImagePort" + ImagePort);
                            Log.e("KKKKK", " parsing json DocumentPort" + DocumentPort);




                        } catch (Exception e) {


                            Log.e("REFRESHSERVICE" , "Error in parsing artifectPortInfo json " + e.getMessage());

                        }





                    }else if(response.contains("FileInfoResponse")) {
                        try {
                            Log.e("KKKKK", "RefreshService Received data  parsing json FileInfoResponse ");

                            JSONObject reader = new JSONObject(response);

                            JSONObject sys = reader.getJSONObject("FileInfoResponse");
                            String status = sys.getString("Status");
                            Log.e("KKKKK", " parsing json FileInfoResponse" + status);
                            if (status.equalsIgnoreCase("received")) {

                                passMessageToActivity("StartDataTransfer");
                            }else if(status.equalsIgnoreCase("done")){

                                closelistningThread();
                            }
                        }
                        catch(Exception e){
                            Log.e("REFRESHSERVICE" , "Error in parsing FileInfoResponse json " + e.getMessage());
                        }

                    }else if(response.contains("DataCount")){
                        Log.e("KKKKK", "RefreshService Received data DataCount " );

                        String[] dataCountArr = response.split(":");
                        Log.e("KKKKK", "Refresh Service data count is "  + dataCountArr[1] );
                        passMessageToActivityDataCount(response);
                    }








                } catch (Exception e) {
                    Log.e(TAG, Build.MANUFACTURER + ": Connection listener EXCEPTION. " + e.toString());
                    e.printStackTrace();
                }

                if(!acceptRequests){

                    break;
                }

            }
        }




        public void tearDown() {
            acceptRequests = false;
        }


    }

    private void closelistningThread() {

        Log.e("REFRESHSERVICE" , "closelistningThread");
        try {


            if(connListenerFeedBackSocketManager!=null) {
                if (connListenerFeedBackSocketManager.isAlive()) {
                    connListenerFeedBackSocketManager.interrupt();
                    connListenerFeedBackSocketManager.tearDown();
                    connListenerFeedBackSocketManager = null;
                }
            }



        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
