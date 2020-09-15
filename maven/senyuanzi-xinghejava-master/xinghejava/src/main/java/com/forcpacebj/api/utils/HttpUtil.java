/*
 * Copyright (c) 2016 树 All rights reserved
 */
package com.forcpacebj.api.utils;

import com.forcpacebj.api.entity.UploadResultInfo;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.net.ssl.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Map.Entry;

@Slf4j
public class HttpUtil {
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final SSLSocketFactory sslSocketFactory = initSSLSocketFactory();
    private static final TrustAnyHostnameVerifier trustAnyHostnameVerifier = new HttpUtil().new TrustAnyHostnameVerifier();
    private static final int BUFFER_SIZE = 4096;
    private static String CHARSET = "UTF-8";
    private static final String BOUNDARY = java.util.UUID.randomUUID().toString();
    private static final String PREFIX = "--";
    private static final String LINEND = "\r\n";
    private static final String MULTIPART_FROM_DATA = "multipart/form-data";
    private static final int TIMEOUT = 30 * 1000;

    private static SSLSocketFactory initSSLSocketFactory() {
        try {
            TrustManager[] tm = {new HttpUtil().new TrustAnyTrustManager()};
            SSLContext sslContext = SSLContext.getInstance("TLS");    // ("TLS", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setCharSet(String charSet) {
        if (StrUtil.isBlank(charSet)) {
            throw new IllegalArgumentException("charSet can not be blank.");
        }
        HttpUtil.CHARSET = charSet;
    }

    private static HttpURLConnection getHttpConnection(String url, String method, Map<String, String> headers) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException {
        URL _url = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) _url.openConnection();
        if (conn instanceof HttpsURLConnection) {
            ((HttpsURLConnection) conn).setSSLSocketFactory(sslSocketFactory);
            ((HttpsURLConnection) conn).setHostnameVerifier(trustAnyHostnameVerifier);
        }
        conn.setRequestMethod(method);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.146 Safari/537.36");
        if (headers != null && !headers.isEmpty())
            for (Entry<String, String> entry : headers.entrySet())
                conn.setRequestProperty(entry.getKey(), entry.getValue());
        return conn;
    }

