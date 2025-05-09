package src.bomberman;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class GameOverController {

    @FXML
    private Label finalScoreLabel;

    @FXML
    private Button playAgainButton;

    @FXML
    private Button mainMenuButton;

    @FXML
    private Button exitButtonGameOver; // Đổi tên để tránh trùng với menu chính nếu cần

    private BombermanApp mainApp;

    // Phương thức này sẽ được gọi bởi BombermanApp để truyền tham chiếu
    public void setMainApp(BombermanApp mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Đặt điểm số cuối cùng lên Label.
     * Sẽ được gọi bởi BombermanApp trước khi hiển thị Scene.
     * @param score Điểm số cuối cùng.
     */
    public void setFinalScore(int score) {
        if (finalScoreLabel != null) {
            finalScoreLabel.setText(String.format("Your Score: %06d", score));
        }
    }

    @FXML
    private void handlePlayAgain(ActionEvent event) {
        if (mainApp != null) {
            System.out.println("Play Again button clicked.");
            mainApp.startGame(); // Gọi phương thức để bắt đầu lại game
        } else {
            System.err.println("MainApp reference not set in GameOverController!");
        }
    }

    @FXML
    private void handleMainMenu(ActionEvent event) {
        if (mainApp != null) {
            System.out.println("Main Menu button clicked.");
            mainApp.showMenu(); // Gọi phương thức để quay lại menu chính
        } else {
            System.err.println("MainApp reference not set in GameOverController!");
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        System.out.println("Exit Game button clicked.");
        Platform.exit(); // Thoát ứng dụng JavaFX
        System.exit(0);  // Đảm bảo JVM cũng thoát
    }

    // (Tùy chọn) Phương thức initialize nếu cần thêm hiệu ứng hover, v.v.
    @FXML
    public void initialize() {
        // Ví dụ hiệu ứng hover
        playAgainButton.setOnMouseEntered(e -> playAgainButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;"));
        playAgainButton.setOnMouseExited(e -> playAgainButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;"));

        mainMenuButton.setOnMouseEntered(e -> mainMenuButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;"));
        mainMenuButton.setOnMouseExited(e -> mainMenuButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;"));

        exitButtonGameOver.setOnMouseEntered(e -> exitButtonGameOver.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: #333; -fx-font-weight: bold; -fx-background-radius: 8;"));
        exitButtonGameOver.setOnMouseExited(e -> exitButtonGameOver.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: #333; -fx-font-weight: bold; -fx-background-radius: 8;"));

    }
}