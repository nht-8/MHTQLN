package src.bomberman.entities;

import javafx.animation.PauseTransition; // << THÊM IMPORT
import javafx.application.Platform;     // << THÊM IMPORT
import javafx.geometry.Rectangle2D;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;            // << THÊM IMPORT
import src.bomberman.Config;
import src.bomberman.core.Game;
import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet;
import src.bomberman.input.InputHandler;
import src.bomberman.sound.SoundManager;

import java.util.List;

public class Player extends Entity {

    // --- Thuộc tính di chuyển và trạng thái ---
    private double speed = Config.PLAYER_SPEED;
    private Direction currentDirection = Direction.DOWN;
    private boolean moving = false;
    private InputHandler input;
    private Game game;

    // --- Thuộc tính Bom ---
    private int bombCapacity = Config.PLAYER_INIT_BOMBS;
    private int currentPlacedBombs = 0;
    private int flameLength = Config.PLAYER_INIT_FLAMES;

    // --- Thuộc tính Animation ---
    private int animationCounter = 0;
    private int animationFrameIndex = 0;
    private final int ANIMATION_SPEED = 8;

    // --- Thuộc tính trạng thái Chết/Hồi sinh ---
    private int deathTimer = 0;
    private final int DEATH_ANIMATION_DURATION = 60;
    private boolean dying = false;
    private boolean justPermanentlyDeadFlag = false;
    private boolean permanentlyDeadNoUpdates = false;

    // ---- THUỘC TÍNH BẤT TỬ ----
    private boolean invincible = false; // Cờ trạng thái bất tử
    private transient PauseTransition invincibilityTimer; // Timer để tắt bất tử (transient nếu có serialize)
    private final double INVINCIBILITY_DURATION_SECONDS = 2.0; // Thời gian bất tử (ví dụ: 2 giây)

    // --- Thuộc tính vị trí và va chạm ---
    private final double COLLISION_BOUNDS_INSET = 4.0;
    private double startTileX, startTileY;


    public Player(double xTile, double yTile, SpriteSheet modernSheet, InputHandler input, Game game) {
        super(xTile, yTile, modernSheet);
        this.input = input;
        this.game = game;
        this.sprite = Sprite.player_d1;
        if (this.sprite == null) {
            System.err.println("CRITICAL WARNING: Initial Player sprite (player_d1) is null!");
        }
        this.startTileX = xTile;
        this.startTileY = yTile;
    }

