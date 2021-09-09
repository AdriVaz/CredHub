package com.example.credhub;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class ShowCred extends AppCompatActivity {

    TextView tvTitle, tvUsername, tvPassword;
    Intent incomingIntent;
    ImageButton togglePassword;
    Button export, edit, delete;

    String label, username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_cred);

        incomingIntent = getIntent();

        tvTitle = findViewById(R.id.appLabel);
        tvUsername = findViewById(R.id.userValue);
        tvPassword = findViewById(R.id.passwordValue);
        togglePassword = findViewById(R.id.togglePassword);
        export = findViewById(R.id.exportCred);
        edit = findViewById(R.id.editCred);
        delete = findViewById(R.id.removeCred);

        label = incomingIntent.getStringExtra("label");
        username = incomingIntent.getStringExtra("username");
        password = incomingIntent.getStringExtra("password");

        tvTitle.setText(label);
        tvUsername.setText(username);
        tvPassword.setText(password);

        togglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

                new CountDownTimer(3000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {}

                    @Override
                    public void onFinish() {
                        tvPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    }
                }.start();
            }
        });

        export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncRemoteExport().execute(label, username, password);
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShowCred.this, AddCred.class);
                intent.putExtra("editLabel", label);
                intent.putExtra("editUsername", username);
                intent.putExtra("editPassword", password);
                startActivity(intent);
                finish();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseManager dbm = new DatabaseManager(ShowCred.this);
                dbm.removeCred(label);
                finish();
            }
        });

    }

    public class AsyncRemoteExport extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... strings) {
            try {
                return new RemoteCredManager(ShowCred.this).exportRecord(strings[0], strings[1], strings[2]);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[] strings) {
            if(strings != null)
                Toast.makeText(ShowCred.this, R.string.cred_exported, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(ShowCred.this, R.string.unable_export, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int retVal = KeystoreManager.deviceSecuredOrWipeData(this);
        switch(retVal) {
            case KeystoreManager.WIPED_DATA:
                Toast.makeText(this, R.string.lockscreen_removed_database_deleted, Toast.LENGTH_LONG).show();
            case KeystoreManager.DEVICE_UNSECURED:
                finish();
            default:
                break;
        } //Faltan los breaks a proposito
    }
}
