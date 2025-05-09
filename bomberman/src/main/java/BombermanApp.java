package src.bomberman;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;

import src.bomberman.core.Game;
import src.bomberman.graphics.Renderer;
import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet;
import src.bomberman.input.InputHandler;
import src.bomberman.sound.SoundManager;

public class BombermanApp extends Application {

    private Game game;
    private Renderer renderer;
    private InputHandler inputHandler;
    // Không cần lưu trữ SpriteSheet ở đây nữa vì chúng là static
    // private SpriteSheet modernSheet;
    // private SpriteSheet nesSheet;

    // Game loop timer
    private AnimationTimer gameLoop;
    // Theo dõi thời gian cho delta time (tùy chọn)
    // private long lastUpdateNanos = 0;

    @Override
    public void start(Stage primaryStage) {
        System.out.println("Starting Bomberman Application...");
        primaryStage.setTitle("Bomberman FX");

        // --- Khởi tạo Tài nguyên và Logic Game ---
        // 1. Load tất cả Sprite Sheets (QUAN TRỌNG: Làm trước khi load Sprites)
        SpriteSheet.loadAllSheets(); // Gọi phương thức tĩnh để tải và lưu sheet

        // 2. Load tất cả các Sprite tĩnh (QUAN TRỌNG: Làm sau khi load sheets)
        //    Phương thức này giờ sẽ tự động sử dụng các sheet tĩnh đã load
        Sprite.loadSprites(SpriteSheet.Sheet1, SpriteSheet.Sheet2);

        // 3. Load Sounds
        SoundManager.getInstance().loadSounds();

        // 4. Tạo Canvas và Scene
        Canvas canvas = new Canvas(Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Group root = new Group(canvas); // Thêm canvas vào Group
        Scene scene = new Scene(root);

        gc.setImageSmoothing(false); // Tắt làm mịn ảnh

        // 5. Khởi tạo Input Handler
        inputHandler = new InputHandler(scene);
        inputHandler.init(); // Gắn listener vào scene

        // 6. Khởi tạo Game Logic
        //    Constructor Game giờ không cần truyền sheet nữa
        game = new Game(inputHandler);

        // 7. Khởi tạo Renderer
        renderer = new Renderer();

        // --- Thiết lập Game Loop ---
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long currentNanoTime) {
                // --- Tùy chọn: Tính toán Delta Time ---
                // if (lastUpdateNanos == 0) {
                //     lastUpdateNanos = currentNanoTime;
                //     return; // Bỏ qua frame đầu tiên
                // }
                // double deltaTimeSeconds = (currentNanoTime - lastUpdateNanos) / 1_000_000_000.0;
                // lastUpdateNanos = currentNanoTime;
                // // Giới hạn delta time để tránh nhảy vọt khi có lag
                // deltaTimeSeconds = Math.min(deltaTimeSeconds, 0.1);

                // --- Cập nhật Logic Game ---
                // game.update(deltaTimeSeconds); // Truyền delta time (nếu dùng)
                game.update(1.0 / 60.0); // Hoặc giả định 60 FPS và truyền delta time cố định

                // --- Vẽ Game ---
                // Kiểm tra renderer và gc không null (an toàn hơn)
                if (renderer != null && gc != null && game != null) {
                    renderer.render(gc, game);
                } else {
                    System.err.println("Error in game loop: Renderer, GC or Game is null!");
                    stop(); // Dừng game loop nếu có lỗi nghiêm trọng
                }
            }
        };

        System.out.println("Starting Game Loop...");

        SoundManager.getInstance().playBackgroundMusic(SoundManager.GAME_BGM, true); // Phát nhạc nền chính và lặp lại
        // Hoặc SoundManager.getInstance().playBackgroundMusic(SoundManager.TITLE_BGM, true); nếu là màn hình chờ

        gameLoop.start(); // Bắt đầu vòng lặp game

        // --- Hiển thị Cửa sổ ---
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Không cho phép thay đổi kích thước
        primaryStage.setOnCloseRequest(event -> stopGame()); // Xử lý khi đóng cửa sổ
        primaryStage.show();
        System.out.println("Application started successfully.");
    }

    /**
     * Phương thức để dừng game loop và dọn dẹp (nếu cần).
     */
    private void stopGame() {
        System.out.println("Stopping game loop...");
        if (gameLoop != null) {
            gameLoop.stop();
        }
        SoundManager.getInstance().cleanup();
    }

    /**
     * Ghi đè phương thức stop của Application để đảm bảo game loop dừng khi ứng dụng thoát.
     */
    @Override
    public void stop() throws Exception {
        stopGame();
        super.stop(); // Gọi stop của lớp cha
        System.out.println("Application stopped.");
    }

    /**
     * Phương thức main để khởi chạy ứng dụng JavaFX.
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