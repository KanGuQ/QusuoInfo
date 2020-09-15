/*
 * Copyright (c) 2016 cocoon-data.com All rights reserved
 */
package com.forcpacebj.api.utils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Map;

/**
 * Http请求工具类
 */
public final class HttpUtils {

    private static final int TIMEOUT = 30 * 1000;   //30秒   单位：毫秒

    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.146 Safari/537.36";

    public static String get(String url) {
        return HttpUtil.get(url);
    }

    public static String get(String url, Map<String, String> queryParas) {
        return HttpUtil.get(url, queryParas);
    }

    public static String post(String url, String data) {
        return HttpUtil.post(url, data);
    }

    /**
     * 涉及资金回滚的接口会使用到商户证书，包括退款、撤销接口的请求
     *
     * @param url      请求的地址
     * @param data     xml数据
     * @param certPath 证书文件目录
     * @param certPass 证书密码
     * @return String 回调的xml信息
     */
    public static String postSSL(String url, String data, String certPath, String certPass) {
        HttpsURLConnection conn = null;
        OutputStream out = null;
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            KeyStore clientStore = KeyStore.getInstance("PKCS12");
            clientStore.load(new FileInputStream(certPath), certPass.toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(clientStore, certPass.toCharArray());
            KeyManager[] kms = kmf.getKeyManagers();
            SSLContext sslContext = SSLContext.getInstance("TLSv1");
            sslContext.init(kms, null, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            URL _url = new URL(url);
            conn = (HttpsURLConnection) _url.openConnection();
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("User-Agent", DEFAULT_USER_AGENT);
            conn.connect();
            out = conn.getOutputStream();
            out.write(data.getBytes(Charsets.UTF_8));
            out.flush();
            inputStream = conn.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, Charsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtil.closeQuietly(out);
            IOUtil.closeQuietly(reader);
            IOUtil.closeQuietly(inputStream);
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * 下载素材（GET方式）
     *
     * @return InputStream 流，考虑到这里可能返回json或file
     */
    public static InputStream download(String url) {
        try {
            URL _url = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) _url.openConnection();
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("User-Agent", DEFAULT_USER_AGENT);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.connect();
            return conn.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取永久素材（POST方式）
     *
     * @return InputStream 流，考虑到这里可能返回json或file
     */
    public static InputStream download(String url, String params) {
        try {
            URL _url = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) _url.openConnection();
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "Keep-Alive");
            conn.setRequestProperty("User-Agent", DEFAULT_USER_AGENT);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.connect();
            if (StrUtil.isNotBlank(params)) {
                OutputStream out = conn.getOutputStream();
                out.write(params.getBytes(Charsets.UTF_8));
                out.flush();
                IOUtil.closeQuietly(out);
            }
            return conn.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String upload(String requestUrl, File file, String params) {
        try {
            URL url = new URL(requestUrl);
            long filelength = file.length();
            String fileName = file.getName();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(TIMEOUT);
            con.setReadTimeout(TIMEOUT);
            con.setRequestMethod("POST"); // 以Post方式提交表单，默认get方式
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false); // post方式不能使用缓存
            // 设置请求头信息
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Charset", "UTF-8");        // 设置边界,这里的boundary是http协议里面的分割符，不懂的可惜百度(http 协议 boundary)，这里boundary 可以是任意的值(111,2222)都行
            String BOUNDARY = "----------" + System.currentTimeMillis();
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);        // 请求正文信息
            // 第一部分：
            StringBuilder sb = new StringBuilder();        //这块是上传video是必须的参数，你们可以在这里根据文件类型做if/else 判断
            if (StrUtil.isNotBlank(params)) {
                sb.append("--"); // 必须多两道线
                sb.append(BOUNDARY);
                sb.append("\r\n");
                sb.append("Content-Disposition: form-data;name=\"description\" \r\n\r\n");
                sb.append(params + "\r\n");
            }
            /**
             * 这里重点说明下，上面两个参数完全可以卸载url地址后面 就想我们平时url地址传参一样，
             * http://api.weixin.qq.com/cgi-bin/material/add_material?access_token=##ACCESS_TOKEN##&type=""&description={} 这样，如果写成这样，上面的
             * 那两个参数的代码就不用写了，不过media参数能否这样提交我没有试，感兴趣的可以试试
             */
            sb.append("--"); // 必须多两道线
            sb.append(BOUNDARY);
            sb.append("\r\n");
            //这里是media参数相关的信息，这里是否能分开下我没有试，感兴趣的可以试试
            sb.append("Content-Disposition: form-data;name=\"media\";filename=\"" + fileName + "\";filelength=\"" + filelength + "\" \r\n");
            sb.append("Content-Type:application/octet-stream\r\n\r\n");
            byte[] head = sb.toString().getBytes("utf-8");
            // 获得输出流
            OutputStream out = new DataOutputStream(con.getOutputStream());
            // 输出表头
            out.write(head);
            // 文件正文部分
            // 把文件已流文件的方式 推入到url中
            DataInputStream in = new DataInputStream(new FileInputStream(file));
            int bytes = 0;
            byte[] bufferOut = new byte[1024];
            while ((bytes = in.read(bufferOut)) != -1) {
                out.write(bufferOut, 0, bytes);
            }
            in.close();
            // 结尾部分，这里结尾表示整体的参数的结尾，结尾要用"--"作为结束，这些都是http协议的规定
            byte[] foot = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("utf-8");// 定义最后数据分隔线
            out.write(foot);
            out.flush();
            out.close();
            StringBuffer buffer = new StringBuffer();
            BufferedReader reader = null;
            // 定义BufferedReader输入流来读取URL的响应
            reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            return buffer.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
