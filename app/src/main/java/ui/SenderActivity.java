package ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.wifidirect_datatransfer.GlobalApplication;
import com.wifidirect_datatransfer.R;

import java.util.ArrayList;
import java.util.List;

import Utils.CryptoUtil;
import pojos.DevicePojo;
import services.RefreshService;

import static Utils.Constants.COMMAND_1;
import static Utils.Constants.RECEIVER_HOST;
import static Utils.Constants.RECEIVER_PORT;
import static Utils.Constants.createDir;
import static Utils.Constants.key;
import static android.content.ContentValues.TAG;
import static com.wifidirect_datatransfer.GlobalApplication._rxBus;
import static com.wifidirect_datatransfer.GlobalApplication.isOnSameNetwork;


public class SenderActivity extends Activity implements NsdManager.DiscoveryListener {




    NsdManager.ResolveListener mResolveListener;
    NsdServiceInfo mService;
    private NsdManager _serviceDiscoverManager;


    public String mServiceType = "_peertopeer._tcp.";

    private ArrayList<String> _discoveredServices = new ArrayList<>();
    private ArrayList<String> _discoveredServicesWifiNetwoks = new ArrayList<>();
    ArrayList<DevicePojo> list_devicePojo = new ArrayList<>();
    private boolean _discovering;


    ListView _serviceListView;
    MyAdapter arradap;
    String host;
    int port;
    TextView txt_sender;

    ImageView refresh_image_view;
    LinearLayout refreshLinearLayout, connectToIos;

    // wifi discovery

