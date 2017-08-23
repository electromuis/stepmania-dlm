package com.electromuis.smdl;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

/**
 * Created by electromuis on 12.05.16.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/layout.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/fxml/layout.css").toExternalForm());
        stage.setTitle("Stepmania DLM");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String args[]){
        launch(args);

//        MainForm form = new MainForm();
//
//        form.getNewPacks();
//        if(args.length==1){
//            form.openList(new File(args[0]));
//        }
    }
}
