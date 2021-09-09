package com.example.credhub;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.security.SecureRandom;

public class DatabaseManager extends SQLiteOpenHelper {

    public static final String DB_NAME = "creds.db";
    public static final String TABLE_NAME = "creds";
    public static final String LABEL_COL = "label";
    public static final String USER_COL = "username";
    public static final String PW_COL = "password";

    public static final int CREATED = 0;
    public static final int UPDATED = 1;

    public Context context;
    public SQLiteDatabase db;

    public DatabaseManager(Context context) {
        super(context, DB_NAME, null, 1);
        this.context = context;
    }

    private void generateEncryptedPassword() {
        //Generate random string to encrypt database
        SecureRandom secureRandom = new SecureRandom();
        int length = secureRandom.nextInt(20-15+1)+15;
        String dbPass = "";
        for(int i = 0; i < length; i++)
            dbPass += (char) (secureRandom.nextInt(126-32+1)+32);

        //Generate key pair to encrypt database key
        KeystoreManager ksm = new KeystoreManager();
        ksm.generateKeyPair(KeystoreManager.DATABASE_KEY_ALIAS, this.context);

        //Save generated password into shared preferences
        SharedPreferences sp = this.context.getSharedPreferences(SharedPreferencesConstants.PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SharedPreferencesConstants.ENCRYPTED_DB_PASS, ksm.encryptString(KeystoreManager.DATABASE_KEY_ALIAS, dbPass));
        editor.commit();

    }

    private String getClearPassword() {
        SharedPreferences sp = this.context.getSharedPreferences(SharedPreferencesConstants.PREF_FILE_NAME, Context.MODE_PRIVATE);
        if(!sp.contains(SharedPreferencesConstants.ENCRYPTED_DB_PASS))
            generateEncryptedPassword();

        String encryptedKey = sp.getString(SharedPreferencesConstants.ENCRYPTED_DB_PASS, "");
        KeystoreManager ksm = new KeystoreManager();
        return ksm.decryptString(KeystoreManager.DATABASE_KEY_ALIAS, encryptedKey);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Create tables into database
        String createTable = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                LABEL_COL + " TEXT PRIMARY KEY," +
                USER_COL  + " TEXT NOT NULL," +
                PW_COL    + " TEXT NOT NULL)";

        SQLiteDatabase.loadLibs(this.context);
        db.execSQL(createTable);

        this.db = db;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public int insertOrReplaceCred(String label, String username, String password) {
        SQLiteDatabase.loadLibs(this.context);

        ContentValues values = new ContentValues();
        values.put(LABEL_COL, label);
        values.put(USER_COL, username);
        values.put(PW_COL, password);

        SQLiteDatabase db = getWritableDatabase(getClearPassword());

       int ret = (int) db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
       if(ret == -1) {
           db.update(TABLE_NAME, values, LABEL_COL + "=?", new String[]{label});
           return UPDATED;
       }
        return CREATED;
    }

    public Cursor getAllCreds() {
        SQLiteDatabase.loadLibs(this.context);
        SQLiteDatabase db = getReadableDatabase(getClearPassword());
        Cursor cursor = db.rawQuery("SELECT rowid _id, * FROM " + TABLE_NAME, null);
        return cursor;
    }

    public int removeCred(String id) {
        SQLiteDatabase.loadLibs(this.context);
        SQLiteDatabase db = getWritableDatabase(getClearPassword());
        int retVal = db.delete(TABLE_NAME, LABEL_COL + "=?", new String[]{id});
        return retVal;
    }

}
