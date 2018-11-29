package com.electromuis.smdl.provider;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.fileproperties.PropertyField;
import com.dropbox.core.v2.fileproperties.PropertyGroup;
import com.dropbox.core.v2.files.*;
import com.dropbox.core.v2.sharing.GetSharedLinkFileBuilder;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import com.electromuis.smdl.MainController;
import com.electromuis.smdl.Pack;
import com.electromuis.smdl.Processing.PackRow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DropboxProvider implements PackProvider {
    String name;
    String token = "";
    String sharedFolder = "";
    PackFolder folders[];
    DbxClientV2 client;
    private static final int BUFFER_SIZE = 4096;

    public DropboxProvider(String name, String token, String sharedFolder, PackFolder[] folders) {
        this.name = name;
        this.token = token;
        this.folders = folders;
        this.sharedFolder = sharedFolder;
    }

    protected DbxClientV2 getClient() {
        if(client == null) {
            DbxRequestConfig config = new DbxRequestConfig("electromuis/smdlm", "en_US");
            client = new DbxClientV2(config, token);
        }

        return client;
    }

    public void downloadFolder(String path, String target, PackRow pr) throws IOException, DbxException {
        try {
            pr.setStatus(PackRow.Status.RETRIEVING);
            List<DbxFile> dbxFiles = listFiles(path, true, null, 0, pr::setProgress);
            float progressPt = 1 / dbxFiles.size();

            pr.setStatus(PackRow.Status.DOWNLOADING);
            for (int i = 0; i < dbxFiles.size(); i++) {
                DbxFile dFile = dbxFiles.get(i);
                float progress = (float) i / dbxFiles.size();
                String localPath = dFile.fullPath.substring(path.length());
                localPath = target + File.separator + localPath;

                if (!localPath.isEmpty()) {
                    if (dFile.isFile) {
                        GetSharedLinkFileBuilder builder = getClient()
                                .sharing()
                                .getSharedLinkFileBuilder(sharedFolder)
                                .withPath(dFile.fullPath);

                        DbxDownloader<SharedLinkMetadata> meta = builder.start();

                        FileOutputStream outputStream = new FileOutputStream(localPath);
                        meta.download(outputStream);
                        outputStream.close();

                    } else {
                        new File(localPath).mkdirs();
                    }

                    pr.setProgress(progress);
                }
            }
        } catch (Exception e) {
            pr.pack.deletePack();
            throw e;
        }
    }

    protected List<DbxFile> listFiles(String path) throws DbxException {
        return listFiles(path, false, null ,0, null);
    }

    protected List<DbxFile> listFiles(String path, boolean recursive, List<DbxFile> files, int level, ListingProgression progression) throws DbxException {
        if (path == null) {
            path = "";
        }

        if(files == null) {
            files = new ArrayList<DbxFile>();
        }

        try {
            ListFolderBuilder listFolderBuilder =
                    getClient().
                            files().
                            listFolderBuilder(path)
                            .withSharedLink(new SharedLink(sharedFolder));

            List<Metadata> entries = listFolderBuilder.start().getEntries();
            for (int i = 0; i < entries.size(); i++) {
                Metadata file = entries.get(i);

                DbxFile dbxFile = new DbxFile(file, path);
                files.add(dbxFile);

                if (dbxFile.isFile == false && recursive) {
                    listFiles(dbxFile.fullPath, recursive, files, level + 1, null);
                }

                if (level == 0 && progression != null) {
                    progression.setProgress((float) i / entries.size());
                }
            }
        } catch (ListFolderErrorException exception) {
            System.out.println(path + " not found");
        }

        return files;
    }

    @Override
    public List<Pack> getPacks() throws IOException {
        List<Pack> packs = new ArrayList<Pack>();

        try {
            for (PackFolder folder : folders) {
                for (DbxFile metadata : listFiles(folder.folderPath)) {
                    if(metadata.isFile == false) {
                        Pack p = new Pack(this, metadata.name, "?", folder.packType, metadata.fullPath, metadata.name);
                        packs.add(p);
                    }
                }

            }
        } catch (DbxException e) {
            e.printStackTrace();
        }


        return packs;
    }

    @Override
    public boolean download(Pack p, PackRow pd) throws IOException {
        String targetDir = MainController.getSettings().getSongsFolder() + File.separator + p.getName();

        try {
            downloadFolder(p.getUrl(), targetDir, pd);
        } catch (DbxException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public void disconnect() throws IOException {

    }

    @Override
    public String getName() {
        return name;
    }

    public static class PackFolder {
        String folderPath;
        String packType;

        public PackFolder(String folderPath, String packType) {
            this.folderPath = folderPath;
            this.packType = packType;
        }

        public String getFolderPath() {
            return folderPath;
        }

        public String getPackType() {
            return packType;
        }
    }

    public static class DbxFile {
        String parentPath;
        String fullPath;
        String name;
        boolean isFile;

        public DbxFile(Metadata meta, String path) {
            name = meta.getName();
            parentPath = path;
            this.isFile = (meta instanceof FileMetadata);
            fullPath = path + "/" + name;
        }
    }

    public static interface ListingProgression {
        public void setProgress(float progress);
    }
}
