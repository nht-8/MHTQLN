// src/main/java/src/bomberman/entities/Portal.java
package src.bomberman.entities;

import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet;
import java.util.List;

public class Portal extends Entity {

    public Portal(double xTile, double yTile, SpriteSheet sheet) {
        super(xTile, yTile, sheet);
        this.sprite = Sprite.portal; // Sprite đã được định nghĩa trong Sprite.java
        if (this.sprite == null) {
            System.err.println("CRITICAL WARNING: Sprite.portal is null during Portal construction!");
        }
        if (this.entitySheet == null || this.entitySheet.getSheet() == null) {
            System.err.println("CRITICAL WARNING [Portal Constructor]: Portal's entitySheet or its underlying image is null!");
        }
    }

    @Override
    public void update(double deltaTime, List<Entity> entities) {
        // Portal thường không có logic update phức tạp.
    }

    @Override
    public boolean isSolid() {
        return false; // Player có thể đi qua
    }
    boolean revealed;
    public boolean isRevealed() {
        return revealed;
    }
}