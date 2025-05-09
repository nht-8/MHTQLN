package src.bomberman;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import src.bomberman.core.Game;
import src.bomberman.graphics.Renderer;
import src.bomberman.input.InputHandler;

public class GameHUDController {

    @FXML private BorderPane rootPane; 
    @FXML private HBox hudPane;        
    @FXML private Label levelLabel;   
    @FXML private Label livesLabel; 
    @FXML private Label scoreLabel;
    @FXML private Canvas gameCanvas;   

    private Game game;                 
    private Renderer renderer;         
    private GraphicsContext gameCanvasGC; 
    private BombermanApp mainApp;      

    public GameHUDController() {
        // Khởi tạo có thể thực hiện ở đây hoặc trong initialize()/setupGame()
    }

    public void setupGame(InputHandler inputHandler, BombermanApp mainApp) {
        this.mainApp = mainApp;

        setElementSizesFromConfig();

        this.gameCanvasGC = gameCanvas.getGraphicsContext2D();
        if (this.gameCanvasGC == null) {
            System.err.println("CRITICAL ERROR: Failed to get GraphicsContext from Canvas in GameHUDController!");
            
            if (this.mainApp != null) this.mainApp.showMenu();
            return;
        }
        gameCanvasGC.setImageSmoothing(false);

        this.game = new Game(inputHandler);
        this.renderer = new Renderer(); // Renderer giờ chỉ vẽ nội dung game

        System.out.println("GameHUDController setup complete. Canvas and Game ready.");

        // Cập nhật HUD lần đầu tiên
        updateHUDLabels();
    }

    /**
     * Đặt kích thước cho các phần tử FXML dựa trên hằng số trong Config.
     */
    private void setElementSizesFromConfig() {
        // BorderPane gốc thường sẽ tự khớp với Scene, không cần đặt cứng ở đây
        // if (rootPane != null) {
        //     rootPane.setPrefSize(Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
        // }

        if (hudPane != null) {
            hudPane.setPrefHeight(Config.HUD_HEIGHT); // Đặt chiều cao cho thanh HUD
        } else {
            System.err.println("Warning: hudPane is null in setElementSizesFromConfig.");
        }

        if (gameCanvas != null) {
            // Đặt kích thước chính xác cho Canvas là vùng chơi game
            gameCanvas.setWidth(Config.GAME_AREA_WIDTH);
            gameCanvas.setHeight(Config.GAME_AREA_HEIGHT);
            System.out.println("Canvas size set to: " + gameCanvas.getWidth() + "x" + gameCanvas.getHeight());
        } else {
            System.err.println("ERROR: gameCanvas is null when trying to set size!");
        }
    }


    /**
     * Phương thức chính được gọi liên tục từ AnimationTimer trong BombermanApp.
     * Cập nhật trạng thái game, HUD, và vẽ lại màn hình game.
     * @param deltaTime Thời gian (tính bằng giây) trôi qua kể từ frame trước (hiện đang dùng giá trị cố định).
     */
    public void updateAndRender(double deltaTime) {
        // Kiểm tra các đối tượng cần thiết đã sẵn sàng chưa
        if (game == null || renderer == null || gameCanvasGC == null) {
        
            return;
        }

        game.update(deltaTime);

        updateHUDLabels();

        renderer.render(gameCanvasGC, game); 

        if (game.getPlayerLives() <= 0 && game.getPlayer() != null && !game.getPlayer().isAlive() && !game.getPlayer().isDying()) {
            if(mainApp != null && mainApp.getCurrentAppState() != BombermanApp.AppState.GAME_OVER) {
               
                mainApp.showGameOverScreen(game.getPlayerScore());
            } else if (mainApp == null) {
                System.err.println("Cannot trigger Game Over screen: mainApp is null in GameHUDController");
            }
        }
    }

    private void updateHUDLabels() {
        if (game != null) {
         
            if (levelLabel != null) {
                levelLabel.setText("LEVEL: " + game.getCurrentLevelNumber());
            }
            if (livesLabel != null) {
                livesLabel.setText("LIVES: " + game.getPlayerLives());
            }
            if (scoreLabel != null) {
                scoreLabel.setText(String.format("SCORE: %06d", game.getPlayerScore()));
            }
        }
    }

    public Game getGame() {
        return game;
    }
    
    @FXML
    public void initialize() {
        System.out.println("GameHUDController FXML components injected.");
    }
}
