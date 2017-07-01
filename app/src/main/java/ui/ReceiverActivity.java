package ui;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wifidirect_datatransfer.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import Utils.Constants;
import Utils.CryptoUtil;
import VcfReader.AndroidCustomFieldScribe;
import VcfReader.ContactOperations;
import ezvcard.VCard;
import ezvcard.io.text.VCardReader;
import pojos.ContactPojo;
import pojos.DocPojo;
import pojos.ImagePojo;
import services.GalleryRefreshService;

import static Utils.Constants.createDir;
import static Utils.Constants.key;
import static com.wifidirect_datatransfer.GlobalApplication.FilesFolder;
import static com.wifidirect_datatransfer.GlobalApplication.Folder;
import static com.wifidirect_datatransfer.GlobalApplication.ImageFolder;
import static com.wifidirect_datatransfer.GlobalApplication.Path;
import static com.wifidirect_datatransfer.GlobalApplication.isOnSameNetwork;


/**
 * Created by xcaluser on 14/6/17.
 */

public class ReceiverActivity extends AppCompatActivity implements NsdManager.RegistrationListener {

    TextView txt_publishstatus;


    private ServerSocket _serverSocket, _serverSocketContact, _serverSocketImage, _serverSocketDocument;

    private NsdManager _serviceDiscoverManager;
    private Thread _publishServiceThread;
    private boolean _published;
    public static final String TAG = "NsdHelper";
    public String mServiceName = "Android_PeertoPeer";  //iOS_iPad
    public String mServiceType = "_peertopeer._tcp.";
    int port;


    public static String strType = "C";



    public String response = "NA";
    public final static int FILE_SIZE = 6022386 * 2;

    TextView txtLogs;
    LinearLayout connectToIos, donelayout;
    ImageView refresh_image_view;



    String hotspotName = "MyAp_";


    //
    public static final int BUFFER_SIZE = 1024; //1024
    private byte[] buffer;


    int portContact =0, portImage =0, portDocument =0;

    ConnectionListener connectionListener;
    private ConnectionListenerContact connListenerContact;
    private ConnectionListenerImage connectionListenerImage;
    private ConnectionListenerDocument connectionListenerDocument;

    public ArrayList<ImagePojo> list_Image = new ArrayList<>();
    public ArrayList<DocPojo> list_Document = new ArrayList<>();
    public ArrayList<ContactPojo> list_Contact = new ArrayList<>();


    public static int iCount = 0;

    int imagelength,doclength,contactlength;



    TextView dataelementText;
    long totalCount=0;
    static long currentCount = 0;


    // for dual chk

    DataInputStream dataInputStreamCommand = null;
    DataOutputStream dataOutputStreamCommand = null;

    TextView txtTimerCount;



    Handler handlerTimeCount;
    int Seconds, Minutes, MilliSeconds ;
    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L ;


