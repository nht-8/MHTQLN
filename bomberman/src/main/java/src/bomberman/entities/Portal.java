// src/main/java/src/bomberman/entities/Portal.java
package src.bomberman.entities;

import javafx.scene.canvas.GraphicsContext;
import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet;
import java.util.List;

public class Portal extends Entity {
    private boolean revealed =false;

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
    public void render(GraphicsContext gc) {
        if (this.alive && this.revealed) { // Chỉ vẽ nếu còn sống VÀ đã lộ diện
            super.render(gc); // Gọi render của lớp cha để vẽ sprite
        }
        // Nếu không revealed, không vẽ gì cả (nền cỏ sẽ hiển thị qua)
    }
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