    WifiManager mWifiManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender_list);


        _serviceListView = (ListView) findViewById(R.id.servicesView);

        txt_sender = (TextView) findViewById(R.id.sender_status_textview);

        refresh_image_view = (ImageView) findViewById(R.id.refresh_image_view);
        refreshLinearLayout = (LinearLayout) findViewById(R.id.refreshLinearLayout);
        connectToIos = (LinearLayout) findViewById(R.id.connectioslayout);


        createDir();


        if(isOnSameNetwork) {


            arradap = new MyAdapter(this, R.layout.device_list_item, _discoveredServices); //_discoveredServicesWifiNetwoks for wifi network discovery


            _serviceListView.setAdapter(arradap);
        }
        else{

            arradap = new MyAdapter(this, R.layout.device_list_item, _discoveredServicesWifiNetwoks); //_discoveredServicesWifiNetwoks for wifi network discovery


            _serviceListView.setAdapter(arradap);

        }

        // wifi discovery
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        _serviceDiscoverManager = (NsdManager) getSystemService(Context.NSD_SERVICE);

        //GetWifiSSID();


        _serviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Log.e(TAG, "clicked : ");



                if(isOnSameNetwork) {
                    port = list_devicePojo.get(i).getPort();
                    host = list_devicePojo.get(i).getHost();
                    String os = list_devicePojo.get(i).getOs();

                    Log.e(TAG, "clicked : " + "port " + port + " host " + host + " servicename " + list_devicePojo.get(i).getServiceName());
                    Log.e(TAG, "clicked : " + "os " + os);


                    RECEIVER_HOST = host;
                    RECEIVER_PORT = port;

                    if (os.toLowerCase().contains("ios")) {

                        _rxBus.send(COMMAND_1);
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SenderActivity.this);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("HOST", host);
                        editor.putString("RECTYPE", os);
                        editor.putString("DEVICE", list_devicePojo.get(i).getServiceName());
                        editor.putInt("PORT", port);
                        editor.putString("ACTIVE", "1");
                        editor.apply();


                        Intent intent = new Intent(SenderActivity.this, Artifacts.class);
                        startActivity(intent);

                        finish();
                    } else if (os.equalsIgnoreCase("android")) {


                        _rxBus.send(COMMAND_1);


                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SenderActivity.this);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("HOST", host);
                        editor.putString("RECTYPE", os);
                        editor.putString("DEVICE", list_devicePojo.get(i).getServiceName());
                        editor.putInt("PORT", port);
                        editor.putString("ACTIVE", "1");
                        editor.apply();
                        Intent intent = new Intent(SenderActivity.this, Artifacts.class);
                        startActivity(intent);

                        finish();

                    }

                }else {

                    ConnectToWiFi(_discoveredServicesWifiNetwoks.get(i), null, SenderActivity.this);
                }

            }
        });



        refresh_image_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!_discovering) {

                    startDiscovery();

                } else {

                    stopDiscovery();

                }
            }
        });

        refreshLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!_discovering) {

                    startDiscovery();


                } else {

                    stopDiscovery();

                }
            }
        });


        connectToIos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    if (mWifiScanReceiver != null) {


                        unregisterReceiver(mWifiScanReceiver);
                    }


                } catch (Exception e) {

                }
                showConnectMessage();


            }
        });
    }


    private void showConnectMessage() {


        AlertDialog.Builder builder1 = new AlertDialog.Builder(SenderActivity.this);
        builder1.setMessage("Connect To IOS device");
        builder1.setMessage("Please use hotspot created on this device,on Sender's device.");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {


                        // uncomment this in actual
                        //createHotSpotProgram();


                        GlobalApplication.mode = "IOS";

                        stopDiscovery();


                        discoverServiceInit();

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



    private String getWifiFromServiceName(String serviceName) {

        String hotspotName = "MyAp";
        try {
            CryptoUtil cryptoUtil = new CryptoUtil();

            String plain = "Android_" + serviceName;
            hotspotName = cryptoUtil.encrypt(key, plain);
            Log.e("ENCRYPTION", "original " + plain);
            Log.e("ENCRYPTION", "encrypted  " + hotspotName);


        } catch (Exception e) {

        }

        return hotspotName;


    }


    private void startDiscovery() {
        if (!_discovering) {
            Log.e("SERVICECHK", "start discovery");
            _discovering = true;
            txt_sender.setText("\n" + "Discovering devices");

            _serviceDiscoverManager.discoverServices(mServiceType, NsdManager.PROTOCOL_DNS_SD, this);
        }
    }

    private void stopDiscovery() {
        if (_discovering) {
            _discovering = false;

            _serviceDiscoverManager.stopServiceDiscovery(this);


            txt_sender.setText("\n" + "Discovering Stoped");
            list_devicePojo.clear();
            _discoveredServices.clear();

            arradap.notifyDataSetChanged();


        }
    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);


                mService = serviceInfo;

                Log.e(TAG, "Resolve Succeeded. host  " + mService.getHost().getHostAddress());
                runOnUiThread(new Runnable() {

                    public void run() {


                        Log.e(TAG, "run on ui thread");

                        String serviceName = mService.getServiceName();

                        String Os = "NA";
                        if (mService.getServiceName().contains("_")) {


                            String[] strarr = mService.getServiceName().split("_", 2);

                            Os = strarr[0];
                            serviceName = strarr[1];
                            Log.e(TAG, "service name strarr serviceName " + serviceName);
                        }
                        Log.e(TAG, "service name spliting " + serviceName);

                        if (!_discoveredServices.contains(serviceName)) {

                            Log.e(TAG, "adding service name " + mService.getServiceName());


                            _discoveredServices.add(serviceName);

                            DevicePojo devicePojo = new DevicePojo();
                            devicePojo.setServiceName(serviceName);
                            devicePojo.setPort(mService.getPort());
                            devicePojo.setHost(mService.getHost().getHostAddress());
                            devicePojo.setOs(Os);

                            list_devicePojo.add(devicePojo);


                            // below is for wifi scan an connect add here same as list onitemclick

                            if(!isOnSameNetwork){

                                port = mService.getPort();
                                host = mService.getHost().getHostAddress();
                                String os = Os;

                                Log.e(TAG, "clicked : " + "port " + port + " host " + host + " servicename " + serviceName);
                                Log.e(TAG, "clicked : " + "os " + os);


                                RECEIVER_HOST = host;
                                RECEIVER_PORT = port;

                                if (os.toLowerCase().contains("ios")) {

                                    _rxBus.send(COMMAND_1);
                                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SenderActivity.this);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString("HOST", host);
                                    editor.putString("RECTYPE", os);
                                    editor.putString("DEVICE", serviceName);
                                    editor.putInt("PORT", port);
                                    editor.putString("ACTIVE", "1");
                                    editor.apply();


                                    Intent intent = new Intent(SenderActivity.this, Artifacts.class);
                                    startActivity(intent);

                                    finish();
                                } else if (os.equalsIgnoreCase("android")) {


                                    _rxBus.send(COMMAND_1);


                                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SenderActivity.this);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString("HOST", host);
                                    editor.putString("RECTYPE", os);
                                    editor.putString("DEVICE", serviceName);
                                    editor.putInt("PORT", port);
                                    editor.putString("ACTIVE", "1");
                                    editor.apply();
                                    Intent intent = new Intent(SenderActivity.this, Artifacts.class);
                                    startActivity(intent);

                                    finish();

                                }
                            }





                        } else {
                            Log.e(TAG, "adding service name " + "service name already present");
                        }


                        txt_sender.setText("Device Found " + serviceName);
                        updateUi();


                    }
                });

            }
        };
    }

    private void updateUi() {

        arradap.notifyDataSetChanged();


    }

    @Override
    public void onStartDiscoveryFailed(String serviceType, int errorCode) {
        Log.d("network", "discovery failed");
        txt_sender.append("\n" + "Discovery failed");
    }

    @Override
    public void onStopDiscoveryFailed(String serviceType, int errorCode) {
        Log.d("network", "onStopDiscoveryFailed");
    }

    @Override
    public void onDiscoveryStarted(String serviceType) {
        Log.d("network", "discovery started");
    }

    @Override
    public void onDiscoveryStopped(String serviceType) {
        Log.d("network", "discovery stopped");
        _discovering = false;


        //update ui if any
        /*runOnUiThread(new Runnable() {

            public void run() {

            }
        });*/
    }

    @Override
    public void onServiceFound(NsdServiceInfo serviceInfo) {
        try {
            Log.e(TAG, "service found:serviceInfo " + serviceInfo);
            Log.e(TAG, "service found: " + serviceInfo.getServiceName());
            Log.e(TAG, "service found: getport " + serviceInfo.getPort());
            Log.e(TAG, "service found: type " + serviceInfo.getServiceType());
            Log.e(TAG, "service found: host " + serviceInfo.getHost());


            _serviceDiscoverManager.resolveService(serviceInfo, mResolveListener);
        } catch (Exception e) {

            Log.e(TAG,"Exception in onservice Found " + e.getMessage());
        }
    }

    @Override
    public void onServiceLost(final NsdServiceInfo serviceInfo) {
        Log.d("network", "service lost: " + serviceInfo.getServiceName());
        runOnUiThread(new Runnable() {

            public void run() {

                if (serviceInfo.getServiceName().contains("_")) {


                    String[] strarr = serviceInfo.getServiceName().split("_", 2);


                    String serviceName = strarr[1];
                    Log.e(TAG, "service name strarr serviceName " + serviceName);
                    _discoveredServices.remove(serviceName);
                    arradap.notifyDataSetChanged();

                }

            }
        });
    }



    public class MyAdapter extends ArrayAdapter<String> {

        ArrayList<String> animalList = new ArrayList<>();

        public MyAdapter(Context context, int textViewResourceId, ArrayList<String> objects) {
            super(context, textViewResourceId, objects);
            animalList = objects;
        }

        @Override
        public int getCount() {
            return super.getCount();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.device_list_item, null);
            TextView textView = (TextView) v.findViewById(R.id.textView);
            ImageView imageView = (ImageView) v.findViewById(R.id.imageView);


            if(isOnSameNetwork) {
                textView.setText(_discoveredServices.get(position));
            }
            else{

                textView.setText(_discoveredServicesWifiNetwoks.get(position));  // for wifi networks
            }


            return v;

        }

    }


    public void ConnectToWiFi(String networkSSID, String key, Context context) {

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + networkSSID + "\"";
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);


        mWifiManager.addNetwork(conf);


        List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration i : list) {


            Log.e("ConnectToWiFi", "connecting to SSID " + i.SSID);
            if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {

                mWifiManager.disconnect();
                mWifiManager.enableNetwork(i.networkId, true);
                mWifiManager.reconnect();


                Log.e("ConnectToWiFi", "wifi manager reconnect true ");
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        //Your task it will execute at 1 time only...


                        discoverServiceInit();


                    }
                }, 10000);


                break;
            }

        }


    }


    private void discoverServiceInit() {

        initializeResolveListener();

        if (isMyServiceRunning(RefreshService.class)) {
            Log.e("SERVICECHK", "service is already running");
        } else {


            Intent intent = new Intent(SenderActivity.this, RefreshService.class);
            startService(intent);
        }

        if (!_discovering) {

            Log.e("SERVICECHK", " starting discovery");
            startDiscovery();


        } else {

            stopDiscovery();


        }
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

    public void GetWifiSSID() {

        Log.e("SSIDRECEIVER", "GetWifiSSID");

        registerReceiver(mWifiScanReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mWifiManager.startScan();


        // GetAvailableNetworks();


    }

    private void GetAvailableNetworks() {

        WifiConfiguration conf = new WifiConfiguration();
        //conf.SSID = "\"" + networkSSID + "\"";
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);


        mWifiManager.addNetwork(conf);


        List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration i : list) {


            Log.e("GetAvailableNetworks", "SSID " + i.SSID);
            if (i.SSID != null && i.SSID.contains("MyAp_")) {

                String ssid = i.SSID.replaceAll("\"", "");
                Log.e("GetAvailableNetworks", "adding to list ");
                _discoveredServicesWifiNetwoks.add(ssid);
            }

        }
        arradap.notifyDataSetChanged();
    }

    private final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                List<ScanResult> mScanResults = mWifiManager.getScanResults();


                for (int i = 0; i < mScanResults.size(); i++) {

                    Log.e("SSIDRECEIVER", "SSID: " + mScanResults.get(i).SSID);

                    if (mScanResults.get(i).SSID.contains("MyAp_")) {


                        _discoveredServicesWifiNetwoks.add(mScanResults.get(i).SSID);
                    }

                }


                arradap.notifyDataSetChanged();

            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (mWifiScanReceiver != null) {


                unregisterReceiver(mWifiScanReceiver);
            }
        } catch (Exception e) {

        }
        stopDiscovery();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            stopDiscovery();
        }
        catch(Exception e){

            Log.e(TAG,"Exception in sender activity ondestroy " + e.getMessage());
        }

    }


    @Override
    protected void onResume() {
        super.onResume();


        // GetWifiSSID();

        if(isOnSameNetwork) {
            discoverServiceInit();
        }else {

            GetWifiSSID();
        }

    }







}
