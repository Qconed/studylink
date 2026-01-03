package dev.studylink.studylink;

import dev.studylink.studylink.db.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void init() {
        // Initialiser la base de données avant de lancer l'interface
        System.out.println("Initialisation de l'application...\n");

        try {
            DatabaseInitializer initializer = new DatabaseInitializer();
            initializer.initialize();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de la base de données:");
            e.printStackTrace();
        }
    }
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Login StudyLink");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
