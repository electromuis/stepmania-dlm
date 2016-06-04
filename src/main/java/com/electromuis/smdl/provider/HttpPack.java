package com.electromuis.smdl.provider;

import com.electromuis.smdl.MainForm;
import com.electromuis.smdl.Pack;
import com.electromuis.smdl.Processing.PackDownloader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by electromuis on 01.06.16.
 */
public class HttpPack extends Pack {
    private static final int BUFFER_SIZE = 4096;

    public HttpPack(String name, String size, String type, String url) {
        super(name, size, type, url);
    }

    @Override
    public String download(PackDownloader downloader) throws IOException {
        String ret = null;

        URL url = new URL(this.getUrl());

        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = "";
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            int contentLength = httpConn.getContentLength();

            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10,
                            disposition.length() - 1);
                }
            } else {
                // extracts file name from URL
                fileName = getUrl().substring(getUrl().lastIndexOf("/") + 1,
                        getUrl().length()).replace("%20", " ");
            }

            System.out.println("Content-Type = " + contentType);
            System.out.println("Content-Disposition = " + disposition);
            System.out.println("Content-Length = " + contentLength);
            System.out.println("fileName = " + fileName);

            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            String saveFilePath = MainForm.getSettings().getSongsFolder() + File.separator + fileName;

            File saveFile = new File(saveFilePath);
            if (!(saveFile.exists() && (saveFile.length() == contentLength))) {
                downloader.setStatus(PackDownloader.Status.DOWNLOADING);

                FileOutputStream outputStream = new FileOutputStream(saveFilePath);

                int bytesRead = -1;
                long readAmmount = 0;
                byte[] buffer = new byte[BUFFER_SIZE];

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    readAmmount += bytesRead;
                    outputStream.write(buffer, 0, bytesRead);


                    int progress = (int) ((100 * readAmmount) / contentLength);
                    downloader.setPercentage(progress);
                }
                outputStream.close();
            }


            inputStream.close();

            ret = saveFilePath;
        }

        return ret;
    }
}
