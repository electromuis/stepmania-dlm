package com.electromuis.smdl;

import com.electromuis.smdl.Processing.PackRow;
import com.electromuis.smdl.provider.ProviderLoading;
import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by anton on 23-8-17.
 */
public class MainController {
    @FXML
    TableView packsTable;

    @FXML
    ProgressBar progress;

    @FXML
    VBox downloadContainer;

    ObservableList<Pack> packList = FXCollections.observableArrayList();
    ProviderLoading loader = new ProviderLoading();

    @FXML
    private void initialize() {
        initTable();
    }

    private void initTable() {
        FilteredList<Pack> filteredPacks = new FilteredList<>(packList, p -> true);
        SortedList<Pack> sortedPacks = new SortedList<>(filteredPacks);
        sortedPacks.comparatorProperty().bind(packsTable.comparatorProperty());
        packsTable.setItems(sortedPacks);

        ObservableList columns = packsTable.getColumns();
        packsTable.setEditable(true);

        for(Field f : Pack.class.getDeclaredFields()) {
            if(f.isAnnotationPresent(FXML.class)) {
                TableColumn col = new TableColumn(f.getName());
                col.setPrefWidth(100);
                if(f.getType() == String.class) {
                    col.setCellValueFactory(new PropertyValueFactory<Pack, String>(f.getName()));
                    col.setEditable(false);
                    packsTable.getColumns().add(col);
                } else if (f.getType() == BooleanProperty.class) {
//                    col.setCellValueFactory(new PropertyValueFactory<Pack, Boolean>(f.getName()));
                    col.setCellValueFactory(
                            new Callback<TableColumn.CellDataFeatures<Pack,Boolean>,ObservableValue<Boolean>>()
                            {
                                @Override
                                public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<Pack, Boolean> param)
                                {
                                    return param.getValue().downloadProperty();
                                }
                            });
                    col.setCellFactory(column -> new CheckBoxTableCell());
                    col.setEditable(true);
                    packsTable.getColumns().add(col);
                }

            }
        }

        loadPacks();
    }

    @FXML
    public void loadPacks()
    {
        packList.clear();
        progress.setProgress(0);
        progress.setPrefHeight(30);

        new Thread(() -> {
            Pack[] packs = loader.getPacks(progress);
            packList.addAll(packs);

            progress.setPrefHeight(0);
        }).start();

    }

    @FXML
    public void startDownload()
    {
        try {
            downloadContainer.getChildren().add(new PackRow());
            downloadContainer.getChildren().add(new PackRow());
            downloadContainer.getChildren().add(new PackRow());
            downloadContainer.getChildren().add(new PackRow());
            downloadContainer.getChildren().add(new PackRow());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void close()
    {
        System.exit(0);
    }
}
