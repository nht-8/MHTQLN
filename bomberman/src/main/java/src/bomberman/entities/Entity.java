package src.bomberman.entities;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import src.bomberman.Config;
import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet;

import java.util.List;

public abstract class Entity {
    protected double x;
    protected double y;

    protected Sprite sprite;
    protected boolean alive = true;

    protected SpriteSheet entitySheet;

    public Entity(double xTile, double yTile, SpriteSheet sheet) {
        this.x = xTile * Config.TILE_SIZE;
        this.y = yTile * Config.TILE_SIZE;
        this.entitySheet = sheet;
    }

    public abstract void update(double deltaTime, List<Entity> entities);

    public void render(GraphicsContext gc) {
        if (sprite != null && sprite.sheet != null && sprite.sheet.getSheet() != null && !sprite.sheet.getSheet().isError()) {
            Image sourceImage = sprite.sheet.getSheet();
            double sx = sprite.getSourceX();
            double sy = sprite.getSourceY();
            double sw = sprite.getSourceWidth();
            double sh = sprite.getSourceHeight();

            gc.drawImage(sourceImage, sx, sy, sw, sh,
                    this.x, this.y, Config.TILE_SIZE, Config.TILE_SIZE);
        } else {
            gc.setFill(Color.MAGENTA);
            gc.fillRect(this.x, this.y, Config.TILE_SIZE, Config.TILE_SIZE);
        }
    }

    public double getWidth() {

        return Config.TILE_SIZE;
    }

    public double getHeight() {

        return Config.TILE_SIZE;
    }

    public Rectangle2D getBounds() {

        return new Rectangle2D(x, y, getWidth(), getHeight());
    }

    public boolean intersects(Entity other) {
        if (other == null || !other.isAlive() || !this.isAlive()) {
            return false;
        }
        return this.getBounds().intersects(other.getBounds());
    }

    public double getX() { return x; }
    public double getY() { return y; }

    public int getTileX() {

        return (int) (x + Config.TILE_SIZE / 2) / Config.TILE_SIZE;
    }

    public int getTileY() {
        return (int) (y + Config.TILE_SIZE / 2) / Config.TILE_SIZE;
    }

    public boolean isAlive() { return alive; }

    public void destroy() {
        this.alive = false;
    }

    public boolean isDying() {
        return !this.alive;
    }

    public boolean isSolid() {
        return false;
    }
}
