package com.electromuis.smdl.provider;

import com.electromuis.smdl.MainController;
import com.electromuis.smdl.MainForm;
import com.electromuis.smdl.Pack;
import com.electromuis.smdl.Processing.PackDownloader;
import com.electromuis.smdl.Processing.PackRow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by electromuis on 23.06.16.
 */
public abstract class DefaultProvider implements PackProvider {
    private static int BUFFER_SIZE = 2048;

    public String downloadFile(PackRow downloader, InputStream inputStream, Pack p) throws IOException {
        String saveFilePath = MainController.getSettings().getSongsFolder() + File.separator + p.getFileName();
        File saveFile = new File(saveFilePath);

        if (!(saveFile.exists() && (saveFile.length() == p.getContentLength()))) {
            downloader.setStatus(PackRow.Status.DOWNLOADING);

            FileOutputStream outputStream = new FileOutputStream(saveFilePath);

            int bytesRead = -1;
            long readAmmount = 0;
            byte[] buffer = new byte[BUFFER_SIZE];

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                readAmmount += bytesRead;
                outputStream.write(buffer, 0, bytesRead);


                float progress = ((float)readAmmount) / p.getContentLength();
                if(downloader.getBar() != null) {
                    downloader.getBar().setProgress(progress);
                }
            }
            outputStream.close();
        }


        inputStream.close();

        return saveFilePath;
    }

    public abstract InputStream getInputStream(Pack p) throws IOException;

    @Override
    public String download(Pack p, PackRow pd) throws IOException {
        return downloadFile(pd, getInputStream(p), p);
    }

    public void disconnect() throws IOException {;}

    public static class Config{
        private String username;
        private String password;
        private String endpoint;
        private String root;

        public Config(String endpoint, String username, String password, String root) {
            this.endpoint = endpoint;
            this.password = password;
            this.root = root;
            this.username = username;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getRoot() {
            return root;
        }

        public void setRoot(String root) {
            this.root = root;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }
    }
}
