// src/main/java/src/bomberman/entities/Explosion.java
package src.bomberman.entities; // Hoặc package của bạn: uet.oop.bomberman.entities

// Import các lớp cần thiết
import javafx.geometry.Rectangle2D; // Cần cho getBounds() trong checkCollisions
import src.bomberman.Config;        // Hoặc package của bạn
import src.bomberman.graphics.Sprite; // Hoặc package của bạn
import src.bomberman.graphics.SpriteSheet; // Hoặc package của bạn

import java.util.List;

/**
 * Đại diện cho một đoạn lửa của vụ nổ (Flame Segment).
 * Có thời gian tồn tại ngắn, chạy animation và gây ảnh hưởng (phá hủy, sát thương)
 * lên các thực thể khác khi va chạm.
 */
public class Explosion extends Entity {

    private int duration; // Thời gian tồn tại còn lại (tính bằng số frame update)
    private final ExplosionType type; // Loại (hình dạng) của đoạn lửa này
    private int animationCounter = 0; // Bộ đếm để điều khiển tốc độ animation

    // Tốc độ animation: Số frame update hiển thị cho mỗi sprite lửa
    // Chia đều thời gian tồn tại cho 3 frame animation
    private final int ANIMATION_SPEED = Math.max(1, Config.BOMB_EXPLOSION_DURATION / 3);

    /**
     * Enum định nghĩa các loại (hình dạng) khác nhau của đoạn lửa.
     * Tên tương ứng với các sprite trong định nghĩa NES.
     */
    public enum ExplosionType {
        CENTER,                 // Tâm vụ nổ (sprite: explosion_center, _center1, _center2)
        HORIZONTAL_MIDDLE,      // Đoạn giữa tia ngang (sprite: explosion_horizontal, _horizontal1, _horizontal2)
        VERTICAL_MIDDLE,        // Đoạn giữa tia dọc (sprite: explosion_vertical, _vertical1, _vertical2)
        END_UP,                 // Đoạn cuối tia hướng lên (sprite: explosion_vertical_top_last, _top_last1, _top_last2)
        END_DOWN,               // Đoạn cuối tia hướng xuống (sprite: explosion_vertical_down_last, _down_last1, _down_last2)
        END_LEFT,               // Đoạn cuối tia hướng trái (sprite: explosion_horizontal_left_last, _left_last1, _left_last2)
        END_RIGHT               // Đoạn cuối tia hướng phải (sprite: explosion_horizontal_right_last, _right_last1, _right_last2)
    }

    /**
     * Constructor cho một đoạn lửa Explosion.
     * @param xTile Tọa độ ô X ban đầu trên lưới map.
     * @param yTile Tọa độ ô Y ban đầu trên lưới map.
     * @param sheet SpriteSheet chứa hình ảnh của vụ nổ (thường là nesSheet).
     * @param type Loại (hình dạng) của đoạn lửa này (ExplosionType).
     */
    public Explosion(double xTile, double yTile, SpriteSheet sheet, ExplosionType type) {
        super(xTile, yTile, sheet); // Gọi constructor Entity
        this.duration = Config.BOMB_EXPLOSION_DURATION; // Lấy thời gian tồn tại từ Config
        this.type = type;
        setSpriteForFrame(0); // Đặt sprite cho frame đầu tiên (index 0)

        // Kiểm tra ngay sau khi tạo xem sprite ban đầu có bị null không
        if (this.sprite == null) {
            System.err.println("CRITICAL WARNING: Initial Explosion sprite is null for type: " + type +
                    ". Ensure Sprite.loadSprites() ran correctly and sprites exist.");
            // Có thể gán một sprite mặc định an toàn ở đây nếu cần, ví dụ wall hoặc grass
            // this.sprite = Sprite.grass; // Ví dụ fallback cuối cùng
        }
    }

    /**
     * Helper method để đặt giá trị cho biến `sprite` dựa trên loại lửa (`type`)
     * và chỉ số frame animation hiện tại (0, 1, hoặc 2).
     * Sử dụng các tên biến `Sprite` tĩnh tương ứng với định nghĩa NES.
     * @param frameIndex Chỉ số frame animation (0, 1, hoặc 2).
     */
    private void setSpriteForFrame(int frameIndex) {
        // Đảm bảo frameIndex nằm trong khoảng [0, 2]
        frameIndex = Math.max(0, Math.min(2, frameIndex));
        Sprite targetSprite = null; // Sprite sẽ được gán

        switch (type) {
            case CENTER:
                if (frameIndex == 0) targetSprite = Sprite.explosion_center;
                else if (frameIndex == 1) targetSprite = Sprite.explosion_center1;
                else targetSprite = Sprite.explosion_center2;
                break;
            case HORIZONTAL_MIDDLE:
                if (frameIndex == 0) targetSprite = Sprite.explosion_horizontal;
                else if (frameIndex == 1) targetSprite = Sprite.explosion_horizontal1;
                else targetSprite = Sprite.explosion_horizontal2;
                break;
            case VERTICAL_MIDDLE:
                if (frameIndex == 0) targetSprite = Sprite.explosion_vertical;
                else if (frameIndex == 1) targetSprite = Sprite.explosion_vertical1;
                else targetSprite = Sprite.explosion_vertical2;
                break;
            case END_UP: // Top last
                if (frameIndex == 0) targetSprite = Sprite.explosion_top;
                else if (frameIndex == 1) targetSprite = Sprite.explosion_top1;
                else targetSprite = Sprite.explosion_top2;
                break;
            case END_DOWN: // Down last
                if (frameIndex == 0) targetSprite = Sprite.explosion_bottom;
                else if (frameIndex == 1) targetSprite = Sprite.explosion_bottom1;
                else targetSprite = Sprite.explosion_bottom2;
                break;
            case END_LEFT: // Left last
                if (frameIndex == 0) targetSprite = Sprite.explosion_left;
                else if (frameIndex == 1) targetSprite = Sprite.explosion_left1;
                else targetSprite = Sprite.explosion_left2;
                break;
            case END_RIGHT: // Right last
                if (frameIndex == 0) targetSprite = Sprite.explosion_right;
                else if (frameIndex == 1) targetSprite = Sprite.explosion_right1;
                else targetSprite = Sprite.explosion_right2;
                break;
            default: // Trường hợp không xác định (không nên xảy ra)
                System.err.println("Warning: Unknown ExplosionType: " + type);
                targetSprite = Sprite.explosion_center; // Mặc định về tâm nổ frame 1
                break;
        }

        // Gán sprite và kiểm tra null
        this.sprite = targetSprite;
        if (this.sprite == null) {
            System.err.println("Warning: Explosion sprite is null after assignment for type: " + type +
                    ", frameIndex: " + frameIndex + ". Defaulting to center.");
            this.sprite = Sprite.explosion_center; // Fallback cuối cùng
            if (this.sprite == null) {
                System.err.println("CRITICAL ERROR: Default explosion sprite (explosion_center) is also null!");
            }
        }
    }

