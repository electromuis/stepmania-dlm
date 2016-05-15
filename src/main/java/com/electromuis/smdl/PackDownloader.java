package com.electromuis.smdl;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.UnzipParameters;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by electromuis on 13.05.16.
 */
public class PackDownloader extends JPanel {
    private Pack pack;
    private JProgressBar progressBar;
    private JLabel label;
    private JLabel statusLabel;
    private Settings settings;
    private Status status;
    private static final int BUFFER_SIZE = 4096;
    boolean failed = false;

    public Status getStatus(){return status;}
    
    public enum Status {
        PENDING("Pending"),
        STARTED("Started"),
        DOWNLOADING("Downloading"),
        EXTRACTING("Extracting"),
        MOVING("Moving"),
        DONE("Done");

        public String status;
        Status(String s){
            status=s;
        }

    }

    public PackDownloader(Pack p){
        pack = p;

        setLayout(new GridBagLayout());

        label = new JLabel(p.getName());
        add(label);

        progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
        add(progressBar);

        statusLabel = new JLabel();
        add(statusLabel);

        setStatus(Status.PENDING);
    }

    private void setStatus(Status s){
        status = s;
        statusLabel.setText(s.status);
        updateUI();
    }

    public void startDownload(){
        Thread downloadThread = new Thread(new Runnable() {
            public void run() {
                progressBar.setValue(0);
                setStatus(Status.STARTED);

                try {
                    URL url = new URL(pack.getUrl());
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
                            fileName = pack.getUrl().substring(pack.getUrl().lastIndexOf("/") + 1,
                                    pack.getUrl().length());
                        }

                        System.out.println("Content-Type = " + contentType);
                        System.out.println("Content-Disposition = " + disposition);
                        System.out.println("Content-Length = " + contentLength);
                        System.out.println("fileName = " + fileName);

                        // opens input stream from the HTTP connection
                        InputStream inputStream = httpConn.getInputStream();
                        String saveFilePath = MainForm.getSettings().getSongsFolder() + File.separator + fileName;

                        // opens an output stream to save into file
                        FileOutputStream outputStream = new FileOutputStream(saveFilePath);

                        int bytesRead = -1;
                        long readAmmount = 0;
                        byte[] buffer = new byte[BUFFER_SIZE];

                        setStatus(Status.DOWNLOADING);

                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            readAmmount += bytesRead;
                            outputStream.write(buffer, 0, bytesRead);


                            int progress = (int) ((100*readAmmount)/contentLength);
                            progressBar.setValue(progress);
                            updateUI();
                        }

                        outputStream.close();
                        inputStream.close();

                        System.out.println("File downloaded");

                        setStatus(Status.EXTRACTING);

                        String ext = FilenameUtils.getExtension(saveFilePath);
                        if(ext.equals("zip")){
                            ZipFile zip = new ZipFile(saveFilePath);
                            File targetDir = new File(MainForm.getSettings().getSongsFolder()+File.separator+pack.getName());
                            if(!targetDir.exists())
                                targetDir.mkdirs();

                            zip.extractAll(targetDir.getAbsolutePath());

                        } else if(ext.equals("rar")){

                        } else {
                            failed = true;
                        }
                    } else {
                        failed = true;
                        System.out.println("No file to download. Server replied HTTP code: " + responseCode);
                    }
                    httpConn.disconnect();

                } catch (MalformedURLException e) {
                    failed = true;
                    e.printStackTrace();
                } catch (IOException e) {
                    failed = true;
                    e.printStackTrace();
                } catch (ZipException e) {
                    failed = true;
                    e.printStackTrace();
                }


                setStatus(Status.DONE);

            }
        });

        downloadThread.start();
    }
}
