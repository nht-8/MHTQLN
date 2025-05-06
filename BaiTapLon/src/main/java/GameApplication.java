package org.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class GameApplication extends Application {

    private GameController gameController;

    @Override
    public void start(Stage stage) throws IOException {
        URL fxmlLocation = getClass().getResource("game-view.fxml");
        if (fxmlLocation == null) {
            System.err.println("Cannot find game-view.fxml!");
            throw new IOException("Cannot find FXML");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Parent root = fxmlLoader.load();
        gameController = fxmlLoader.getController();
        if (gameController == null) {
            throw new IOException("Could not get GameController instance");
        }

        Scene scene = new Scene(root);

        scene.setOnKeyPressed(event -> {
            if (gameController != null) {
                KeyCode code = event.getCode();
                // System.out.println("Key Pressed: " + code); // Debug
                switch (code) {
                    case UP:
                    case W:
                        gameController.setMoveUp(true);
                        break;
                    case DOWN:
                    case S:
                        gameController.setMoveDown(true);
                        break;
                    case LEFT:
                    case A:
                        gameController.setMoveLeft(true);
                        break;
                    case RIGHT:
                    case D:
                        gameController.setMoveRight(true);
                        break;
                    case SPACE:
                        gameController.placeBomb();
                        break;
                }
            }
        });

        scene.setOnKeyReleased(event -> {
            if (gameController != null) {
                KeyCode code = event.getCode();
                switch (code) {
                    case UP:
                    case W:
                        gameController.setMoveUp(false);
                        break;
                    case DOWN:
                    case S:
                        gameController.setMoveDown(false);
                        break;
                    case LEFT:
                    case A:
                        gameController.setMoveLeft(false);
                        break;
                    case RIGHT:
                    case D:
                        gameController.setMoveRight(false);
                        break;
                }
            }
        });

        stage.setTitle("Bomberman");
        stage.setScene(scene);
        stage.setIconified(true);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}