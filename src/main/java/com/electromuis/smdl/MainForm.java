package com.electromuis.smdl;

import com.electromuis.smdl.Processing.PackDownloader;
import com.electromuis.smdl.provider.PackProvider;
import com.electromuis.smdl.provider.ProviderLoading;
import com.electromuis.smdl.provider.StepmaniaOnline;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by electromuis on 12.05.16.
 */
public class MainForm {
    private JTable packsTable;
    private JPanel panel1;
    private JButton fetchPacks;
    private JButton applyPacksButton;
    private JScrollPane downloadPane;
    private JScrollPane songsPane;
    private JPanel downloadPanel;
    private JButton resetButton;
    private PacksModel packsModel;
    private static Settings settings;
    //private List<PackDownloader> packDownloaders;
    private Map<String, Pack> packs;
    private ProviderLoading providerLoading;
    private boolean downloading = false;

    public MainForm() {
        fetchPacks.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                providerLoading.updatePacks();
            }
        });
        applyPacksButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    cleanDownloaders();

                    boolean added = false;

                    for(Pack p : packsModel.getPacks()){
                        if(p.isDownload() && !p.getExists()){
                            addDownloader(p);
                             added = true;
                        }
                    }

                    if(added){
                        int n = JOptionPane.showConfirmDialog(
                                MainForm.this.panel1,
                                "Do you want to apply the current changes?",
                                "Apply packs",
                                JOptionPane.YES_NO_OPTION);

                        if(n == JOptionPane.YES_OPTION) {
                            for (Component pd : downloadPanel.getComponents()) {
                                if(pd instanceof PackDownloader) {
                                    setDownloading(true);
                                    ((PackDownloader) pd).startDownload(MainForm.this);
                                }
                            }
                        }
                    }
                }
            });
            }
        });

        resetButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                updateExistingPacks();
            }
        });

        packsModel = new PacksModel(new Pack[0]);

        settings = new Settings();
        initSettings();

        packsTable.setModel(packsModel);

        providerLoading = new ProviderLoading(this);
        providerLoading.pack();
    }

    public void setDownloading(boolean b){
        for (Component pd : downloadPanel.getComponents()) {
            if(pd instanceof PackDownloader) {
                switch (((PackDownloader) pd).getStatus()){
                    case DONE:
                    case PENDING:
                        break;
                    default:
                        return;
                }
            }
        }

        downloading = b;
        applyPacksButton.setEnabled(!b);
        applyPacksButton.setText(b?
        "Downloading ...":
        "Apply packs");

        resetButton.setEnabled(!b);
        fetchPacks.setEnabled(!b);
    }

    public void setPacks(Map<String, Pack> packs){
        this.packs = packs;

        updateExistingPacks();
    }

    private void updateExistingPacks(){
        for (Map.Entry<String, Pack> entry : packs.entrySet()) {
            Pack pack = entry.getValue();
            pack.setDownload(pack.getExists());
        }

        packsModel.setPacks(getPacksArray());
        packsTable.updateUI();
    }

    private void addDownloader(Pack p){
        boolean exists = false;
        for (Component pd : downloadPanel.getComponents()) {
            if(pd instanceof PackDownloader) {
                Pack pdl = ((PackDownloader) pd).getPack();
                if(pdl.equals(p)){
                    exists = true;
                    break;
                }
            }
        }

        if(!exists) {
            downloadPanel.add(new PackDownloader(p));
            downloadPanel.updateUI();
        }
    }

    private void cleanDownloaders(){
        for (Component p : downloadPanel.getComponents()) {
            PackDownloader pd = (PackDownloader)p;
            if(pd.getStatus() == PackDownloader.Status.DONE){
                downloadPanel.remove(p);
            }
        }
        downloadPanel.updateUI();
    }

    private Pack[] getPacksArray(){
        Pack[] packArray = new Pack[packs.size()];

        int count = 0;
        for(Pack p : packs.values()){
            packArray[count] = p;
            count++;
        }

        return packArray;
    }

    public static Settings getSettings(){
        return settings;
    }

    private void initSettings(){

        if (settings.getSongsFolder()==null){
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY);
            int returnVal = chooser.showSaveDialog(panel1);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File yourFolder = chooser.getSelectedFile();
                settings.setSongsFolder(yourFolder.getAbsolutePath());
            } else {
                initSettings();
            }
        }
    }

    public void run() {
        JFrame frame = new JFrame("Stepmania DLM");
        frame.setContentPane(new MainForm().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
            }
        });
        frame.pack();
        frame.setVisible(true);
    }

    private void createUIComponents() {
        downloadPanel = new JPanel();
        downloadPanel.setLayout(new GridLayout(0,1 ));

    }
}
