// src/main/java/src/bomberman/entities/Oneal.java
package src.bomberman.entities;

import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet;
import src.bomberman.core.Game;

import java.util.List;
// import java.util.Random; // Random đã có trong Enemy

public class Oneal extends Enemy {
    private final int ANIMATION_SPEED = 15;
    private int animationFrameIndex = 0;

    // Tốc độ của Oneal khi đuổi theo Player (có thể nhanh hơn các Enemy khác)
    private static final double ONEAL_CHASE_SPEED = 0.75;


    // Khoảng cách (số ô) để Oneal bắt đầu đuổi theo Player
    private static final int CHASE_RADIUS_TILES = 5;
    private boolean isChasing = false;

    public Oneal(double xTile, double yTile, SpriteSheet nesSheet, Game game) {
        super(xTile, yTile, nesSheet, game);
        this.speed = 0.5;
        setSpriteBasedOnDirectionAndFrame();
        if (this.sprite == null) {}
    }

    /**
     * Ghi đè phương thức move của Enemy để cài đặt AI đuổi theo Player.
     */
    @Override
    protected void move(double deltaTime, List<Entity> entities) {
        if (isDying() || !alive) return;

        Player player = game.getPlayer();
        if (player == null || !player.isAlive()) {
            isChasing = false;
            super.move(deltaTime, entities);
            return;
        }

        double distanceToPlayerX = Math.abs(player.getTileX() - this.getTileX());
        double distanceToPlayerY = Math.abs(player.getTileY() - this.getTileY());
        double totalDistance = Math.sqrt(distanceToPlayerX * distanceToPlayerX + distanceToPlayerY * distanceToPlayerY);

        Direction preferredDirection = Direction.NONE;

        if (totalDistance <= CHASE_RADIUS_TILES) {
            isChasing = true;
            this.speed = ONEAL_CHASE_SPEED;

            int playerTileX = player.getTileX();
            int playerTileY = player.getTileY();
            int myTileX = this.getTileX();
            int myTileY = this.getTileY();

            if (Math.abs(playerTileX - myTileX) > Math.abs(playerTileY - myTileY)) {
                if (playerTileX < myTileX) preferredDirection = Direction.LEFT;
                else if (playerTileX > myTileX) preferredDirection = Direction.RIGHT;
                if (preferredDirection != Direction.NONE && !canMoveTowards(preferredDirection, entities)) {
                    if (playerTileY < myTileY) preferredDirection = Direction.UP;
                    else if (playerTileY > myTileY) preferredDirection = Direction.DOWN;
                }
            } else {
                if (playerTileY < myTileY) preferredDirection = Direction.UP;
                else if (playerTileY > myTileY) preferredDirection = Direction.DOWN;
                if (preferredDirection != Direction.NONE && !canMoveTowards(preferredDirection, entities)) {
                    if (playerTileX < myTileX) preferredDirection = Direction.LEFT;
                    else if (playerTileX > myTileX) preferredDirection = Direction.RIGHT;
                }
            }

            if (preferredDirection == Direction.NONE || !canMoveTowards(preferredDirection, entities)) {
                changeDirectionTowardsPlayerOrRandomly(player, entities);
            } else {
                currentDirection = preferredDirection;
            }
        } else {
            isChasing = false;
            if (this.speed == ONEAL_CHASE_SPEED) {
                this.speed = 0.5;
            }
            super.move(deltaTime, entities);
            return;
        }

        double dx = 0;
        double dy = 0;
        switch (currentDirection) {
            case UP: dy = -speed; break;
            case DOWN: dy = speed; break;
            case LEFT: dx = -speed; break;
            case RIGHT: dx = speed; break;
            case NONE: break;
        }

        if (dx != 0 || dy != 0) {
            double nextX = x + dx;
            double nextY = y + dy;
            if (canMoveTo(nextX, nextY, entities)) {
                x = nextX;
                y = nextY;
            }
        }
    }

    /**
     * Kiểm tra xem có thể di chuyển theo một hướng cụ thể không.
     */
    private boolean canMoveTowards(Direction direction, List<Entity> entities) {
        double testDx = 0, testDy = 0;
        switch (direction) {
            case UP: testDy = -1; break; // Chỉ cần 1 pixel để test
            case DOWN: testDy = 1; break;
            case LEFT: testDx = -1; break;
            case RIGHT: testDx = 1; break;
            case NONE: return true; // Đứng yên luôn có thể
        }
        return canMoveTo(x + testDx, y + testDy, entities);
    }

    /**
     * Nếu không thể đi theo hướng Player, thử đổi hướng ngẫu nhiên,
     * nhưng ưu tiên các hướng có thể giảm khoảng cách đến Player.
     */
    private void changeDirectionTowardsPlayerOrRandomly(Player player, List<Entity> entities) {
        super.changeDirectionRandomly();
    }


    @Override
    protected void updateAnimation() {
        animationCounter++;
        if (animationCounter >= ANIMATION_SPEED) {
            animationCounter = 0;
            animationFrameIndex = (animationFrameIndex + 1) % 3;
            setSpriteBasedOnDirectionAndFrame();
        }
    }

    private void setSpriteBasedOnDirectionAndFrame() {
        Sprite targetSprite = null;
        // Oneal dùng sprite riêng
        if (currentDirection == Direction.LEFT || currentDirection == Direction.UP) {
            if (animationFrameIndex == 0) targetSprite = Sprite.enemy_oneal_left1;
            else if (animationFrameIndex == 1) targetSprite = Sprite.enemy_oneal_left2;
            else targetSprite = Sprite.enemy_oneal_left3;
        } else { // RIGHT, DOWN, hoặc NONE
            if (animationFrameIndex == 0) targetSprite = Sprite.enemy_oneal_right1;
            else if (animationFrameIndex == 1) targetSprite = Sprite.enemy_oneal_right2;
            else targetSprite = Sprite.enemy_oneal_right3;
        }

        this.sprite = targetSprite;
        if (this.sprite == null) {
            System.err.println("Warning: Oneal sprite is null! Defaulting to right1.");
            this.sprite = Sprite.enemy_oneal_right1;
            if (this.sprite == null) System.err.println("CRITICAL ERROR: Default Oneal sprite is also null!");
        }
    }

    @Override
    public void destroy() {
        if (isAlive() && !isDying()) {
            // System.out.println("Oneal specific die animation starts!");
            this.sprite = Sprite.enemy_oneal_dead; // Đặt sprite Oneal chết
            // Sau đó gọi super.die() để bắt đầu quá trình dying chung (timer, mob_dead animation)
            // Hoặc bạn có thể không muốn Oneal dùng mob_dead mà chỉ hiện sprite enemy_oneal_dead
            // và sau đó alive=false.
            // Tạm thời, chúng ta sẽ để nó dùng mob_dead chung sau khi hiện sprite chết của nó 1 chút.
        }
        super.destroy(); // Luôn gọi super.die() để xử lý logic dying chung
    }
}