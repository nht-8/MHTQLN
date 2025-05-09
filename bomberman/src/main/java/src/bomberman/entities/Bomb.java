// src/main/java/src/bomberman/entities/Bomb.java
package src.bomberman.entities; // Hoặc uet.oop.bomberman.entities

// Import các lớp cần thiết
import src.bomberman.core.Game;
import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet;
import src.bomberman.Config;
import src.bomberman.sound.SoundManager;

import java.util.List;

/**
 * Đại diện cho một quả bom trong game.
 * Bom có bộ đếm thời gian, hiệu ứng hoạt ảnh và sẽ phát nổ sau một khoảng thời gian.
 */
public class Bomb extends Entity {

    private int timer; // Thời gian đếm ngược trước khi nổ (số frame)
    private final int flameLength; // Độ dài của vụ nổ (số ô)
    private Player owner; // Player đã đặt quả bom này
    private Game game; // Tham chiếu đến đối tượng Game để tạo vụ nổ

    private int animationCounter = 0; // Bộ đếm cho animation
    // Tốc độ animation: Số frame update hiển thị cho mỗi sprite bom
    private final int ANIMATION_SPEED = 20; // Khoảng 1/3 giây cho mỗi frame ở 60fps

    /**
     * Constructor chính cho Bomb.
     * @param xTile Tọa độ ô X ban đầu trên lưới map.
     * @param yTile Tọa độ ô Y ban đầu trên lưới map.
     * @param sheet SpriteSheet chứa hình ảnh của bom (thường là nesSheet).
     * @param timer Thời gian nổ tính bằng số frame (ví dụ: Config.BOMB_TIMER).
     * @param flameLength Độ dài tia lửa của vụ nổ (số ô).
     * @param owner Player đã đặt bom.
     * @param game Tham chiếu đến đối tượng Game.
     */
    public Bomb(double xTile, double yTile, SpriteSheet sheet, int timer, int flameLength, Player owner, Game game) {
        super(xTile, yTile, sheet); // Gọi constructor của Entity, truyền sheet vào
        this.timer = timer;
        this.flameLength = flameLength;
        this.owner = owner;
        this.game = game;
        this.sprite = Sprite.bomb; // Sprite ban đầu là frame đầu tiên của bom
        if (this.sprite == null) {
            System.err.println("CRITICAL WARNING: Sprite.bomb is null during Bomb construction!");
        }
    }

    @Override
    public void update(double deltaTime, List<Entity> entities) {
        if (!alive) return; // Không cập nhật nếu bom đã bị đánh dấu là không còn sống

        timer--;

        // Cập nhật animation của bom
        animationCounter++;
        int totalAnimationFrames = 3; // Có 3 sprite cho bom: bomb, bomb_1, bomb_2
        if (animationCounter >= ANIMATION_SPEED * totalAnimationFrames) {
            animationCounter = 0; // Reset bộ đếm
        }

        // Chọn sprite dựa trên giai đoạn animation
        int currentFrameIndex = animationCounter / ANIMATION_SPEED; // 0, 1, hoặc 2

        switch (currentFrameIndex) {
            case 0: sprite = Sprite.bomb; break;
            case 1: sprite = Sprite.bomb_1; break;
            case 2: sprite = Sprite.bomb_2; break;
            default: sprite = Sprite.bomb; break; // Dự phòng
        }

        if (sprite == null) {
            System.err.println("Warning: Bomb sprite is null during update. Defaulting.");
            sprite = Sprite.bomb;
            if (sprite == null) System.err.println("CRITICAL ERROR: Default bomb sprite is also null!");
        }

        if (timer <= 0) {
            explode();
        }
    }

    public void explode() {
        if (!alive) return; // Đảm bảo chỉ nổ một lần
        alive = false;

        SoundManager.getInstance().playSound(SoundManager.EXPLOSION);

        if (owner != null) {
            owner.bombExploded();
        }

        if (game != null) {
            // Truyền entitySheet (chính là sheet của bom) vào addExplosion
            // vì sprite vụ nổ cũng nằm trên cùng sheet (nesSheet)
            game.addExplosion(getTileX(), getTileY(), this.flameLength, this.entitySheet);
        } else {
            System.err.println("Bomb cannot explode: Game reference is null. Bomb at (" + getTileX() + "," + getTileY() + ")");
        }
    }

    /**
     * Bom là vật cản rắn khi chưa nổ (còn sống).
     */
    @Override
    public boolean isSolid() {
        if (Entity instanceof Player) {
            return false;
        }
        return alive;
    }

    /**
     * Kích nổ bom sớm (ví dụ: bởi vụ nổ khác).
     */
    public void triggerExplosion() {
        if (!alive) return;
        // Đặt timer về giá trị nhỏ để nổ ở frame tiếp theo, tránh gọi explode() nhiều lần
        if (this.timer > 1) {
            this.timer = 1;
        }
    }
}