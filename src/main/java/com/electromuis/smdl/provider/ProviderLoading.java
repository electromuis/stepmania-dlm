package com.electromuis.smdl.provider;

import com.electromuis.smdl.Main;
import com.electromuis.smdl.MainForm;
import com.electromuis.smdl.Pack;

import javax.swing.*;
import java.io.IOException;
import java.util.Map;

public class ProviderLoading extends JDialog {
    private JPanel contentPane;
    private JProgressBar progress;
    private PackProvider[] providers = {
            //new MockProvider(),
            new StepmaniaOnline(),
            new BrekerStackProvider()
            //new RicoFtpProvider()
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

    public PackProvider[] getProviders() {
        return providers;
    }
}
