package com.electromuis.smdl.Processing;

import com.electromuis.smdl.MainController;
import com.electromuis.smdl.Pack;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class PackRow {
    private Status status;
    private Command command;

    ProgressBar bar;
    Label label;

    public Pack pack;

    static int colorCounter = 0;

    public String colors[] = {
            "000000",
            "FFFFFF",
            "FF0000"
    };

    public void setLabel(Label label) {
        this.label = label;
    }

    public boolean isWorking() {
        switch (status) {
            case DONE:
            case FAILED:
            case PENDING:
                return false;
            default:
                return true;
        }
    }

    public enum Status {
        PENDING("Pending"),
        STARTED("Started"),
        RETRIEVING("Fetching info"),
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

    public PackRow(Pack p, Command c) throws IOException {
        pack = p;
        command = c;

        setStatus(Status.PENDING);
    }

    public void setPack(Pack pack) {
        this.pack = pack;
    }

    public void setStatus(Status s){
        status = s;

        Platform.runLater(() -> {
            if(label != null) {
                label.setText(getCommand() + " :: " + getStatus());
            }
        });
    }

    public void setBar(ProgressBar bar) {
        this.bar = bar;
    }

    public Status getStatus() {
        return status;
    }

    public Command getCommand() {
        return command;
    }

    protected void startDownload(){
        try {
            boolean success = pack.download(this);

            if (!success) {
                setStatus(Status.FAILED);
                System.out.println("Download failed");
                JOptionPane.showMessageDialog(null, "There was an error downloading the pack", "Download error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            setStatus(Status.FAILED);
            JOptionPane.showMessageDialog(null, "There was an error installing the pack", "Download error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    protected void deletePack(){
        setStatus(Status.DELETING);
        pack.deletePack();
    }

    public void start(MainController mainController){
        new Thread(() -> {
            setStatus(Status.STARTED);

            switch (command) {
                case DOWNLOAD:
                    startDownload();
                    break;
                case DELETE:
                    deletePack();
                    break;
            }

            setProgress(1);
            setStatus(Status.DONE);

            if(mainController.setWorking(false)) {
                JOptionPane.showMessageDialog(null, "Applying done!", "Done", JOptionPane.INFORMATION_MESSAGE);
            }
        }).start();
    }

    public void setProgress(float progress){
        Platform.runLater(() -> {
            bar.setProgress(progress);
        });
    }

    public Pack getPack() {
        return pack;
    }
}
