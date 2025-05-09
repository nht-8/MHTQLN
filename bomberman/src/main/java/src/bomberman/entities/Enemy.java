// src/main/java/src/bomberman/entities/Enemy.java
package src.bomberman.entities; // Hoặc uet.oop.bomberman.entities

import javafx.geometry.Rectangle2D;
import src.bomberman.Config;
import src.bomberman.core.Game;
import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet;
import src.bomberman.core.Level; // Cần Level để kiểm tra isSolidTile
import src.bomberman.sound.SoundManager;

import java.util.List;
import java.util.Random;

public abstract class Enemy extends Entity {

    protected double speed = 0.5; // Tốc độ mặc định
    protected Direction currentDirection;
    protected Random random = new Random();
    protected int moveCounter = 0; // Đếm bước di chuyển theo một hướng
    protected int stepsBeforeChangeDirection = 60; // Số bước trước khi có thể đổi hướng
    protected int animationCounter = 0; // Bộ đếm cho animation
    protected Game game; // Tham chiếu đến Game để truy cập Level

    private boolean dying = false; // Trạng thái đang chạy animation chết
    private int deathTimer = 0; // Bộ đếm cho animation chết
    private final int DEATH_ANIMATION_DURATION = 45; // Khoảng 0.75 giây

    /**
     * Constructor cho Enemy.
     * @param xTile Tọa độ ô X ban đầu.
     * @param yTile Tọa độ ô Y ban đầu.
     * @param nesSheet SpriteSheet chứa hình ảnh của Enemy (thường là nesSheet).
     * @param game Tham chiếu đến đối tượng Game.
     */
    public Enemy(double xTile, double yTile, SpriteSheet nesSheet, Game game) {
        super(xTile, yTile, nesSheet); // Truyền nesSheet cho Entity
        this.game = game;
        changeDirectionRandomly();
        // Sprite ban đầu sẽ được set trong constructor của lớp con (ví dụ: Ballom)
    }

    @Override
    public void update(double deltaTime, List<Entity> entities) {
        if (!alive && !dying) return; // Đã chết hẳn
        if (dying) {
            handleDeathAnimation();
            deathTimer++;
            if (deathTimer > DEATH_ANIMATION_DURATION) {
                alive = false;
                dying = false;// Đánh dấu chết hẳn sau animation
            }
            return;
        }

        // Nếu còn sống và không đang dying
        move(deltaTime, entities);
        updateAnimation(); // Lớp con sẽ cài đặt animation
    }

    protected void move(double deltaTime, List<Entity> entities) {
        if (dying || !alive) return; // Không di chuyển khi đang chết hoặc đã chết

        double dx = 0;
        double dy = 0;

        switch (currentDirection) {
            case UP: dy = -speed; break;
            case DOWN: dy = speed; break;
            case LEFT: dx = -speed; break;
            case RIGHT: dx = speed; break;
            case NONE: return; // Không di chuyển nếu không có hướng
        }

        double nextX = x + dx;
        double nextY = y + dy;

        // Kiểm tra va chạm trước khi thực sự di chuyển
        if (canMoveTo(nextX, nextY, entities)) {
            x = nextX;
            y = nextY;
            moveCounter++;
            // Cân nhắc đổi hướng sau một khoảng thời gian/số bước ngẫu nhiên
            if (moveCounter > stepsBeforeChangeDirection && random.nextInt(100) < 10) { // 10% cơ hội đổi hướng
                changeDirectionRandomly();
            }
        } else {
            // Nếu không di chuyển được (va chạm), đổi hướng ngay lập tức
            changeDirectionRandomly();
        }
    }

