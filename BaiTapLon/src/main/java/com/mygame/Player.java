package com.mygame;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.ArrayList;

import javafx.geometry.Rectangle2D; // Đảm bảo import này

public class Player {

    public enum Direction {UP, DOWN, LEFT, RIGHT}

    // --- Thuộc tính cơ bản ---
    private double x, y; // Tọa độ pixel (góc trên trái)
    private final int size; // Kích thước tile
    private double speed = 120.0; // Tốc độ di chuyển hiện tại

    // --- Trạng thái di chuyển & Hướng ---
    private boolean movingUp, movingDown, movingLeft, movingRight;
    private boolean isMoving = false;
    private Direction currentDirection = Direction.DOWN; // Hướng nhìn mặc định

    // --- Trạng thái Sống/Chết & Bất tử ---
    private int lives = 3; // Số mạng ban đầu
    private int score = 0; // Điểm số
    private boolean alive = true; // Còn sống không
    private boolean dying = false;  // Đang chạy animation chết?
    private boolean invincible = false; // Đang bất tử tạm thời? (bất tử là khi đag thứ hiê quá trình -1lives
    private double invincibilityTimer = 0; // Thời gian bất tử còn lại
    private final double INVINCIBILITY_DURATION = 1.5; // Thời gian bất tử sau khi mất mạng

    // --- Thuộc tính Power-up ---
    private int maxBombs = 1;           // Số lượng bom tối đa có thể đặt cùng lúc
    private int explosionRadius = 1;    // Bán kính vụ nổ (số ô lan ra mỗi hướng)
    private boolean canPassWall = false; // Có khả năng đi xuyên gạch (BRICK) không?
    // Giới hạn tối đa cho power-up (ví dụ)
    private final double BASE_SPEED = 120.0; // Tốc độ cơ bản khi không có power-up
    private final double MAX_SPEED = 240.0;  // Tốc độ di chuyển tối đa
    private final int MAX_BOMBS = 8;         // Số bom tối đa có thể nâng cấp
    private final int MAX_FLAMES = 8;        // Tầm nổ tối đa có thể nâng cấp

    // --- Animation Frames ---
    private List<Image> walkDownFrames;
    private List<Image> walkUpFrames;
    private List<Image> walkLeftFrames;
    private List<Image> walkRightFrames;
    private List<Image> deathFrames;
    // Animation State
    private int currentFrameIndex = 0; // Index cho animation đi bộ/đứng yên
    private double animationTimer = 0;
    private double animationFrameDuration = 0.1; // Tốc độ animation đi bộ
    // Death Animation State
    private int currentDeathFrameIndex = 0;
    private double deathAnimationTimer = 0;
    private double deathFrameDuration = 0.15; // Tốc độ animation chết
    private double deathAnimationTotalTime; // Tính trong constructor


    // --- Constructor ---
    public Player(double startGridX, double startGridY, int tileSize,
                  List<Image> downFrames, List<Image> upFrames,
                  List<Image> leftFrames, List<Image> rightFrames,
                  List<Image> deathAnimationFrames) {
        this.size = tileSize;
        this.x = startGridX * tileSize;
        this.y = startGridY * tileSize;
        this.speed = BASE_SPEED; // Bắt đầu với tốc độ gốc

        this.walkDownFrames = ensureList(downFrames);
        this.walkUpFrames = ensureList(upFrames);
        this.walkLeftFrames = ensureList(leftFrames);
        this.walkRightFrames = ensureList(rightFrames);
        this.deathFrames = ensureList(deathAnimationFrames);

        // Tính tổng thời gian animation chết dựa trên số frame và duration
        this.deathAnimationTotalTime = this.deathFrames.isEmpty() ? 0.6 : this.deathFrames.size() * this.deathFrameDuration;

    }

    // Hàm helper đảm bảo list không null
    private List<Image> ensureList(List<Image> list) {
        return (list != null) ? list : new ArrayList<>();
    }

