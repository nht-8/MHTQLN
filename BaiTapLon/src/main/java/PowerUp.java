package org.example.demo;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class PowerUp {

    private final int gridX, gridY;
    private final int size;
    private final double x, y;
    private final PowerUpType type;
    private final Image image;
    private boolean collected = false;

    public PowerUp(int gridX, int gridY, int tileSize, PowerUpType type, Image image) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.size = tileSize;
        this.x = gridX * tileSize;
        this.y = gridY * tileSize;
        this.type = type;
        this.image = image;
    }

    // Không cần update phức tạp
    // public void update(double deltaTime) { }

    /**
     * GraphicsContext không phải 1 kiểu dữ liệu mà là 1 abstract class
     * nó cung cấp các method để vẽ hình 2D
     * @param gc
     */
    public void render(GraphicsContext gc) {
        if (!collected && image != null) {
            gc.drawImage(image, x, y, size, size);
        }
    }
    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public PowerUpType getType() {
        return type;
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        this.collected = true;
    }

    public Rectangle2D getHitbox() {
        double hitboxSize = size * 0.8;
        double hitboxOffset = (size - hitboxSize) / 2.0;
        return new Rectangle2D(x + hitboxOffset, y + hitboxOffset, hitboxSize, hitboxSize);
    }
}