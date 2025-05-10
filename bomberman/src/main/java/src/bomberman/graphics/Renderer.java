package src.bomberman.graphics;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import src.bomberman.Config;
import src.bomberman.core.Game;
import src.bomberman.core.Level;
import src.bomberman.entities.*;
import src.bomberman.entities.Portal;

import java.util.List;

public class Renderer {

    public void render(GraphicsContext gc, Game game) {

        if (game == null) {
            System.err.println("Renderer Error: Game object is null. Cannot render.");
            clearScreen(gc);
            gc.setFill(Color.RED);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            gc.fillText("Error: Game data unavailable!", 10, 20);
            return;
        }

        clearScreen(gc);

        Level level = game.getLevel();

        if (level == null) {
            System.err.println("Renderer Error: Level is null, cannot render map.");
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            gc.fillText("Error loading level data!", 10, 20);
            return;
        }

        level.renderBackground(gc);

        List<Entity> staticEntities = game.getStaticEntities();
        if (staticEntities != null) {
            for (Entity entity : staticEntities) {
                if (entity.isAlive() || (entity instanceof Brick && ((Brick)entity).isDying()) || (entity instanceof Portal && ((Portal)entity).isRevealed())) {
                    entity.render(gc);
                } else if (entity instanceof Portal && !((Portal)entity).isRevealed() && entity.isAlive()) {
                    entity.render(gc);
                }
            }
        }

        List<PowerUp> powerUps = game.getPowerUps();
        if (powerUps != null) {
            for (PowerUp pu : powerUps) {
                if (pu.isAlive()) {
                    pu.render(gc);
                }
            }
        }

        List<Bomb> bombs = game.getBombs();
        if (bombs != null) {
            for (Bomb bomb : bombs) {
                if (bomb.isAlive()) {
                    bomb.render(gc);
                }
            }
        }

        List<Explosion> explosions = game.getExplosions();
        if (explosions != null) {
            for (Explosion explosion : explosions) {
                if (explosion.isAlive()) {
                    explosion.render(gc);
                }
            }
        }

        List<Enemy> enemies = game.getEnemies();
        if (enemies != null) {
            for (Enemy enemy : enemies) {
                if (enemy.isAlive() || enemy.isDying()) {
                    enemy.render(gc);
                }
            }
        }

        Portal currentPortal = game.getCurrentLevelPortal(); // Lấy portal từ Game
        if (currentPortal != null) {
            // Đối tượng Portal sẽ tự kiểm tra isRevealed và isAlive bên trong render() của nó
            currentPortal.render(gc);
        }

        Player player = game.getPlayer();
        if (player != null) {

            if (player.isAlive() || player.isDying()) {
                player.render(gc);
            }
        }

        List<FloatingText> floatingTextsToRender = game.getFloatingTexts();
        if (floatingTextsToRender != null) {
            for (FloatingText ft : floatingTextsToRender) {
                if (ft.isAlive()) {
                    ft.render(gc);
                }
            }
        }

    }

    private void clearScreen(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
    }

    public void renderGameContent(GraphicsContext gameCanvasGC, Game game) {

    }
}
