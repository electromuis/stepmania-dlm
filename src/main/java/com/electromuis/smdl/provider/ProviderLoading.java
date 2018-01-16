package com.electromuis.smdl.provider;

import com.electromuis.smdl.Pack;
import javafx.scene.control.ProgressBar;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class ProviderLoading {
    private PackProvider[] providers = {
            //new MockProvider(),
//            new WebDavProvider("stack", new DefaultProvider.Config(
//                    "https://debreker.stackstorage.com",
//                    "electromuis",
//                    "Falkensteiner12",
//                    "/remote.php/webdav/Songs"
//            )),
            new DropboxProvider(
        "itgdropbox",
        "p8GYYaqbiAAAAAAAAAALXuysZ-EiSSxAzqe8acMzvl2LqlHOifrGvP-kzunsrrzB",
    "https://www.dropbox.com/sh/o9t6z8n3gdmg6sz/cuV7Kaurg-",
                new DropboxProvider.PackFolder[]{
                    new DropboxProvider.PackFolder("/Albumix", "Albumix"),
                    new DropboxProvider.PackFolder("/DDR Officials", "DDR Officials"),
                    new DropboxProvider.PackFolder("/DDR4EVER", "DDR4EVER"),
                    new DropboxProvider.PackFolder("/DDRei", "DDRei"),
                    new DropboxProvider.PackFolder("/DDRExtreme.co.uk", "DDRExtreme.co.uk"),
                    new DropboxProvider.PackFolder("/DWI Extreme", "DWI Extreme"),
                    new DropboxProvider.PackFolder("/Otaku's Dream", "Otaku's Dream"),
                    new DropboxProvider.PackFolder("/R21", "R21"),
                    new DropboxProvider.PackFolder("/Stepmania (Pad)", "Stepmania (Pad)"),
                    new DropboxProvider.PackFolder("/DDRei", "Mods (SM5)"),
                    new DropboxProvider.PackFolder("/Stepmix", "Stepmix"),
                    new DropboxProvider.PackFolder("/DDRei", "Pad"),
                }
            ),
//            new HttpProvider("smonline", new HttpProvider.Config(
//                    "http://stepmaniaonline.net/index.php?page=downloads",
//                    "div.block:has(div.blocktitle:contains(Songs)) tr"
//            ) {
//                @Override
//                public List<Pack> convertPacks(Elements packElements, HttpProvider provider) {
//                    List<Pack> packsList = new ArrayList<>();
//
//                    for (Element e : packElements){
//                        Elements info = e.select("td");
//                        if(info.size() > 2 && !info.get(0).text().trim().equals("")) {
//                            packsList.add(new Pack(provider,
//                                    info.get(0).text(),
//                                    info.get(1).text(),
//                                    info.get(2).text(),
//                                    info.get(0).select("a").attr("href").replace(" ", "%20")
//                            ));
//                        }
//                    }
//
//                    return packsList;
//                }
//            })
    };

    public void disconnect(){
        for (PackProvider provider : getProviders()) {
            try {
                provider.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Pack[] getPacks(ProgressBar progress){
        Map<String, Pack> packs = new HashMap<>();

        float i = 0;
        boolean error = false;
        for(PackProvider pv : providers) {
            i ++;

            System.out.println("Loading provider: " + pv.getClass().getName());
            progress.setProgress(i / (providers.length + 1));

            try {
                for (Pack p : pv.getPacks())
                    if (!packs.containsKey(p.getName())) {
                        packs.put(p.getName(), p);
                    }
            } catch (IOException e) {
                error = true;
                e.printStackTrace();
            }
        }

        if(error) {
            JOptionPane.showMessageDialog(null, "There was an error loading the packs, are you connected to the internet?", "Network error", JOptionPane.ERROR_MESSAGE);
        }

        return packs.values().toArray(new Pack[0]);
    }

    public PackProvider[] getProviders() {
        return providers;
    }
}
