package com.electromuis.smdl;

import com.electromuis.smdl.Processing.PackDownloader;
import com.electromuis.smdl.provider.ProviderLoading;
import com.sun.deploy.util.ArrayUtil;
import org.apache.commons.io.FilenameUtils;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

/**
 * Created by electromuis on 12.05.16.
 */
public class MainForm {
    private JTable packsTable;
    private JPanel panel1;
    private JButton applyPacksButton;
    private JScrollPane downloadPane;
    private JScrollPane songsPane;
    private JPanel downloadPanel;
    private JButton resetButton;
    private JPanel panel2;
    private JButton clearButton;
    private JMenuItem updateMenuItem;
    private PacksModel packsModel;
    private static Settings settings;
    public static Map<String, Pack> packs = new HashMap<String, Pack>();
    private ProviderLoading providerLoading;
    private boolean working = false;
    private JFrame mainFrame;

    public MainForm() {
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
                            addDownloader(p, PackDownloader.Command.DOWNLOAD);
                             added = true;
                        } else if(!p.isDownload() && p.getExists()){
                            addDownloader(p, PackDownloader.Command.DELETE);
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
                                    setWorking(true);
                                    ((PackDownloader) pd).start(MainForm.this);
                                }
                            }
                        }
                    }
                }
            });
            }
        });

        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (Map.Entry<String, Pack> entry : packs.entrySet()) {
                    Pack pack = entry.getValue();
                    pack.setDownload(false);
                }
                packsTable.updateUI();
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

    public void setWorking(boolean b){
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

        working = b;
        applyPacksButton.setEnabled(!b);
        applyPacksButton.setText(b?
        "Working ...":
        "Apply packs");

        resetButton.setEnabled(!b);
        //updateMenuItem.setEnabled(!b);
        clearButton.setEnabled(!b);
    }

    public void setPacks(Map<String, Pack> packs){
        this.packs = packs;

        updateExistingPacks();
    }

    public void updateExistingPacks(){
        for (Map.Entry<String, Pack> entry : packs.entrySet()) {
            Pack pack = entry.getValue();
            pack.setDownload(pack.getExists());
        }

        packsModel.setPacks(getPacksArray());
        packsTable.updateUI();
    }

    private void addDownloader(Pack p, PackDownloader.Command command){
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
            downloadPanel.add(new PackDownloader(p, command));
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

    private JMenuBar buildMenu(){
        JMenuBar menu = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem close = new JMenuItem("Close");
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MainForm.this.close();
            }
        });
        JMenuItem save = new JMenuItem("Save list");
        save.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, InputEvent.CTRL_MASK));
        save.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = Settings.makeSMLFileChooser();

                int returnVal = chooser.showSaveDialog(panel1);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(chooser.getSelectedFile());
                        for (Pack pack : packsModel.getPacks()) {
                            if (pack.isDownload()) {
                                fos.write((pack.getName()+"\n").getBytes());
                            }
                        }


                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } finally {
                        if (fos != null)
                            try {
                                fos.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                    }
                }
            }
        });

        JMenuItem load = new JMenuItem("Load list");
        load.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_O, InputEvent.CTRL_MASK));
        load.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = Settings.makeSMLFileChooser();
                int returnVal = chooser.showOpenDialog(panel1);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    openList(chooser.getSelectedFile());
                }
            }
        });
        updateMenuItem = new JMenuItem("Update packs");
        updateMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if(!working)
                    providerLoading.updatePacks();

            }
        });

        fileMenu.add(updateMenuItem);
        fileMenu.add(save);
        fileMenu.add(load);
        fileMenu.add(close);

        menu.add(fileMenu);

        return menu;
    }

    public void openList(File f){
        if(f.exists()) {
            BufferedReader fis = null;
            try {
                fis = new BufferedReader(new FileReader(f));

                for (String line; (line = fis.readLine()) != null; ) {
                    if (packs.containsKey(line)) {
                        packs.get(line).setDownload(true);
                    }
                }
                packsTable.updateUI();
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                if (fis != null)
                    try {
                        fis.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
            }
        }
    }

    public void close(){
        //if(!working){
            mainFrame.dispose();
            System.exit(0);
//        } else {
//            int n = JOptionPane.showConfirmDialog(
//                    MainForm.this.panel1,
//                    "Do you really want to exit while working?",
//                    "Exit",
//                    JOptionPane.YES_NO_OPTION);
//
//            if(n == JOptionPane.YES_OPTION) {
//                mainFrame.dispose();
//                System.exit(0);
//            }
//        }
    }

    public void run() {
        mainFrame = new JFrame("Stepmania DLM");
        mainFrame.setContentPane(new MainForm().panel1);

        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                int n = JOptionPane.showConfirmDialog(
                        MainForm.this.panel1,
                        "Do you really want to exit while working?",
                        "Exit",
                        JOptionPane.YES_NO_OPTION);

                if(n == JOptionPane.YES_OPTION) {
                    close();
                }

            }

        });


        mainFrame.setJMenuBar(buildMenu());
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    private void createUIComponents() {
        downloadPanel = new JPanel();
        downloadPanel.setLayout(new GridLayout(0,1 ));

    }
}