    /**
     * Send GET request
     */
    public static String get(String url, Map<String, String> queryParas, Map<String, String> headers) {
        HttpURLConnection conn = null;
        try {
            conn = getHttpConnection(buildUrlWithQueryString(url, queryParas), GET, headers);
            conn.connect();
            return readResponseString(conn);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static String get(String url, Map<String, String> queryParas) {
        return get(url, queryParas, null);
    }

    public static String get(String url) {
        return get(url, null, null);
    }

    /**
     * Send POST request
     */
    public static String post(String url, Map<String, String> queryParas, String data, Map<String, String> headers) {
        HttpURLConnection conn = null;
        try {
            conn = getHttpConnection(buildUrlWithQueryString(url, queryParas), POST, headers);
            conn.connect();
            OutputStream out = conn.getOutputStream();
            out.write(data.getBytes(CHARSET));
            out.flush();
            out.close();
            return readResponseString(conn);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static String post(String url, Map<String, String> queryParas, String data) {
        return post(url, queryParas, data, null);
    }

    public static String post(String url, String data, Map<String, String> headers) {
        return post(url, null, data, headers);
    }

    public static String post(String url, String data) {
        return post(url, null, data, null);
    }

    private static String readResponseString(HttpURLConnection conn) {
        StringBuilder sb = new StringBuilder();
        InputStream inputStream = null;
        try {
            inputStream = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, CHARSET));
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Build queryString of the url
     */
    private static String buildUrlWithQueryString(String url, Map<String, String> queryParas) {
        if (queryParas == null || queryParas.isEmpty())
            return url;
        StringBuilder sb = new StringBuilder(url);
        boolean isFirst;
        if (!url.contains("?")) {
            isFirst = true;
            sb.append("?");
        } else {
            isFirst = false;
        }
        for (Entry<String, String> entry : queryParas.entrySet()) {
            if (isFirst) isFirst = false;
            else sb.append("&");
            String key = entry.getKey();
            String value = entry.getValue();
            if (StrUtil.isNotBlank(value))
                try {
                    value = URLEncoder.encode(value, CHARSET);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            sb.append(key).append("=").append(value);
        }
        return sb.toString();
    }

    public static String readData(HttpServletRequest request) {
        BufferedReader br = null;
        try {
            StringBuilder result = new StringBuilder();
            br = request.getReader();
            for (String line; (line = br.readLine()) != null; ) {
                if (result.length() > 0) {
                    result.append("\n");
                }
                result.append(line);
            }
            return result.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
        }
    }

    /**
     * 向客户端响应一个字符串
     */
    public static void responseText(HttpServletResponse response, String text) {
        PrintWriter writer = null;
        try {
            response.setHeader("Pragma", "no-cache"); // HTTP/1.0 caches might not implement Cache-Control and might only implement Pragma: no-cache
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            writer = response.getWriter();
            writer.write(text);
            writer.flush();
        } catch (IOException e) {
            log.error("http响应失败", e);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * Http下载文件
     */
    public static String downloadFile(String urlfile, String dir, String filename) throws IOException {
        String saveFilePath = "";
        val url = new URL(urlfile);
        val con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("content-type", "application/x-www-form-urlencoded");
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val inputStream = con.getInputStream();
            saveFilePath = Paths.get(dir, filename).toString();
            val outputStream = new FileOutputStream(saveFilePath);
            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
        } else {
            throw new IOException("No file to download. Server replied HTTP code: " + responseCode);
        }
        con.disconnect();
        return saveFilePath;
    }

    public static UploadResultInfo downloadFileToOSS(String fileUrl, String dir, String filename) throws Exception {
        UploadResultInfo res = null;
        URL url = new URL(fileUrl);
        val con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("content-type", "application/x-www-form-urlencoded");
        long contentLength = con.getContentLengthLong();

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val inputStream = con.getInputStream();
            res = AliyunOSSClientUtil.uploadInputStreamToOSS(inputStream, dir, filename, contentLength);
            if (inputStream != null) inputStream.close();
        } else {
            throw new IOException("No file to download. Server replied HTTP code: " + responseCode);
        }
        con.disconnect();
        return res;
    }

    /**
     * 上传文件
     *
     * @param file       文件
     * @param RequestURL 上传URL
     * @return String
     */
    public static String upload(File file, String RequestURL) {
        try {
            URL url = new URL(RequestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIMEOUT);
            conn.setConnectTimeout(TIMEOUT);
            conn.setDoInput(true);// 允许输入流
            conn.setDoOutput(true);// 允许输出流
            conn.setUseCaches(false);// 不允许使用缓存
            conn.setRequestMethod("POST");// 请求方式
            conn.setRequestProperty("Charset", CHARSET);// 设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Charsert", CHARSET);
            conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);
            if (file != null) {
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                StringBuilder sb = new StringBuilder();
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINEND);                //这里重点注意： name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件 filename是文件的名字，包含后缀名的 比如:abc.png
                sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"");
                sb.append(LINEND);                //  sb.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINEND);
                sb.append("Content-Type: application/octet-stream; charset=");
                sb.append(CHARSET);
                sb.append(LINEND);
                sb.append(LINEND);
                dos.write(sb.toString().getBytes());
                InputStream is = new FileInputStream(file);
                byte[] bytes = new byte[1024];
                int len;
                while ((len = is.read(bytes)) != -1) {
                    dos.write(bytes, 0, len);
                }
                is.close();
                dos.write(LINEND.getBytes());
                byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
                dos.write(end_data);
                dos.flush();
                InputStream input = conn.getInputStream();
                StringBuilder sb1 = new StringBuilder();
                int ss;
                while ((ss = input.read()) != -1) {
                    sb1.append((char) ss);
                }
                return sb1.toString();
            }
        } catch (IOException ex) {
            log.error("上传文件异常" + RequestURL, ex);
        }
        return "";
    }

    /**
     * https 域名校验
     */
    private class TrustAnyHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    /**
     * https 证书管理
     */
    private class TrustAnyTrustManager implements X509TrustManager {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
    }

    /**
     * 将网址转换为HashCode作为本地文件缓存
     *
     * @param fileNamePrefix 临时文件名前缀，可以为空
     * @param url            网络地址文件
     * @return 本地临时文件路径
     */
    public static String getHttpFileAsLocalTemp(String fileNamePrefix, String url) throws Exception {

        return getHttpFileAsLocalTemp(fileNamePrefix, url, null);
    }

    /**
     * 将网址转换为HashCode作为本地文件缓存
     *
     * @param fileNamePrefix 临时文件名前缀，可以为空
     * @param url            网络地址文件
     * @param fileSuffix     文件名称后缀
     * @return 本地临时文件路径
     */
    public static String getHttpFileAsLocalTemp(String fileNamePrefix, String url, String fileSuffix) throws Exception {

        if (StrUtil.isBlank(fileNamePrefix)) {
            fileNamePrefix = "";
        }

        String cacheFileName = fileNamePrefix + url.hashCode();
        if (StrUtil.isNotBlank(fileSuffix)) {
            cacheFileName += fileSuffix;
        }
        val cacheFilePath = Paths.get(FileUtil.getSystemTempFolder(), cacheFileName).toString();
        if (!FileUtil.fileIsExists(cacheFilePath)) {
            //log.info("开始下载文件：" + url + " --> " + cacheFilePath);
            HttpUtil.downloadFile(url, FileUtil.getSystemTempFolder(), cacheFileName);
        }

        return cacheFilePath;
    }
}