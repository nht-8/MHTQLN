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

public class   BombermanApp extends Application {

    private Stage primaryStage;
    private Scene menuScene;
    private Scene gameScene;
    private Scene gameOverScene;

    private InputHandler gameInputHandler;
    private AnimationTimer gameLoop;

    private MenuController menuControllerInstance;
    private GameHUDController gameHUDControllerInstance;
    private GameOverController gameOverControllerInstance;

    public enum AppState { MENU, PLAYING, GAME_OVER }
    private AppState currentAppState = AppState.MENU;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        System.out.println("Starting Bomberman Application...");
        primaryStage.setTitle("Bomberman FX");

        try {
            SpriteSheet.loadAllSheets();
            Sprite.loadSprites(SpriteSheet.Sheet1, SpriteSheet.Sheet2);
            SoundManager.getInstance().loadSounds();
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR during static resource initialization!");
            e.printStackTrace();
            Platform.exit();
            return;
        }

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

            menuScene = new Scene(menuRoot, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
        } catch (IOException e) {
            System.err.println("Failed to load menu FXML: " + e.getMessage());
            e.printStackTrace(); Platform.exit(); return;
        }

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long currentNanoTime) {

                if (currentAppState == AppState.PLAYING && gameHUDControllerInstance != null) {
                    try {
                        gameHUDControllerInstance.updateAndRender(1.0 / 60.0);
                    } catch (Exception e) {
                        System.err.println("Error during game update/render loop:");
                        e.printStackTrace();
                        stop();
                        showMenu();
                    }
                }
            }
        };

        showMenu();

        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(event -> {
            stopApplication();
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
        System.out.println("Application started successfully, showing menu.");
    }

    public void showMenu() {
        currentAppState = AppState.MENU;
        if (gameLoop != null) {
            gameLoop.stop();
        }
        primaryStage.setScene(menuScene);
        primaryStage.setTitle("Bomberman FX - Menu");
        SoundManager.getInstance().stopBackgroundMusic();
        SoundManager.getInstance().playBackgroundMusic(SoundManager.TITLE_BGM, true);
        System.out.println("Switched to Menu scene.");
    }

    public void startGame() {
        currentAppState = AppState.PLAYING;
        try {
            URL gameFxmlUrl = getClass().getResource("/game-hud-view.fxml");
            if (gameFxmlUrl == null) {
                throw new IOException("Cannot find resource: /game-hud-view.fxml");
            }
            FXMLLoader gameLoader = new FXMLLoader(gameFxmlUrl);
            Parent gameRoot = gameLoader.load();
            gameHUDControllerInstance = gameLoader.getController();

            gameScene = new Scene(gameRoot, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);

            gameInputHandler = new InputHandler(gameScene);
            gameInputHandler.init();

            if (gameHUDControllerInstance != null) {
                gameHUDControllerInstance.setupGame(gameInputHandler, this);
                primaryStage.setScene(gameScene);
                primaryStage.setTitle("Bomberman FX - Level " + gameHUDControllerInstance.getGame().getCurrentLevelNumber());
                SoundManager.getInstance().stopBackgroundMusic();
                SoundManager.getInstance().playBackgroundMusic(SoundManager.GAME_BGM, true);
                gameLoop.start();
                System.out.println("Switched to Game scene and started game.");
            } else {
                throw new IllegalStateException("Could not get GameHUDController instance after loading FXML.");
            }
        } catch (Exception e) {
            System.err.println("Failed to load game FXML or setup game: " + e.getMessage());
            e.printStackTrace();
            showMenu();
        }
    }

    public void showGameOverScreen(int finalScore) {

        Platform.runLater(() -> {

            if (currentAppState == AppState.GAME_OVER) {
                return;
            }

            currentAppState = AppState.GAME_OVER;
            if (gameLoop != null) {
                gameLoop.stop();
            }
            System.out.println("Showing GAME OVER! Final Score: " + finalScore);
            try {
                URL gameOverFxmlUrl = getClass().getResource("/game-over-view.fxml");
                if (gameOverFxmlUrl == null) throw new IOException("Cannot find /game-over-view.fxml");

                FXMLLoader gameOverLoader = new FXMLLoader(gameOverFxmlUrl);
                Parent gameOverRoot = gameOverLoader.load();
                gameOverControllerInstance = gameOverLoader.getController();

                if (gameOverControllerInstance != null) {
                    gameOverControllerInstance.setMainApp(this);
                    gameOverControllerInstance.setFinalScore(finalScore);
                } else {
                    System.err.println("Warning: Could not get GameOverController instance.");
                }

                if (gameOverScene == null) {
                    gameOverScene = new Scene(gameOverRoot, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
                } else {
                    gameOverScene.setRoot(gameOverRoot);
                }

                primaryStage.setScene(gameOverScene);
                primaryStage.setTitle("Bomberman FX - Game Over");

                SoundManager.getInstance().stopBackgroundMusic();

            } catch (IOException e) {
                System.err.println("Failed to load Game Over FXML: " + e.getMessage());
                e.printStackTrace();
                showMenu();
            }
        });
    }

    public void stopGameLoopAndShowMenu() {
        Platform.runLater(() -> {
            if (gameLoop != null) {
                gameLoop.stop();
                System.out.println("Game loop stopped.");
            }
            showMenu();
        });
    }

    public AppState getCurrentAppState() {
        return currentAppState;
    }

    private void stopApplication() {
        System.out.println("Stopping game application (custom stop)...");
        if (gameLoop != null) {
            gameLoop.stop();
        }
        SoundManager.getInstance().cleanup();
    }


    @Override
    public void stop() throws Exception {
        stopApplication();
        super.stop();
        System.out.println("Application stopped (JavaFX stop method).");
    }

    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            System.err.println("An error occurred during application launch:");
            e.printStackTrace();
        }
    }
}
