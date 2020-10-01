package com.ayyyyyyylmao.lyrics;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;

public class StartActivity extends AppCompatActivity {

    Button b1;
    Button b2;

    Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        i = new Intent(StartActivity.this, MusicListeningService.class);


        b1 = (Button)findViewById(R.id.startbutton);
        b1.setOnClickListener(buttonclicklistener);

        b2 = (Button)findViewById(R.id.stopbutton);
        b2.setOnClickListener(buttonclicklistener);

        updateAndroidSecurityProvider(this);
    }

    View.OnClickListener buttonclicklistener = new View.OnClickListener(){
        public void onClick(View v) {
            if(v==b1){


                StartActivity.this.startService(i);
            }else if (v==b2){
                StartActivity.this.stopService(i);
            }
        }
    };

    private void updateAndroidSecurityProvider(Activity callingActivity) {
        try {
            ProviderInstaller.installIfNeeded(this);
        } catch (GooglePlayServicesRepairableException e) {
            // Thrown when Google Play Services is not installed, up-to-date, or enabled
            // Show dialog to allow users to install, update, or otherwise enable Google Play services.
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e("SecurityException", "Google Play Services not available.");
        }
    }
}
