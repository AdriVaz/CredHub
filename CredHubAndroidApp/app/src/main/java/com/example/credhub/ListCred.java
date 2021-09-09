package com.example.credhub;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class ListCred extends AppCompatActivity {

    ListView lv;
    Button importButton;
    Button addButton;
    static DatabaseManager dbm;

    Cursor result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_cred);

        lv = findViewById(R.id.credList);
        importButton = findViewById(R.id.importButton);
        addButton = findViewById(R.id.addButton);

        dbm = new DatabaseManager(ListCred.this);

        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchImportCred = new Intent(ListCred.this, ImportCred.class);
                startActivity(launchImportCred);
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchAddCred = new Intent(ListCred.this, AddCred.class);
                startActivity(launchAddCred);
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ListCred.this, ShowCred.class);
                Cursor cursor = (Cursor) lv.getItemAtPosition(position);
                if(cursor.moveToPosition(position)) {
                    intent.putExtra("label", cursor.getString(cursor.getColumnIndex(DatabaseManager.LABEL_COL)));
                    intent.putExtra("username", cursor.getString(cursor.getColumnIndex(DatabaseManager.USER_COL)));
                    intent.putExtra("password", cursor.getString(cursor.getColumnIndex(DatabaseManager.PW_COL)));
                }
                cursor.close();
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(result != null)
            result.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        int retval = KeystoreManager.deviceSecuredOrWipeData(this);

        switch(retval) {
            case KeystoreManager.WIPED_DATA:
                Toast.makeText(this, R.string.lockscreen_removed_database_deleted, Toast.LENGTH_LONG).show();
            case KeystoreManager.DEVICE_UNSECURED:
                result = null;
                break;
            case KeystoreManager.DEVICE_SECURED:
                dbm.close();
                dbm = new DatabaseManager(this);
                result = dbm.getAllCreds();
        } //Faltan los breaks a proposito

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1,
                result,
                new String[] {DatabaseManager.LABEL_COL},
                new int[] {android.R.id.text1},
                0);

        lv.setAdapter(adapter);
    }
}
