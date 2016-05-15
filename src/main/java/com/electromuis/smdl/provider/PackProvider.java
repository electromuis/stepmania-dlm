package com.electromuis.smdl.provider;

import com.electromuis.smdl.Pack;

import java.io.IOException;
import java.util.List;

/**
 * Created by electromuis on 12.05.16.
 */
public interface PackProvider {
    public List<Pack> getPacks() throws IOException;
}
