package src.bomberman.graphics;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
// import javafx.scene.text.Font; // Không cần Font ở đây nữa nếu HUD do FXML quản lý
// import javafx.scene.text.FontWeight;
// import javafx.scene.text.TextAlignment;
import src.bomberman.Config;
import src.bomberman.core.Game;
import src.bomberman.core.Level;
import src.bomberman.entities.*;

import java.util.List;

public class Renderer {
    // Không còn biến hudFont hoặc các hằng số liên quan đến HUD ở đây

    public Renderer() {
        // Constructor giờ có thể rỗng hoặc làm việc khác nếu cần
    }

    /**
     * Phương thức này giờ chỉ vẽ nội dung game lên GraphicsContext được cung cấp.
     * HUD sẽ được quản lý bởi FXML và GameHUDController.
     */
    public void renderGameContent(GraphicsContext gc, Game game) {
        // 1. Xóa vùng Canvas của game (không phải toàn bộ màn hình)
        // Màu nền cho vùng game, có thể khác với màu nền chung của cửa sổ
        gc.setFill(Color.rgb(60, 100, 60)); // Ví dụ màu xanh lá cây tối cho nền game
        gc.fillRect(0, 0, Config.GAME_AREA_WIDTH, Config.GAME_AREA_HEIGHT);


        Level level = game.getLevel();
        if (level == null) {
            gc.setFill(Color.RED);
            // gc.setFont(Font.font(20)); // Nếu muốn đặt font
            gc.fillText("Error: Level not loaded in Renderer!", 10, 30);
            return;
        }

        // 2. Vẽ nền game (Cỏ)
        level.renderBackground(gc); // Phương thức này cần vẽ trong giới hạn của nó

        // 3. Vẽ các thực thể tĩnh
        List<Entity> staticEntities = game.getStaticEntities();
        if (staticEntities != null) {
            for (Entity entity : staticEntities) {
                if (entity.isAlive() || (entity instanceof Brick && ((Brick)entity).isDying())) {
                    entity.render(gc);
                }
            }
        }

        // 4. Vẽ PowerUps
        List<PowerUp> powerUps = game.getPowerUps();
        if (powerUps != null) {
            for (PowerUp pu : powerUps) {
                if (pu.isAlive()) {
                    pu.render(gc);
                }
            }
        }

        // 5. Vẽ Bombs
        List<Bomb> bombs = game.getBombs();
        if (bombs != null) {
            for (Bomb bomb : bombs) {
                if (bomb.isAlive()) {
                    bomb.render(gc);
                }
            }
        }

        // 6. Vẽ Explosions
        List<Explosion> explosions = game.getExplosions();
        if (explosions != null) {
            for (Explosion explosion : explosions) {
                if (explosion.isAlive()) {
                    explosion.render(gc);
                }
            }
        }

        // 7. Vẽ Enemies
        List<Enemy> enemies = game.getEnemies();
        if (enemies != null) {
            for (Enemy enemy : enemies) {
                if (enemy.isAlive() || enemy.isDying()) {
                    enemy.render(gc);
                }
            }
        }

        // 8. Vẽ Player
        Player player = game.getPlayer();
        if (player != null) {
            if (player.isAlive() || player.isDying()) {
                player.render(gc);
            }
        }
    }

    private void clearScreen(GraphicsContext gc) {
        gc.setFill(Color.BLACK); // <<< Đặt màu nền ở đây (ví dụ: Đen)
        gc.fillRect(0, 0, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
    }
    // Phương thức clearScreen và drawHUD cũ có thể được xóa hoặc comment lại
    // private void clearScreen(GraphicsContext gc) { ... }
    // private void drawHUD(GraphicsContext gc, Game game) { ... }
}