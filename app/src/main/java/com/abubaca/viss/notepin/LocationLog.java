package com.abubaca.viss.notepin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LocationLog extends AppCompatActivity {

    private final static String TAG = "LOCATION_LOG";
    Button clearLogBTN;
    TextView logTV;
    IntentFilter filter = new IntentFilter("ACCURACY");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_location_log);

        clearLogBTN = (Button)findViewById(R.id.clear_log_btn);
        logTV = (TextView)findViewById(R.id.textView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver ,filter);
        clearLogBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logTV.setText("");
                new CustomToast().makeSuccessToast(LocationLog.this , "Accuracy log cleared");
            }
        });
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG , ""+intent.getIntExtra("ACCURACY" , 1));
            if(intent.getAction().equals("ACCURACY")){
                String time = getDate(intent.getLongExtra("TIME" , 1));
                String accuracy = String.valueOf(intent.getIntExtra("ACCURACY" , 1));
                logTV.setText(logTV.getText().toString()+"\n"+time+"  Accuracy: "+accuracy+"m");
            }
        }
    };

    public static String getDate(long milliSeconds)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }


}
