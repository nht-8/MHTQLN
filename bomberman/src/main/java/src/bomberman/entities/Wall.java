// src/main/java/src/bomberman/entities/Wall.java
package src.bomberman.entities; // HOẶC uet.oop.bomberman.entities

import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet;
import java.util.List;

/**
 * Đại diện cho một bức tường không thể phá hủy.
 */
public class Wall extends Entity {

    public Wall(double xTile, double yTile, SpriteSheet sheet) {
        super(xTile, yTile, sheet);
        this.sprite = Sprite.wall; // Gán sprite tường
        if (this.sprite == null) {
            System.err.println("CRITICAL WARNING: Sprite.wall is null during Wall construction!");
        }
    }

    @Override
    public void update(double deltaTime, List<Entity> entities) {
        // Tường không có logic cập nhật động
    }

    /**
     * Tường luôn là vật cản rắn.
     */
    @Override
    public boolean isSolid() {
        return true;
    }
}