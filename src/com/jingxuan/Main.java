package com.jingxuan;

import com.jingxuan.core.Downloader;
import com.jingxuan.utils.LogUtils;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        String url = null;
        if(args == null || args.length == 0){
            while (true){
                LogUtils.info("请输入下载地址");
                Scanner scanner = new Scanner(System.in);
                url = scanner.next();
                if(url != null) break;
            }
        } else {
            url = args[0];
        }

        Downloader downloader = new Downloader();
        downloader.download(url);
    }
}
