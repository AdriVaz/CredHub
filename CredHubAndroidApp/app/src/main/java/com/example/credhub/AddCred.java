package com.example.credhub;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class AddCred extends AppCompatActivity {

    Button generatePassword;
    EditText passwordInput;
    EditText userInput;
    Spinner appSpinner;
    Button save;
    Intent incomingIntent;

    boolean editMode = false;
    TextView fixedLabel;
    String fixedLabelString = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_cred);

        incomingIntent = getIntent();

        generatePassword = findViewById(R.id.generatePassword);
        passwordInput = findViewById(R.id.passwordInput);
        userInput = findViewById(R.id.userInput);
        appSpinner = findViewById(R.id.appSpinner);
        save = findViewById(R.id.saveCred);
        fixedLabel = findViewById(R.id.editScreenLabel);

        //Edit or create mode (fills input with values)
        if(incomingIntent.getExtras() != null) {
            setTitle(R.string.addcred_edit_title);
            editMode = true;
            fixedLabelString = incomingIntent.getStringExtra("editLabel");
            fixedLabel.setText(fixedLabelString);
            userInput.setText(incomingIntent.getStringExtra("editUsername"));
            passwordInput.setText(incomingIntent.getStringExtra("editPassword"));

            appSpinner.setVisibility(View.GONE);
        } else {
            fixedLabel.setVisibility(View.GONE);

            PackageManager pm = getPackageManager();
            List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

            ArrayList<String> appLabels = new ArrayList<>();
            String appLabel;
            String[] aux;

            for(ApplicationInfo appInfo : apps) {
                appLabel = pm.getApplicationLabel(appInfo).toString();
                if(appLabel.contains(".")) {
                    aux = appLabel.split("\\.");
                    appLabels.add(aux[aux.length - 1]);
                } else {
                    appLabels.add(appLabel);
                }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,appLabels);
            appSpinner.setAdapter(adapter);
        }

        generatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SecureRandom secureRandom = new SecureRandom();
                int length = secureRandom.nextInt(20-15+1)+15;
                String pass = "";

                for(int i = 0; i < length; i++)
                    pass += (char) (secureRandom.nextInt(126-32+1)+32);

                passwordInput.setText(pass);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String label;
                if(editMode)
                    label = fixedLabelString;
                else
                    label = appSpinner.getSelectedItem().toString();

                String username = userInput.getText().toString();
                String password = passwordInput.getText().toString();
                
                if(username.isEmpty()) {
                    Toast.makeText(AddCred.this, R.string.must_username, Toast.LENGTH_SHORT).show();
                    return;
                }

                if(password.isEmpty()) {
                    Toast.makeText(AddCred.this, R.string.must_password, Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!KeystoreManager.isDeviceSecured(AddCred.this)) {
                    Toast.makeText(AddCred.this, R.string.device_must_be_locked, Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseManager dbm = new DatabaseManager(AddCred.this);
                int retVal = dbm.insertOrReplaceCred(label, username, password);
                if(retVal == DatabaseManager.UPDATED)
                    Toast.makeText(AddCred.this, R.string.updated_successfully, Toast.LENGTH_SHORT).show();
                else if(retVal == DatabaseManager.CREATED)
                    Toast.makeText(AddCred.this, R.string.created_successfully, Toast.LENGTH_SHORT).show();
                else {
                    Toast.makeText(AddCred.this, R.string.faied_write_register, Toast.LENGTH_SHORT).show();
                    return;
                }

                if(editMode) {
                    Intent intent = new Intent(AddCred.this, ShowCred.class);
                    intent.putExtra("label", label);
                    intent.putExtra("username", username);
                    intent.putExtra("password", password);
                    startActivity(intent);
                    finish();
                } else
                    finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(KeystoreManager.deviceSecuredOrWipeData(this) == KeystoreManager.WIPED_DATA) {
            KeystoreManager ksm = new KeystoreManager();
            Toast.makeText(this, R.string.lockscreen_removed_database_deleted, Toast.LENGTH_LONG).show();
            if(editMode)
                finish();
        }
    }
}
