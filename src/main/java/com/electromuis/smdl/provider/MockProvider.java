package com.electromuis.smdl.provider;

import com.electromuis.smdl.Pack;
import com.electromuis.smdl.Processing.PackRow;

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
            Pack p = new Pack(this, "Pack"+i, i+"", "New", "");
            packsList.add(p);
        }

        return packsList;
    }

    @Override
    public boolean download(Pack p, PackRow pd) throws IOException {
        for (int i = 0; i < 101; i++) {
            pd.setProgress((float)i / 100);

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    @Override
    public void disconnect() throws IOException {

    }

    @Override
    public String getName() {
        return "dummy";
    }
}
