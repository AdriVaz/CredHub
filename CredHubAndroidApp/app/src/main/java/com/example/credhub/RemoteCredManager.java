package com.example.credhub;

import android.content.Context;
import android.content.SharedPreferences;

import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class RemoteCredManager {

    //private static HttpTransportSE androidHttpTransport;
    private static List<HeaderProperty> headerList_basicAuth;

    private static final String WS_NAMESPACE = "http://sdm_webrepo/";
    private static final String WS_METHOD_LIST = "ListCredentials";
    private static final String WS_METHOD_IMPORT = "ImportRecord";
    private static final String WS_METHOD_EXPORT = "ExportRecord";

    /* Timeout for the connection to the server.
     * When server IP is not valid, connect method takes a lot of time
     * to throw IOException. The timeout limits that wait time.
     */
    private static final int WS_TIMEOUT_MILLIS = 2000;

    /* Credentials for the remote server. This variables can only be assigned
     * via the setCredentials method, and they only can be assigned
     * once (when login credentials are validated).
     */
    private static String basicAuthUsername = null;
    private static String basicAuthPassword = null;

    // Trust manager that doesn´t verify certificate chain
    private static TrustManager[] trustAllCerts = new TrustManager[] {
        new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {return new X509Certificate[0];}
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
        }
    };

    private Context context;

    public RemoteCredManager(Context context) {
        this.context = context;

        headerList_basicAuth = new ArrayList<>();
        String serverCreds = basicAuthUsername + ":" + basicAuthPassword;
        headerList_basicAuth.add(new HeaderProperty("Authorization", "Basic " + org.kobjects.base64.Base64.encode(serverCreds.getBytes())));

        //TLS Configuration
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });

        try {
            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    private String getWsUrl() {
        SharedPreferences sp = this.context.getSharedPreferences(SharedPreferencesConstants.PREF_FILE_NAME, Context.MODE_PRIVATE);

        /* defValue is the default IP of the server. As long as
         * sharedPreferences file should
         * always contain a server IP, this value should never be used.
         */
        String paramIp = sp.getString(SharedPreferencesConstants.REMOTE_SERVER_IP, "");

        //Method must be https in order to use the correct 443 port instead of http's port 80
        return "https://" + paramIp + "/SDM/WebRepo?wsdl";
    }

    /*
     * This method allows assignation of remote server credentials only once.
     * If a value is already stores, the method does nothing and returns false.
     */
    public static boolean setCredentials(String username, String password) {
        if(basicAuthUsername == null && basicAuthPassword == null) {
            basicAuthUsername = username;
            basicAuthPassword = password;
            return true;
        }
        return false;
    }

    public String[] listCredentials() throws IOException, XmlPullParserException {
        SoapObject request = new SoapObject(WS_NAMESPACE, WS_METHOD_LIST);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);

        new HttpTransportSE(getWsUrl(), WS_TIMEOUT_MILLIS).call("\"" + WS_NAMESPACE + WS_METHOD_LIST + "\"", envelope, headerList_basicAuth);

        Vector<SoapPrimitive> listIds = new Vector<SoapPrimitive>();

        try {
            if(envelope.getResponse() instanceof Vector) // 2+ elements
                listIds.addAll((Vector<SoapPrimitive>) envelope.getResponse());
            else if(envelope.getResponse() instanceof SoapPrimitive) // 1 element
                listIds.add((SoapPrimitive)envelope.getResponse());
        } catch (SoapFault soapFault) {
            soapFault.printStackTrace();
        }

        ArrayList<String> out = new ArrayList<>();
        for(int i = 0; i < listIds.size(); i++) {
            out.add(listIds.get(i).toString());
        }

        return out.toArray(new String[out.size()]);
    }

    public String[] importRecord(String id) throws IOException, XmlPullParserException {
        SoapObject request = new SoapObject(WS_NAMESPACE, WS_METHOD_IMPORT);

        PropertyInfo propId = new PropertyInfo();
        propId.name = "arg0"; propId.setValue(id);
        propId.type = PropertyInfo.STRING_CLASS;
        request.addProperty(propId);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);
        new HttpTransportSE(getWsUrl(), this.WS_TIMEOUT_MILLIS).call("\"" + WS_NAMESPACE + WS_METHOD_IMPORT + "\"", envelope, headerList_basicAuth);

        Vector<SoapPrimitive> importedRecord = null;
        try {
            importedRecord = (Vector<SoapPrimitive>) envelope.getResponse();
        } catch (SoapFault soapFault) {
            soapFault.printStackTrace();
        }

        String[] out = new String[3];

        if((importedRecord != null) && (importedRecord.size() == 3)) {
            out[0] = importedRecord.get(0).toString();
            out[1] = importedRecord.get(1).toString();
            out[2] = importedRecord.get(2).toString();

            return out;
        }
        else {
            return null;
        }
    }

    public String[] exportRecord(String id, String username, String password) throws IOException, XmlPullParserException {
        SoapObject request = new SoapObject(WS_NAMESPACE, WS_METHOD_EXPORT);

        PropertyInfo propId = new PropertyInfo();
        propId.name = "arg0"; propId.setValue(id);
        propId.type = PropertyInfo.STRING_CLASS;
        request.addProperty(propId);

        PropertyInfo propUser = new PropertyInfo();
        propUser.name = "arg1"; propUser.setValue(username);
        propUser.type = PropertyInfo.STRING_CLASS;
        request.addProperty(propUser);

        PropertyInfo propPass = new PropertyInfo();
        propPass.name = "arg2"; propPass.setValue(password);
        propPass.type = PropertyInfo.STRING_CLASS;
        request.addProperty(propPass);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);

        new HttpTransportSE(getWsUrl(), WS_TIMEOUT_MILLIS).call("\"" + WS_NAMESPACE + WS_METHOD_EXPORT + "\"", envelope, headerList_basicAuth);

        return new String[]{id, username, password};
    }

}
