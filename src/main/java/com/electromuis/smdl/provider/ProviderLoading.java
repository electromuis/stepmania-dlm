package com.electromuis.smdl.provider;

import com.electromuis.smdl.MainForm;
import com.electromuis.smdl.Pack;

import javax.swing.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProviderLoading extends JDialog {
    private JPanel contentPane;
    private JProgressBar progress;
    private PackProvider[] providers = {
            //new MockProvider(),
            new StepmaniaOnline(),
            new RicoFtpProvider()
    };
    private MainForm mainForm;

    public ProviderLoading(MainForm mainForm) {

        setContentPane(contentPane);
        this.mainForm = mainForm;
    }

    public void updatePacks(){
        setLocationRelativeTo(null);

        new Thread(new Runnable() {
            public void run() {
                setVisible(true);
                getPacks();
                mainForm.updateExistingPacks();

                setVisible(false);
            }
        }).start();

    }

    private void getPacks() {
        Map<String, Pack> packsList = MainForm.packs;

        for(PackProvider pv : providers)
            try {
                for(Pack p : pv.getPacks())
                    if(!packsList.containsKey(p.getName())) {
                        packsList.put(p.getName(), p);
                    }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "There was an error loading the packs, are you connected to the internet?", "Network error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }

    }
}
