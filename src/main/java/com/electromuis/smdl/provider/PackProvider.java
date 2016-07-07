package com.electromuis.smdl.provider;

import com.electromuis.smdl.Pack;
import com.electromuis.smdl.Processing.PackDownloader;

import java.io.IOException;
import java.util.List;

/**
 * Created by electromuis on 12.05.16.
 */
public interface PackProvider {
    public List<Pack> getPacks() throws IOException;

    public String download(Pack p, PackDownloader pd) throws IOException;

    public void disconnect() throws IOException;
}
