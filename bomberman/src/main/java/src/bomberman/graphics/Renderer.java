// src/main/java/src/bomberman/graphics/Renderer.java
package src.bomberman.graphics;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import src.bomberman.Config;
import src.bomberman.core.Game;
import src.bomberman.core.Level;
import src.bomberman.entities.*; // Import entities

import java.util.List;

public class Renderer {

    public void render(GraphicsContext gc, Game game) {
        // 1. Xóa màn hình bằng màu nền cơ bản (Ví dụ: Đen)
        // Tránh để lộ màu nền magenta của cửa sổ nếu có lỗi vẽ
        clearScreen(gc);

        Level level = game.getLevel();
        // Kiểm tra Level có tồn tại không
        if (level == null) {
            System.err.println("Renderer Error: Level is null, cannot render map.");
            return; // Không vẽ gì thêm
        }

        // 2. Vẽ nền (Cỏ) cho toàn bộ map LÊN TRÊN màu nền đen
        level.renderBackground(gc);

        // 3. Vẽ các thực thể tĩnh (Tường '#', Gạch '*') ĐÈ LÊN nền cỏ
        // Lấy danh sách Wall, Brick từ Game (đã được tạo trong Level.createEntitiesFromMap)
        List<Entity> staticEntities = game.getStaticEntities();
        if (staticEntities != null) {
            for (Entity entity : staticEntities) {
                // Wall và Brick sẽ tự gọi render() của chúng để vẽ Sprite.wall/Sprite.brick
                entity.render(gc);
            }
        }

        // 4. POWERUPS (Sau nền/tường/gạch, trước bom/player)
        List<PowerUp> powerUps = game.getPowerUps();
        if (powerUps != null) {
            for (PowerUp pu : powerUps) {
                if (pu.isAlive()) { // Chỉ vẽ powerup nào chưa được nhặt
                    pu.render(gc);
                }
            }
        }

        // 5. Vẽ Bombs (Đè lên nền, tường, gạch nếu trùng vị trí)
        List<Bomb> bombs = game.getBombs();
        if (bombs != null) {
            for (Bomb bomb : bombs) {
                bomb.render(gc);
            }
        }

        // 6. Vẽ Explosions (Đè lên mọi thứ trừ Player/Enemy nếu trùng)
        List<Explosion> explosions = game.getExplosions();
        if (explosions != null) {
            for (Explosion explosion : explosions) {
                explosion.render(gc);
            }
        }

        // 7. Vẽ Enemies
        List<Enemy> enemies = game.getEnemies();
        if (enemies != null) {
            for (Enemy enemy : enemies) {
                if (enemy.isAlive()) { // Chỉ vẽ enemy còn sống
                    enemy.render(gc);
                }
            }
        }

        // 8. Vẽ Player (Lớp trên cùng)
        Player player = game.getPlayer();
        if (player != null && (player.isAlive() || player.isDying())) { // Chỉ vẽ player còn sống
            player.render(gc);
        }
        // Nếu muốn vẽ animation chết thì bỏ điều kiện isAlive() hoặc thêm isDying()

    }

    private void clearScreen(GraphicsContext gc) {
        gc.setFill(Color.BLACK); // <<< Đặt màu nền ở đây (ví dụ: Đen)
        gc.fillRect(0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
    }

    // ... (drawUI) ...
}