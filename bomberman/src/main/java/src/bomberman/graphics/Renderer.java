// src/main/java/src/bomberman/graphics/Renderer.java
package src.bomberman.graphics;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import src.bomberman.Config;
import src.bomberman.core.Game;
import src.bomberman.core.Level;
import src.bomberman.entities.*; // Import entities (bao gồm cả Player, Enemy, Bomb, Explosion)
import src.bomberman.entities.Portal; // Import Portal riêng nếu cần (thường đã có trong entities.*)

import java.util.List;

public class Renderer {

    /**
     * Phương thức chính để vẽ toàn bộ trạng thái game lên canvas.
     *
     * @param gc   GraphicsContext của canvas để vẽ.
     * @param game Đối tượng Game chứa toàn bộ thông tin trạng thái game.
     */
    public void render(GraphicsContext gc, Game game) {
        // 0. Kiểm tra game có null không (phòng trường hợp gọi render khi game chưa sẵn sàng)
        if (game == null) {
            System.err.println("Renderer Error: Game object is null. Cannot render.");
            clearScreen(gc); // Xóa màn hình để tránh rác đồ họa
            gc.setFill(Color.RED);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            gc.fillText("Error: Game data unavailable!", 10, 20);
            return;
        }

        // 1. Xóa màn hình bằng màu nền cơ bản
        clearScreen(gc);

        Level level = game.getLevel();
        // Kiểm tra Level có tồn tại không
        if (level == null) {
            System.err.println("Renderer Error: Level is null, cannot render map.");
            gc.setFill(Color.WHITE); // Đổi màu chữ cho dễ đọc trên nền đen
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            gc.fillText("Error loading level data!", 10, 20);
            return; // Không vẽ gì thêm
        }

        // 2. Vẽ nền (Cỏ) cho toàn bộ map LÊN TRÊN màu nền đen
        level.renderBackground(gc);

        // 3. Vẽ các thực thể tĩnh (Tường, Gạch, và có thể cả Portal nếu nó là static)
        // Portal thường được coi là một Entity tĩnh, được thêm vào staticEntities từ Level.
        // Nó sẽ tự render trạng thái ẩn/hiện của mình.
        List<Entity> staticEntities = game.getStaticEntities();
        if (staticEntities != null) {
            for (Entity entity : staticEntities) {
                if (entity.isAlive() || (entity instanceof Brick && ((Brick)entity).isDying()) || (entity instanceof Portal && ((Portal)entity).isRevealed())) {
                    // Wall và Brick sẽ tự gọi render() của chúng để vẽ Sprite.wall/Sprite.brick
                    // Portal sẽ tự render sprite tương ứng (ẩn, hiện, animation nếu có)
                    entity.render(gc);
                } else if (entity instanceof Portal && !((Portal)entity).isRevealed() && entity.isAlive()) {
                    // Nếu Portal chưa reveal nhưng vẫn alive (tức là tồn tại ẩn), vẫn render nó
                    // (Lớp Portal sẽ tự quyết định vẽ sprite ẩn hay không)
                    entity.render(gc);
                }
            }
        }

        // 4. Vẽ PowerUps (Nếu chúng không phải là static entities và cần vẽ riêng)
        // Thông thường PowerUp sẽ xuất hiện sau khi Brick vỡ và nằm trên nền
        List<PowerUp> powerUps = game.getPowerUps();
        if (powerUps != null) {
            for (PowerUp pu : powerUps) {
                if (pu.isAlive()) { // Chỉ vẽ power-up còn có thể nhặt
                    pu.render(gc);
                }
            }
        }

        // 5. Vẽ Bombs (Đè lên nền, tường, gạch nếu trùng vị trí)
        List<Bomb> bombs = game.getBombs();
        if (bombs != null) {
            for (Bomb bomb : bombs) {
                if (bomb.isAlive()) { // Chỉ vẽ bomb đang hoạt động
                    bomb.render(gc);
                }
            }
        }

        // 6. Vẽ Explosions (Đè lên mọi thứ)
        List<Explosion> explosions = game.getExplosions();
        if (explosions != null) {
            for (Explosion explosion : explosions) {
                if (explosion.isAlive()) { // Chỉ vẽ explosion đang diễn ra
                    explosion.render(gc);
                }
            }
        }

        // 7. Vẽ Enemies
        List<Enemy> enemies = game.getEnemies();
        if (enemies != null) {
            for (Enemy enemy : enemies) {
                // Vẽ enemy nếu nó còn sống hoặc đang trong animation chết
                if (enemy.isAlive() || enemy.isDying()) {
                    enemy.render(gc);
                }
            }
        }

        // 8. Vẽ Player (Lớp trên cùng)
        Player player = game.getPlayer();
        if (player != null) {
            // Vẽ player nếu còn sống hoặc đang trong animation chết
            if (player.isAlive() || player.isDying()) {
                player.render(gc);
            }
        }

        // 9. Vẽ UI (Thông tin level, điểm, mạng)
        drawUI(gc, game);
    }

