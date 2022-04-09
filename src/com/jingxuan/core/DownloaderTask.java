package com.jingxuan.core;

import com.jingxuan.constant.Constant;
import com.jingxuan.utils.HttpUtils;
import com.jingxuan.utils.LogUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class DownloaderTask implements Callable {

    private String url;

    private long startPos;

    private long endPos;

    // 表示当前是哪一部分
    private int part;

    private CountDownLatch countDownLatch;


    public DownloaderTask(String url, long startPos, long endPos, int part, CountDownLatch countDownLatch) {
        this.url = url;
        this.startPos = startPos;
        this.endPos = endPos;
        this.part = part;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public Object call() throws Exception {

        String httpFileName = HttpUtils.getHttpFileName(url);
        // 分块的文件名
        httpFileName = httpFileName + ".temp" + part;
        // 目标路径
        httpFileName = Constant.PATH + httpFileName;

        // 获取分块下载的链接
        HttpURLConnection httpURLConnection = HttpUtils.getHttpURLConnection(url, startPos, endPos);

        try (
                //获取输入流，将对应的文件写入内存
                InputStream input = httpURLConnection.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(input);
                // 将内存中的文件写入硬盘
                RandomAccessFile accessFile = new RandomAccessFile(httpFileName, "rw");
        ) {
            int len = -1;
            byte[] buffer = new byte[Constant.BYTE_SIZE];
            while ((len = bis.read(buffer)) != -1) {
                // 一秒钟下载数据之和，通过原子类进行操作
                DownloadInfoThread.downSize.add(len);
                accessFile.write(buffer, 0, len);
            }

        } catch (FileNotFoundException e) {
            LogUtils.error("下载的文件不存在{}", url);
            return false;
        } catch (IOException e) {
            LogUtils.error("下载失败");
            return false;
        } finally {
            httpURLConnection.disconnect();
            countDownLatch.countDown();
        }
        return true;
    }
}
