package com.example.credhub;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditServerIp extends AppCompatActivity {

    EditText ipInput;
    Button changeIpButton;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_server_ip);

        sp = getSharedPreferences(SharedPreferencesConstants.PREF_FILE_NAME, Context.MODE_PRIVATE);

        ipInput = findViewById(R.id.serverIpEdit);
        changeIpButton = findViewById(R.id.changeIpButton);

        ipInput.setText(sp.getString(SharedPreferencesConstants.REMOTE_SERVER_IP, ""));

        changeIpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newIpValue = ipInput.getText().toString();

                if(Patterns.IP_ADDRESS.matcher(newIpValue).matches()) {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString(SharedPreferencesConstants.REMOTE_SERVER_IP, newIpValue);
                    editor.commit();

                    Intent intent = new Intent(EditServerIp.this, ImportCred.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(EditServerIp.this, R.string.invalid_ip_address, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(KeystoreManager.deviceSecuredOrWipeData(this) == KeystoreManager.WIPED_DATA)
            Toast.makeText(this, R.string.lockscreen_removed_database_deleted, Toast.LENGTH_LONG).show();
    }
}
