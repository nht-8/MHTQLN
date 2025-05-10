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

<<<<<<< Updated upstream
        // 8. Vẽ Player (Lớp trên cùng)
=======
        Portal currentPortal = game.getCurrentLevelPortal(); // Lấy portal từ Game
        if (currentPortal != null) {
            // Đối tượng Portal sẽ tự kiểm tra isRevealed và isAlive bên trong render() của nó
            currentPortal.render(gc);
        }

>>>>>>> Stashed changes
        Player player = game.getPlayer();
        if (player != null) {
            // Vẽ player nếu còn sống hoặc đang trong animation chết
            if (player.isAlive() || player.isDying()) {
                player.render(gc);
            }
        }

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