package com.electromuis.smdl.provider;

import com.electromuis.smdl.MainController;
import com.electromuis.smdl.Pack;
import javafx.application.Platform;
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
            new WebDavProvider("DDR Exp", new DefaultProvider.Config(
                    "http://nas.electromuis.nl:5005",
                    "packapp",
                    "3}]*wH",
                    "/packs"
            )),
            new DropboxProvider(
        "ITG Dropbox",
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
                    new DropboxProvider.PackFolder("/Stepmania 3 (Pad)", "Stepmania 3 (Pad)"),
                    new DropboxProvider.PackFolder("/DDRei", "Mods (SM5)"),
                    new DropboxProvider.PackFolder("/Stepmix", "Stepmix")
                }
            )
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

    public Pack[] getPacks(MainController controller){
        Map<String, Pack> packs = new HashMap<>();


        boolean error = false;

        for(int i = 0; i < providers.length; i ++) {
            PackProvider pv = providers[i];
            int ni = i;

            System.out.println("Loading provider: " + pv.getClass().getName());

            Platform.runLater(() -> {
                controller.progress.setProgress((float)ni / (providers.length));
                controller.progressLabel.setText("Loading " + pv.getName() + " " + (ni) + "/" + (providers.length));
            });

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
