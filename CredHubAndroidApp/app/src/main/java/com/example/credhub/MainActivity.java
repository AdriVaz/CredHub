package com.example.credhub;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(KeystoreManager.deviceSecuredOrWipeData(this) != KeystoreManager.DEVICE_SECURED) {
            Toast.makeText(this, R.string.main_activity_device_unsecured, Toast.LENGTH_SHORT).show();
            return;
        }

        sp = getSharedPreferences(SharedPreferencesConstants.PREF_FILE_NAME, Context.MODE_PRIVATE);

        if(!sp.contains(SharedPreferencesConstants.REMOTE_SERVER_IP)) {
            SharedPreferences.Editor editor = sp.edit();

            editor.putString(SharedPreferencesConstants.REMOTE_SERVER_IP, SharedPreferencesConstants.REMOTE_SERVER_IP_DEFAULT);
            editor.commit();
        }

        Thread clock = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Intent launchListCred = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(launchListCred);
                }
            }
        };

        clock.start();
        
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
