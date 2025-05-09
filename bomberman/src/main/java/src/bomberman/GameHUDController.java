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

    @FXML private BorderPane rootPane; // Tham chiếu đến container gốc
    @FXML private HBox hudPane;        // Tham chiếu đến HBox chứa HUD
    @FXML private Label levelLabel;    // Label hiển thị Level
    @FXML private Label livesLabel;    // Label hiển thị Mạng
    @FXML private Label scoreLabel;    // Label hiển thị Điểm
    @FXML private Canvas gameCanvas;   // Canvas để vẽ trò chơi

    private Game game;                 // Instance của logic game
    private Renderer renderer;         // Đối tượng vẽ game
    private GraphicsContext gameCanvasGC; // Context để vẽ lên Canvas
    private BombermanApp mainApp;      // Tham chiếu đến ứng dụng chính để chuyển màn hình

    /**
     * Constructor mặc định (cần thiết cho FXMLLoader).
     */
    public GameHUDController() {
        // Khởi tạo có thể thực hiện ở đây hoặc trong initialize()/setupGame()
    }

    /**
     * Phương thức này được gọi bởi BombermanApp sau khi FXML được load
     * để truyền các đối tượng cần thiết và hoàn tất thiết lập.
     * @param inputHandler Đối tượng xử lý input cho màn hình game.
     * @param mainApp Tham chiếu đến ứng dụng chính (BombermanApp).
     */
    public void setupGame(InputHandler inputHandler, BombermanApp mainApp) {
        this.mainApp = mainApp;

        // Đặt kích thước cho các thành phần giao diện từ Config
        setElementSizesFromConfig();

        // Lấy GraphicsContext từ Canvas SAU KHI đã đặt kích thước
        this.gameCanvasGC = gameCanvas.getGraphicsContext2D();
        if (this.gameCanvasGC == null) {
            System.err.println("CRITICAL ERROR: Failed to get GraphicsContext from Canvas in GameHUDController!");
            // Có thể quay lại menu hoặc hiển thị lỗi
            if (this.mainApp != null) this.mainApp.showMenu();
            return;
        }
        gameCanvasGC.setImageSmoothing(false); // Tắt làm mịn ảnh (quan trọng cho pixel art)

        // Khởi tạo các thành phần game
        // Truyền inputHandler đã được gắn với Scene của màn hình này
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
            // In lỗi ra console nếu có vấn đề (chỉ nên xảy ra lúc khởi tạo)
            // System.err.println("Attempted to update/render before game/renderer/gc was ready.");
            return;
        }

        // 1. Cập nhật logic game
        game.update(deltaTime);

        // 2. Cập nhật các Label trên HUD
        updateHUDLabels();

        // 3. Yêu cầu Renderer vẽ nội dung game lên GraphicsContext của Canvas
        renderer.renderGameContent(gameCanvasGC, game); // Renderer chỉ vẽ phần game

        // 4. Kiểm tra điều kiện Game Over sau khi đã update và render frame hiện tại
        // Đảm bảo player không null trước khi kiểm tra trạng thái
        if (game.getPlayerLives() <= 0 && game.getPlayer() != null && !game.getPlayer().isAlive() && !game.getPlayer().isDying()) {
            // Gọi mainApp để hiển thị màn hình game over, chỉ gọi nếu chưa ở trạng thái game over
            if(mainApp != null && mainApp.getCurrentAppState() != BombermanApp.AppState.GAME_OVER) {
                mainApp.showGameOverScreen(game.getPlayerScore());
            } else if (mainApp == null) {
                System.err.println("Cannot trigger Game Over screen: mainApp is null in GameHUDController");
            }
        }
    }

    /**
     * Cập nhật nội dung text của các Label trên HUD dựa trên trạng thái hiện tại của Game.
     */
    private void updateHUDLabels() {
        if (game != null) {
            // Cập nhật chỉ khi các Label đã được inject
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

    /**
     * Cung cấp truy cập đến đối tượng Game (nếu cần từ bên ngoài, ví dụ BombermanApp).
     * @return Đối tượng Game hiện tại.
     */
    public Game getGame() {
        return game;
    }

    // Phương thức initialize chuẩn của FXML, được gọi sau khi các @FXML đã được inject.
    // Bạn có thể đặt code khởi tạo giao diện ban đầu ở đây nếu không cần truyền tham số từ ngoài vào.
    // Tuy nhiên, vì chúng ta cần InputHandler và mainApp từ BombermanApp, nên dùng phương thức setupGame() tùy chỉnh.
    @FXML
    public void initialize() {
        System.out.println("GameHUDController FXML components injected.");
        // Không nên khởi tạo Game, Renderer ở đây vì cần InputHandler từ Scene
        // và mainApp từ BombermanApp. Việc đó được thực hiện trong setupGame().
    }
}