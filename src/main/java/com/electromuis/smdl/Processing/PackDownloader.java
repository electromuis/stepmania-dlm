package com.electromuis.smdl.Processing;

import com.electromuis.smdl.MainForm;
import com.electromuis.smdl.Pack;
import com.electromuis.smdl.Settings;
import com.electromuis.smdl.provider.FtpPack;
import com.electromuis.smdl.provider.HttpPack;
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
    boolean failed = false;

    public Status getStatus(){return status;}

    public Pack getPack(){return pack;}
    
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

    public void setStatus(Status s){
        status = s;
        statusLabel.setText(s.status);
        updateUI();
    }

    public void setPercentage(int val){
        progressBar.setValue(val);
        updateUI();
    }

    public void startDownload(final MainForm mainForm){
        Thread downloadThread = new Thread(new Runnable() {
            public void run() {
                progressBar.setValue(0);
                setStatus(Status.STARTED);


                try {
                    String archive = pack.download(PackDownloader.this);
                    if (archive != null) {
                        setStatus(Status.EXTRACTING);

                        String targetDir = MainForm.getSettings().getSongsFolder() + File.separator + pack.getName();
                        Extractor extractor = new Extractor(archive, targetDir, PackDownloader.this);
                        extractor.extract();
                        setPercentage(100);

                        new File(archive).delete();

                        setStatus(Status.DONE);
                        mainForm.setDownloading(false);
                    } else {
                        System.out.println("Download failed");
                        JOptionPane.showMessageDialog(PackDownloader.this, "There was an error downloading the pack", "Archive error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Extractor.ExtractionException e) {
                    JOptionPane.showMessageDialog(PackDownloader.this, "There was an error extracting the pack", "Archive error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }




            }
        });

        downloadThread.start();
    }
}
