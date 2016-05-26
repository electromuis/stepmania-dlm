package com.electromuis.smdl.provider;

import com.electromuis.smdl.Pack;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by electromuis on 12.05.16.
 */
public class StepmaniaOnline implements PackProvider {
    public List<Pack> getPacks() throws IOException {
        Document doc = Jsoup.connect("http://stepmaniaonline.net/index.php?page=downloads").get();
        Elements packs = doc.select("div.block:has(div.blocktitle:contains(Songs)) tr");


        List<Pack> packsList = new ArrayList<Pack>();


        for (Element e : packs){
            Elements info = e.select("td");
            if(info.size() > 2 && !info.get(0).text().trim().equals(""))
                packsList.add(new Pack(info.get(0).text(),
                        info.get(1).text(),
                        info.get(2).text(),
                        "http://stepmaniaonline.net"+info.get(0).select("a").attr("href").replace(" ", "%20")
                ));
        }

        return packsList;
    }

    public boolean downloadPack(Pack p) {
        return false;
    }
}