    public void update(double dT, int[][] boardData) {
        if (dying) {
            updateDeathAnimation(dT);
            return;
        }
        if (!alive) return;
        if (invincible) {
            invincibilityTimer -= dT;
            if (invincibilityTimer <= 0) invincible = false;
        }

        boolean prevMoving = isMoving;
        isMoving = movingUp || movingDown || movingLeft || movingRight;
        Direction intendedDir = currentDirection;
        // Khai báo và tính toán vị trí tiềm năng
        double potentialX = x;
        double potentialY = y;
        double distance = speed * dT;

        if (movingUp) {
            potentialY -= distance;
            intendedDir = Direction.UP;
        } else if (movingDown) {
            potentialY += distance;
            intendedDir = Direction.DOWN;
        }
        if (movingLeft) {
            potentialX -= distance;
            intendedDir = Direction.LEFT;
        } else if (movingRight) {
            potentialX += distance;
            intendedDir = Direction.RIGHT;
        }
        if (isMoving) currentDirection = intendedDir;

        // Cập nhật animation đi bộ
        List<Image> curFrames = getCurrentDirectionFrames();
        if (isMoving) {
            animationTimer += dT;
            if (animationTimer >= animationFrameDuration) {
                animationTimer -= animationFrameDuration;
                currentFrameIndex = (currentFrameIndex + 1) % (curFrames.isEmpty() ? 1 : curFrames.size());
            }
        } else {
            currentFrameIndex = 0;
            animationTimer = 0;
        }

        // --- Kiểm tra va chạm và cập nhật vị trí (ĐÃ SỬA) ---
        boolean canMoveResult = GameController.isValidMove(potentialX, potentialY, size, boardData, this.canPassWall); // Dùng potentialX, potentialY

        if (canMoveResult) {
            // Di chuyển đến vị trí tiềm năng nếu hợp lệ
            x = potentialX; // Dùng potentialX
            y = potentialY; // Dùng potentialY
        } else {
            // Thử trượt tường nếu di chuyển trực tiếp bị chặn
            boolean canMoveX = GameController.isValidMove(potentialX, y, size, boardData, this.canPassWall); // Dùng potentialX, y hiện tại
            boolean canMoveY = GameController.isValidMove(x, potentialY, size, boardData, this.canPassWall); // Dùng x hiện tại, potentialY

            // Chỉ di chuyển theo trục X nếu chỉ nhấn ngang VÀ di chuyển X hợp lệ
            if ((movingLeft || movingRight) && !(movingUp || movingDown) && canMoveX) {
                x = potentialX; // Dùng potentialX
            }
            // Chỉ di chuyển theo trục Y nếu chỉ nhấn dọc VÀ di chuyển Y hợp lệ
            else if ((movingUp || movingDown) && !(movingLeft || movingRight) && canMoveY) {
                y = potentialY; // Dùng potentialY
            }
            // Nếu không thể trượt theo cả X và Y thì đứng yên (không làm gì cả)
        }
        // --- Kết thúc sửa lỗi ---
    }

    // Cập nhật animation chết
    private void updateDeathAnimation(double dT) {
        deathAnimationTimer += dT;
        if (deathAnimationTimer < deathAnimationTotalTime && !deathFrames.isEmpty()) {
            currentDeathFrameIndex = Math.min((int) (deathAnimationTimer / deathFrameDuration), deathFrames.size() - 1);
        } else {
            // Kết thúc animation
            dying = false; // Không còn đang chết nữa
            alive = false; // Đã chết hẳn
            System.out.println("Player is officially dead.");
        }
    }

    // --- Render ---
    public void render(GraphicsContext gc) {
        if (dying) {
            renderDeathAnimation(gc);
            return;
        }
        if (!alive) {
            return;
        } // Không vẽ nếu đã chết hẳn

        List<Image> currentFrames = getCurrentDirectionFrames();
        Image imageToRender = null;
        if (!currentFrames.isEmpty()) {
            // Đảm bảo index không bao giờ vượt quá giới hạn (dùng modulo)
            imageToRender = currentFrames.get(currentFrameIndex % currentFrames.size());
        }

        // Hiệu ứng nhấp nháy khi bất tử
        if (invincible && (System.currentTimeMillis() / 100) % 2 == 0) { // Nhấp nháy đơn giản
            // Không vẽ gì cả trong frame này để tạo hiệu ứng nhấp nháy
        } else {
            gc.drawImage(imageToRender, x, y, size, size); // Vẽ bình thường
        }

    }

