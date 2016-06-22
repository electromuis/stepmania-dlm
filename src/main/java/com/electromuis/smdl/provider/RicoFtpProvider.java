package com.electromuis.smdl.provider;

import com.electromuis.smdl.Pack;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by electromuis on 17.05.16.
 */
public class RicoFtpProvider implements PackProvider {
    public static final String username = "public";
    public static final String password = "ddr1352";

    public List<Pack> getPacks() throws IOException {
        List<Pack> packs = new ArrayList<Pack>();

        FTPClient ftp = new FTPClient();
        try {
            int reply;
            String server = "gamebreakersnl.synology.me";
            ftp.connect(server);

            reply = ftp.getReplyCode();

            if(!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                System.err.println("FTP server refused connection.");
            } else {
                ftp.login(username, password);

                String root = "DDR Download Folder"+File.separator+"DLM";

                for (FTPFile group : ftp.listDirectories(root))
                {
                    if(group.isDirectory()){
                        String groupFolder = root+File.separator+group.getName();
                        for (FTPFile file : ftp.listFiles(groupFolder)){
                            String name = FilenameUtils.getBaseName(file.getName());
                            packs.add(new FtpPack(name, (file.getSize()/1000000)+"MB", group.getName(), groupFolder+File.separator+file.getName(), file.getSize()));

                        }
                    }
                }
            }




            ftp.logout();
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if(ftp.isConnected()) {
                ftp.disconnect();
            }
        }


        return packs;
    }


}
