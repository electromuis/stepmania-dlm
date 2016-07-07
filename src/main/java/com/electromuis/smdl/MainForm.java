package com.electromuis.smdl;

import com.electromuis.smdl.Processing.PackDownloader;
import com.electromuis.smdl.provider.PackProvider;
import com.electromuis.smdl.provider.ProviderLoading;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * Created by electromuis on 12.05.16.
 */
public class MainForm  extends JFrame {
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
    public Map<String, Pack> packs = new HashMap<String, Pack>();
    private ProviderLoading providerLoading;
    private boolean working = false;

    public MainForm() {
        super("Stepmania DLM");

        settings = new Settings();
        initSettings();

        setContentPane(panel1);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setIconImage(settings.getIcon().getImage());
        setLocationRelativeTo(null);
        setJMenuBar(buildMenu());

        packsModel = new PacksModel(new Pack[0]);
        packsTable.setModel(packsModel);

        providerLoading = new ProviderLoading(this);
        providerLoading.pack();

        addListeners();

        setVisible(true);
    }

    public void getNewPacks(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                providerLoading.showLoading();
                for(PackProvider pv : providerLoading.getProviders()) {
                    System.out.println("Loading provider: " + pv.getClass().getName());
                    try {
                        for (Pack p : pv.getPacks())
                            if (!packs.containsKey(p.getName())) {
                                packs.put(p.getName(), p);
                            }
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(panel1, "There was an error loading the packs, are you connected to the internet?", "Network error", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                }

                providerLoading.setVisible(false);
                updateExistingPacks();
            }
        }).start();
    }

    private void addListeners(){
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
                packsModel.fireTableDataChanged();
            }
        });

        resetButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                updateExistingPacks();
            }
        });

        packsTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                System.out.println(packsTable.getModel().getRowCount());
                System.out.println(Integer.toHexString(System.identityHashCode(packs)));
                //packsModel.fireTableDataChanged();
            }
        });

        addWindowListener(new WindowAdapter() {
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
    }

    public void setWorking(boolean b){
        for (Component pd : downloadPanel.getComponents()) {
            if(pd instanceof PackDownloader) {
                switch (((PackDownloader) pd).getStatus()){
                    case DONE:
                    case PENDING:
                    case FAILED:
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

    public void updateExistingPacks(){
        for (Map.Entry<String, Pack> entry : packs.entrySet()) {
            Pack pack = entry.getValue();
            pack.setDownload(pack.getExists());
        }

        panel1.requestFocus();
        packsModel.setPacks(getPacksArray());
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


        List<Pack> list = new ArrayList<Pack>(packs.values());

        Collections.sort(list, new Pack.PackComparator());

        Pack[] packArray = new Pack[list.size()];

        int l = 0;
        for (Pack p : list){
            packArray[l]=list.get(l);
            l++;
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

    private void changeSongsFolder(){
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showSaveDialog(panel1);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File yourFolder = chooser.getSelectedFile();
            settings.setSongsFolder(yourFolder.getAbsolutePath());
            updateExistingPacks();
        }
    }

    private JMenuBar buildMenu(){
        JMenuBar menu = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        //Close button
        JMenuItem close = new JMenuItem("Close");
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MainForm.this.close();
            }
        });

        //Save button
        JMenuItem save = new JMenuItem("Save list");
        save.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, InputEvent.CTRL_MASK));
        save.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = settings.makeSMLFileChooser();

                int returnVal = chooser.showSaveDialog(panel1);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    String selected = chooser.getSelectedFile().getAbsolutePath();
                    if(chooser.getFileFilter() instanceof Settings.SMLFileFilter && !selected.endsWith("."+ Settings.SMLFileFilter.EXT))
                        selected+= "."+ Settings.SMLFileFilter.EXT;

                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(new File(selected));
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

        //Load button
        JMenuItem load = new JMenuItem("Load list");
        load.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_O, InputEvent.CTRL_MASK));
        load.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = settings.makeSMLFileChooser();
                int returnVal = chooser.showOpenDialog(panel1);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    openList(chooser.getSelectedFile());
                }
            }
        });

        //Update packs button
        updateMenuItem = new JMenuItem("Update packs");
        updateMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(!working)
                    getNewPacks();
            }
        });

        JMenuItem changeFolder = new JMenuItem("Change song folder");
        changeFolder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!working)
                    changeSongsFolder();
            }
        });

        fileMenu.add(updateMenuItem);
        fileMenu.add(save);
        fileMenu.add(load);
        fileMenu.add(changeFolder);
        fileMenu.add(close);

        menu.add(fileMenu);

        return menu;
    }

    public Map<String, Pack> getPacks() {
        return packs;
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
                packsModel.fireTableDataChanged();
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
            providerLoading.disconnect();
            dispose();
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



    private void createUIComponents() {
        downloadPanel = new JPanel();
        downloadPanel.setLayout(new GridLayout(0,1 ));
    }
}
