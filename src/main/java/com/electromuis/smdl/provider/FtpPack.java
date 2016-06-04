package com.electromuis.smdl.provider;

import com.electromuis.smdl.MainForm;
import com.electromuis.smdl.Pack;
import com.electromuis.smdl.Processing.PackDownloader;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;

/**
 * Created by electromuis on 01.06.16.
 */
public class FtpPack extends Pack {
    private long fileSize = 0;

    public FtpPack(String name, String size, String type, String url, long fileSize) {
        super(name, size, type, url);
        this.fileSize = fileSize;
    }

    @Override
    public String download(PackDownloader downloader) throws IOException {
        String ret = null;

        FTPClient ftp = new FTPClient();
        try {
            int reply;
            String server = "gamebreakersnl.synology.me";
            ftp.connect(server);

            reply = ftp.getReplyCode();

            if(!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                System.err.println("FTP server refused connection.");
            } else {
                ftp.login("public", "ddr1352");
                ftp.enterLocalPassiveMode();
                ftp.setFileType(FTP.BINARY_FILE_TYPE);

                String fileName = FilenameUtils.getName(getUrl());

                String saveFilePath = MainForm.getSettings().getSongsFolder() + File.separator + fileName;



                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(saveFilePath));
                InputStream inputStream = ftp.retrieveFileStream(getUrl());
                byte[] bytesArray = new byte[4096];
                int bytesRead = -1;
                long readAmmount = 0;

                downloader.setStatus(PackDownloader.Status.DOWNLOADING);
                while ((bytesRead = inputStream.read(bytesArray)) != -1) {
                    readAmmount += bytesRead;
                    outputStream.write(bytesArray, 0, bytesRead);

                    int progress = (int) ((100 * readAmmount) / fileSize);
                    downloader.setPercentage(progress);
                }

                boolean success = ftp.completePendingCommand();
                if(success){
                    ret = saveFilePath;
                } else {
                    System.out.println(ftp.getReplyString());
                }

                inputStream.close();
                outputStream.close();

            }




            ftp.logout();
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if(ftp.isConnected()) {
                ftp.disconnect();
            }
        }

        return ret;
    }
}
