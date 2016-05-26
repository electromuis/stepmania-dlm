package com.electromuis.smdl;

import com.electromuis.smdl.Processing.PackDownloader;
import com.electromuis.smdl.provider.PackProvider;
import com.electromuis.smdl.provider.ProviderLoading;
import com.electromuis.smdl.provider.StepmaniaOnline;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private List<PackDownloader> packDownloaders;
    private Map<String, Pack> packs;
    private ProviderLoading providerLoading;

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
                cleanDownloaders();

                for(Pack p : packsModel.getPacks()){
                    if(p.isDownload() && !p.getExists()){
                        addDownloader(new PackDownloader(p));
                    }
                }

                for(PackDownloader pd : packDownloaders){
                    pd.startDownload();
                }

                
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
        packDownloaders = new ArrayList<PackDownloader>();

        providerLoading = new ProviderLoading(this);
        providerLoading.pack();
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

    private void addDownloader(PackDownloader pd){
        packDownloaders.add(pd);
        downloadPanel.add(pd);
        downloadPanel.updateUI();
    }

    private void cleanDownloaders(){
        for(PackDownloader pd : packDownloaders){
            if(pd.getStatus() == PackDownloader.Status.DONE){
                packDownloaders.remove(pd);
            }
        }

        downloadPanel.removeAll();
        for(PackDownloader pd : packDownloaders)
            downloadPanel.add(pd);
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
        frame.pack();
        frame.setVisible(true);
    }

    private void createUIComponents() {
        downloadPanel = new JPanel();
        downloadPanel.setLayout(new GridLayout(0,1 ));

    }
}
