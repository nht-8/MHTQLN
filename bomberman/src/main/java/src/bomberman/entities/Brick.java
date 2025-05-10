package src.bomberman.entities;

import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet;
import src.bomberman.core.Game;

import java.util.List;
import java.util.Random;

public class Brick extends Entity {

    private boolean breaking = false;
    private int breakAnimationCounter = 0;
    private final int BREAK_ANIMATION_SPEED = 15;

    private PowerUp.PowerUpType containedPowerUpType = PowerUp.PowerUpType.NONE;
    private Game game;

    public Brick(double xTile, double yTile, SpriteSheet sheet, Game game) {
        super(xTile, yTile, sheet);
        this.game = game;
        this.sprite = Sprite.brick;
        if (this.sprite == null) {
            System.err.println("CRITICAL WARNING: Sprite.brick is null during Brick construction!");
        }

        Random random = new Random();

        if (random.nextDouble() < 0.25) {
            PowerUp.PowerUpType[] allPowerUps = PowerUp.PowerUpType.values();

            int randomIndex = random.nextInt(allPowerUps.length - (allPowerUps[allPowerUps.length-1] == PowerUp.PowerUpType.NONE ? 1:0) );
            this.containedPowerUpType = allPowerUps[randomIndex];
        }
    }

    @Override
    public void update(double deltaTime, List<Entity> entities) {
        if (!alive) return;

        if (breaking) {
            breakAnimationCounter++;

            int totalAnimationFrames = 3;
            int currentFrameIndex = breakAnimationCounter / BREAK_ANIMATION_SPEED;

            if (currentFrameIndex == 0) {
                this.sprite = Sprite.brick;
            } else if (currentFrameIndex == 1) {
                this.sprite = Sprite.brick_exploded;
            } else if (currentFrameIndex == 2) {
                this.sprite = Sprite.brick_exploded1;
            } else {
                this.alive = false;
                this.breaking = false;

                if (containedPowerUpType != PowerUp.PowerUpType.NONE && game != null && this.entitySheet != null) {
                    PowerUp pu = new PowerUp(getTileX(), getTileY(), this.entitySheet, containedPowerUpType);
                    game.addPowerUp(pu);
                    System.out.println("Spawned PowerUp: " + containedPowerUpType + " at (" + getTileX() + "," + getTileY() + ")");
                }

                return;
            }

            if (sprite == null) {
                System.err.println("Warning: Brick explosion sprite is null. Defaulting to normal brick.");
                sprite = Sprite.brick;
                if(sprite == null) {
                    System.err.println("CRITICAL ERROR: Default brick sprite (Sprite.brick) is also null!");
                }
            }
        }
    }

    public PowerUp.PowerUpType getContainedPowerUpType() {
        return containedPowerUpType;
    }

    public void startBreaking() {
        if (!breaking && alive) {
            breaking = true;
            breakAnimationCounter = 0;
        }
    }

    @Override
    public void destroy() {
        startBreaking();
    }

    @Override
    public boolean isSolid() {
        return alive && !breaking;
    }

    @Override
    public boolean isDying() {
        return breaking && alive;
    }

    public void setContainedPowerUpType(PowerUp.PowerUpType type) {
        this.containedPowerUpType = type;

    }
}
