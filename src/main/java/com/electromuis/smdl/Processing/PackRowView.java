package com.electromuis.smdl.Processing;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;

import java.io.IOException;

public class PackRowView extends ListCell<PackRow> {
    FXMLLoader loader;

    @FXML
    BorderPane view;

    @FXML
    Label packName;

    @FXML
    Label packStatus;

    @FXML
    ProgressBar progress;

    @Override
    protected void updateItem(PackRow packRow, boolean empty) {
        super.updateItem(packRow, empty);

        if(packRow == null) {

            setText(null);
            setGraphic(null);

        } else {
            if (loader == null) {
                loader = new FXMLLoader(getClass().getResource("/fxml/packrow.fxml"));
                loader.setController(this);

                try {
                    loader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            packRow.setBar(progress);
            packRow.setLabel(packStatus);
            packName.setText(packRow.getPack().getName());

            setGraphic(view);
        }

    }

    void setProgress(float f) {
        progress.setProgress(f);
    }
}
