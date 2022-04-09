package com.jingxuan.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class HttpUtils {

    /**
     * 获取文件下载大小
     * @param url
     * @return
     * @throws IOException
     */
    public static long getHttpFileContentLength(String url) throws IOException {
        int contentLength;
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = getHttpURLConnection(url);
            contentLength = httpURLConnection.getContentLength();
        } finally {
            if(httpURLConnection != null){
                httpURLConnection.disconnect();
            }
        }
        return contentLength;
    }





    /**
     * 分块下载
     * @param url 下载地址
     * @param startPos 下载文件起始位置
     * @param endPOS 下载文件结束位置
     * @return
     */

    public static HttpURLConnection getHttpURLConnection(String url, long startPos, long endPOS) throws IOException {
        HttpURLConnection httpURLConnection = getHttpURLConnection(url);
        LogUtils.info("x下载的区间是：{}-{}", startPos, endPOS);
        if(endPOS != 0){
            httpURLConnection.setRequestProperty("RANGE", "bytes=" + startPos + "-" + endPOS);
        } else {
            httpURLConnection.setRequestProperty("RANGE", "bytes=" + startPos + "-");
        }

        return httpURLConnection;
    }



    /**
     * 获取HttpURLConnection链接对象
     * @param url 文件地址
     * @return
     * @throws IOException
     */

    public static HttpURLConnection getHttpURLConnection(String url) throws IOException {
        URL httpUrl = new URL(url);
        HttpURLConnection httpURLConnection = (HttpURLConnection)httpUrl.openConnection();
        httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.51 Safari/537.36");
        return httpURLConnection;
    }

    /**
     * 获取网络文件的文件名
     * @param url 文件地址
     * @return
     */
    public static String getHttpFileName(String url){
        int index = url.lastIndexOf("/");
        return url.substring(index + 1);

    }
}
