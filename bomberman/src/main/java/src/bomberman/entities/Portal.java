// src/main/java/src/bomberman/entities/Portal.java
package src.bomberman.entities;

import javafx.scene.canvas.GraphicsContext;
import src.bomberman.Config;
import javafx.scene.paint.Color; // Import Color
import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet;

import java.util.List;

import static java.awt.Color.BLUE;

public class Portal extends Entity {
    private boolean revealed =false;

    public Portal(double xTile, double yTile, SpriteSheet sheet) {
        super(xTile, yTile, sheet);

    }

    @Override
    public void render(GraphicsContext gc) {
        System.out.println("[Portal DEBUG Render] Called. Revealed: " + this.revealed + ", Alive: " + this.alive);
        if (this.alive && this.revealed) {
            System.out.println("[Portal DEBUG Render] Drawing BLUE square for portal.");
            gc.setFill(Color.BLUE);
            gc.fillRect(this.x, this.y, Config.TILE_SIZE, Config.TILE_SIZE);
        }  }
    @Override
    public void update(double deltaTime, List<Entity> entities) {
        // Portal thường không có logic update phức tạp.
    }

    @Override
    public boolean isSolid() {
        return false; // Player có thể đi qua
    }
    public boolean isRevealed() {
        return revealed;
    }
    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
        if (revealed) {
            System.out.println("Portal at (" + getTileX() + "," + getTileY() + ") is now REVEALED.");
            // Có thể đổi sprite ở đây nếu bạn có sprite khác cho portal khi active
            // this.sprite = Sprite.portal_active; (nếu có)
        }
    }
}