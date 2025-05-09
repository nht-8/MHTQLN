package src.bomberman.entities;

import javafx.scene.canvas.GraphicsContext;
import src.bomberman.Config;
import javafx.scene.paint.Color;
import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet;

import java.util.List;

import static java.awt.Color.BLUE;

public class Portal extends Entity {
    private boolean revealed =false;
    private boolean active=false;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

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

    }

    @Override
    public boolean isSolid() {
        return false;
    }
    public boolean isRevealed() {
        return revealed;
    }
    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
        if (revealed) {
            System.out.println("Portal at (" + getTileX() + "," + getTileY() + ") is now REVEALED.");
        }
    }

}
