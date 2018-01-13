package com.electromuis.smdl.provider;

import com.electromuis.smdl.Pack;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by electromuis on 17.05.16.
 */
public class FtpProvider extends DefaultProvider {
//    public static final String username = "public";
//    public static final String password = "ddr1352";
    private WebDavProvider.Config config;
    private FTPClient client;

    private FTPClient getClient() throws IOException {

        if(client==null)
            client = new FTPClient();

        if(!client.isConnected()){
            client.connect(config.getEndpoint());
            int reply = client.getReplyCode();

            if(!FTPReply.isPositiveCompletion(reply)){
                client.disconnect();
                throw new IOException("Ftp client refused connection");
            }

            //client.login("public", "ddr1352");
            client.login(config.getUsername(), config.getPassword());
            client.enterLocalPassiveMode();
            client.setFileType(FTP.BINARY_FILE_TYPE);

            if(!FTPReply.isPositiveCompletion(reply)){
                client.disconnect();
                throw new IOException("Ftp client refused connection");
            }

        }

        return client;
    }

    public FtpProvider(String name, WebDavProvider.Config config) {
        super(name);
        this.config = config;
    }

    public List<Pack> getPacks() throws IOException {
        List<Pack> packs = new ArrayList<Pack>();

        FTPClient ftp = getClient();

        for (FTPFile group : ftp.listDirectories(config.getRoot()))
        {
            if(group.isDirectory()){
                String groupFolder = config.getRoot()+File.separator+group.getName();
                for (FTPFile file : ftp.listFiles(groupFolder)){
                    String url = groupFolder+File.separator+file.getName();
                    packs.add(new Pack(
                            this,
                            url,
                            file.getName(),
                            group.getName(),
                            file.getSize()
                    ));

                }
            }
        }

        return packs;
    }

    public void disconnect() throws IOException {
        if(client!=null && client.isConnected()){
            client.logout();
            client.disconnect();
        }
    }

    @Override
    public InputStream getInputStream(Pack p) throws IOException {
        return getClient().retrieveFileStream(p.getUrl());
    }
}
