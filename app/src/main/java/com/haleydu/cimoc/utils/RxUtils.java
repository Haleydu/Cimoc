package com.haleydu.cimoc.utils;

import android.annotation.SuppressLint;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class RxUtils {

    @SuppressLint("TrulyRandom")
    public static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory sSLSocketFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllManager()},
                    new SecureRandom());
            sSLSocketFactory = sc.getSocketFactory();
        } catch (Exception ignored) {
        }
        return sSLSocketFactory;
    }

    public static class TrustAllManager implements X509TrustManager {
        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    public static class TrustAllHostnameVerifier implements HostnameVerifier {
        @SuppressLint("BadHostnameVerifier")
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}
