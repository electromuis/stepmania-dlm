package com.electromuis.smdl.Processing;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class PackRow extends GridPane {
    @FXML
    Label name;

    @FXML
    ProgressBar progress;

    @FXML
    Label status;

    public PackRow() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/layout.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        loader.load();
    }
}
