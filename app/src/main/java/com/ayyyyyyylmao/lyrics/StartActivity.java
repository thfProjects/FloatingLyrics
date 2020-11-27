package com.ayyyyyyylmao.lyrics;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;

public class StartActivity extends AppCompatActivity {

    Button b1;
    Button b2;

    Intent i;

    int MANAGE_OVERLAY_PERMISSION_CODE = 5579;

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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(StartActivity.this)) {

                    LayoutInflater inflater = getLayoutInflater();
                    View adview = inflater.inflate(R.layout.overlay_permission_dialog, null);

                    final AlertDialog alertDialog = new AlertDialog.Builder(StartActivity.this).create();
                    alertDialog.setView(adview);
                    TextView textView = (TextView)adview.findViewById(R.id.text);
                    Button buttonOk = (Button)adview.findViewById(R.id.buttonOk);
                    Button buttonCancel = (Button)adview.findViewById(R.id.buttonCancel);
                    buttonOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, MANAGE_OVERLAY_PERMISSION_CODE);
                            alertDialog.dismiss();
                        }
                    });
                    buttonCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.show();
                }else {
                    StartActivity.this.startService(i);
                }
            }else if (v==b2){
                StartActivity.this.stopService(i);
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && requestCode == MANAGE_OVERLAY_PERMISSION_CODE)  {
            if(Settings.canDrawOverlays(this)){
                StartActivity.this.startService(i);
            }
        }
    }

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
