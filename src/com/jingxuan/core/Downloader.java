package com.jingxuan.core;

import com.jingxuan.constant.Constant;
import com.jingxuan.utils.FileUtils;
import com.jingxuan.utils.HttpUtils;
import com.jingxuan.utils.LogUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Downloader {

    // 该线程负责每秒打印下载信息
    public ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    // 线程池负责负责分片下载
    public ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(Constant.THREAD_NUM,
            Constant.THREAD_NUM, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(5));

    private CountDownLatch countDownLatch = new CountDownLatch(Constant.THREAD_NUM);

    public void download(String url) {

        // 获取文件名
        String httpFileName = HttpUtils.getHttpFileName(url);
        // 拼接文件下载的目标路径
        httpFileName = Constant.PATH + httpFileName;
        // 获取链接对象
        HttpURLConnection httpURLConnection = null;
        // 获取本地文件大小
        long localFileLength = FileUtils.getFileContentLength(httpFileName);
        // 获取打印信息线程
        DownloadInfoThread downloadInfoThread = null;


        try {
            httpURLConnection = HttpUtils.getHttpURLConnection(url);
            // 获取下载文件的总大小
            int contentLength = httpURLConnection.getContentLength();

            // 判断文件是否已经下载完成
            if (localFileLength >= contentLength) {
                LogUtils.info("{}已经下载完毕，无需重新下载", httpFileName);
                return;
            }

            // 创建获取下载信息的任务对象
            downloadInfoThread = new DownloadInfoThread(contentLength);

            // 将任务交给线程执行，每隔一秒执行一次
            scheduledExecutorService.scheduleAtFixedRate(downloadInfoThread, 1, 1, TimeUnit.SECONDS);

            ArrayList<Future> list = new ArrayList<>();
            split(url, list);

            countDownLatch.await();

//            list.forEach(future -> {
//                try {
//                     future.get();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                }
//            });

            // 合并文件
            if (merge(httpFileName)) {
                clearTemp(httpFileName);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.print("\r");
            System.out.print("下载完成");

            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }

            scheduledExecutorService.shutdownNow();

            poolExecutor.shutdown();
        }

    }

    /**
     * 文件切分
     *
     * @param url
     * @param futureList
     */
    public void split(String url, List<Future> futureList) {
        try {
            long contentLength = HttpUtils.getHttpFileContentLength(url);
            long size = contentLength / Constant.THREAD_NUM;
            for (int i = 0; i < Constant.THREAD_NUM; i++) {
                long startPos = i * size;
                long endPos;
                if (i == Constant.THREAD_NUM - 1) {
                    endPos = 0;
                } else {
                    endPos = startPos + size;
                }
                if (startPos != 0) {
                    startPos++;
                }
                // 创建任务对象
                DownloaderTask downloaderTask = new DownloaderTask(url, startPos, endPos, i, countDownLatch);
                // 将任务提交到线程池
                Future future = poolExecutor.submit(downloaderTask);
                futureList.add(future);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件合并
     *
     * @param fileName
     * @return
     */

    public boolean merge(String fileName) {
        LogUtils.info("开始合并文件{}", fileName);
        byte[] buffer = new byte[Constant.BYTE_SIZE];
        int len = -1;
        try (
                RandomAccessFile accessFile = new RandomAccessFile(fileName, "rw");
        ) {
            for (int i = 0; i < Constant.THREAD_NUM; i++) {
                try (BufferedInputStream bis =
                             new BufferedInputStream(new FileInputStream(fileName + ".temp" + i))
                ) {
                    while ((len = bis.read(buffer)) != -1) {
                        accessFile.write(buffer, 0, len);
                    }
                }
            }
            LogUtils.info("文件合并完毕{}", fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public boolean clearTemp(String fileName) {
        for (int i = 0; i < Constant.THREAD_NUM; i++) {
            File file = new File(fileName + ".temp" + i);
            file.delete();

        }
        return true;
    }


}
