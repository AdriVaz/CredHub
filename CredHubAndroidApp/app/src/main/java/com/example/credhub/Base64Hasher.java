package com.example.credhub;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Base64Hasher {

    public static String digest(String message, String alg) {
        MessageDigest digest;
        byte[] hash = null;

        try {
            digest = MessageDigest.getInstance(alg);
            digest.reset();
            hash = digest.digest(message.getBytes("UTF-8"));

        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(hash, Base64.NO_WRAP);
    }
}
