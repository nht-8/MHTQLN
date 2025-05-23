package src.bomberman;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.VBox;

import java.net.URL;

public class MenuController {

    @FXML
    private VBox rootVBox;

    @FXML
    private Button startGameButton;

    @FXML
    private Button exitButton;

    private BombermanApp mainApp;


    public void setMainApp(BombermanApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleStartGame(ActionEvent event) {
        if (mainApp != null) {
            mainApp.startGame();
        } else {
            System.err.println("MainApp reference not set in MenuController!");
        }
    }

    @FXML
    private void handleExitGame(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }
    @FXML
    public void initialize() {

        startGameButton.setOnMouseEntered(e -> startGameButton.setStyle("-fx-background-color: #45a049;" +
                " -fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold; -fx-background-radius: 10;"));
        startGameButton.setOnMouseExited(e -> startGameButton.setStyle("-fx-background-color: #4CAF50; " +
                "-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold; -fx-background-radius: 10;"));

        exitButton.setOnMouseEntered(e -> exitButton.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold; -fx-background-radius: 10;"));
        exitButton.setOnMouseExited(e -> exitButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold; -fx-background-radius: 10;"));

        try {

            URL imageUrl = getClass().getResource(Config.MENU_BACKGROUND_IMAGE_PATH);

            if (rootVBox == null) {
                System.err.println("ERROR: rootVBox is null. Check fx:id in FXML and @FXML in Controller.");
                return;
            }

            if (imageUrl != null) {
                Image backgroundImageFile = new Image(imageUrl.toExternalForm());

                BackgroundSize backgroundSize = new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true);


                BackgroundImage backgroundImage = new BackgroundImage(backgroundImageFile, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize // Sử dụng đối tượng BackgroundSize đã tạo
                );
                rootVBox.setBackground(new Background(backgroundImage));
                System.out.println("SUCCESS: Background image set from Java code for: " + Config.MENU_BACKGROUND_IMAGE_PATH);
            } else {
                System.err.println("ERROR: Could NOT find image in Java code: " + Config.MENU_BACKGROUND_IMAGE_PATH);

            }
        } catch (Exception e) {
            System.err.println("EXCEPTION while setting background image from Java code: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
