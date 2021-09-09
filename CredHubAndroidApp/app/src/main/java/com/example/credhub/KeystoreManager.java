package com.example.credhub;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.security.KeyPairGeneratorSpec;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.security.auth.x500.X500Principal;

public class KeystoreManager {

    public static final String DATABASE_KEY_ALIAS = "credhub_db_keystore_alias";

    public static final int DEVICE_SECURED = 600;
    public static final int DEVICE_UNSECURED = 601;
    public static final int WIPED_DATA = 702;

    public KeyStore keyStore;

    public KeystoreManager() {
        try {
            this.keyStore = KeyStore.getInstance("AndroidKeyStore");
            this.keyStore.load(null);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void generateKeyPair(String alias, Context context) {
        if(!isDeviceSecured(context)) {
            return;
        }
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 1);

        KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                .setAlias(alias)
                .setSubject(new X500Principal("CN=" + alias))
                .setSerialNumber(BigInteger.TEN)
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .setEncryptionRequired()
                .build();
        KeyPairGenerator gen = null;
        try {
            gen = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
            gen.initialize(spec);
            gen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    public boolean containsKey(String alias) {
        boolean out = false;
        try {
            out = this.keyStore.isKeyEntry(alias);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        return out;
    }

    private PrivateKey loadPrivateKey(String alias) throws Exception {
        if(!keyStore.isKeyEntry(alias)) {
            return null;
        }

        KeyStore.Entry entry = keyStore.getEntry(alias, null);
        if(!(entry instanceof KeyStore.PrivateKeyEntry)) {
            return null;
        }
        return ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
    }

    public String encryptString(String alias, String text) {
        try {
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
            RSAPublicKey publicKey = (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();

            if(text.isEmpty()) {
                return null;
            }

            Cipher inCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            inCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(
                    outputStream, inCipher);
            cipherOutputStream.write(text.getBytes("UTF-8"));
            cipherOutputStream.close();

            byte [] vals = outputStream.toByteArray();
            return Base64.encodeToString(vals, Base64.DEFAULT);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String decryptString(String alias, String encryptedText) {
        try {
            PrivateKey privateKey = loadPrivateKey(alias);

            Cipher output = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            output.init(Cipher.DECRYPT_MODE, privateKey);

            CipherInputStream cipherInputStream = new CipherInputStream(
                    new ByteArrayInputStream(Base64.decode(encryptedText, Base64.DEFAULT)), output);
            ArrayList<Byte> values = new ArrayList<>();
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                values.add((byte)nextByte);
            }

            byte[] bytes = new byte[values.size()];
            for(int i = 0; i < bytes.length; i++) {
                bytes[i] = values.get(i);
            }

            return new String(bytes, 0, bytes.length, "UTF-8");

        } catch (Exception e) {
           e.printStackTrace();
        }
        return null;
    }

    public static boolean isDeviceSecured(Context context) {
        KeyguardManager kgm = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return kgm.isDeviceSecure();
        } else {
            return (isPatternEnabled(context) || isPassOrPinEnabled(kgm));
        }
    }

    private static boolean isPatternEnabled(Context context) {
        return (Settings.System.getInt(context.getContentResolver(), Settings.System.LOCK_PATTERN_ENABLED, 0) == 1);
    }

    private static boolean isPassOrPinEnabled(KeyguardManager kgm) {
        return kgm.isKeyguardSecure();
    }

    private static void wipeData(Context context) {
        //Remove old database encryption key from shared preferences
        SharedPreferences sp = context.getSharedPreferences(SharedPreferencesConstants.PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(SharedPreferencesConstants.ENCRYPTED_DB_PASS);
        editor.commit();

        //Remove old inaccessible database
        try {
            ListCred.dbm.db.close();
        } catch (NullPointerException e) {
        } finally {
            context.deleteDatabase(DatabaseManager.DB_NAME);
        }
    }

    public static int deviceSecuredOrWipeData(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SharedPreferencesConstants.PREF_FILE_NAME, Context.MODE_PRIVATE);
        KeystoreManager ksm = new KeystoreManager();

        if(!isDeviceSecured(context)) {
            if(!sp.contains(SharedPreferencesConstants.ENCRYPTED_DB_PASS)) {
                return DEVICE_UNSECURED;
            }

            wipeData(context);
            return WIPED_DATA;
        }

        if(ksm.containsKey(KeystoreManager.DATABASE_KEY_ALIAS) || !sp.contains(SharedPreferencesConstants.ENCRYPTED_DB_PASS)) {
            return DEVICE_SECURED;
        }

        wipeData(context);
        return WIPED_DATA;
    }

}
