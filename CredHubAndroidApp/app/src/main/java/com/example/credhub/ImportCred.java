package com.example.credhub;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class ImportCred extends AppCompatActivity {

    ListView remoteContentListView;
    Button changeIpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_cred);

        remoteContentListView = findViewById(R.id.remoteContent);
        changeIpButton = findViewById(R.id.changeIpButton);

        new AsyncRemoteList().execute();

        remoteContentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(KeystoreManager.isDeviceSecured(ImportCred.this))
                    new AsyncRemoteImport().execute(remoteContentListView.getItemAtPosition(position).toString());
                else {
                    Toast.makeText(ImportCred.this, R.string.not_secured_unable_import, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        changeIpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ImportCred.this, EditServerIp.class);
                startActivity(intent);
                finish();
            }
        });

    }

    public class AsyncRemoteList extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... strings) {
            try {
                return new RemoteCredManager(ImportCred.this).listCredentials();
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
            if (strings != null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(ImportCred.this, android.R.layout.simple_list_item_1, strings);
                remoteContentListView.setAdapter(adapter);
            } else {
                Toast.makeText(ImportCred.this, R.string.unable_list, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class AsyncRemoteImport extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... strings) {
            try {
                return new RemoteCredManager(ImportCred.this).importRecord(strings[0]);
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
            if (strings != null) {
                DatabaseManager dbm = new DatabaseManager(ImportCred.this);
                dbm.insertOrReplaceCred(strings[0], strings[1], strings[2]);
                dbm.close();

                Toast.makeText(ImportCred.this, R.string.cred_imported, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(ImportCred.this, R.string.unable_import, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(KeystoreManager.deviceSecuredOrWipeData(this) == KeystoreManager.WIPED_DATA)
            Toast.makeText(this, R.string.lockscreen_removed_database_deleted, Toast.LENGTH_LONG).show();
    }
}
