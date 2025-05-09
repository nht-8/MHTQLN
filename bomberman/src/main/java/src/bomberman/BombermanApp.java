package src.bomberman;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet;
import src.bomberman.input.InputHandler;
import src.bomberman.sound.SoundManager;

import java.io.IOException;
import java.net.URL;

public class BombermanApp extends Application {

    private Stage primaryStage; // Stage chính của ứng dụng
    private Scene menuScene;    // Scene cho màn hình menu
    private Scene gameScene;    // Scene cho màn hình chơi game (từ FXML)
    private Scene gameOverScene; // Scene cho màn hình Game Over

    private InputHandler gameInputHandler; // Input handler cho game scene
    private AnimationTimer gameLoop;       // Vòng lặp chính của game

    // Giữ tham chiếu đến các controller để tương tác nếu cần
    private MenuController menuControllerInstance;
    private GameHUDController gameHUDControllerInstance;
    private GameOverController gameOverControllerInstance;

    // Trạng thái ứng dụng
    public enum AppState { MENU, PLAYING, GAME_OVER } // Public để Controller truy cập nếu cần
    private AppState currentAppState = AppState.MENU; // Khởi tạo trạng thái ban đầu

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        System.out.println("Starting Bomberman Application...");
        primaryStage.setTitle("Bomberman FX");

        // --- Khởi tạo tài nguyên tĩnh một lần ---
        try {
            SpriteSheet.loadAllSheets();
            Sprite.loadSprites(SpriteSheet.Sheet1, SpriteSheet.Sheet2);
            SoundManager.getInstance().loadSounds();
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR during static resource initialization!");
            e.printStackTrace();
            Platform.exit(); // Thoát nếu tài nguyên cơ bản không load được
            return;
        }