    public ArrayList<String> list_files_received = new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_receiver_test);


        txt_publishstatus = (TextView) findViewById(R.id.publishstatusTextView);
        txtLogs = (TextView) findViewById(R.id.serviceNametxt);
        dataelementText = (TextView) findViewById(R.id.dataelementText);
        txtTimerCount=(TextView) findViewById(R.id.chronometer);

        handlerTimeCount = new Handler() ;

        connectToIos = (LinearLayout) findViewById(R.id.connectioslayout);
        donelayout = (LinearLayout) findViewById(R.id.done_layout);
        refresh_image_view = (ImageView) findViewById(R.id.refresh_image_view);

        createDir();
        mServiceName = createServiceName();

        _serviceDiscoverManager = (NsdManager) getSystemService(Context.NSD_SERVICE);







        refresh_image_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                publishServiceInit();



            }
        });

        connectToIos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                showConnectMessage();

            }
        });
        donelayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {




            }
        });


    }


    public void publishServiceInit(){

        buffer = new byte[BUFFER_SIZE];




        if (_published) {

            depublishService();

            destroyThreads();




            strType = "C";
        } else {

            txtLogs.append("\n" + "Publishing Service");

            startPublishService();

            strType = "C";
        }

    }

    private void destroyThreads() {


        if(connListenerContact!=null) {
            if (connListenerContact.isAlive()) {
                connListenerContact.interrupt();
                connListenerContact.tearDown();
                connListenerContact = null;
            }
        }
        if(connectionListenerImage!=null) {
            if (connectionListenerImage.isAlive()) {
                connectionListenerImage.interrupt();
                connectionListenerImage.tearDown();
                connectionListenerImage = null;
            }
        }
        if(connectionListenerDocument!=null) {
            if (connectionListenerDocument.isAlive()) {
                connectionListenerDocument.interrupt();
                connectionListenerDocument.tearDown();
                connectionListenerDocument = null;
            }
        }
        if(connectionListener!=null) {
            if (connectionListener.isAlive()) {
                connectionListener.interrupt();
                connectionListener.tearDown();
                connectionListener = null;
            }
        }
    }

    private void showConnectMessage() {


        AlertDialog.Builder builder1 = new AlertDialog.Builder(ReceiverActivity.this);
        builder1.setMessage("Connect To IOS device");
        builder1.setMessage("Please use hotspot created on this device,on Sender's device.");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        strType = "C";
                        depublishService();

                        _published = false;



                        publishServiceInit();


                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();

    }



    @Override
    public void onRegistrationFailed(NsdServiceInfo serviceInfo, final int errorCode) {

        _published = false;
        closeServerSocket();

        runOnUiThread(new Runnable() {
            public void run() {

                txt_publishstatus.setText("Failed to publish a bonjour service, error code: " + errorCode);

                Log.d("network", "failed to publish a bounjour serverice, error code: " + errorCode);
            }
        });
    }

    @Override
    public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {

    }

    @Override
    public void onServiceRegistered(final NsdServiceInfo serviceInfo) {
        final Context context = this;

        Log.d(TAG, "service registered:serviceInfo " + serviceInfo);



        runOnUiThread(new Runnable() {
            public void run() {


                String[] strarr = serviceInfo.getServiceName().split("_", 2);

                txt_publishstatus.append("\n" + "Service " + strarr[1] + " Registered");
            }
        });

        Log.e(TAG, "starting listing thread task " + serviceInfo.getPort());
        startListningIncomingData();
    }

    @Override
    public void onServiceUnregistered(final NsdServiceInfo serviceInfo) {
        final Context context = this;
        _published = false;

        runOnUiThread(new Runnable() {
            public void run() {
                txt_publishstatus.setText("Service depublished");

            }
        });


    }

    private void startListningIncomingData() {


        Log.e(TAG, "startListningIncomingData port . " + port);


        connectionListener = new ConnectionListener(getApplicationContext(), port);
        connectionListener.start();


    }

    private void closeServerSocket() {
        try {
            if (_serverSocket != null) {
                _serverSocket.close();
            }
            if (_serverSocketContact != null) {
                _serverSocketContact.close();
            }
            if (_serverSocketImage != null) {
                _serverSocketImage.close();
            }
            if (_serverSocketDocument != null) {
                _serverSocketDocument.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int startServerSocket() {
        int port = 0;
        // Initialize a server socket on the next available port.
        try {
            _serverSocket = new ServerSocket(0);
            port = _serverSocket.getLocalPort();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.e("network", "server socket created at " + port);
        // Store the chosen port.
        return port;
    }

    private void depublishService() {
        closeServerSocket();
        if (_published) {
            _serviceDiscoverManager.unregisterService(this);
        }

    }

    private String createServiceName() {


        String deviceName = "NA";
        try {
            BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
            deviceName = myDevice.getName();

            hotspotName = hotspotName+deviceName;

        } catch (Exception e) {

        }


        if (deviceName.equals("NA")) {

            deviceName = "Android_PeertoPeer";
        } else {

            deviceName = "Android_" + deviceName;
        }


       // hotspotName = createHotSpotName(deviceName);

        return deviceName;
    }

    private String createHotSpotName(String deviceName) {


        try {
            CryptoUtil cryptoUtil = new CryptoUtil();

            String plain = deviceName;
            hotspotName = cryptoUtil.encrypt(key, plain);


            Log.e("ENCRYPTION" ,"original " + plain);
            Log.e("ENCRYPTION" ,"encrypted  " + hotspotName);




        }
        catch(Exception e){

        }

        return hotspotName;

    }

    private void startPublishService() {

        _publishServiceThread = new Thread(new Runnable() {

            public void run() {
                publishService();
            }
        });
        _publishServiceThread.start();
    }

    private void publishService() {
        int port = startServerSocket();

        Log.e("DATA", "publish Service Port: " + port);


        if (port == 0) {


            txt_publishstatus.setText("Unable to create a server socket for the service");
            return;
        }
        NsdServiceInfo service = createService(port);
        if (service != null) {
            Log.e("network", "start to register a service " + _serviceDiscoverManager);
            _serviceDiscoverManager.registerService(service, NsdManager.PROTOCOL_DNS_SD, this);
            _published = true;


            Log.d("network", "registering a service " + service.getHost());
        }
    }

    private NsdServiceInfo createService(int mport) {
        NsdServiceInfo service = new NsdServiceInfo();
        try {

            service.setServiceName(mServiceName);
            service.setServiceType(mServiceType);
            InetAddress addr = InetAddress.getByName("10.0.1.34");
            service.setHost(addr);
            Log.d("DATA", "create service port at : " + mport);

            port = mport;
            service.setPort(port);

        }
        catch(Exception e){

        }
        return service;
    }



    private class ConnectionListenerContact extends Thread {

        int mPort;

        private boolean acceptRequests = true;

        private ConnectionListenerContact(Context context, int port) {
            Log.d(TAG, "ConnectionListenerContact constructor : " + port);
            mPort = port;
        }

        @Override
        public void run() {

            try {

                Log.e(TAG, "ConnectionListenerContact run method");
                _serverSocketContact.setReuseAddress(true);

                if (_serverSocketContact != null && !_serverSocketContact.isBound()) {
                    _serverSocketContact.bind(new InetSocketAddress(mPort));

                    Log.e(TAG, "ConnectionListenerContact _serverSocketContact not null : " + mPort);
                }

                Log.e(TAG, "Inet4Address: contact " + Inet4Address.getLocalHost().getHostAddress());
                Log.e(TAG, "acceptRequest contact: " + acceptRequests);

                Socket socket = null;


                while (true) {


                    socket = _serverSocketContact.accept();


                    Log.e(TAG, "acceptRequest contact: socket port" + socket.getLocalPort());

                    Log.e(TAG, "Received data handledata ");

                    try {

                        ContactPojo contactPojo = list_Contact.get(0);

                        String contactName = contactPojo.getContactName();
                        int fileSize = contactPojo.getContactLength();


                        File file = new File(Path + "/" + Folder + "/" + contactName);

                        if (file.exists()) {
                            String extension = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));

                            String name = getDateTime() + extension;


                            Log.e("FILEOPE", "file already present so renaming to " + name);

                            file.renameTo(new File(Path + "/" + Folder + "/" + name));
                        } else {
                            file.createNewFile();
                        }

                        BufferedInputStream in = null;
                        BufferedOutputStream out = null;
                        try {

                            int len = fileSize;
                            in =
                                    new BufferedInputStream(socket.getInputStream());

                            out =
                                    new BufferedOutputStream(new FileOutputStream(file));

                            while (len > 0 && (contactlength = in.read(buffer)) > 0) {
                                out.write(buffer, 0, contactlength);
                                currentCount = currentCount+contactlength;
                                len = len - contactlength;


                            }


                            Log.e(TAG, "contact file saved ");

                            iCount--;
                            list_files_received.add(contactName);


                            new ApplyContacts(file.getAbsolutePath()).execute();

                            if (iCount == 0) {


                                new SendDataUpdate().execute();

                            }



                        } finally {


                            if (in != null) in.close();
                            if (out != null) out.close();
                        }
                    } catch (Exception e) {

                        Log.e(TAG, "Received data exeception " + e.getMessage());
                    } finally {


                    }


                    if(!acceptRequests){

                        break;
                    }

                }




            } catch (IOException e) {
                Log.e("DXDX", Build.MANUFACTURER + ": Connection listenercontact EXCEPTION. " + e.toString());
                e.printStackTrace();
            }


        }


        public void tearDown() {
            acceptRequests = false;
        }


    }

    private class ConnectionListenerImage extends Thread {

        int mPort;

        private boolean acceptRequests = true;

        private ConnectionListenerImage(Context context, int port) {
            Log.d(TAG, "ConnectionListenerImage constructor : ");
            mPort = port;
        }

        @Override
        public void run() {

            try {


                _serverSocketImage.setReuseAddress(true);

                if (_serverSocketImage != null && !_serverSocketImage.isBound()) {
                    _serverSocketImage.bind(new InetSocketAddress(mPort));
                }

                Log.d(TAG, "ConnectionListenerImage Inet4Address: Image " + Inet4Address.getLocalHost().getHostAddress());
                Log.d(TAG, "ConnectionListenerImage acceptRequest : Image" + acceptRequests);
                Socket socket = null;


                while (true) {


                    socket = _serverSocketImage.accept();

                    Log.e(TAG, "ConnectionListenerImage Received data handledata ");

                    try {

                        ImagePojo imagePojo = list_Image.get(0);

                        String imageName = imagePojo.getImageName();
                        int fileSize = imagePojo.getImageLength();



                        File file = new File(Path + "/" + Folder + "/" + ImageFolder + "/" + imageName);
                        Log.e(TAG, "ConnectionListenerImage image name " + imageName);
                        Log.e(TAG, "ConnectionListenerImage Received image size " + fileSize);
                        if (file.exists()) {
                            String extension = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));

                            String name = getDateTime() + extension;


                            Log.e("FILEOPE", "file already present so renaming to " + name);

                            file.renameTo(new File(Path + "/" + Folder + "/" + ImageFolder + "/" + name));
                        } else {
                            file.createNewFile();
                        }

                        BufferedInputStream in = null;
                        BufferedOutputStream out = null;

                        try {

                            int len = fileSize;

                            in = new BufferedInputStream(socket.getInputStream());

                            out = new BufferedOutputStream(new FileOutputStream(file));




                            while (len > 0 && (imagelength = in.read(buffer)) > 0) {
                                out.write(buffer, 0, imagelength);
                                currentCount = currentCount+imagelength;
                                len = len - imagelength;




                            }



                            Log.e(TAG, "image file saved ");

                            list_Image.remove(0);
                            iCount--;
                            list_files_received.add(imageName);



                            if (iCount == 0) {





                                new SendDataUpdate().execute();

                            }

                        } finally {


                            if (in != null) in.close();
                            if (out != null) out.close();

                        }
                    } catch (Exception e) {

                        Log.e(TAG, "Image receiving exeception " + e.getMessage());
                    } finally {

                    }


                    if(!acceptRequests)
                    {
                        break;
                    }

                }


            } catch (IOException e) {
                Log.e("DXDX", Build.MANUFACTURER + ": Connection listenercontact EXCEPTION. " + e.toString());
                e.printStackTrace();
            }


        }


        public void tearDown() {
            acceptRequests = false;
        }


    }


    private class ConnectionListenerDocument extends Thread {

        int mPort;

        private boolean acceptRequests = true;

        private ConnectionListenerDocument(Context context, int port) {
            Log.d(TAG, "ConnectionListenerImage constructor : ");
            mPort = port;
        }

        @Override
        public void run() {

            try {


                _serverSocketDocument.setReuseAddress(true);

                if (_serverSocketDocument != null && !_serverSocketDocument.isBound()) {
                    _serverSocketDocument.bind(new InetSocketAddress(mPort));
                }

                Log.d(TAG, "Inet4Address: Image " + Inet4Address.getLocalHost().getHostAddress());
                Log.d(TAG, "acceptRequest : Image" + acceptRequests);
                Socket socket = null;


                while (true) {


                    socket = _serverSocketDocument.accept();

                    Log.e(TAG, "Received data handledata ");

                    try {


                        DocPojo documentPojo = list_Document.get(0);

                        String documentName = documentPojo.getDocumentName();
                        int fileSize = documentPojo.getDocumentLength();




                        File file = new File(Path + "/" + Folder + "/" + FilesFolder + "/" +documentName);

                        if (file.exists()) {
                            String extension = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));

                            String name = getDateTime() + extension;


                            Log.e("FILEOPE", "file already present so renaming to " + name);

                            file.renameTo(new File(Path + "/" + Folder + "/" + FilesFolder + "/" + name));
                        } else {
                            file.createNewFile();
                        }

                        BufferedInputStream in = null;
                        BufferedOutputStream out = null;
                        try {


                            int len = fileSize;
                            in =
                                    new BufferedInputStream(socket.getInputStream());

                            out =
                                    new BufferedOutputStream(new FileOutputStream(file));


                            while (len > 0 && (doclength = in.read(buffer)) > 0) {
                                out.write(buffer, 0, doclength);
                                currentCount = currentCount+doclength;
                                len = len - doclength;




                            }



                            list_Document.remove(0);
                            iCount--;
                            list_files_received.add(documentName);



                            if (iCount == 0) {





                                new SendDataUpdate().execute();

                            }



                        } finally {


                            if (in != null) in.close();
                            if (out != null) out.close();
                        }
                    } catch (Exception e) {

                        Log.e(TAG, "Received data exeception " + e.getMessage());
                    } finally {


                    }
                    if(!acceptRequests){

                        break;
                    }
                }


            } catch (IOException e) {
                Log.e("DXDX", Build.MANUFACTURER + ": Connection listenercontact EXCEPTION. " + e.toString());
                e.printStackTrace();
            }


        }


        public void tearDown() {
            acceptRequests = false;
        }


    }




    public static String getWiFiIPAddress(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String ip = getDottedDecimalIP(wm.getConnectionInfo().getIpAddress());
        return ip;
    }

    public static String getDottedDecimalIP(int ipAddr) {

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddr = Integer.reverseBytes(ipAddr);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddr).toByteArray();

        //convert to dotted decimal notation:
        String ipAddrStr = getDottedDecimalIP(ipByteArray);
        return ipAddrStr;
    }

    public static String getDottedDecimalIP(byte[] ipAddr) {
        //convert to dotted decimal notation:
        String ipAddrStr = "";
        for (int i = 0; i < ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i] & 0xFF;
        }
        return ipAddrStr;
    }

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 999) {
                String strMessage = (String) msg.obj;

                if (strMessage.equalsIgnoreCase("receiving starts")) {


                } else {
                    //txtLogs.append("\n" + "Message:- " + strMessage);
                }


                Log.e(TAG, "message in handler : " + strMessage);
            }
            super.handleMessage(msg);
        }
    };


    final Handler handlerDataUpdate = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 999) {
                String strMessage = (String) msg.obj;

                if(strMessage.equalsIgnoreCase("starttimer")){

                    StartTimer();
                }
                else {
                    dataelementText.setText(strMessage + "/" + String.valueOf(totalCount));

                    //new SendDataUpdate().execute();


                    Log.e(TAG, "message in handler : " + strMessage);
                }
            }
            super.handleMessage(msg);
        }
    };




    private String createStatusJson(String status) {

        String strJson = "NA";
        try {
            JSONObject json = new JSONObject();
            JSONObject manJson = new JSONObject();
            manJson.put("Status", status);


            json.put("FileInfoResponse", manJson);

            strJson = json.toString();
        } catch (Exception e) {

        }



        return strJson;

    }
    private String StartArtifectPort() {

        try {
            _serverSocketContact = new ServerSocket(0);
            portContact = _serverSocketContact.getLocalPort();


            Log.e("PORT", "portimage " + portContact);


        } catch (IOException e) {
            e.printStackTrace();
        }

        connListenerContact = new ConnectionListenerContact(getApplicationContext(), portContact);
        connListenerContact.start();


        try {
            _serverSocketImage = new ServerSocket(0);
            portImage = _serverSocketImage.getLocalPort();


            Log.e("PORT", "port image " + portImage);


        } catch (IOException e) {
            e.printStackTrace();
        }

        connectionListenerImage = new ConnectionListenerImage(getApplicationContext(), portImage);
        connectionListenerImage.start();


        try {
            _serverSocketDocument = new ServerSocket(0);
            portDocument = _serverSocketDocument.getLocalPort();


            Log.e("PORT", "port image " + portDocument);


        } catch (IOException e) {
            e.printStackTrace();
        }

        connectionListenerDocument = new ConnectionListenerDocument(getApplicationContext(), portDocument);
        connectionListenerDocument.start();






        String messageToSend = createJson();

        return messageToSend;
    }



    public class SendDataUpdate extends AsyncTask<Void,Void,Void> {


        @Override
        protected Void doInBackground(Void... params) {

            Log.e("PORT", "SendDataUpdate onbackground ");


            try {

                if(Constants.SENDER_TYPE.equalsIgnoreCase("android")) {
                    String messageToSend = createStatusJson("done");
                    dataOutputStreamCommand.writeUTF(messageToSend);
                }
                else if(Constants.SENDER_TYPE.equalsIgnoreCase("ios")){

                    // send all files received to ios if dual
                }
            } catch (Exception e) {

            }



            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            txtLogs.append("\n" + "\n" + "All Files received successfully.");
            StopTimer();
        }
    }
    public class ApplyContacts extends AsyncTask<Void,Void,Void> {

        String filepath;

        public ApplyContacts(String cfilepath){
            super();

            filepath =cfilepath;

        }
        @Override
        protected Void doInBackground(Void... params) {

            Log.e("PORT", "SendDataUpdate onbackground ");


            try {

                File file = new File(filepath);
                readInsertVcf(file);
            } catch (Exception e) {

            }



            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            txtLogs.append("\n" + "\n" + " Contacts restored.");

        }
    }


    private String createJson() {

        String strJson = "NA";
        try {
            JSONObject json = new JSONObject();
            JSONObject manJson = new JSONObject();
            manJson.put("ContactPort", String.valueOf(portContact));
            manJson.put("ImagePort", String.valueOf(portImage));
            manJson.put("DocumentPort", String.valueOf(portDocument));

            json.put("ArtifectPortInfo", manJson);

            strJson = json.toString();
        } catch (Exception e) {

        }


        return strJson;

    }

    private void readInsertVcf(File file) {


        if (file.exists()) {

            Log.e("CONTACTS", "file present");
        } else {
            Log.e("CONTACTS", "file not present");
        }
        VCardReader reader = null;
        try {
            reader = new VCardReader(file);
            reader.registerScribe(new AndroidCustomFieldScribe());

            ContactOperations operations = new ContactOperations(getApplicationContext());
            VCard vcard = null;
            while ((vcard = reader.readNext()) != null) {
                Log.e("VCARD", "Inserting contact");


                operations.insertContact(vcard);
            }

            reader.close();
        } catch (Exception e) {


        } finally {

            if (file.exists()) {


                file.delete();
                Log.e("CONTACTS", "readInsertVcf file deleted");
            } else {
                Log.e("CONTACTS", "readInsertVcf no file to delete");
            }


        }


        Message msg = handler.obtainMessage();
        msg.what = 999;
        msg.obj = " Contacts restored.";

        handler.sendMessage(msg);


    }








    private final static String getDateTime() {
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return df.format(new Date());
    }



    public void enableWifi() {


        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(this.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);

    }

    public void createHotSpotProgram() {

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(this.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }

        WifiConfiguration netConfig = new WifiConfiguration();

        netConfig.SSID = hotspotName;

        //netConfig.SSID = "\""+hotspotName+"\"";

        netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        try {
            Method setWifiApMethod = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            boolean apstatus = (Boolean) setWifiApMethod.invoke(wifiManager, netConfig, true);

            Method isWifiApEnabledmethod = wifiManager.getClass().getMethod("isWifiApEnabled");
            while (!(Boolean) isWifiApEnabledmethod.invoke(wifiManager)) {
            }
            ;
            Method getWifiApStateMethod = wifiManager.getClass().getMethod("getWifiApState");
            int apstate = (Integer) getWifiApStateMethod.invoke(wifiManager);
            Method getWifiApConfigurationMethod = wifiManager.getClass().getMethod("getWifiApConfiguration");
            netConfig = (WifiConfiguration) getWifiApConfigurationMethod.invoke(wifiManager);
            Log.e("CLIENT", "\nSSID:" + netConfig.SSID + "\nPassword:" + netConfig.preSharedKey + "\n");

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {


                    publishServiceInit();

                }
            }, 3000);




        } catch (Exception e) {
            Log.e(this.getClass().toString(), "", e);
        }

    }





    public class ConnectionListener extends Thread {

        int mPort;

        private boolean acceptRequests = true;

        public ConnectionListener(Context context, int port) {
            Log.d(TAG, "ConnectionListener constructor : ");
            mPort = port;
        }

        @Override
        public void run() {

            try {
                Log.d(TAG, Build.MANUFACTURER + ": conn listener: " + mPort);

                _serverSocket.setReuseAddress(true);

                if (_serverSocket != null && !_serverSocket.isBound()) {
                    Log.e(TAG, "ConnectionListenerIOS _serverSocket not null : ");
                    _serverSocket.bind(new InetSocketAddress(mPort));
                }
                Log.e(TAG, "Inet4Address: old " + Inet4Address.getLocalHost().getHostAddress());
                Log.e(TAG, "acceptRequest old: " + mPort);
                Socket socket = null;


                while (true) {


                    response = "NA";
                    socket = _serverSocket.accept();



                    Log.e(TAG, "Received data handledata ");

                    try {
                        dataInputStreamCommand = new DataInputStream(
                                socket.getInputStream());
                        dataOutputStreamCommand = new DataOutputStream(
                                socket.getOutputStream());

                        String messageFromClient = "";

                        messageFromClient = dataInputStreamCommand.readUTF();






                        if(messageFromClient.contains(Constants.COMMAND_1)) {

                            Log.e(TAG, " received command  " + messageFromClient);

                            JSONObject reader = new JSONObject(messageFromClient);

                            JSONObject sys = reader.getJSONObject("Command");

                            if(reader.toString().contains("SenderType")) {
                                Constants.SENDER_TYPE = sys.getString("SenderType");
                            }

                            Log.e(TAG, " received command senderType " +  Constants.SENDER_TYPE);




                            if(Constants.SENDER_TYPE.equalsIgnoreCase("android")) {

                                String portInfo = StartArtifectPort();
                                dataOutputStreamCommand.writeUTF(portInfo);
                            }else if(Constants.SENDER_TYPE.equalsIgnoreCase("ios")){

                                // start default ports for ios

                            }





                        }
                        else if (messageFromClient.contains("ConnectInfo")) {


                            Log.e("FILEPATHINFO", "response contains connectinfo");
                            JSONObject reader = new JSONObject(messageFromClient);

                            JSONObject sys = reader.getJSONObject("ConnectInfo");

                            iCount = Integer.parseInt(sys.getString("FileCount"));
                            totalCount = Long.parseLong(sys.getString("TotalSize"));


                            Log.e("FILEPATHINFO", "Total file will be  " + iCount);

                            try {
                                JSONObject jsonObject = new JSONObject(messageFromClient);
                                if (messageFromClient.contains("contactfile")) {
                                    JSONArray slideContent = jsonObject.getJSONArray("contactfile");

                                    for (int i = 0; i < slideContent.length(); i++) {  // **line 2**
                                        JSONObject childJSONObject = slideContent.getJSONObject(i);
                                        String name = childJSONObject.getString("name");
                                        String size = childJSONObject.getString("size");

                                        ContactPojo contactPojo = new ContactPojo();
                                        contactPojo.setContactName(name);
                                        contactPojo.setContactLength(Integer.parseInt(size));


                                        list_Contact.add(contactPojo);
                                        Log.e("FILEPATHINFO", "contactfile name " + name);
                                    }
                                }
                                if (messageFromClient.contains("imagefile")) {
                                    JSONArray slideContent = jsonObject.getJSONArray("imagefile");

                                    for (int i = 0; i < slideContent.length(); i++) {  // **line 2**
                                        JSONObject childJSONObject = slideContent.getJSONObject(i);
                                        String name = childJSONObject.getString("name");

                                        String size = childJSONObject.getString("size");
                                        Log.e("FILEPATHINFO", "imagefile name " + name);
                                        Log.e("FILEPATHINFO", "imagefile size " + size);

                                        ImagePojo imagePojo = new ImagePojo();
                                        imagePojo.setImageName(name);
                                        imagePojo.setImageLength(Integer.parseInt(size));


                                        list_Image.add(imagePojo);
                                    }
                                }
                                if (messageFromClient.contains("documentfile")) {
                                    JSONArray slideContent = jsonObject.getJSONArray("documentfile");

                                    for (int i = 0; i < slideContent.length(); i++) {  // **line 2**
                                        JSONObject childJSONObject = slideContent.getJSONObject(i);
                                        String name = childJSONObject.getString("name");

                                        String size = childJSONObject.getString("size");

                                        DocPojo documentPojo = new DocPojo();
                                        documentPojo.setDocumentName(name);
                                        documentPojo.setDocumentLength(Integer.parseInt(size));


                                        list_Document.add(documentPojo);

                                    }
                                }

                            } catch (Exception e) {

                                Log.e("FILEPATHINFO", "response contains Exception " + e.getMessage());

                            }

                            if(Constants.SENDER_TYPE.equalsIgnoreCase("android")) {

                                String messageToSend  = createStatusJson("Received");
                                dataOutputStreamCommand.writeUTF(messageToSend);
                            }else if(Constants.SENDER_TYPE.equalsIgnoreCase("ios")){

                                // send file info json ack to ios if dual

                            }


                            Message msg = handlerDataUpdate.obtainMessage();
                            msg.what = 999;
                            msg.obj = "starttimer";   // s1.toString();

                            handlerDataUpdate.sendMessage(msg);


                        }



                    } catch (Exception e) {

                        Log.e(TAG, "Received data exeception " + e.getMessage());
                    } finally {


                    }
                    if(!acceptRequests){

                        break;
                    }
                }


            } catch (IOException e) {
                Log.e("DXDX", Build.MANUFACTURER + ": Connection listener EXCEPTION. " + e.toString());
                e.printStackTrace();
            }


        }




        public void tearDown() {
            acceptRequests = false;

            if (dataInputStreamCommand != null) {
                            try {
                                dataInputStreamCommand.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        if (dataOutputStreamCommand != null) {
                            try {
                                dataOutputStreamCommand.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

        }


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

                dataelementText.setText(currentCount + "/" + totalCount);
                txtTimerCount.setText("" + Minutes + ":"
                        + String.format("%02d", Seconds) + ":"
                        + String.format("%03d", MilliSeconds));


                for (int i = 0; i < list_files_received.size(); i++) {

                    if (list_files_received.size() > i) {

                        txtLogs.append(list_files_received.get(i).toString() + " Received" + "\n");
                        list_files_received.remove(list_files_received.get(i));
                    }
                }



                handlerTimeCount.postDelayed(this, 0);


        }

    };
    private void StopTimer(){


        if(handlerTimeCount!=null) {
            handlerTimeCount.removeCallbacks(runnable);

            iCount =0;

            if(totalCount>0) {
                String bytesconverted = ByteConversion(totalCount);

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


                dataelementText.setText(bytesconverted + " in " + stringBuilder.toString());

            }


            Log.e("TIMEROPERATION" , "updateTime " + UpdateTime);
            if(UpdateTime>0) {
                float sizeInMb = (float)totalCount / (1024 * 1024);

                float speed = (float) (sizeInMb / UpdateTime) * 1000;

                Log.e("TIMEROPERATION" , "size " + sizeInMb);
                Log.e("TIMEROPERATION" , "miliseconds " + MilliSeconds);

                txtTimerCount.setText("Speed: " + speed + " MB/s");


                if(!isMyServiceRunning(GalleryRefreshService.class)){

                    Intent intent = new Intent(ReceiverActivity.this,GalleryRefreshService.class);
                    startService(intent);
                }


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


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    @Override
    protected void onResume() {
        super.onResume();

       if(isOnSameNetwork){

           publishServiceInit();
       }
       else{
           createHotSpotProgram();
       }
    }

    @Override
    protected void onStop() {
        super.onStop();

        try {
            destroyThreads();
            depublishService();
        }catch(Exception e){

            Log.e(TAG,"Receiver onStop exception " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            destroyThreads();
            depublishService();
        }catch(Exception e){

            Log.e(TAG,"Receiver onDestroy exception " + e.getMessage());
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            destroyThreads();
            depublishService();
            StopTimer();
            if (dataInputStreamCommand != null) {
                try {
                    dataInputStreamCommand.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (dataOutputStreamCommand != null) {
                try {
                    dataOutputStreamCommand.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        catch(Exception e){

            Log.e(TAG,"Receiver onPause exception " + e.getMessage());
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // uncomment this once final
        //  enableWifi();
    }





}