    /**
     * Cập nhật trạng thái Explosion mỗi frame:
     * Giảm thời gian tồn tại và cập nhật frame animation.
     * Việc kiểm tra va chạm được thực hiện một lần khi tạo ra trong Game.addExplosion().
     */
    @Override
    public void update(double deltaTime, List<Entity> entities) {
        if (!alive) return; // Không làm gì nếu đã hết hạn

        duration--;
        if (duration <= 0) {
            alive = false; // Hết thời gian, đánh dấu để Game xóa đi
            return;
        }

        // Cập nhật frame animation dựa trên thời gian đã trôi qua
        animationCounter++;
        int totalAnimationTime = ANIMATION_SPEED * 3; // Tổng thời gian cho 3 frame
        // Không cần reset animationCounter nếu không muốn lặp lại animation

        // Xác định chỉ số frame hiện tại (0, 1, hoặc 2)
        // Chia thời gian tồn tại đã trôi qua cho tốc độ mỗi frame
        // Ví dụ: duration = 30, speed = 10.
        // counter 0-9 -> frame 0. counter 10-19 -> frame 1. counter 20-29 -> frame 2.
        int elapsedCounter = Config.BOMB_EXPLOSION_DURATION - duration; // Thời gian đã trôi qua
        int currentFrameIndex = elapsedCounter / ANIMATION_SPEED;
        if (currentFrameIndex > 2) currentFrameIndex = 2; // Đảm bảo không vượt quá frame cuối

        setSpriteForFrame(currentFrameIndex); // Cập nhật sprite hiển thị
    }

    /**
     * Thực hiện kiểm tra va chạm ban đầu ngay sau khi Explosion được tạo ra.
     * Phương thức này được gọi từ `Game.addExplosion()`.
     * @param entities Danh sách tất cả các thực thể hiện có trong game để kiểm tra va chạm.
     */
    public void checkInitialCollisions(List<Entity> entities) {
        // Lấy vùng bao của đoạn lửa này (sử dụng getBounds() từ lớp Entity)
        Rectangle2D explosionBounds = this.getBounds();

        // Lặp qua danh sách các thực thể trong game
        for (Entity entity : entities) {
            // Bỏ qua kiểm tra với chính nó hoặc các thực thể đã "chết" (không còn hoạt động)
            if (entity == this || !entity.isAlive()) continue;

            // Chỉ kiểm tra va chạm với các loại thực thể có thể tương tác với lửa
            if (entity instanceof Player || entity instanceof Enemy || entity instanceof Brick || entity instanceof Bomb) {
                // Kiểm tra xem vùng bao của lửa có giao cắt với vùng bao của thực thể kia không
                if (explosionBounds.intersects(entity.getBounds())) {
                    // Nếu có va chạm, xử lý tùy theo loại thực thể
                    if (entity instanceof Player) {
                        ((Player) entity).destroy(); // Gọi phương thức xử lý khi Player chết
                    } else if (entity instanceof Enemy) {
                        ((Enemy) entity).destroy();   // Gọi phương thức xử lý khi Enemy chết
                    } else if (entity instanceof Brick) {
                        ((Brick) entity).startBreaking(); // Bắt đầu animation phá gạch
                    } else if (entity instanceof Bomb) {
                        // Kích hoạt vụ nổ của Bomb khác (tạo hiệu ứng nổ dây chuyền)
                        ((Bomb) entity).triggerExplosion(); // Gọi phương thức kích nổ sớm
                    }
                }
            }
            // Có thể thêm kiểm tra va chạm với PowerUp ở đây nếu muốn lửa phá hủy PowerUp
            // else if (entity instanceof PowerUp) { ... }
        }
    }

    /**
     * Vụ nổ không phải là vật cản rắn, các thực thể khác có thể đi xuyên qua.
     * @return luôn trả về false.
     */
    @Override
    public boolean isSolid() {
        return false;
    }
}