    /**
     * Xóa toàn bộ canvas với một màu nền định trước.
     *
     * @param gc GraphicsContext để vẽ.
     */
    private void clearScreen(GraphicsContext gc) {
        gc.setFill(Color.BLACK); // Màu nền đen (hoặc màu bạn muốn cho background mặc định)
        gc.fillRect(0, 0, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
    }

    /**
     * Vẽ các yếu tố giao diện người dùng (UI) như level, điểm, mạng.
     *
     * @param gc   GraphicsContext để vẽ.
     * @param game Đối tượng Game để lấy thông tin UI.
     */
    private void drawUI(GraphicsContext gc, Game game) {
        // game đã được kiểm tra null ở phương thức render() chính

        // Cài đặt font chữ và màu sắc chung cho UI
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.setFill(Color.WHITE); // Màu chữ trắng để nổi bật trên nền tối

        double textXPosition = 15.0; // Cách lề trái
        double initialYPosition = 25.0; // Cách lề trên cho dòng đầu tiên
        double lineSpacing = 25.0; // Khoảng cách giữa các dòng

        // Hiển thị Level
        int currentLevelDisplay = game.getCurrentLevelNumber(); // Game.java trả về 1-based
        String levelText = "Level: " + currentLevelDisplay;
        gc.fillText(levelText, textXPosition, initialYPosition);

        // Hiển thị Mạng (Lives)
        int lives = game.getPlayerLives();
        String livesText = "Lives: " + lives;
        gc.fillText(livesText, textXPosition, initialYPosition + lineSpacing);

        // Hiển thị Điểm (Score)
        int score = game.getPlayerScore();
        String scoreText = "Score: " + score;
        gc.fillText(scoreText, textXPosition, initialYPosition + (lineSpacing * 2));

        // Có thể thêm thời gian còn lại nếu game có timer
        // int timeRemaining = game.getTimeRemaining(); // Cần Game có getter này
        // String timeText = "Time: " + timeRemaining;
        // gc.fillText(timeText, Config.WINDOW_WIDTH - 100, initialYPosition); // Căn phải
    }

    /**
     * Phương thức này có thể dùng để render các thành phần cụ thể của game
     * nếu bạn muốn tách biệt logic render. Hiện tại, `render()` đã xử lý mọi thứ.
     * Để trống hoặc xóa nếu không cần thiết.
     *
     * @param gameCanvasGC GraphicsContext của canvas game.
     * @param game         Đối tượng Game.
     */
    public void renderGameContent(GraphicsContext gameCanvasGC, Game game) {
        // Nếu bạn muốn sử dụng phương thức này, hãy chuyển logic render game
        // từ phương thức render() chính vào đây. Ví dụ:
        // if (game == null || gameCanvasGC == null) return;
        // clearScreen(gameCanvasGC);
        // Level level = game.getLevel();
        // if (level == null) { /* ... */ return; }
        // level.renderBackground(gameCanvasGC);
        // ... (vẽ static entities, bombs, explosions, player, enemies)
        //
        // Sau đó, phương thức render() chính có thể gọi:
        // renderGameContent(gc, game);
        // drawUI(gc, game);
        //
        // Hiện tại để trống vì render() đã đủ.
    }
}