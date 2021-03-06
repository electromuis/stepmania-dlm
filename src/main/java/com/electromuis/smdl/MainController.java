package com.electromuis.smdl;

import com.electromuis.smdl.Processing.PackRow;
import com.electromuis.smdl.Processing.PackRowView;
import com.electromuis.smdl.provider.ProviderLoading;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import static com.electromuis.smdl.Processing.PackRow.Status.*;
import static com.electromuis.smdl.Processing.PackRow.Command.*;

/**
 * Created by anton on 23-8-17.
 */
public class MainController {
    @FXML
    HBox menu;

    @FXML
    ListView<PackRow> downloadContainer;
    ObservableList<PackRow> packDownloaders;

    @FXML
    TextField packFilter;

    @FXML
    public StackPane progressContainer;

    @FXML
    public ProgressBar progress;

    @FXML
    public Label progressLabel;

    @FXML
    public Label mainLabel;

    @FXML
    TableView packsTable;
    ObservableList<Pack> packList = FXCollections.observableArrayList();
    ProviderLoading loader = new ProviderLoading();

    boolean working = false;
    static Settings settings = new Settings();

    public MainController(){
        settings.initSettings();
    }

    @FXML
    private void initialize() {
        initTable();
        initPackDownloader();
        findVersion();
    }

    private void findVersion() {
        String v = settings.getVersion();
        if (v != null) {
            mainLabel.setText(mainLabel.getText() + " (v" + v + ")");
        }
    }

    private void initPackDownloader() {
        packDownloaders = FXCollections.observableArrayList();

        downloadContainer.setItems(packDownloaders);
        downloadContainer.setCellFactory(studentListView -> new PackRowView());
    }

    private void initTable() {
        FilteredList<Pack> filteredPacks = new FilteredList<>(packList, p -> true);

        packFilter.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredPacks.setPredicate(pack -> {
                // If filter text is empty, display all persons.
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Compare first name and last name of every person with filter text.
                String lowerCaseFilter = newValue.toLowerCase();

                if (pack.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false; // Does not match.
            });
        });

        SortedList<Pack> sortedPacks = new SortedList<>(filteredPacks);
        sortedPacks.comparatorProperty().bind(packsTable.comparatorProperty());
        packsTable.setItems(sortedPacks);
        packsTable.setEditable(true);

