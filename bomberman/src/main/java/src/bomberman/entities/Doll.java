package src.bomberman.entities; 

import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet;
import src.bomberman.core.Game;       // Thêm import Game

import java.util.List;

public class Doll extends Enemy {
    // Tốc độ animation: Số frame update cho mỗi sprite của Doll
    private final int ANIMATION_SPEED = 15;
    private int animationFrameIndex = 0; // Chỉ số frame animation hiện tại (0, 1, 2)

    private static final double DOLL_MIN_SPEED = 0.4; // Tốc độ chậm nhất của Doll
    private static final double DOLL_MAX_SPEED = 0.9; // Tốc độ nhanh nhất của Doll
    private int stepsBeforeChangeSpeedDoll = 180; // Doll thay đổi tốc độ thường xuyên hơn hoặc ít hơn
    private int speedChangeCounterDoll = 0;

    /**
     * Constructor cho Doll.
     * @param xTile Tọa độ ô X ban đầu.
     * @param yTile Tọa độ ô Y ban đầu.
     * @param nesSheet SpriteSheet chứa hình ảnh của Doll.
     * @param game Tham chiếu đến đối tượng Game.
     */
    public Doll(double xTile, double yTile, SpriteSheet nesSheet, Game game) {
        super(xTile, yTile, nesSheet, game); // Gọi constructor của Enemy, truyền game vào
        initializeSpeed(); // Tốc độ di chuyển của Doll
        // Đặt sprite ban đầu dựa trên hướng ngẫu nhiên được chọn trong Enemy constructor
        setSpriteBasedOnDirectionAndFrame();
        if (this.sprite == null) {
            System.err.println("CRITICAL WARNING: Initial Doll sprite is null!");
        }
    }

    protected void initializeSpeed() {
        // Doll sẽ có tốc độ ngẫu nhiên trong khoảng riêng của nó khi được tạo
        randomizeDollSpeed();
    }

    /**
     * Đặt tốc độ ngẫu nhiên cho Doll trong khoảng DOLL_MIN_SPEED và DOLL_MAX_SPEED.
     */
    private void randomizeDollSpeed() {
        this.speed = DOLL_MIN_SPEED + (DOLL_MAX_SPEED - DOLL_MIN_SPEED) * random.nextDouble();
        // System.out.println("Doll new speed: " + String.format("%.2f", this.speed)); // Debug
    }

    @Override
    public void update(double deltaTime, List<Entity> entities) {
        super.update(deltaTime, entities);

        if (isAlive() && !isDying()) {
            // --- LOGIC THAY ĐỔI TỐC ĐỘ RIÊNG CỦA DOLL ---
            speedChangeCounterDoll++;
            if (speedChangeCounterDoll >= stepsBeforeChangeSpeedDoll) {
                if (random.nextDouble() < 0.6) { // 60% cơ hội Doll thay đổi tốc độ
                    randomizeDollSpeed();
                }
                speedChangeCounterDoll = 0; // Reset bộ đếm
            }
        }
    }

    /**
     * Cập nhật sprite animation dựa trên hướng di chuyển và frame hiện tại.
     * Được gọi từ Enemy.update() -> this.updateAnimation().
     */
    @Override
    protected void updateAnimation() {
        animationCounter++; // animationCounter được kế thừa từ Enemy
        if (animationCounter >= ANIMATION_SPEED) {
            animationCounter = 0;
            animationFrameIndex = (animationFrameIndex + 1) % 3; // Lặp 3 frame (0, 1, 2)
            setSpriteBasedOnDirectionAndFrame(); // Cập nhật sprite
        }
    }

    /**
     * Helper method để đặt sprite dựa trên hướng và frame animation hiện tại.
     */
    private void setSpriteBasedOnDirectionAndFrame() {
        Sprite targetSprite = null;
        if (currentDirection == Direction.LEFT || currentDirection == Direction.UP) {
            if (animationFrameIndex == 0) targetSprite = Sprite.enemy_doll_left1;
            else if (animationFrameIndex == 1) targetSprite = Sprite.enemy_doll_left2;
            else targetSprite = Sprite.enemy_doll_left3;
        } else { // RIGHT, DOWN, hoặc NONE (mặc định là phải)
            if (animationFrameIndex == 0) targetSprite = Sprite.enemy_doll_right1;
            else if (animationFrameIndex == 1) targetSprite = Sprite.enemy_doll_right2;
            else targetSprite = Sprite.enemy_doll_right3;
        }

        this.sprite = targetSprite;
        if (this.sprite == null) {
            System.err.println("Warning: Minvo sprite is null for direction " + currentDirection +
                    ", frame " + animationFrameIndex + ". Defaulting to left1.");
            this.sprite = Sprite.enemy_doll_left1; // Fallback cuối cùng
            if (this.sprite == null) {
                System.err.println("CRITICAL ERROR: Default Minvo sprite (enemy_doll_left1) is also null!");
            }
        }
        this.sprite = targetSprite;
        if (this.sprite == null) { this.sprite = Sprite.enemy_doll_right1; /* Log lỗi */ }
    }

    /**
     * Ghi đè die() để sử dụng animation chết cụ thể của Doll hoặc mob_dead chung.
     */
    @Override
    public void destroy() {
        if (isAlive() && !isDying()) { // Kiểm tra cả isDying từ Enemy
            // System.out.println("Doll is dying!");
            // this.sprite = Sprite.enemy_ballom_dead; // Đặt sprite chết cụ thể nếu có
            // Hoặc để handleDeathAnimation của lớp Enemy xử lý với mob_dead
            super.destroy(); // Gọi die của Enemy để đặt cờ dying = true
        }
    }
}