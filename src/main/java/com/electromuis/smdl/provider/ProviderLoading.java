package com.electromuis.smdl.provider;

import com.electromuis.smdl.MainForm;
import com.electromuis.smdl.Pack;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProviderLoading extends JDialog {
    private JPanel contentPane;
    private JProgressBar progress;
    private PackProvider[] providers = {
            //new MockProvider(),
            new WebDavProvider(new DefaultProvider.Config(
                    "https://debreker.stackstorage.com",
                    "electromuis",
                    "Falkensteiner12",
                    "/remote.php/webdav/Songs"
            )),
            new HttpProvider(new HttpProvider.Config(
                    "http://stepmaniaonline.net/index.php?page=downloads",
                    "div.block:has(div.blocktitle:contains(Songs)) tr"
            ) {
                @Override
                public List<Pack> convertPacks(Elements packElements, HttpProvider provider) {
                    List<Pack> packsList = new ArrayList<>();


                    for (Element e : packElements){
                        Elements info = e.select("td");
                        if(info.size() > 2 && !info.get(0).text().trim().equals("")) {
                            packsList.add(new Pack(provider,
                                    info.get(0).text(),
                                    info.get(1).text(),
                                    info.get(2).text(),
                                    "http://stepmaniaonline.net" + info.get(0).select("a").attr("href").replace(" ", "%20")
                            ));
                        }
                    }

                    return packsList;
                }
            }),
//            new FtpProvider(new DefaultProvider.Config(
//                    "gamebreakersnl.synology.me",
//                    "public",
//                    "ddr1352",
//                    "DDR/Songs"
//            ))
    };
    private MainForm mainForm;

    public ProviderLoading(MainForm mainForm) {

        this.mainForm = mainForm;
        setContentPane(contentPane);
        setIconImage(MainForm.getSettings().getIcon().getImage());
        setUndecorated(true);
        getRootPane().setWindowDecorationStyle(JRootPane.NONE);
    }

    public void showLoading(){
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void disconnect(){
        for (PackProvider provider : getProviders()) {
            try {
                provider.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public PackProvider[] getProviders() {
        return providers;
    }
}