    // Vẽ animation chết
    private void renderDeathAnimation(GraphicsContext gc) {
        if (!deathFrames.isEmpty() && currentDeathFrameIndex >= 0 && currentDeathFrameIndex < deathFrames.size()) {
            Image deathFrame = deathFrames.get(currentDeathFrameIndex);
            if (deathFrame != null) gc.drawImage(deathFrame, x, y, size, size);
            else {
                gc.setFill(Color.DARKRED);
                gc.fillRect(x, y, size, size);
            }
        } else {
            gc.setFill(Color.DARKRED);
            gc.fillRect(x, y, size, size);
        }
    }


    // --- Actions ---
    // Xử lý mất mạng
    public boolean loseLife() {
        if (alive && !dying && !invincible) { // Chỉ mất mạng nếu hợp lệ
            lives--;
            System.out.println("Player lost a life! Lives remaining: " + lives);
            if (lives <= 0) {
                startDeathAnimation(); // Bắt đầu chết nếu hết mạng
            } else {
                invincible = true; // Kích hoạt bất tử
                invincibilityTimer = INVINCIBILITY_DURATION;
            }
            return true; // Đã mất mạng thành công
        }
        return false; // Không mất mạng (đang bất tử hoặc đang chết)
    }

    // Bắt đầu animation chết
    private void startDeathAnimation() {
        if (alive && !dying) {
            System.out.println("Player starting death animation...");
            dying = true; // Đặt trạng thái đang chết
            isMoving = false; // Ngừng di chuyển
            movingUp = movingDown = movingLeft = movingRight = false; // Clear input flags
            currentDeathFrameIndex = 0; // Reset animation chết
            deathAnimationTimer = 0;
        }
    }

    // Cộng điểm
    public void addScore(int points) {
        if (alive) this.score += points;
    }

    // Áp dụng hiệu ứng vật phẩm
    public void applyPowerUp(PowerUpType type) {
        if (!alive || dying) return; // Không áp dụng nếu đang chết
        System.out.println("Player collected PowerUp: " + type);
        switch (type) {
            case BOMBS:
                if (maxBombs < MAX_BOMBS) maxBombs++; // Tăng số bom tối đa, có giới hạn
                System.out.println(" -> Max Bombs: " + maxBombs);
                break;
            case FLAMES:
                if (explosionRadius < MAX_FLAMES) explosionRadius++; // Tăng tầm nổ, có giới hạn
                System.out.println(" -> Flames: " + explosionRadius);
                break;
            case SPEED:
                speed = Math.min(speed + 20.0, MAX_SPEED); // Tăng tốc độ, có giới hạn
                System.out.println(" -> Speed: " + String.format("%.1f", speed));
                break;
            case WALL_PASS:
                canPassWall = true; // Kích hoạt đi xuyên tường
                System.out.println(" -> Wall Pass Activated!");
                // TODO: Có thể thêm timer cho Wall Pass nếu muốn nó hết hạn
                break;
            // Thêm các case khác cho BOMB_PASS, DETONATOR nếu làm
        }
        // TODO: Play sound effect
    }


    // --- Getters & Setters ---
    private List<Image> getCurrentDirectionFrames() {
        switch (currentDirection) {
            case UP:
                return walkUpFrames;
            case DOWN:
                return walkDownFrames;
            case LEFT:
                return walkLeftFrames;
            case RIGHT:
                return walkRightFrames;
            default:
                return walkDownFrames;
        }
    }

    public void setMovingUp(boolean m) {
        this.movingUp = m;
    }

    public void setMovingDown(boolean m) {
        this.movingDown = m;
    }

    public void setMovingLeft(boolean m) {
        this.movingLeft = m;
    }

    public void setMovingRight(boolean m) {
        this.movingRight = m;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getLives() {
        return lives;
    }

    public int getScore() {
        return score;
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean isInvincible() {
        return invincible;
    }

    public boolean isDying() {
        return dying;
    }

    public Rectangle2D getHitbox() {
        double s = size * 0.7;
        double o = (size - s) / 2.0;
        return new Rectangle2D(x + o, y + o, s, s);
    }

    public int getMaxBombs() {
        return maxBombs;
    } // Getter số bom tối đa

    public int getExplosionRadius() {
        return explosionRadius;
    } // Getter bán kính nổ

    public boolean canPassWall() {
        return canPassWall;
    } // Getter cho Wall Pass
}