// src/main/java/src/bomberman/entities/Portal.java
<<<<<<< Updated upstream
package src.bomberman.entities;
=======
package src.bomberman.entities; // HOẶC uet.oop.bomberman.entities
>>>>>>> Stashed changes

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image; // Cần cho render
import javafx.scene.paint.Color;  // Cần cho fallback render
import src.bomberman.Config;
<<<<<<< Updated upstream
import javafx.scene.paint.Color; // Import Color
=======
import src.bomberman.core.Game;
>>>>>>> Stashed changes
import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet;

import java.util.List;

public class Portal extends Entity {
<<<<<<< Updated upstream
    private boolean revealed =false;

    public Portal(double xTile, double yTile, SpriteSheet sheet) {
=======
    private boolean revealed = false; // Ban đầu Portal bị che, chưa lộ ra
    private Game game; // (Tùy chọn) Có thể cần tham chiếu Game nếu Portal có logic phức tạp

    /**
     * Constructor cho Portal.
     * @param xTile Tọa độ ô X ban đầu.
     * @param yTile Tọa độ ô Y ban đầu.
     * @param sheet SpriteSheet chứa hình ảnh Portal (thường là nesSheet).
     * @param game (Tùy chọn) Tham chiếu đến đối tượng Game.
     */
    public Portal(double xTile, double yTile, SpriteSheet sheet, Game game) { // Thêm Game vào constructor
>>>>>>> Stashed changes
        super(xTile, yTile, sheet);
        this.game = game; // Lưu lại nếu cần
        this.sprite = Sprite.portal; // Gán sprite Portal đã load
        this.alive = true; // Portal luôn "sống" khi đã được tạo
        if (this.sprite == null) {
            System.err.println("CRITICAL ERROR: Sprite.portal is null during Portal construction!");
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        // Chỉ vẽ Portal nếu nó đã được "revealed" (gạch che nó bị phá)
        if (this.alive && this.revealed) {
<<<<<<< Updated upstream
            System.out.println("[Portal DEBUG Render] Drawing BLUE square for portal.");
            gc.setFill(Color.BLUE);
            gc.fillRect(this.x, this.y, Config.TILE_SIZE, Config.TILE_SIZE);
        }  }
    @Override
    public void update(double deltaTime, List<Entity> entities) {
        // Portal thường không có logic update phức tạp.
=======
            if (sprite != null && sprite.sheet != null && sprite.sheet.getSheet() != null && !sprite.sheet.getSheet().isError()) {
                Image sourceImage = sprite.sheet.getSheet();
                double sx = sprite.getSourceX();
                double sy = sprite.getSourceY();
                double sw = sprite.getSourceWidth();  // Kích thước gốc của sprite portal
                double sh = sprite.getSourceHeight(); // Kích thước gốc của sprite portal

                // Vẽ sprite Portal với kích thước bằng Config.TILE_SIZE để lấp đầy ô
                gc.drawImage(sourceImage, sx, sy, sw, sh,
                        this.x, this.y,
                        Config.TILE_SIZE, Config.TILE_SIZE);
            } else {
                // Fallback nếu Sprite.portal bị lỗi
                gc.setFill(Color.DEEPSKYBLUE); // Một màu dễ nhận biết cho Portal
                gc.fillRect(this.x, this.y, Config.TILE_SIZE, Config.TILE_SIZE);
                System.err.println("[Portal RENDER ERROR] Sprite.portal is invalid for portal at (" + getX() + "," + getY() + ").");
            }
        }
        // Nếu !revealed, Portal không được vẽ
>>>>>>> Stashed changes
    }

    @Override
    public void update(double deltaTime, List<Entity> entities) {
        // Portal thường không có logic update phức tạp sau khi đã revealed.
        // Nó chỉ đứng đó chờ Player.
        // Nếu bạn muốn Portal có animation, hãy thêm vào đây.
    }

    /**
     * Portal không phải là vật cản rắn, Player có thể đi qua để kích hoạt.
     */
    @Override
    public boolean isSolid() {
        return false; // Player có thể đi qua
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void setRevealed(boolean revealed) {
        if (this.revealed == revealed) return; // Không làm gì nếu trạng thái không đổi

        this.revealed = revealed;
        if (revealed) {
<<<<<<< Updated upstream
            System.out.println("Portal at (" + getTileX() + "," + getTileY() + ") is now REVEALED.");
            // Có thể đổi sprite ở đây nếu bạn có sprite khác cho portal khi active
            // this.sprite = Sprite.portal_active; (nếu có)
=======
            System.out.println("Portal at TILE (" + getTileX() + "," + getTileY() + ") is now REVEALED.");
            // TODO: Có thể phát âm thanh khi Portal xuất hiện
            // SoundManager.getInstance().playSound("portal_reveal_sfx");
        } else {
            // Thường không có trường hợp Portal bị ẩn lại
            System.out.println("Portal at TILE (" + getTileX() + "," + getTileY() + ") is now HIDDEN.");
>>>>>>> Stashed changes
        }
    }
}