    @Override
    public void update(double deltaTime, List<Entity> entities) {
        if (permanentlyDeadNoUpdates) return;
        if (!alive && !dying) return;

        if (dying) {
            handleDeathAnimation();
            deathTimer++;
            if (deathTimer > DEATH_ANIMATION_DURATION + 15) {
                alive = false;
                dying = false;
                justPermanentlyDeadFlag = true;
                System.out.println("Player animation dead finished. Flag set for Game process.");
            }
            return;
        }

        // Nếu còn sống (alive = true, dying = false)
        handleInputAndMovement(entities);
        handleBombPlacement();

        // --- (Tùy chọn) Logic nhấp nháy khi bất tử ---
        // if (invincible) {
        //     // Ví dụ: nhấp nháy mỗi 5 frame update
        //     boolean showSprite = (animationCounter / 5) % 2 == 0;
        //     // Bạn cần một cách để lưu sprite gốc trước khi ẩn nó
        //     // và đặt sprite thành null hoặc một sprite trong suốt khi ẩn
        //     // Hoặc thay đổi opacity: this.opacity = showSprite ? 1.0 : 0.5; (Cần sửa render)
        // } else {
        //     // Đảm bảo sprite/opacity bình thường khi không bất tử
        //     // this.opacity = 1.0;
        // }
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
            // Chỉ đặt lại sprite đứng nếu không đang bất tử (hoặc xử lý trong logic nhấp nháy)
            if (!invincible) {
                setStandingSprite();
            }
            animationCounter = 0;
            animationFrameIndex = 0;
        }
    }

    private void handleBombPlacement() {
        if (alive && !dying && (input.isPressed(KeyCode.SPACE) || input.isPressed(KeyCode.X))) {
            placeBomb();
            input.releaseKey(KeyCode.SPACE);
            input.releaseKey(KeyCode.X);
        }
    }

    private void move(double dx, double dy, List<Entity> entities) {
        if (dx == 0 && dy == 0) return;
        double oldX = x;
        double oldY = y;
        x += dx;
        if (checkCollision(entities)) x = oldX;
        y += dy;
        if (checkCollision(entities)) y = oldY;
        confineToMapBounds();
    }

    private void confineToMapBounds() {
        if (game != null && game.getLevel() != null) {
            double mapPixelWidth = game.getLevel().getWidth() * Config.TILE_SIZE;
            double mapPixelHeight = game.getLevel().getHeight() * Config.TILE_SIZE;
            double playerWidth = getWidth();
            double playerHeight = getHeight();
            x = Math.max(0, Math.min(x, mapPixelWidth - playerWidth));
            y = Math.max(0, Math.min(y, mapPixelHeight - playerHeight));
        }
    }

    private boolean checkCollision(List<Entity> entities) {
        // Không kiểm tra va chạm nếu đang bất tử (có thể là một lựa chọn)
        // if (invincible) return false;

        Rectangle2D playerBounds = this.getBounds();
        for (Entity entity : entities) {
            if (entity == this || !entity.isAlive() || !entity.isSolid()) continue;
            if (playerBounds.intersects(entity.getBounds())) {
                return true;
            }
        }
        return false;
    }

    private void updateMovingAnimation() {
        animationCounter++;
        if (animationCounter >= ANIMATION_SPEED) {
            animationCounter = 0;
            animationFrameIndex = (animationFrameIndex + 1) % 3;
            // Nếu đang bất tử, có thể bỏ qua việc cập nhật sprite ở đây
            // và để logic nhấp nháy trong update() xử lý.
            if (!invincible) {
                setMovingSpriteBasedOnFrameAndDirection();
            }
        }
        // Cập nhật sprite ngay cả khi không đủ ANIMATION_SPEED nếu vừa hết bất tử
        if (!invincible && moving && this.sprite == null /* hoặc sprite đang là sprite nhấp nháy */) {
            setMovingSpriteBasedOnFrameAndDirection();
        }
    }

    /**
     * Helper method để đặt sprite di chuyển dựa trên frame và hướng hiện tại.
     * Tách ra từ updateMovingAnimation để dễ gọi lại.
     */
    private void setMovingSpriteBasedOnFrameAndDirection() {
        Sprite targetSprite = null;
        switch (currentDirection) {
            case UP:
                if (animationFrameIndex == 0) targetSprite = Sprite.player_u1;
                else if (animationFrameIndex == 1) targetSprite = Sprite.player_u2;
                else targetSprite = Sprite.player_u3;
                break;
            case DOWN:
                if (animationFrameIndex == 0) targetSprite = Sprite.player_d1;
                else if (animationFrameIndex == 1) targetSprite = Sprite.player_d2;
                else targetSprite = Sprite.player_d3;
                break;
            case LEFT:
                if (animationFrameIndex == 0) targetSprite = Sprite.player_l1;
                else if (animationFrameIndex == 1) targetSprite = Sprite.player_l2;
                else targetSprite = Sprite.player_l3;
                break;
            case RIGHT:
                if (animationFrameIndex == 0) targetSprite = Sprite.player_r1;
                else if (animationFrameIndex == 1) targetSprite = Sprite.player_r2;
                else targetSprite = Sprite.player_r3;
                break;
            default:
                targetSprite = Sprite.player_d1; // Fallback
                break;
        }
        this.sprite = targetSprite;
        if (sprite == null) {
            System.err.println("Warning: Player moving sprite is null after set. Dir: " + currentDirection + ", Frame: " + animationFrameIndex);
            setStandingSprite(); // Fallback
        }
    }


    private void setStandingSprite() {
        // Nếu đang bất tử, không đặt lại sprite (để hiệu ứng nhấp nháy kiểm soát)
        if (invincible) return;

        animationFrameIndex = 0;
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
            this.sprite = Sprite.player_d1;
            if (this.sprite == null)  System.err.println("CRITICAL ERROR: Default player_d1 is null!");
        }
    }

    private void placeBomb() {
        if (alive && !dying && currentPlacedBombs < bombCapacity && game != null) {
            int tileX = getTileX();
            int tileY = getTileY();
            if (game.canPlaceBombAt(tileX, tileY)) {
                Bomb newBomb = new Bomb(tileX, tileY, game.getModernSheet(), Config.BOMB_TIMER, this.flameLength, this, game);
                game.addBomb(newBomb);
                currentPlacedBombs++;
                SoundManager.getInstance().playSound(SoundManager.PLACE_BOMB);
            }
        }
    }

    public void bombExploded() {
        currentPlacedBombs = Math.max(0, currentPlacedBombs - 1);
    }

    @Override
    public void destroy() {
        // Chỉ chết nếu đang sống, không đang chết, VÀ KHÔNG BẤT TỬ
        if (alive && !dying && !invincible) {
            System.out.println("Player is dying!");
            dying = true;
            moving = false;
            animationCounter = 0;
            deathTimer = 0;
            sprite = Sprite.player_dead1;
            if (sprite == null) {
                System.err.println("Warning: Player_dead1 sprite is null.");
                sprite = Sprite.player_d1;
            }
            SoundManager.getInstance().playSound(SoundManager.PLAYER_DEATH);
        } else if (invincible) {
            System.out.println("Player was hit but is currently invincible.");
        }
    }


    private void handleDeathAnimation() {
        animationCounter++;
        int deathFrameTime = DEATH_ANIMATION_DURATION / 3;
        Sprite targetSprite = null;
        if (deathTimer < deathFrameTime) {
            targetSprite = Sprite.player_dead1;
        } else if (deathTimer < deathFrameTime * 2) {
            targetSprite = Sprite.player_dead2;
        } else {
            targetSprite = Sprite.player_dead3;
        }
        this.sprite = targetSprite;
        if (this.sprite == null) {
            System.err.println("Warning: Player dead animation sprite is null during animation.");
        }
    }

    @Override
    public boolean isDying() {
        return dying;
    }

    public boolean isJustPermanentlyDeadAndDecrementLife() {
        if (justPermanentlyDeadFlag) {
            justPermanentlyDeadFlag = false;
            return true;
        }
        return false;
    }

    public void resetToStartPositionAndRevive() {
        this.x = this.startTileX * Config.TILE_SIZE;
        this.y = this.startTileY * Config.TILE_SIZE;
        this.alive = true;
        this.dying = false;
        this.deathTimer = 0;
        this.animationCounter = 0;
        this.animationFrameIndex = 0;
        this.justPermanentlyDeadFlag = false;
        this.permanentlyDeadNoUpdates = false;
        this.currentDirection = Direction.DOWN;
        setStandingSprite(); // Đặt lại sprite đứng ban đầu

        // Kích hoạt bất tử
        activateInvincibility(INVINCIBILITY_DURATION_SECONDS);

        System.out.println("Player revived, reset to start position, and is invincible.");
    }

    /**
     * Kích hoạt trạng thái bất tử trong một khoảng thời gian.
     * @param durationSeconds Thời gian bất tử (giây).
     */
    private void activateInvincibility(double durationSeconds) {
        if (invincibilityTimer != null) {
            invincibilityTimer.stop(); // Đảm bảo dừng timer cũ nếu có
        }
        invincible = true;
        System.out.println("Player invincible for " + durationSeconds + " seconds.");
        // TODO: Bắt đầu hiệu ứng nhấp nháy sprite hoặc thay đổi opacity ở đây

        invincibilityTimer = new PauseTransition(Duration.seconds(durationSeconds));
        invincibilityTimer.setOnFinished(event -> {
            invincible = false;
            System.out.println("Player invincibility ended.");
            // TODO: Dừng hiệu ứng nhấp nháy và đảm bảo sprite/opacity trở lại bình thường
            // Đặt lại sprite phù hợp sau khi hết bất tử
            if (!moving) {
                setStandingSprite();
            } else {
                // Cần cập nhật lại sprite di chuyển phù hợp với frame/hướng hiện tại
                setMovingSpriteBasedOnFrameAndDirection();
            }
        });

        // Đảm bảo timer chạy trên luồng JavaFX
        if (Platform.isFxApplicationThread()) {
            invincibilityTimer.play();
        } else {
            Platform.runLater(() -> invincibilityTimer.play());
        }
    }


    public void setPermanentlyDeadNoUpdates() {
        this.permanentlyDeadNoUpdates = true;
        this.alive = false;
        this.dying = false;
        // Dừng timer bất tử nếu đang chạy khi game over
        if (invincibilityTimer != null) {
            invincibilityTimer.stop();
        }
    }

    // --- Các phương thức nhận Power-up ---
    public void addBombCapacity(int amount) { this.bombCapacity += amount; }
    public void addFlameLength(int amount) { this.flameLength += amount; }
    public void addSpeed(double amount) { this.speed = Math.max(0.5, this.speed + amount); }

    // --- Getters ---
    public int getFlameLength() { return flameLength; }
    public boolean isInvincible() { return invincible; } // Getter cho trạng thái bất tử

    /**
     * Trả về vùng bao va chạm (hitbox) của Player.
     */
    @Override
    public Rectangle2D getBounds() {
        double visualWidth = Config.TILE_SIZE; // Giả sử kích thước trực quan bằng ô
        double visualHeight = Config.TILE_SIZE;
        double collisionX = x + COLLISION_BOUNDS_INSET;
        double collisionY = y + COLLISION_BOUNDS_INSET * 1.5;
        double collisionWidth = visualWidth - (2 * COLLISION_BOUNDS_INSET);
        double collisionHeight = visualHeight - (COLLISION_BOUNDS_INSET * 2);
        collisionWidth = Math.max(1, collisionWidth);
        collisionHeight = Math.max(1, collisionHeight);
        return new Rectangle2D(collisionX, collisionY, collisionWidth, collisionHeight);
    }
}