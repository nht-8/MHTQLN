package src.bomberman.entities; 

import javafx.geometry.Rectangle2D;
import src.bomberman.Config;        
import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet;

import java.util.List;

public class Explosion extends Entity {

    private int duration;
    private final ExplosionType type; 
    private int animationCounter = 0; 

    private final int ANIMATION_SPEED = Math.max(1, Config.BOMB_EXPLOSION_DURATION / 3);

    public enum ExplosionType {
        CENTER,                 
        HORIZONTAL_MIDDLE,      
        VERTICAL_MIDDLE,        
        END_UP,                
        END_DOWN,               
        END_LEFT,               
        END_RIGHT               
    }

  
    public Explosion(double xTile, double yTile, SpriteSheet sheet, ExplosionType type) {
        super(xTile, yTile, sheet); 
        this.duration = Config.BOMB_EXPLOSION_DURATION;
        this.type = type;
        setSpriteForFrame(0);
        
        if (this.sprite == null) {
            System.err.println("CRITICAL WARNING: Initial Explosion sprite is null for type: " + type +
                    ". Ensure Sprite.loadSprites() ran correctly and sprites exist.");
        }
    }

    private void setSpriteForFrame(int frameIndex) {
        frameIndex = Math.max(0, Math.min(2, frameIndex));
        Sprite targetSprite = null; 

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
            case END_UP:
                if (frameIndex == 0) targetSprite = Sprite.explosion_top;
                else if (frameIndex == 1) targetSprite = Sprite.explosion_top1;
                else targetSprite = Sprite.explosion_top2;
                break;
            case END_DOWN: 
                if (frameIndex == 0) targetSprite = Sprite.explosion_bottom;
                else if (frameIndex == 1) targetSprite = Sprite.explosion_bottom1;
                else targetSprite = Sprite.explosion_bottom2;
                break;
            case END_LEFT:
                if (frameIndex == 0) targetSprite = Sprite.explosion_left;
                else if (frameIndex == 1) targetSprite = Sprite.explosion_left1;
                else targetSprite = Sprite.explosion_left2;
                break;
            case END_RIGHT: 
                if (frameIndex == 0) targetSprite = Sprite.explosion_right;
                else if (frameIndex == 1) targetSprite = Sprite.explosion_right1;
                else targetSprite = Sprite.explosion_right2;
                break;
            default: 
                System.err.println("Warning: Unknown ExplosionType: " + type);
                targetSprite = Sprite.explosion_center; 
                break;
        }

        this.sprite = targetSprite;
        if (this.sprite == null) {
            System.err.println("Warning: Explosion sprite is null after assignment for type: " + type +
                    ", frameIndex: " + frameIndex + ". Defaulting to center.");
            this.sprite = Sprite.explosion_center; 
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


    @Override
    public boolean isSolid() {
        return false;
    }
}
