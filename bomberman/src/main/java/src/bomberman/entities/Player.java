package src.bomberman.entities;

import javafx.geometry.Rectangle2D;
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
    private int animationFrameIndex = 0;
    private final int ANIMATION_SPEED = 8;

    private int deathTimer = 0;
    private final int DEATH_ANIMATION_DURATION = 60;
    private boolean dying = false;

    private final double COLLISION_BOUNDS_INSET = 4.0;

    private int initialTileX;
    private int initialTileY;
    private boolean justPermanentlyDead=false;

    public Player(double xTile, double yTile, SpriteSheet modernSheet, InputHandler input, Game game) {
        super(xTile, yTile, modernSheet);
        this.input = input;
        this.game = game;
        this.sprite = Sprite.player_d1;
        if (this.sprite == null) {
            System.err.println("CRITICAL WARNING: Initial Player sprite (player_d1) is null!");
        }

        this.initialTileX = (int) xTile;
        this.initialTileY = (int) yTile;
    }

    @Override
    public void update(double deltaTime, List<Entity> entities) {
        if (!alive&&!dying) {
            return;
        }
        if (dying) {
            handleDeathAnimation();
            deathTimer++;
            if (deathTimer > DEATH_ANIMATION_DURATION + 30) {
                alive = false;

                System.out.println("Player permanently dead (handle game over logic in Game class)");
            }
            return;
        }

        handleInputAndMovement(entities);
        handleBombPlacement();
    }

    public boolean isJustPermanentlyDeadAndDecrementLife() {
        if (justPermanentlyDead) {

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

        if (game != null && game.getLevel() != null) {
            x = Math.max(0, Math.min(x, game.getLevel().getWidth() * Config.TILE_SIZE - getWidth()));
            y = Math.max(0, Math.min(y, game.getLevel().getHeight() * Config.TILE_SIZE - getHeight()));
        }
    }

    public void consumePermanentlyDeadFlag() {
        this.justPermanentlyDead = false;
    }


    private boolean checkCollision(List<Entity> entities) {
        Rectangle2D playerBounds = this.getBounds();
        for (Entity entity : entities) {
            if (entity == this || !entity.isAlive()) continue;
            if (entity.isSolid()) {
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

            animationFrameIndex = (animationFrameIndex + 1) % 3;

            switch (currentDirection) {
                case UP:
                    if (animationFrameIndex == 0) sprite = Sprite.player_u1;
                    else if (animationFrameIndex == 1) sprite = Sprite.player_u2;
                    else sprite = Sprite.player_u3;

                    break;
                case DOWN:
                    if (animationFrameIndex == 0) sprite = Sprite.player_d1;
                    else if (animationFrameIndex == 1) sprite = Sprite.player_d2;
                    else sprite = Sprite.player_d3;

                    break;
                case LEFT:
                    if (animationFrameIndex == 0) sprite = Sprite.player_l1;
                    else if (animationFrameIndex == 1) sprite = Sprite.player_l2;
                    else sprite = Sprite.player_l3;

                    break;
                case RIGHT:
                    if (animationFrameIndex == 0) sprite = Sprite.player_r1;
                    else if (animationFrameIndex == 1) sprite = Sprite.player_r2;
                    else sprite = Sprite.player_r3;

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
            animationCounter = 0;
            deathTimer = 0;
            sprite = Sprite.player_dead1;
            if (sprite == null) {
                System.err.println("ERROR: Sprite.player_dead1 is null in Player.destroy()!");
                sprite = Sprite.player_d1;
            }
            SoundManager.getInstance().playSound(SoundManager.PLAYER_DEATH);
        }

    }

    private void handleDeathAnimation() {
        deathTimer++;
        int deathFrameTime = DEATH_ANIMATION_DURATION / 3;

        Sprite targetSprite = null;
        if (deathTimer < deathFrameTime) {
            targetSprite = Sprite.player_dead1;
        } else if (deathTimer < deathFrameTime * 2) {
            targetSprite = Sprite.player_dead2;
        } else if (deathTimer <= DEATH_ANIMATION_DURATION) {
            targetSprite = Sprite.player_dead3;
        } else {

            targetSprite = Sprite.player_dead3;
            if (dying) {
                this.dying = false;
                this.alive = false;
                this.justPermanentlyDead = true;
                System.out.println("Player death animation finished. Flags set: alive=false, dying=false, justPermanentlyDead=true");
            }
        }
        this.sprite = targetSprite;
        if (this.sprite == null) {
            System.err.println("Warning: Player dead animation sprite is null. deathTimer: " + deathTimer);
            this.sprite = Sprite.player_d1;
        }
    }

    @Override
    public boolean isDying() {
        return dying;
    }

    public int getFlameLength() { return flameLength; }

    @Override
    public Rectangle2D getBounds() {

        double visualWidth = super.getWidth();
        double visualHeight = super.getHeight();

        double collisionX = x + COLLISION_BOUNDS_INSET;
        double collisionY = y + COLLISION_BOUNDS_INSET;
        double collisionWidth = visualWidth - (2 * COLLISION_BOUNDS_INSET);
        double collisionHeight = visualHeight - (COLLISION_BOUNDS_INSET);

        collisionWidth = Math.max(1, collisionWidth);
        collisionHeight = Math.max(1, collisionHeight);

        return new Rectangle2D(collisionX, collisionY, collisionWidth, collisionHeight);

    }
    public void resetToStartPositionAndRevive() {
        this.x = initialTileX * Config.TILE_SIZE;
        this.y = initialTileY * Config.TILE_SIZE;
        this.alive = true;
        this.dying = false;
        this.justPermanentlyDead = false;
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

    }
    public void setInitialPosition(int tileX, int tileY) {
        this.initialTileX = tileX;
        this.initialTileY = tileY;
    }

    public void addBombCapacity(int amount) { if(bombCapacity<3)  this.bombCapacity += amount; }
    public void addFlameLength(int amount) { if(flameLength<2) this.flameLength += amount; }
    public void addSpeed(double amount) { this.speed += amount; }
}
