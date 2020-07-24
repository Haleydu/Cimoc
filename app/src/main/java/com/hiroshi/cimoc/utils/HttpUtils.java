package com.hiroshi.cimoc.utils;

import okhttp3.Request;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URL;
import javax.annotation.Nullable;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * Created by toesbieya on 2020/03/04
 * okhttp的常用方法
 */
public class HttpUtils {
    public static Request getSimpleMobileRequest(String url) {
        return new Request.Builder()
                .addHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 12_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/12.0 Mobile/15A372 Safari/604.1")
                .url(url)
                .build();
    }

    public static JSONObject httpsRequest(String requestUrl, String requestMethod, @Nullable String outputStr) {
        JSONObject jsonObject = null;
        StringBuffer buffer = new StringBuffer();
        try {
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化
            TrustManager[] tm = {  new RxUtils.TrustAllManager()  };
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tm, new java.security.SecureRandom());
            // 从上述SSLContext对象中得到SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();

            URL url = new URL(requestUrl);
            HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();
            httpUrlConn.setSSLSocketFactory(ssf);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);
            httpUrlConn.setInstanceFollowRedirects(false);
            // 设置请求方式（GET/POST）
            httpUrlConn.setRequestMethod(requestMethod);

            if ("GET".equalsIgnoreCase(requestMethod)) {
//                httpUrlConn.addRequestProperty("refer","https://www.copymanga.com/search?q=".concat(requestUrl.replace("https://www.copymanga.com/api/kb/web/search/count?offset=0&platform=2&limit=500&q=","")));
                httpUrlConn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                httpUrlConn.addRequestProperty("Accept-Charset","UTF-8");
                httpUrlConn.addRequestProperty("contentType","utf-8");
                httpUrlConn.addRequestProperty("pragma","no-cache");
                httpUrlConn.addRequestProperty("cache-control","no-cache");
                httpUrlConn.addRequestProperty("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36");
                httpUrlConn.addRequestProperty("accept","*/*");
                httpUrlConn.addRequestProperty("sec-fetch-site","same-origin");
                httpUrlConn.addRequestProperty("sec-fetch-mode","cors");
                httpUrlConn.addRequestProperty("sec-fetch-dest","empty");
//                httpUrlConn.addRequestProperty("accept-encoding","gzip, deflate, br");
                httpUrlConn.addRequestProperty("accept-language","zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7");
                httpUrlConn.connect();
            }

            // 当有数据需要提交时
            if (null != outputStr) {
                httpUrlConn.setDoOutput(false);
                OutputStream outputStream = httpUrlConn.getOutputStream();
                // 注意编码格式，防止中文乱码
                outputStream.write(outputStr.getBytes("UTF-8"));
                outputStream.close();
            }

            // 将返回的输入流转换成字符串
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源
            inputStream.close();
            httpUrlConn.disconnect();
            str=buffer.toString();
            jsonObject = new JSONObject(str);
        } catch (ConnectException ce) {
            System.out.println(ce.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jsonObject;
    }
}
