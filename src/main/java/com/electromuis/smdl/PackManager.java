package com.electromuis.smdl;

import com.electromuis.smdl.provider.ProviderLoading;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by electromuis on 08.06.16.
 */
public class PackManager {
    public static Map<String, Pack> packs = new HashMap<String, Pack>();
    private ProviderLoading providerLoading;
    private boolean working = false;
    private static Settings settings;
}
