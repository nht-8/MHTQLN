// src/main/java/src/bomberman/entities/Player.java
package src.bomberman.entities; // Hoặc uet.oop.bomberman.entities

// Import các lớp cần thiết
import javafx.geometry.Rectangle2D; // Vẫn cần cho checkCollision
import javafx.scene.input.KeyCode;
import src.bomberman.Config;
import src.bomberman.core.Game;
import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet;
import src.bomberman.input.InputHandler;
import src.bomberman.sound.SoundManager;

import java.util.List;

public class Player extends Entity {

    private double speed = Config.PLAYER_SPEED;
    private int bombCapacity = Config.PLAYER_INIT_BOMBS;
    private int currentPlacedBombs = 0;
    private int flameLength = Config.PLAYER_INIT_FLAMES;
    private InputHandler input;
    private Game game;

    private Direction currentDirection = Direction.DOWN;
    private boolean moving = false;
    private int animationCounter = 0;
    private int animationFrameIndex = 0; // 0, 1, 2 (cho 3 frame animation)
    private final int ANIMATION_SPEED = 8; // Tốc độ chuyển frame

    private int deathTimer = 0;
    private final int DEATH_ANIMATION_DURATION = 60; // Thời gian animation chết (khoảng 1 giây)
    private boolean dying = false; // Cờ cho trạng thái đang chạy animation chết

    private final double COLLISION_BOUNDS_INSET = 4.0;

    private int initialTileX;
    private int initialTileY;
    private boolean justPermanentlyDead=false;
    /**
     * Constructor cho Player.
     * @param xTile Tọa độ ô X ban đầu.
     * @param yTile Tọa độ ô Y ban đầu.
     * @param modernSheet SpriteSheet chứa hình ảnh của Player (modern_spritesheet).
     * @param input InputHandler để nhận điều khiển.
     * @param game Tham chiếu đến đối tượng Game.
     */
    public Player(double xTile, double yTile, SpriteSheet modernSheet, InputHandler input, Game game) {
        super(xTile, yTile, modernSheet);
        this.input = input;
        this.game = game;
        this.sprite = Sprite.player_d1;
        if (this.sprite == null) {
            System.err.println("CRITICAL WARNING: Initial Player sprite (player_d1) is null!");
        }
        // Lưu vị trí ban đầu
        this.initialTileX = (int) xTile;
        this.initialTileY = (int) yTile;
    }

    @Override
    public void update(double deltaTime, List<Entity> entities) {
        if (!alive&&!dying) { // Nếu đã chết hẳn (sau animation)
            return; // Không làm gì cả
        }
        if (dying) { // Nếu đang trong quá trình chạy animation chết
            handleDeathAnimation();
            deathTimer++;
            if (deathTimer > DEATH_ANIMATION_DURATION + 30) { // Chờ thêm 0.5s sau animation
                alive = false;
                // Đánh dấu là chết hẳn
                System.out.println("Player permanently dead (handle game over logic in Game class)");
            }
            return;
        }

        // Nếu còn sống và không đang dying, xử lý input và đặt bom
        handleInputAndMovement(entities);
        handleBombPlacement();
    }

    public boolean isJustPermanentlyDeadAndDecrementLife() {
        if (justPermanentlyDead) {
            // justPermanentlyDead = false; // Game sẽ reset cờ này sau khi xử lý
            return true;
        }
        return false;
    }


    public void bombExploded(){
        currentPlacedBombs = Math.max(0,currentPlacedBombs-1);
    }
    private void handleInputAndMovement(List<Entity> entities) {
        double dx = 0;
        double dy = 0;
        boolean requestedMove = false;

        if (input.isPressed(KeyCode.UP) || input.isPressed(KeyCode.W)) {
            dy = -speed; currentDirection = Direction.UP; requestedMove = true;
        } else if (input.isPressed(KeyCode.DOWN) || input.isPressed(KeyCode.S)) {
            dy = speed; currentDirection = Direction.DOWN; requestedMove = true;
        } else if (input.isPressed(KeyCode.LEFT) || input.isPressed(KeyCode.A)) {
            dx = -speed; currentDirection = Direction.LEFT; requestedMove = true;
        } else if (input.isPressed(KeyCode.RIGHT) || input.isPressed(KeyCode.D)) {
            dx = speed; currentDirection = Direction.RIGHT; requestedMove = true;
        }

        if (requestedMove) {
            moving = true;
            move(dx, dy, entities);
            updateMovingAnimation();
        } else {
            moving = false;
            setStandingSprite();
            animationCounter = 0;
            animationFrameIndex = 0;
        }
    }

