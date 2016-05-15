package com.electromuis.smdl.provider;

import com.electromuis.smdl.Pack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by electromuis on 12.05.16.
 */
public class MockProvider implements PackProvider {
    public List<Pack> getPacks() throws IOException {
        List<Pack> packsList = new ArrayList<Pack>();

        for (int i=0; i<255; i++){
            packsList.add(new Pack("Pack"+i, i+"", "New", ""));
        }

        return packsList;
    }
}
