package com.electromuis.smdl.provider;

import com.electromuis.smdl.Pack;
import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Electromuis on 22-6-2016.
 */
public class BrekerStackProvider implements PackProvider {
    private static final String username = "electromuis";
    private static final String password = "Falkensteiner12";
    private static final String url = "https://debreker.stackstorage.com/remote.php/webdav/Songs/Pad/";

    @Override
    public List<Pack> getPacks() throws IOException {
        List<Pack> packs = new ArrayList<Pack>();

        Sardine sardine = SardineFactory.begin(username, password);

        List<DavResource> resources = sardine.list(url);

        for (DavResource resource : resources) {
            System.out.println(resource.getName()+"-");

        }


        return packs;
    }
}
