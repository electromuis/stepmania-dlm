package com.electromuis.smdl;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by anton on 23-8-17.
 */
public class MainController {
    @FXML
    TableView packsTable;

    Map<String, Pack> packs = new HashMap<String, Pack>();
    ObservableList<Pack> packList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        FilteredList<Pack> filteredPacks = new FilteredList<>(packList, p -> true);
        SortedList<Pack> sortedPacks = new SortedList<>(filteredPacks);
        sortedPacks.comparatorProperty().bind(packsTable.comparatorProperty());
        packsTable.setItems(sortedPacks);
    }
}