        // --- Tạo Scene cho Menu ---
        try {
            URL menuFxmlUrl = getClass().getResource("/menu-view.fxml");
            if (menuFxmlUrl == null) {
                throw new IOException("Cannot find resource: /menu-view.fxml");
            }
            FXMLLoader menuLoader = new FXMLLoader(menuFxmlUrl);
            Parent menuRoot = menuLoader.load();
            menuControllerInstance = menuLoader.getController();
            if (menuControllerInstance != null) {
                menuControllerInstance.setMainApp(this);
            } else {
                System.err.println("Warning: Could not get MenuController instance.");
            }
            // Sử dụng kích thước cửa sổ tổng thể từ Config cho menu scene
            menuScene = new Scene(menuRoot, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
        } catch (IOException e) {
            System.err.println("Failed to load menu FXML: " + e.getMessage());
            e.printStackTrace(); Platform.exit(); return;
        }

        // --- Thiết lập Game Loop ---
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long currentNanoTime) {
                // Chỉ chạy update/render nếu đang ở trạng thái PLAYING và controller đã sẵn sàng
                if (currentAppState == AppState.PLAYING && gameHUDControllerInstance != null) {
                    try {
                        gameHUDControllerInstance.updateAndRender(1.0 / 60.0); // Giả định 60 FPS
                    } catch (Exception e) {
                        System.err.println("Error during game update/render loop:");
                        e.printStackTrace();
                        stop(); // Dừng AnimationTimer nếu có lỗi nghiêm trọng
                        showMenu(); // Quay về menu nếu có lỗi trong game loop
                    }
                }
            }
        };

        showMenu(); // Bắt đầu bằng việc hiển thị menu

        primaryStage.setResizable(false); // Không cho thay đổi kích thước cửa sổ
        // Xử lý sự kiện đóng cửa sổ
        primaryStage.setOnCloseRequest(event -> {
            stopApplication(); // Dọn dẹp tài nguyên
            Platform.exit();   // Đảm bảo JavaFX thoát
            System.exit(0);    // Đảm bảo JVM thoát hoàn toàn
        });
        primaryStage.show();
        System.out.println("Application started successfully, showing menu.");
    }

    /**
     * Hiển thị giao diện Menu chính.
     */
    public void showMenu() {
        currentAppState = AppState.MENU;
        if (gameLoop != null) {
            gameLoop.stop(); // Dừng game loop nếu đang chạy
        }
        primaryStage.setScene(menuScene);
        primaryStage.setTitle("Bomberman FX - Menu"); // Cập nhật tiêu đề cửa sổ
        SoundManager.getInstance().stopBackgroundMusic(); // Dừng nhạc game/gameover (nếu có)
        SoundManager.getInstance().playBackgroundMusic(SoundManager.TITLE_BGM, true); // Phát nhạc nền menu
        System.out.println("Switched to Menu scene.");
    }

    /**
     * Khởi tạo và chuyển sang màn hình chơi game.
     */
    public void startGame() {
        currentAppState = AppState.PLAYING; // Đặt trạng thái trước khi load FXML
        try {
            URL gameFxmlUrl = getClass().getResource("/game-hud-view.fxml");
            if (gameFxmlUrl == null) {
                throw new IOException("Cannot find resource: /game-hud-view.fxml");
            }
            FXMLLoader gameLoader = new FXMLLoader(gameFxmlUrl);
            Parent gameRoot = gameLoader.load();
            gameHUDControllerInstance = gameLoader.getController();

            // Tạo Scene cho game với kích thước cửa sổ tổng thể
            gameScene = new Scene(gameRoot, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);

            // Tạo và khởi tạo InputHandler cho gameScene
            gameInputHandler = new InputHandler(gameScene);
            gameInputHandler.init();

            // Setup trò chơi bên trong controller (controller sẽ tạo Game, Renderer, đặt kích thước Canvas...)
            if (gameHUDControllerInstance != null) {
                gameHUDControllerInstance.setupGame(gameInputHandler, this);
                primaryStage.setScene(gameScene); // Chuyển sang màn hình game
                primaryStage.setTitle("Bomberman FX - Level " + gameHUDControllerInstance.getGame().getCurrentLevelNumber()); // Ví dụ cập nhật tiêu đề
                SoundManager.getInstance().stopBackgroundMusic(); // Dừng nhạc menu
                SoundManager.getInstance().playBackgroundMusic(SoundManager.GAME_BGM, true); // Phát nhạc nền game
                gameLoop.start(); // Bắt đầu vòng lặp game
                System.out.println("Switched to Game scene and started game.");
            } else {
                throw new IllegalStateException("Could not get GameHUDController instance after loading FXML.");
            }
        } catch (Exception e) { // Bắt Exception chung để an toàn hơn
            System.err.println("Failed to load game FXML or setup game: " + e.getMessage());
            e.printStackTrace();
            showMenu(); // Quay lại menu nếu có lỗi khi load game
        }
    }

    /**
     * Hiển thị màn hình Game Over.
     * @param finalScore Điểm số cuối cùng để hiển thị.
     */
    public void showGameOverScreen(int finalScore) {
        // Chạy trên luồng JavaFX để đảm bảo an toàn khi thay đổi giao diện
        Platform.runLater(() -> {
            // Chỉ thực hiện nếu chưa ở trạng thái GAME_OVER để tránh gọi nhiều lần
            if (currentAppState == AppState.GAME_OVER) {
                return;
            }

            currentAppState = AppState.GAME_OVER; // Cập nhật trạng thái
            if (gameLoop != null) {
                gameLoop.stop(); // Dừng vòng lặp game
            }
            System.out.println("Showing GAME OVER! Final Score: " + finalScore);
            try {
                URL gameOverFxmlUrl = getClass().getResource("/game-over-view.fxml");
                if (gameOverFxmlUrl == null) throw new IOException("Cannot find /game-over-view.fxml");

                FXMLLoader gameOverLoader = new FXMLLoader(gameOverFxmlUrl);
                Parent gameOverRoot = gameOverLoader.load();
                gameOverControllerInstance = gameOverLoader.getController(); // Lấy controller

                if (gameOverControllerInstance != null) {
                    gameOverControllerInstance.setMainApp(this); // Đặt tham chiếu mainApp
                    gameOverControllerInstance.setFinalScore(finalScore); // Đặt điểm số
                } else {
                    System.err.println("Warning: Could not get GameOverController instance.");
                }

                // Tạo Scene mới hoặc tái sử dụng nếu đã có
                if (gameOverScene == null) {
                    gameOverScene = new Scene(gameOverRoot, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
                } else {
                    gameOverScene.setRoot(gameOverRoot); // Cập nhật root nếu scene đã tồn tại
                }

                primaryStage.setScene(gameOverScene);
                primaryStage.setTitle("Bomberman FX - Game Over");

                // Tùy chọn: Phát nhạc game over hoặc dừng nhạc nền
                SoundManager.getInstance().stopBackgroundMusic();
                // SoundManager.getInstance().playSound(SoundManager.GAME_OVER_SOUND); // Nếu có âm thanh game over

            } catch (IOException e) {
                System.err.println("Failed to load Game Over FXML: " + e.getMessage());
                e.printStackTrace();
                showMenu(); // Fallback về menu chính nếu không load được màn hình game over
            }
        });
    }


    /**
     * Dừng vòng lặp game và hiển thị lại menu chính.
     * Được gọi bởi Controller khi người dùng chọn về menu.
     */
    public void stopGameLoopAndShowMenu() {
        Platform.runLater(() -> {
            if (gameLoop != null) {
                gameLoop.stop();
                System.out.println("Game loop stopped.");
            }
            showMenu(); // Quay lại màn hình menu
        });
    }

    /**
     * Trả về trạng thái hiện tại của ứng dụng.
     * @return AppState trạng thái hiện tại (MENU, PLAYING, GAME_OVER).
     */
    public AppState getCurrentAppState() {
        return currentAppState;
    }

    /**
     * Dọn dẹp tài nguyên khi ứng dụng đóng.
     */
    private void stopApplication() {
        System.out.println("Stopping game application (custom stop)...");
        if (gameLoop != null) {
            gameLoop.stop();
        }
        SoundManager.getInstance().cleanup(); // Dọn dẹp SoundManager
        // Có thể thêm các dọn dẹp khác ở đây nếu cần
    }

    /**
     * Ghi đè phương thức stop của Application để gọi phương thức dọn dẹp tùy chỉnh.
     */
    @Override
    public void stop() throws Exception {
        stopApplication(); // Gọi hàm dọn dẹp của chúng ta
        super.stop();      // Gọi hàm stop của lớp cha
        System.out.println("Application stopped (JavaFX stop method).");
    }

    /**
     * Phương thức main để khởi chạy ứng dụng.
     */
    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            System.err.println("An error occurred during application launch:");
            e.printStackTrace();
        }
    }
}