        for(Field f : Pack.class.getDeclaredFields()) {
            if(f.isAnnotationPresent(FXML.class)) {
                TableColumn col = new TableColumn(StringUtils.capitalize(f.getName()));
                col.setPrefWidth(100);
                if(f.getType() == String.class) {
                    col.setCellValueFactory(new PropertyValueFactory<Pack, String>(f.getName()));
                    col.setEditable(false);
                    packsTable.getColumns().add(col);
                } else if (f.getType() == BooleanProperty.class) {
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

        ((TableColumn)packsTable.getColumns().get(0)).setPrefWidth(220);
        ((TableColumn)packsTable.getColumns().get(1)).setPrefWidth(130);
        ((TableColumn)packsTable.getColumns().get(2)).setPrefWidth(130);
        ((TableColumn)packsTable.getColumns().get(3)).setPrefWidth(50);


        settings.loadCachedSongs(this);

        if(packList.size() == 0) {
            loadPacks();
        }
    }

    @FXML
    public void changeSongsDir(){
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showSaveDialog(null);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File yourFolder = chooser.getSelectedFile();
            settings.setSongsFolder(yourFolder.getAbsolutePath());
            updateExistingPacks();
        }
    }

    @FXML
    public void saveList(){
        FileChooser fc = settings.makeSMLFileChooser();
        fc.setTitle("Select where to save the DLM list");

        File file = fc.showSaveDialog(progress.getScene().getWindow());

        if(file != null) {
            settings.setLastSmlDir(file.getParent());

            try {
                FileWriter fw = new FileWriter(file);
                FilteredList<Pack> checked = new FilteredList<Pack>(packList, Pack::isDownload);
                for (Pack pack : checked) {
                    fw.write(pack.getName() + "\n");
                }

                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void loadList(){
        FileChooser chooser = settings.makeSMLFileChooser();
        chooser.setTitle("Select the DLM list to load");

        File file = chooser.showOpenDialog(progress.getScene().getWindow());
        try {
            FileReader reader = new FileReader(file);
            BufferedReader fr = new BufferedReader(reader);

            String line;
            while ((line = fr.readLine()) != null) {
                for (Pack p : packList) {
                    if(line.equals(p.getName())) {
                        p.setDownload(true);
                    }
                }

            }

            fr.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void loadPacks()
    {
        packList.clear();
        progress.setProgress(0);
        progressContainer.setPrefHeight(30);

        new Thread(() -> {
            setWorking(true);
            Pack[] packs = loader.getPacks(this);
            packList.addAll(packs);
            updateExistingPacks();

            Platform.runLater(() -> {
                progress.setProgress(0);
                progressLabel.setText("Loaded " + packList.size() + " packs");
            });

            packList.sort((o1, o2) -> {
                return o1.getName().compareTo(o2.getName());
            });

            settings.updateSongsCache(this);
            setWorking(false);
        }).start();

    }

    @FXML
    public void updateExistingPacks(){
        packFilter.setText("");
        for (Pack pack : packList) {
            pack.setDownload(pack.getExists());
        }
    }

    @FXML
    public void deselectAll()
    {
        packFilter.setText("");
        for (Pack pack : packList) {
            pack.setDownload(false);
        }
    }

    private void addDownloader(Pack p, PackRow.Command command){
        boolean exists = false;
        for (PackRow pd : packDownloaders) {
            Pack pdl = pd.getPack();
            if(pdl.equals(p)){
                exists = true;
                break;
            }
        }

        if(!exists) {
            try {
                packDownloaders.add(new PackRow(p, command));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void applyPacks()
    {
        cleanDownloaders();

        boolean added = false;

        for(Pack p : packList){
            if(p.isDownload() && !p.getExists()){
                addDownloader(p, DOWNLOAD);
                added = true;
            } else if(!p.isDownload() && p.getExists()){
                addDownloader(p, DELETE);
                added = true;
            }
        }

        if(added){
            new Thread(() -> {
                int n = JOptionPane.showConfirmDialog(
                        null,
                        "Do you want to apply the current changes?",
                        "Apply packs",
                        JOptionPane.YES_NO_OPTION);

                if(n == JOptionPane.YES_OPTION) {
                    setWorking(true);
                }
            }).start();
        }
    }

    private void cleanDownloaders(){
        List<PackRow> toRemove = new ArrayList<PackRow>();
        for (PackRow p : packDownloaders) {
            PackRow pd = (PackRow)p;
            if(!pd.isWorking()){
                toRemove.add(p);
            }
        }

        packDownloaders.removeAll(toRemove);
    }

    public boolean setWorking(boolean b){
        int numWorking = 0;
        int numPending = 0;

        for (PackRow pd : packDownloaders) {
            if(pd.isWorking()) {
                numWorking ++;
            } else if(pd.getStatus() == PENDING) {
                numPending ++;
            }
        }

        if(numPending > 0) {
            for (PackRow pd : packDownloaders) {
                if(numWorking >= settings.getNumThreads()) {
                    break;
                }

                if(pd.getStatus() == PENDING) {
                    pd.start(this);
                    numWorking ++;
                }
            }
        }

        if(!b && numWorking > 0) {
            return false;
        }

        working = b;

        for (Node node : menu.getChildren()) {
            if(node instanceof Button) {
                node.setDisable(b);
            }
        }

        return true;
    }

    public static Settings getSettings(){
        return settings;
    }

    @FXML
    public void close()
    {
        loader.disconnect();
        System.exit(0);
    }
}
