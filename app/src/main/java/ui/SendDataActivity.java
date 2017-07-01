package ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wifidirect_datatransfer.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.util.ArrayList;

import services.RefreshService;

import static Utils.Constants.COMMAND_2;
import static com.wifidirect_datatransfer.GlobalApplication.ContactPort;
import static com.wifidirect_datatransfer.GlobalApplication.DocumentPort;
import static com.wifidirect_datatransfer.GlobalApplication.FilePathJson;
import static com.wifidirect_datatransfer.GlobalApplication.ImagePort;
import static com.wifidirect_datatransfer.GlobalApplication._rxBus;
import static com.wifidirect_datatransfer.GlobalApplication.feedbackHost;
import static com.wifidirect_datatransfer.GlobalApplication.feedbackPort;
import static com.wifidirect_datatransfer.GlobalApplication.list_Sender;
import static com.wifidirect_datatransfer.GlobalApplication.mode;


/**
 * Created by xcaluser on 14/6/17.
 */

public class SendDataActivity extends AppCompatActivity {


    TextView txt_status,txt_deviceName;

    String host;
    int port;

    LinearLayout doneLayout;



    public static final int BUFFER_SIZE = 1024; //1024
    private byte[] buffer;





    public ArrayList<String> list_Contact = new ArrayList<>();
    public ArrayList<String> list_Image = new ArrayList<>();
    public ArrayList<String> list_Document = new ArrayList<>();

    MyReceiver myReceiver;

    public static int iCount =0;
    public long totalBytes = 0;

    TextView dataCounttext_view;



    static long currentCount = 0;



    TextView txtTimerCount;