    /**
     * Kiểm tra xem Enemy có thể di chuyển đến vị trí (targetX, targetY) không.
     * Bao gồm kiểm tra va chạm với map tiles và các entity rắn khác.
     */
    protected boolean canMoveTo(double targetX, double targetY, List<Entity> entities) {
        // Lấy kích thước hiện tại của Enemy để tính toán vùng bao
        double currentWidth = getWidth(); // Sẽ là Config.TILE_SIZE
        double currentHeight = getHeight(); // Sẽ là Config.TILE_SIZE

        // Tạo vùng bao giả định ở vị trí mới
        Rectangle2D nextBounds = new Rectangle2D(targetX, targetY, currentWidth, currentHeight);

        // 1. Kiểm tra va chạm với map tiles (Wall, Brick)
        Level currentLevel = game.getLevel();
        if (currentLevel != null) {
            // Kiểm tra 4 góc của vùng bao mới với map
            // (Lưu ý: tọa độ pixel của Entity là góc trên trái)
            int topLeftTileX = (int) targetX / Config.TILE_SIZE;
            int topLeftTileY = (int) targetY / Config.TILE_SIZE;
            int topRightTileX = (int) (targetX + currentWidth - 0.1) / Config.TILE_SIZE; // Trừ 1 epsilon để không bị lỗi làm tròn
            int topRightTileY = topLeftTileY;
            int bottomLeftTileX = topLeftTileX;
            int bottomLeftTileY = (int) (targetY + currentHeight - 0.1) / Config.TILE_SIZE;
            int bottomRightTileX = topRightTileX;
            int bottomRightTileY = bottomLeftTileY;

            if (currentLevel.isSolidTile(topLeftTileX, topLeftTileY) ||
                    currentLevel.isSolidTile(topRightTileX, topRightTileY) ||
                    currentLevel.isSolidTile(bottomLeftTileX, bottomLeftTileY) ||
                    currentLevel.isSolidTile(bottomRightTileX, bottomRightTileY)) {
                return false; // Va chạm với tường hoặc gạch trên map
            }
        } else {
            return false; // Không có level, không di chuyển được
        }

        // 2. Kiểm tra va chạm với các Entity rắn khác (ví dụ: Bomb)
        for (Entity entity : entities) {
            if (entity == this || !entity.isAlive() || !entity.isSolid()) continue; // Bỏ qua chính mình, entity chết, entity không rắn

            if (nextBounds.intersects(entity.getBounds())) {
                return false; // Va chạm với một entity rắn khác
            }
        }
        return true; // Có thể di chuyển đến vị trí đó
    }


    protected void changeDirectionRandomly() {
        int i = random.nextInt(4); // 0=UP, 1=DOWN, 2=LEFT, 3=RIGHT
        Direction newDirection;
        switch (i) {
            case 0: newDirection = Direction.UP; break;
            case 1: newDirection = Direction.DOWN; break;
            case 2: newDirection = Direction.LEFT; break;
            default: newDirection = Direction.RIGHT; break;
        }
        // Chỉ đổi hướng nếu hướng mới khác hướng cũ để tránh kẹt
        if (newDirection != currentDirection) {
            currentDirection = newDirection;
        }
        moveCounter = 0; // Reset bộ đếm mỗi khi cố gắng đổi hướng
    }

    // Phương thức cập nhật animation (trừu tượng, lớp con phải cài đặt)
    protected abstract void updateAnimation();

    @Override
    public void destroy() {
        if (alive && !dying) { // Chỉ chết một lần
            System.out.println(this.getClass().getSimpleName() + " is dying!");
            dying = true; // Bắt đầu trạng thái "đang chết"
            animationCounter = 0; // Reset cho animation chết
            deathTimer = 0;
            // Lớp con có thể đặt sprite chết đầu tiên ở đây nếu muốn,
            // hoặc handleDeathAnimation sẽ tự xử lý
        }
        SoundManager.getInstance().playSound(SoundManager.ENEMY_DEATH);
    }

    /**
     * Xử lý animation khi Enemy đang trong trạng thái chết.
     */
    protected void handleDeathAnimation() {
        animationCounter++;
        int deathFrameTime = DEATH_ANIMATION_DURATION / 3; // Giả sử có 3 frame chết (mob_dead1/2/3)

        Sprite targetSprite = null;
        if (animationCounter < deathFrameTime) {
            targetSprite = Sprite.mob_dead1;
        } else if (animationCounter < deathFrameTime * 2) {
            targetSprite = Sprite.mob_dead2;
        } else if (animationCounter <= DEATH_ANIMATION_DURATION) {
            targetSprite = Sprite.mob_dead3;
        } else {
            targetSprite = Sprite.mob_dead3; // Giữ frame cuối
        }
        this.sprite = targetSprite;
        if (this.sprite == null) {
            System.err.println("Warning: Enemy death animation sprite is null.");
            // Có thể không làm gì để enemy biến mất nếu không có sprite chết
        }
    }

    @Override
    public boolean isDying() {
        return dying;
    }

    /**
     * Enemies thường không phải là vật cản rắn với nhau hoặc với Player,
     * nhưng chúng bị chặn bởi Wall, Brick, Bomb.
     */
    @Override
    public boolean isSolid() {
        return false; // Enemy không chặn đường các entity khác theo mặc định
    }
}