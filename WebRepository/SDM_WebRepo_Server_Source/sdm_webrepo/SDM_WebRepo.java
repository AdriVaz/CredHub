// 
// Decompiled by Procyon v0.5.36
// 

package sdm_webrepo;

import javax.net.ssl.TrustManager;
import javax.net.ssl.KeyManager;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import java.security.SecureRandom;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.KeyManagerFactory;
import java.io.InputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import javax.xml.ws.Endpoint;

public class SDM_WebRepo
{
    public static void main(final String[] args) {
        try {
            if (args.length == 1 && args[0].toLowerCase().equals("http")) {
                Endpoint.publish("http://localhost/SDM/WebRepo", (Object)new RepoWS());
            }
            else if (args.length == 1 && args[0].toLowerCase().equals("http+auth")) {
                final HttpServer httpServer = HttpServer.create(new InetSocketAddress("localhost", 80), 80);
                final HttpContext httpContext = httpServer.createContext("/SDM/WebRepo");
                httpServer.start();
                final BasicAuth_Server authenticatorServer = new BasicAuth_Server("sdm_webrepo", "sdm", "repo4droid");
                httpContext.setAuthenticator(authenticatorServer);
                final Endpoint endpoint = Endpoint.create((Object)new RepoWS());
                endpoint.publish((Object)httpContext);
            }
            else if (args.length == 1 && (args[0].toLowerCase().equals("https+auth") || args[0].toLowerCase().equals("https+auth+rogue"))) {
                final KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
                if (!args[0].toLowerCase().endsWith("rogue")) {
                    store.load(new FileInputStream("keystore_sdm.jks"), "sdmFRoMuc3m".toCharArray());
                }
                else {
                    store.load(new FileInputStream("keystore_sdm_rogue.jks"), "sdmFRoMuc3m".toCharArray());
                }
                store.deleteEntry("sdm_ca");
                final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(store, "sdmFRoMuc3m".toCharArray());
                final KeyManager[] keyManagers = kmf.getKeyManagers();
                final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(store);
                final TrustManager[] trustManagers = tmf.getTrustManagers();
                final SSLContext ssl = SSLContext.getInstance("TLSv1.2");
                ssl.init(keyManagers, trustManagers, new SecureRandom());
                final HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress("localhost", 443), 443);
                final HttpsConfigurator configurator = new HttpsConfigurator(ssl);
                httpsServer.setHttpsConfigurator(configurator);
                final HttpContext httpContext2 = httpsServer.createContext("/SDM/WebRepo");
                httpsServer.start();
                final BasicAuth_Server authenticatorServer2 = new BasicAuth_Server("sdm_webrepo", "sdm", "repo4droid");
                httpContext2.setAuthenticator(authenticatorServer2);
                final Endpoint endpoint2 = Endpoint.create((Object)new RepoWS());
                endpoint2.publish((Object)httpContext2);
            }
            else {
                System.out.println("Usage: \n\tSDM_WebRepo http \t(HTTP server)\n\tSDM_WebRepo http+auth \t(HTTP server + basic authentication)\n\tSDM_WebRepo https+auth \t(HTTPS server + basic authentication)\n\tSDM_WebRepo https+auth+rogue \t(HTTPS server + basic authentication, using rogue certificate)");
            }
        }
        catch (Exception ex) {
            System.out.println("ERROR - " + ex.toString());
        }
    }
}
