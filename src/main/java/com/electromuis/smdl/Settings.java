package com.electromuis.smdl;

import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;
import org.ini4j.Wini;
import org.json.JSONArray;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.Base64;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
    public File getLastSmlDir() {
        String conf = config.get("main", "last_sml_dir");
        if(conf == null) {
            return null;
        }
        File file = new File(conf);
        if(!file.exists()) {
            return null;
        }
        return file;
    }

    public void setLastSmlDir(String dir) {
        config.put("main", "last_sml_dir", dir);
        try {
            config.store();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public FileChooser makeSMLFileChooser(){
        FileChooser fc = new FileChooser();

        fc.setInitialDirectory(getLastSmlDir());
        fc.setTitle("Select where to save the DLM list");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("SM DLM files (*.sml)", "*.sml"));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files", "*"));

        return fc;
    }

    public void setLastPacks(Map<String, Pack> packs){
        try {
            if(!jsonConfig.exists())
                jsonConfig.createNewFile();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ImageIcon getIcon(){
        URL iconURL = getClass().getClassLoader().getResource("images/icon.png");
        ImageIcon icon = new ImageIcon(iconURL);

        return icon;
    }

    public ImageIcon getIconSmall(){
        URL iconURL = getClass().getClassLoader().getResource("images/icon_small.png");
        ImageIcon icon = new ImageIcon(iconURL);

        return icon;
    }

    public static class SMLFileFilter extends FileFilter {
        public static final String EXT="sml";

        @Override
        public boolean accept(File f) {
            return (FilenameUtils.getExtension(f.getName()).equals(EXT));
        }

        @Override
        public String getDescription() {
            return "Stepmania DLM pack list";
        }
    }

    public void initSettings(){

        if (getSongsFolder()==null){
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY);
            int returnVal = chooser.showSaveDialog(null);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File yourFolder = chooser.getSelectedFile();
                setSongsFolder(yourFolder.getAbsolutePath());
            } else {
                initSettings();
            }
        }
    }

    public void changeSongsFolder(){
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showSaveDialog(null);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File yourFolder = chooser.getSelectedFile();
            setSongsFolder(yourFolder.getAbsolutePath());
        }
    }

    public void loadCachedSongs(MainController controller)
    {
        try {
            String json = config.get("main", "songCache");
            json = decompress(json);
            if (json == null || json.equals("")) {
                System.out.println("Loading cache failed 1");
                return;
            }

            JSONArray array = new JSONArray(json);

            if (array.length() > 0) {
                controller.packList.clear();
                for (int i = 0; i < array.length(); i++) {
                    Pack p = Pack.fromJson(array.getJSONObject(i), controller.loader);
                    if (p == null) {
                        continue;
                    }
                    controller.packList.add(p);
                }

                controller.updateExistingPacks();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateSongsCache(MainController controller)
    {
        JSONArray array = new JSONArray();
        for (Pack pack : controller.packList) {
            array.put(pack.toJson());
        }

        try {
            String data = compress(array.toString());

            config.put("main", "songCache", data);
            config.store();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String compress(String data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length());
        GZIPOutputStream gzip = new GZIPOutputStream(bos);
        gzip.write(data.getBytes());
        gzip.close();
        byte[] compressed = bos.toByteArray();
        bos.close();
        return Base64.getEncoder().encodeToString(compressed);
    }

    public static String decompress(String data) throws IOException {
        if(data == null) {
            return null;
        }

        byte[] bytes = java.util.Base64.getDecoder().decode(data);

        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        GZIPInputStream gis = new GZIPInputStream(bis);
        BufferedReader br = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        gis.close();
        bis.close();
        return sb.toString();
    }
}
