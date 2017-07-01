package ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.wifidirect_datatransfer.GlobalApplication;
import com.wifidirect_datatransfer.R;

import Utils.Constants;
import services.GalleryRefreshService;
import services.RefreshService;


public class LauncherActivity extends Activity {




    // 49152 to 65535


    Button btn_send,btn_receive;
    public final String folderName = "BlanccoDataTransfer";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);


        btn_send = (Button)findViewById(R.id.sender_button_view);
        btn_receive = (Button)findViewById(R.id.receive_button_view);


        Constants constants = new Constants();
        constants.createDir();


        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isMyServiceRunning(GalleryRefreshService.class)){

                    Intent intent = new Intent(LauncherActivity.this,GalleryRefreshService.class);
                    startService(intent);
                }

                Intent intent = new Intent(LauncherActivity.this,SenderActivity.class);
                startActivity(intent);

            }
        });

        btn_receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isMyServiceRunning(GalleryRefreshService.class)){

                    Intent intent = new Intent(LauncherActivity.this,GalleryRefreshService.class);
                    startService(intent);
                }

                Intent intent = new Intent(LauncherActivity.this, ReceiverActivity.class);
                startActivity(intent);

            }
        });


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

        GlobalApplication.mode = "ANDROID";
        if(isMyServiceRunning(RefreshService.class))
        {
            Log.e("SERVICECHK" , "service is already running");


            Intent intent = new Intent(LauncherActivity.this,RefreshService.class);
            stopService(intent);

        }


    }
}
