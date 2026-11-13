package com.example.evaluacion3controlequipos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class EquipoApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(EquipoApplication.class.getResource("equipo-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Control de equipos de laboratorio");
        stage.setScene(scene);
        stage.show();
    }
}
