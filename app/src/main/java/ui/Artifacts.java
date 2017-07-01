package ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wifidirect_datatransfer.R;

import static Utils.Constants.createDir;
import static com.wifidirect_datatransfer.GlobalApplication.list_Sender;


/**
 * Created by xcaluser on 7/6/17.
 */

public class Artifacts extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private TabLayout tabLayout;

    private ViewPager viewPager;

    LinearLayout sendLayout;

    TextView txt_selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_artifect);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);


        viewPager = (ViewPager) findViewById(R.id.pager);

        sendLayout = (LinearLayout)findViewById(R.id.senddata_layout);

        txt_selected = (TextView)findViewById(R.id.selected_textView);
        createDir();

        list_Sender.clear();


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String strOS = preferences.getString("RECTYPE", "Android");

        if(strOS.equalsIgnoreCase("ios"))
        {
            Pager adapter = new Pager(getSupportFragmentManager(), 2);


            viewPager.setAdapter(adapter);

            tabLayout.setupWithViewPager(viewPager);

            tabLayout.setOnTabSelectedListener(this);
        }
        else{

            Pager adapter = new Pager(getSupportFragmentManager(), 3);


            viewPager.setAdapter(adapter);

            tabLayout.setupWithViewPager(viewPager);

            tabLayout.setOnTabSelectedListener(this);
        }


        sendLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Artifacts.this);
                String strActive = preferences.getString("ACTIVE", "2");


                Log.e("SERVICE" ,"strActive " + strActive);

                if(list_Sender.size()>0) {


                    Intent intent = new Intent(Artifacts.this, SendDataActivity.class);

                    startActivity(intent);
                    finish();
                }
                else{
                    Toast.makeText(Artifacts.this,"Please select atleast one Item to Transfer" ,Toast.LENGTH_LONG).show();
                }



            }
        });
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }


    public void updateSenderList(boolean flag , String path){


        if(flag){

            if(!list_Sender.contains(path))
            {
                Log.e("DATA" , "adding to list");

                list_Sender.add(path);
                txt_selected.setText("Selected" + "("+list_Sender.size()+")" );
            }
        }
        else{
            if(list_Sender.contains(path)) {
                Log.e("DATA" , "deleting from list");
                list_Sender.remove(path);
                txt_selected.setText("Selected" + "("+list_Sender.size()+")" );
            }
        }



    }
}
