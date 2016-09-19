package com.electromuis.smdl.Processing;

import com.electromuis.smdl.MainForm;
import com.electromuis.smdl.Pack;
import com.electromuis.smdl.Settings;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

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
    private Command command;
    boolean failed = false;

    public Status getStatus(){return status;}

    public Pack getPack(){return pack;}
    
    public enum Status {
        PENDING("Pending"),
        STARTED("Started"),
        DELETING("Deleting"),
        DOWNLOADING("Downloading"),
        EXTRACTING("Extracting"),
        FAILED("Failed"),
        DONE("Done");

        public String status;
        Status(String s){
            status=s;
        }
    }

    public enum Command {
        DOWNLOAD("Download"),
        DELETE("Delete");

        public String status;
        Command(String s){
            status=s;
        }
    }


    public PackDownloader(Pack p, Command c){
        pack = p;
        command = c;

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        label = new JLabel(p.getName());
        add(label);

        progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
        add(progressBar);

        statusLabel = new JLabel();
        add(statusLabel);

        setSize(100, 30);

        setStatus(Status.PENDING);
    }

    public void setStatus(Status s){
        status = s;
        if(s==Status.PENDING){
            statusLabel.setText(command.status);
        } else {
            statusLabel.setText(command.status + "::" + s.status);
        }
    }

    public void setPercentage(int val){
        progressBar.setValue(val);
    }

    public void start(final MainForm mainForm){

        Thread thread = new Thread(new Runnable() {
            public void run() {
                progressBar.setValue(0);
                setStatus(Status.STARTED);

                switch (command) {
                    case DOWNLOAD:
                        startDownload(mainForm);
                        break;
                    case DELETE:
                        deletePack(mainForm);
                        break;
                }


                setStatus(Status.DONE);
                mainForm.setWorking(false);
            }});

        thread.start();
    }

    public void deletePack(final MainForm mainForm){
        setStatus(Status.DELETING);
        pack.deletePack();
        setPercentage(100);

    }

    public void startDownload(final MainForm mainForm){
       try {
            String archive = pack.download(PackDownloader.this);
            if (archive != null) {
                setStatus(Status.EXTRACTING);

                String targetDir = MainForm.getSettings().getSongsFolder() + File.separator + pack.getName();
                Extractor extractor = new Extractor(archive, targetDir, PackDownloader.this);
                extractor.extract();
                setPercentage(100);

                new File(archive).delete();

            } else {
                setStatus(Status.FAILED);
                System.out.println("Download failed");
                JOptionPane.showMessageDialog(PackDownloader.this, "There was an error downloading the pack", "Download error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Extractor.ExtractionException e) {
            setStatus(Status.FAILED);
            JOptionPane.showMessageDialog(PackDownloader.this, "There was an error extracting the pack", "Archive error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (IOException | IllegalArgumentException e) {
           setStatus(Status.FAILED);
           JOptionPane.showMessageDialog(PackDownloader.this, "There was an error downloading the pack", "Download error", JOptionPane.ERROR_MESSAGE);
           System.out.println("Download failed");
           e.printStackTrace();
        }

    }
}
