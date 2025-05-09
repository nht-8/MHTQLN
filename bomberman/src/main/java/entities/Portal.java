// src/main/java/src/bomberman/entities/Portal.java
package src.bomberman.entities;

import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet;
import java.util.List;

public class Portal extends Entity {

    /**
     * Constructor cho Portal.
     * @param xTile Tọa độ ô X ban đầu.
     * @param yTile Tọa độ ô Y ban đầu.
     * @param sheet SpriteSheet chứa hình ảnh portal (thường là nesSheet).
     */
    public Portal(double xTile, double yTile, SpriteSheet sheet) {
        super(xTile, yTile, sheet);
        this.sprite = Sprite.portal; // Sprite đã được định nghĩa trong Sprite.java
        if (this.sprite == null) {
            System.err.println("CRITICAL WARNING: Sprite.portal is null during Portal construction!");
        }
        // Kiểm tra xem entitySheet (được truyền từ constructor của Entity) có bị null không
        if (this.entitySheet == null || this.entitySheet.getSheet() == null) {
            System.err.println("CRITICAL WARNING: Portal's entitySheet or its underlying image is null!");
        }
    }

    @Override
    public void update(double deltaTime, List<Entity> entities) {
        // Portal thường không có logic update phức tạp.
        // Việc kích hoạt portal sẽ được xử lý trong lớp Game.
    }

    /**
     * Portal không phải là vật cản rắn, Player có thể đi qua.
     */
    @Override
    public boolean isSolid() {
        return false;
    }

    // Phương thức render() được kế thừa từ Entity và sẽ tự động vẽ sprite đã gán.
}
