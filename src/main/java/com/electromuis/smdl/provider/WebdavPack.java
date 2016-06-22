package com.electromuis.smdl.provider;

import com.electromuis.smdl.Pack;
import com.electromuis.smdl.Processing.PackDownloader;

import java.io.IOException;

/**
 * Created by Electromuis on 22-6-2016.
 */
public class WebdavPack extends Pack {
    public WebdavPack(String name, String size, String type, String url) {
        super(name, size, type, url);
    }

    @Override
    public String download(PackDownloader downloader) throws IOException {
        return super.download(downloader);
    }
}
