package com.electromuis.smdl;

import org.apache.commons.io.FilenameUtils;
import org.ini4j.Wini;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**Button
 * Created by electromuis on 12.05.16.
 */
public class Settings {
    private Wini config;
    private File jsonConfig;

    public Settings(){
        try {
            File f = new File("smdl.ini");
            if(!f.exists())
                f.createNewFile();

            jsonConfig = new File("smdl.json");

            config = new Wini(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getSongsFolder() {
        return config.get("main", "song_folder");
    }

    public void setSongsFolder(String songsFolder) {
        config.put("main", "song_folder", songsFolder);
        try {
            config.store();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Pack> getLastPacks(){
        return null;
    }

    public static JFileChooser makeSMLFileChooser(){
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return (FilenameUtils.getExtension(f.getName()).equals("sml"));
            }

            @Override
            public String getDescription() {
                return "Stepmania DLM pack list";
            }
        });
        chooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return true;
            }

            @Override
            public String getDescription() {
                return "All files";
            }
        });

        return chooser;
    }

    public void setLastPacks(Map<String, Pack> packs){
        try {
            if(!jsonConfig.exists())
                jsonConfig.createNewFile();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