    Handler handlerTimeCount;
    int Seconds, Minutes, MilliSeconds ;
    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_senddata);


        txt_status = (TextView)findViewById(R.id.datasendinglogs_textView);
        txt_deviceName = (TextView) findViewById(R.id.datasendingserviceName_textView);
        doneLayout = (LinearLayout)findViewById(R.id.linearViewbottom);
        dataCounttext_view = (TextView) findViewById(R.id.dataCounttext_view);

        doneLayout.setVisibility(View.INVISIBLE);
        txtTimerCount=(TextView) findViewById(R.id.chronometer);

        handlerTimeCount = new Handler() ;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String strhost = preferences.getString("HOST", "NA");
        if(!strhost.equalsIgnoreCase("NA"))
        {
            host = strhost;
        }
        String device = preferences.getString("DEVICE", "NA");
        if(!device.equalsIgnoreCase("NA"))
        {
            txt_deviceName.setText("Sending Files to " + device);
        }
        int strport = preferences.getInt("PORT", 0);
        if(strport!=0)
        {
            port = strport;
        }



        Log.e("Senddata" , "host " + host);
        Log.e("Senddata" , "port " + port);
        Log.e("Senddata" , "list size " + list_Sender.size());


        iCount = list_Sender.size();

        for(int i=0;i<list_Sender.size();i++){
            Log.e("Senddata" , " path " + list_Sender.get(i));

            if(list_Sender.get(i).endsWith(".vcf")){

                list_Contact.add(list_Sender.get(i));

                File file  = new File(list_Sender.get(i));
                totalBytes = totalBytes+ file.length();


            }
            if(list_Sender.get(i).endsWith(".jpg")||list_Sender.get(i).endsWith(".png")||list_Sender.get(i).endsWith(".JPG")
                    || list_Sender.get(i).endsWith(".PNG")){

                Log.e("Senddata" , " image file  " + list_Sender.get(i));
                File file  = new File(list_Sender.get(i));
                totalBytes = totalBytes+ file.length();
                list_Image.add(list_Sender.get(i));
            }
            else if((list_Sender.get(i).endsWith(".txt"))
                    ||(list_Sender.get(i).endsWith(".pdf"))
                    ||(list_Sender.get(i).endsWith(".docx"))
                    || (list_Sender.get(i).endsWith(".xlsx"))
                    || (list_Sender.get(i).endsWith(".html"))
                    || (list_Sender.get(i).endsWith(".xml"))){


                File file  = new File(list_Sender.get(i));
                totalBytes = totalBytes+ file.length();
                list_Document.add(list_Sender.get(i));

            }




        }



        SendFilePathInfoJson();

        doneLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {








            }
        });


    }

    private void SendFilePathInfoJson() {




        FilePathJson = createFilepathJson();


        _rxBus.send(COMMAND_2);



        if(mode.equalsIgnoreCase("ios")){

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    //Your task it will execute at 1 time only...

                    Log.e("SENDIMAGE", "delayed handler run");


                    sendTestData();
                }
            }, 200);

        }


    }

    private void sendTestData() {




        Log.e("SENDTESTDATA" , "sendtestdata");

        StartTimer();

        if(list_Contact.size()>0){

            new SendContact().execute();
        }
        if(list_Image.size()>0){

            new SendImage().execute();
        }
        if(list_Document.size()>0){
            Log.e("SENDTESTDATA" , "list_Document>0");
            new SendDocument().execute();

        }






    }


    private String createFilepathJson() {




        String strJson = "NA";
        try {
            String strFileCount = String.valueOf(list_Sender.size());
            JSONObject jsonFinal = new JSONObject();


            JSONObject connectInfoJson = new JSONObject();


            connectInfoJson.put("host", feedbackHost);
            connectInfoJson.put("infoport", String.valueOf(feedbackPort));
            connectInfoJson.put("FileCount" ,strFileCount);
            connectInfoJson.put("TotalSize" , String.valueOf(totalBytes));

            jsonFinal.put("ConnectInfo", connectInfoJson);

            ////////////  contact file info


            if(list_Contact.size()>0) {
                JSONArray contactFileArray = new JSONArray();

                JSONObject json = new JSONObject();
                File f = new File(list_Contact.get(0));
                json.put("name", f.getName());
                json.put("size", String.valueOf(f.length()));

                contactFileArray.put(json);

                jsonFinal.put("contactfile" ,contactFileArray);
            }



            ////////// image file list and info

            if(list_Image.size()>0) {
                JSONArray imageFileArray = new JSONArray();


                Log.e("JSONADD" , "Image list size "  + list_Image.size());
                for (int image = 0; image < list_Image.size(); image++) {

                    File f = new File(list_Image.get(image));
                    JSONObject jsonImage = new JSONObject();
                    Log.e("JSONADD" , "Image file name " + f.getName());
                    jsonImage.put("name", f.getName());
                    jsonImage.put("size", String.valueOf(f.length()));
                    imageFileArray.put(jsonImage);
                }


                jsonFinal.put("imagefile", imageFileArray);
            }

            //////////// video file list and info




            //////////// document file list and info

            if(list_Document.size()>0) {
                JSONArray docFileArray = new JSONArray();


                for (int image = 0; image < list_Document.size(); image++) {
                    JSONObject jsonDoc = new JSONObject();
                    File f = new File(list_Document.get(image));
                    jsonDoc.put("name", f.getName());
                    jsonDoc.put("size", String.valueOf(f.length()));
                    docFileArray.put(jsonDoc);
                }


                jsonFinal.put("documentfile", docFileArray);
            }

            //////////// apps apk  file list and info










            strJson = jsonFinal.toString();

            Log.e("JSON" ,"" + strJson);

        } catch (Exception e) {

        }


        return strJson;


    }


    public class SendContact extends AsyncTask<Void,Void,Void> {


        @Override
        protected Void doInBackground(Void... params) {

            Log.e("MULTIDATA" , "send contact doinbackground");



            sendLargeContactFile();


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Log.e("MULTIDATA" , "send contact onpost");

            File contactFile = new File(list_Contact.get(0));

            txt_status.append("\n" + contactFile.getName() + " Sent successfully");
            iCount--;
            if(iCount==0){

                txt_status.append("\n"+"\n" + " File sending done");
                doneLayout.setVisibility(View.VISIBLE);

                StopTimer();
            }
            if(contactFile.exists()){

                contactFile.delete();
            }


        }
    }
    public class SendImage extends AsyncTask<Void,Void,Void> {


        @Override
        protected Void doInBackground(Void... params) {

            Log.e("MULTIDATA" , "send image doinbackground");


            sendLargeImageFile();



            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Log.e("MULTIDATA" , "send image onpost");

            File file  = new File(list_Image.get(0));

            txt_status.append("\n" + file.getName() + " Sent successfully");
            list_Image.remove(0);
            iCount--;
            if(iCount==0){
                doneLayout.setVisibility(View.VISIBLE);
                txt_status.append("\n"+"\n" + " File sending done");
                StopTimer();
            }
            if(list_Image.size()>0){


                sendImageFiles();

            }



        }
    }
    public class SendDocument extends AsyncTask<Void,Void,Void> {


        @Override
        protected Void doInBackground(Void... params) {

            Log.e("MULTIDATA" , "SendDocument doinbackground");


            sendLargeDocumentFile();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Log.e("MULTIDATA" , "SendDocument onpost");
            File file  = new File(list_Document.get(0));

            txt_status.append("\n" + file.getName() + " Sent successfully");
            list_Document.remove(0);
            iCount--;
            if(iCount==0){
                doneLayout.setVisibility(View.VISIBLE);
                txt_status.append("\n"+"\n" + " File sending done");
                StopTimer();
            }
            if(list_Document.size()>0){


                sendDocumentFiles();


            }

        }
    }
    private void sendImageFiles() {



        new SendImage().execute();

    }
    private void sendDocumentFiles() {



        new SendDocument().execute();

    }

    public void sendLargeContactFile(){
        Log.e("SENDDATA" , "send contact vcf file");

        File fileContact = new File(list_Contact.get(0));

        buffer = new byte[BUFFER_SIZE];
        try {
            Socket socket = new Socket(host, ContactPort);
            BufferedInputStream in =
                    new BufferedInputStream(
                            new FileInputStream(fileContact));

            BufferedOutputStream out =
                    new BufferedOutputStream(socket.getOutputStream());


            int len = 0;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);

                currentCount = currentCount+len;



                Log.e("SENDDATA" , "Contact vcf--------------------");
            }
            in.close();
            out.flush();
            out.close();
            socket.close();
            socket.close();
            System.out.println("\nDone!");
        }
        catch(Exception e){

            Log.e("LARGEFILE" , "Exception " + e.getMessage());
        }

    }

    public void sendLargeImageFile(){
        Log.e("SENDDATA" , "send image  file");

        File fileContact = new File(list_Image.get(0));

        buffer = new byte[BUFFER_SIZE];
        try {
            Socket socket = new Socket(host, ImagePort);


            BufferedInputStream in =
                    new BufferedInputStream(
                            new FileInputStream(fileContact));

            BufferedOutputStream out =
                    new BufferedOutputStream(socket.getOutputStream());


            int len = 0;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
                currentCount = currentCount+len;


                Log.e("SENDDATA" , "image --------------------");



            }

            in.close();
            out.flush();
            out.close();


            socket.close();

            System.out.println("\nDone!");
        }
        catch(Exception e){

            Log.e("LARGEFILE" , "Exception " + e.getMessage());
        }

    }


    public void sendLargeDocumentFile(){
        Log.e("SENDDATA" , "send Document  file port " + DocumentPort);
        Log.e("SENDDATA" , "send Document  file  " + list_Document.get(0));
        File fileContact = new File(list_Document.get(0));

        buffer = new byte[BUFFER_SIZE];
        try {
            Socket socket = new Socket(host, DocumentPort);
            BufferedInputStream in =
                    new BufferedInputStream(
                            new FileInputStream(fileContact));

            BufferedOutputStream out =
                    new BufferedOutputStream(socket.getOutputStream());


            int len = 0;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
                currentCount = currentCount+len;

                Log.e("SENDDATA" , "Document --------------------");
            }
            in.close();
            out.flush();
            out.close();
            socket.close();
            socket.close();
            System.out.println("\nDone!");
        }
        catch(Exception e){

            Log.e("LARGEFILE" , "Exception " + e.getMessage());
        }

    }




    /*@Override
    public void onBackPressed() {
        super.onBackPressed();
    }

*/

    private void registerMyReceiver(){

        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RefreshService.SENDMESAGGE);
        registerReceiver(myReceiver, intentFilter);
    }


    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {


            if (arg1.hasExtra("message")) {

                Log.e("RECEIVERDATA" , "Message from service " + arg1.getStringExtra("message"));
                if(arg1.getStringExtra("message").equalsIgnoreCase("startdatatransfer"));
                {


                    sendTestData();

                }

            }
            if (arg1.hasExtra("messagecount")) {

                Log.e("RECEIVERDATA" , "Message from service " + arg1.getStringExtra("messagecount"));

                if(arg1.getStringExtra("messagecount").contains("DataCount"));
                {


                    updateUi(arg1.getStringExtra("messagecount"));

                }
            }
        }
    }

    public void updateUi(String strCount){

        Log.e("DATACOUNTACTIVITY" , "strcount " + strCount );
        String[] dataCountArr = strCount.split(":");

        Log.e("DATACOUNTACTIVITY" , "strcount [1] " + dataCountArr[1] );


        dataCounttext_view.setText(dataCountArr[1]);


    }



    private void StartTimer() {

        try {

            StartTime = SystemClock.uptimeMillis();
            handlerTimeCount.postDelayed(runnable, 0);


        } catch (IllegalStateException e){

        }

    }
    public Runnable runnable = new Runnable() {

        public void run() {

            MillisecondTime = SystemClock.uptimeMillis() - StartTime;

            UpdateTime = TimeBuff + MillisecondTime;

            Seconds = (int) (UpdateTime / 1000);

            Minutes = Seconds / 60;

            Seconds = Seconds % 60;

            MilliSeconds = (int) (UpdateTime % 1000);

            dataCounttext_view.setText(currentCount+ "/" + totalBytes);
            txtTimerCount.setText("" + Minutes + ":"
                    + String.format("%02d", Seconds) + ":"
                    + String.format("%03d", MilliSeconds));

            handlerTimeCount.postDelayed(this, 0);
        }

    };
    private void StopTimer(){


        if(handlerTimeCount!=null) {
            handlerTimeCount.removeCallbacks(runnable);

            iCount =0;

            if(totalBytes>0) {
                String bytesconverted = ByteConversion(totalBytes);

                StringBuilder stringBuilder = new StringBuilder();

                int h = (int) ((MilliSeconds / 1000) / 3600);


                if (h > 0) {

                    stringBuilder.append(h + " hours ");


                }
                if (Minutes > 0) {

                    stringBuilder.append(Minutes + " minutes ");
                }
                if (Seconds > 0 && MilliSeconds>0) {

                    stringBuilder.append(Seconds + "." + MilliSeconds + " seconds ");
                }
                if (MilliSeconds > 0 && Seconds<=0) {




                    stringBuilder.append(MilliSeconds + " Milliseconds ");


                }


                dataCounttext_view.setText(bytesconverted + " in " + stringBuilder.toString());

            }


            Log.e("TIMEROPERATION" , "updateTime " + UpdateTime);
            if(UpdateTime>0) {
                float sizeInMb = (float)totalBytes / (1024 * 1024);

                float speed = (float) (sizeInMb / UpdateTime) * 1000;

                Log.e("TIMEROPERATION" , "size " + sizeInMb);
                Log.e("TIMEROPERATION" , "miliseconds " + MilliSeconds);

                txtTimerCount.setText("Speed: " + speed + " MB/s");

            }









        }

    }

    public String ByteConversion(long bytes){



        if(bytes < 1024)
            return bytes + " bytes";

        float bytesKb = (float) bytes / 1024;
        if(bytesKb < 1024)
            return bytesKb + " KB";

        float bytesMb = (float) bytesKb / 1024;
        if(bytesMb < 1024)
            return bytesMb + " MB";

        float bytesGb = (float) bytesMb / 1024;
        if(bytesGb < 1024)
            return bytesGb + " GB";

        return bytes + " bytes";
    }


    @Override
    protected void onPause() {
        super.onPause();

        StopTimer();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(myReceiver); //unregister my receiver...
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerMyReceiver();


    }


}