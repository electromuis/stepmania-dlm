package com.electromuis.smdl;

import com.electromuis.smdl.Processing.PackRow;
import com.electromuis.smdl.Processing.PackRowView;
import com.electromuis.smdl.provider.FtpProvider;
import com.electromuis.smdl.provider.HttpProvider;
import com.electromuis.smdl.provider.PackProvider;
import com.electromuis.smdl.provider.WebDavProvider;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.Comparator;

/**
 * Created by electromuis on 12.05.16.
 */
public class Pack {
    @FXML
    private String name;
    @FXML
    private String size;
    @FXML
    private String type;
    private String url;
    private String fileName;
    @FXML
    private BooleanProperty download;
    private int progress;
    private Song[] songs;
    private long contentLength;
    private PackProvider provider;

    public Pack(PackProvider provider, String name, String size, String type, String url, String fileName) {
        this.name = name;
        this.size = size;
        this.type = type;
        this.url = url;
        this.fileName = fileName;
        this.provider = provider;
        download = new SimpleBooleanProperty(false);
    }

    public static String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "Kb", "Mb", "Gb", "Tb" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public Pack(PackProvider provider, String name, String size, String type, String url, String fileName, long contentLength){
        this(provider, name, size, type, url, fileName);
        this.contentLength = contentLength;
    }

    public Pack(PackProvider provider, String url, String filename, String type, long contentLength) {
        this(provider, FilenameUtils.getBaseName(filename), readableFileSize(contentLength), type, url, filename, contentLength);
    }

    public Pack(PackProvider provider, String name, String size, String type, String url) {
        this(provider, name, size, type, url, FilenameUtils.getName(url), 0);
    }


    public String download(PackRow downloader) throws IOException {
        return provider.download(this, downloader);
    }

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
        return download.get();
    }

    public void setDownload(boolean download) {
        this.download.set(download);
    }

    public BooleanProperty downloadProperty() {
        return download;
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
        return (new File(MainController.getSettings().getSongsFolder()+File.separator+name));
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

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public static class PackComparator implements Comparator<Pack> {
        @Override
        public int compare(Pack o1, Pack o2) {

            return o1.getName().compareTo(o2.getName());
        }
    }
}