    private void handleBombPlacement() {
        if (input.isPressed(KeyCode.SPACE) || input.isPressed(KeyCode.X)) {
            placeBomb();
            input.releaseKey(KeyCode.SPACE);
            input.releaseKey(KeyCode.X);
        }
    }

    private void move(double dx, double dy, List<Entity> entities) {
        if (dx == 0 && dy == 0) return;

        double oldX = x;
        x += dx;
        if (checkCollision(entities)) {
            x = oldX;
        }

        double oldY = y;
        y += dy;
        if (checkCollision(entities)) {
            y = oldY;
        }
        // Giới hạn trong map
        if (game != null && game.getLevel() != null) { // Cần tham chiếu level từ game
            x = Math.max(0, Math.min(x, game.getLevel().getWidth() * Config.TILE_SIZE - getWidth()));
            y = Math.max(0, Math.min(y, game.getLevel().getHeight() * Config.TILE_SIZE - getHeight()));
        }
    }
    /**
     * Reset cờ justPermanentlyDead. Được gọi bởi Game sau khi đã xử lý.
     */
    public void consumePermanentlyDeadFlag() {
        this.justPermanentlyDead = false;
    }


    private boolean checkCollision(List<Entity> entities) {
        Rectangle2D playerBounds = this.getBounds();
        for (Entity entity : entities) {
            if (entity == this || !entity.isAlive()) continue;
            if (entity.isSolid()) { // Kiểm tra với các entity rắn (Wall, Brick, Bomb chưa nổ)
                if (playerBounds.intersects(entity.getBounds())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void updateMovingAnimation() {
        animationCounter++;
        if (animationCounter >= ANIMATION_SPEED) {
            animationCounter = 0;
            // Player có thể có 3 hoặc 4 frame animation. Giả sử 3 frame (0, 1, 2)
            animationFrameIndex = (animationFrameIndex + 1) % 3; // Lặp 3 frame _1, _2, _3

            switch (currentDirection) {
                case UP:
                    if (animationFrameIndex == 0) sprite = Sprite.player_u1;
                    else if (animationFrameIndex == 1) sprite = Sprite.player_u2;
                    else sprite = Sprite.player_u3;
                    // else sprite = Sprite.player_u4; // Nếu có frame 4
                    break;
                case DOWN:
                    if (animationFrameIndex == 0) sprite = Sprite.player_d1;
                    else if (animationFrameIndex == 1) sprite = Sprite.player_d2;
                    else sprite = Sprite.player_d3;
                    // else sprite = Sprite.player_d4;
                    break;
                case LEFT:
                    if (animationFrameIndex == 0) sprite = Sprite.player_l1;
                    else if (animationFrameIndex == 1) sprite = Sprite.player_l2;
                    else sprite = Sprite.player_l3;
                    // else sprite = Sprite.player_l4;
                    break;
                case RIGHT:
                    if (animationFrameIndex == 0) sprite = Sprite.player_r1;
                    else if (animationFrameIndex == 1) sprite = Sprite.player_r2;
                    else sprite = Sprite.player_r3;
                    // else sprite = Sprite.player_r4;
                    break;
                default: setStandingSprite(); break;
            }
            if (sprite == null) {
                System.err.println("Warning: Player moving sprite is null. Dir: " + currentDirection + ", Frame: " + animationFrameIndex);
                setStandingSprite();
            }
        }
    }

    private void setStandingSprite() {
        animationFrameIndex = 0; // Khi đứng yên, luôn là frame đầu
        Sprite targetSprite = null;
        switch (currentDirection) {
            case UP: targetSprite = Sprite.player_u1; break;
            case DOWN: targetSprite = Sprite.player_d1; break;
            case LEFT: targetSprite = Sprite.player_l1; break;
            case RIGHT: targetSprite = Sprite.player_r1; break;
            default: targetSprite = Sprite.player_d1;
        }
        this.sprite = targetSprite;
        if (this.sprite == null) {
            System.err.println("Warning: Player standing sprite is null. Dir: " + currentDirection);
            this.sprite = Sprite.player_d1; // Fallback
            if (this.sprite == null)  System.err.println("CRITICAL ERROR: Default player_d1 is null!");
        }
    }

    private void placeBomb() {
        if (currentPlacedBombs < bombCapacity && game != null) {
            int tileX = getTileX();
            int tileY = getTileY();
            if (game.canPlaceBombAt(tileX, tileY)) {
                // Bomb dùng nesSheet
                Bomb newBomb = new Bomb(tileX, tileY, game.getNesSheet(), Config.BOMB_TIMER, this.flameLength, this, game);
                game.addBomb(newBomb);
                currentPlacedBombs++;

                SoundManager.getInstance().playSound(SoundManager.PLACE_BOMB);
            }
        }
    }



    @Override
    public void destroy() {
        if (alive && !dying) {
            System.out.println("Player is dying!");
            dying = true;
            moving = false;
            animationCounter = 0; // Reset animation di chuyển
            deathTimer = 0;       // Reset animation chết
            sprite = Sprite.player_dead1; // Bắt đầu animation chết
            if (sprite == null) {
                System.err.println("ERROR: Sprite.player_dead1 is null in Player.destroy()!");
                sprite = Sprite.player_d1; // Fallback
            }
            SoundManager.getInstance().playSound(SoundManager.PLAYER_DEATH);
        }

    }

    private void handleDeathAnimation() {
        deathTimer++; // Tăng bộ đếm thời gian cho animation chết
        int deathFrameTime = DEATH_ANIMATION_DURATION / 3;

        Sprite targetSprite = null;
        if (deathTimer < deathFrameTime) {
            targetSprite = Sprite.player_dead1;
        } else if (deathTimer < deathFrameTime * 2) {
            targetSprite = Sprite.player_dead2;
        } else if (deathTimer <= DEATH_ANIMATION_DURATION) {
            targetSprite = Sprite.player_dead3;
        } else {
            // Animation đã hoàn tất
            targetSprite = Sprite.player_dead3; // Giữ frame cuối
            if (dying) { // Chỉ đặt cờ nếu thực sự đang trong quá trình dying
                this.dying = false; // Kết thúc trạng thái "đang chết"
                this.alive = false; // Đánh dấu là "không còn sống" (để Game xử lý)
                this.justPermanentlyDead = true; // Báo cho Game biết là đã chết hẳn
                System.out.println("Player death animation finished. Flags set: alive=false, dying=false, justPermanentlyDead=true");
            }
        }
        this.sprite = targetSprite;
        if (this.sprite == null) {
            System.err.println("Warning: Player dead animation sprite is null. deathTimer: " + deathTimer);
            this.sprite = Sprite.player_d1; // Fallback
        }
    }

    @Override
    public boolean isDying() {
        return dying; // Trả về trạng thái đang chạy animation chết
    }


    // Getters
    public int getFlameLength() { return flameLength; }

    @Override
    public Rectangle2D getBounds() {
        // Lấy kích thước trực quan của Player (đã scale nếu có)
        double visualWidth = super.getWidth(); // Gọi getWidth() của Entity
        double visualHeight = super.getHeight(); // Gọi getHeight() của Entity

        // Tính toán vùng bao va chạm nhỏ hơn
        double collisionX = x + COLLISION_BOUNDS_INSET;
        double collisionY = y + COLLISION_BOUNDS_INSET; // Có thể chỉ thu nhỏ theo chiều rộng
        double collisionWidth = visualWidth - (2 * COLLISION_BOUNDS_INSET);
        double collisionHeight = visualHeight - (COLLISION_BOUNDS_INSET); // Ví dụ chỉ thu nhỏ 1 bên ở Y

        // Đảm bảo width/height không âm
        collisionWidth = Math.max(1, collisionWidth);
        collisionHeight = Math.max(1, collisionHeight);

        return new Rectangle2D(collisionX, collisionY, collisionWidth, collisionHeight);

    }
    public void resetToStartPositionAndRevive() {
        this.x = initialTileX * Config.TILE_SIZE;
        this.y = initialTileY * Config.TILE_SIZE;
        this.alive = true;
        this.dying = false;
        this.justPermanentlyDead = false; // Quan trọng: reset cờ này
        this.deathTimer = 0;
        this.animationCounter = 0;
        this.currentDirection = Direction.DOWN;
        setStandingSprite();
        System.out.println("Player revived and reset to start position: (" + initialTileX + "," + initialTileY + ")");
    }
    public void setPermanentlyDeadNoUpdates() {
        this.alive = false;
        this.dying = false;
        this.justPermanentlyDead = false;
        // Bạn có thể thêm một cờ "permanentlyDead" nếu muốn bỏ qua hoàn toàn logic update
        // trong phương thức update() của Player.
    }
    public void setInitialPosition(int tileX, int tileY) {
        this.initialTileX = tileX;
        this.initialTileY = tileY;
    }
    // Power-up methods
    public void addBombCapacity(int amount) { this.bombCapacity += amount; }
    public void addFlameLength(int amount) { this.flameLength += amount; }
    public void addSpeed(double amount) { this.speed += amount; }
}