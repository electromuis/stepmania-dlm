package com.electromuis.smdl.provider;

import com.electromuis.smdl.Pack;
import com.electromuis.smdl.Processing.Extractor;
import com.electromuis.smdl.Processing.PackRow;

import java.io.IOException;
import java.util.List;

/**
 * Created by electromuis on 12.05.16.
 */
public interface PackProvider {
    public List<Pack> getPacks() throws IOException;

    public boolean download(Pack p, PackRow pd) throws IOException, Extractor.ExtractionException;

    public void disconnect() throws IOException;

    public String getName();
}
