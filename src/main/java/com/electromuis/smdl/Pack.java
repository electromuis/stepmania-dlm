package com.electromuis.smdl;

import com.electromuis.smdl.Processing.PackDownloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Created by electromuis on 12.05.16.
 */
public class Pack {
    private String name;
    private String size;
    private String type;
    private String url;
    private boolean download = false;
    private int progress;
    private Song[] songs;

    public Pack(String name, String size, String type, String url) {
        this.name = name;
        this.size = size;
        this.type = type;
        this.url = url;
    }

    public String download(PackDownloader downloader) throws IOException {return null;}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Song[] getSongs() {
        return songs;
    }

    public void setSongs(Song[] songs) {
        this.songs = songs;
    }

    public boolean isDownload() {
        return download;
    }

    public void setDownload(boolean download) {
        this.download = download;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public boolean getExists(){
        return getFolder().exists();
    }

    private File getFolder(){
        return (new File(MainForm.getSettings().getSongsFolder()+File.separator+name));
    }

    public void deletePack(){
        try {
            delete(getFolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void delete(File f) throws FileNotFoundException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }
}
