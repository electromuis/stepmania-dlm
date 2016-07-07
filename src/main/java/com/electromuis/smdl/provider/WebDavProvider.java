package com.electromuis.smdl.provider;

import com.electromuis.smdl.MainForm;
import com.electromuis.smdl.Pack;
import com.electromuis.smdl.Processing.PackDownloader;
import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.sun.javafx.scene.shape.PathUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Electromuis on 22-6-2016.
 */
public class WebDavProvider extends DefaultProvider {
    private Config config;
    private Sardine client = null;

    public WebDavProvider(Config config) {
        this.config = config;
    }

    private Sardine getClient(){
        if(client==null){
            client = SardineFactory.begin(config.getUsername(), config.getPassword());
        }

        return client;
    }

    private List<DavResource> getContent(String folder) throws IOException {
        List<DavResource> resources = getClient().list(getFilePath(folder));

        String finalFolder = folder;
        resources.removeIf(s -> s.getPath().equals(config.getRoot()+ finalFolder));

        return resources;
    }

    @Override
    public List<Pack> getPacks() throws IOException {
        List<Pack> packs = new ArrayList<Pack>();

        String root="/";
        for (DavResource group : getContent(root)) {
            for (DavResource pack : getContent(root + group.getName())) {
                Map<String, String> customProps = pack.getCustomProps();
                long size = 0;

                if(customProps.containsKey("size"))
                    size = Long.parseLong(customProps.get("size"));

                packs.add(new Pack(
                        this,
                        root+group.getName()+"/"+pack.getName(),
                        pack.getName(),
                        group.getName(),
                        size
                ));
            }
        }

        return packs;
    }

    private String getFilePath(String folder){
        if(!folder.startsWith("/"))
            folder="/"+folder;

        if(!folder.endsWith("/"))
            folder+="/";

        String ret = "/"+config.getRoot()+"/"+folder;

        ret = ret.replace("//", "/").replace(" ", "%20");

        return config.getEndpoint()+ret;
    }

    @Override
    public InputStream getInputStream(Pack p) throws IOException {
        return getClient().get(getFilePath(p.getUrl()));
    }


}
