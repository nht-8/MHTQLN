// src/main/java/src/bomberman/entities/Entity.java
package src.bomberman.entities; // Hoặc uet.oop.bomberman.entities

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color; // Import Color để vẽ lỗi
import src.bomberman.Config;
import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet; // Cần cho constructor

import java.util.List;

/**
 * Lớp trừu tượng cơ sở cho tất cả các đối tượng trong game (Player, Enemy, Bomb, Wall, Brick,...).
 * Chứa các thuộc tính và phương thức chung như vị trí, hình ảnh (sprite), trạng thái sống/chết,
 * và các phương thức trừu tượng cho việc cập nhật và vẽ.
 */
public abstract class Entity {
    protected double x; // Tọa độ pixel X trên màn hình (góc trên trái)
    protected double y; // Tọa độ pixel Y trên màn hình (góc trên trái)

    protected Sprite sprite; // Sprite hiện tại dùng để vẽ cho Entity này
    protected boolean alive = true; // Trạng thái sống/chết của Entity
    // SpriteSheet của entity này, dùng để các lớp con chọn sprite
    // Hoặc nếu tất cả sprite của entity nằm trên cùng 1 sheet, thì gán trong constructor
    protected SpriteSheet entitySheet;

    /**
     * Constructor cơ sở cho Entity.
     * Nhận tọa độ ban đầu theo ô Tile và chuyển đổi thành tọa độ pixel.
     * @param xTile Tọa độ ô X ban đầu trên lưới map.
     * @param yTile Tọa độ ô Y ban đầu trên lưới map.
     * @param sheet SpriteSheet mặc định cho entity này (ví dụ: nesSheet cho Wall, modernSheet cho Player).
     */
    public Entity(double xTile, double yTile, SpriteSheet sheet) {
        this.x = xTile * Config.TILE_SIZE;
        this.y = yTile * Config.TILE_SIZE;
        this.entitySheet = sheet; // Lưu lại sheet
        // Sprite cụ thể ban đầu sẽ được gán trong constructor của lớp con.
    }

    public abstract void update(double deltaTime, List<Entity> entities);

    public void render(GraphicsContext gc) {
        if (sprite != null && sprite.sheet != null && sprite.sheet.getSheet() != null && !sprite.sheet.getSheet().isError()) {
            // Sử dụng kích thước gốc của sprite để vẽ (không scale ở đây nữa nếu Config.TILE_SIZE đã đúng)
            // Nếu bạn muốn scale, logic scale phải ở đây hoặc trong Config/Sprite
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
        // Trả về kích thước ô chuẩn, vì entity được vẽ vừa vào ô đó
        return Config.TILE_SIZE;
    }

    public double getHeight() {
        // Trả về kích thước ô chuẩn
        return Config.TILE_SIZE;
    }

    public Rectangle2D getBounds() {
        // Vùng bao cũng theo kích thước ô chuẩn trên màn hình
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
        // Tâm của entity nằm ở x + TILE_SIZE/2 (vì getWidth() giờ là TILE_SIZE)
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