package com.electromuis.smdl.provider;

import com.electromuis.smdl.Pack;
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

    public WebDavProvider(String name, Config config) {
        super("name");
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
                long size = pack.getContentLength();

                packs.add(new Pack(
                        this,
                        pack.getHref().toString(),
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

        ret = ret.replace("//", "/");

        return config.getEndpoint()+ret;
    }

    @Override
    public InputStream getInputStream(Pack p) throws IOException {
        return getClient().get(config.getEndpoint() + p.getUrl());
    }


}
