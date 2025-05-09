package src.bomberman.entities; 

import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet;
import src.bomberman.core.Game;      
import src.bomberman.entities.Direction;

import java.util.List;

public class Doll extends Enemy {
    
    private final int ANIMATION_SPEED = 15;
    private int animationFrameIndex = 0; 

    private static final double DOLL_MIN_SPEED = 0.4; 
    private static final double DOLL_MAX_SPEED = 0.9; 
    private int stepsBeforeChangeSpeedDoll = 180;
    private int speedChangeCounterDoll = 0;

    public Doll(double xTile, double yTile, SpriteSheet nesSheet, Game game) {
        super(xTile, yTile, nesSheet, game); 
        initializeSpeed();
        setSpriteBasedOnDirectionAndFrame();
        if (this.sprite == null) {
            System.err.println("CRITICAL WARNING: Initial Doll sprite is null!");
        }
    }

    protected void initializeSpeed() {
        randomizeDollSpeed();
    }

    private void randomizeDollSpeed() {
        this.speed = DOLL_MIN_SPEED + (DOLL_MAX_SPEED - DOLL_MIN_SPEED) * random.nextDouble();
        // System.out.println("Doll new speed: " + String.format("%.2f", this.speed)); // Debug
    }

    @Override
    public void update(double deltaTime, List<Entity> entities) {
        super.update(deltaTime, entities);

        if (isAlive() && !isDying()) {
            speedChangeCounterDoll++;
            if (speedChangeCounterDoll >= stepsBeforeChangeSpeedDoll) {
                if (random.nextDouble() < 0.6) { 
                    randomizeDollSpeed();
                }
                speedChangeCounterDoll = 0; 
            }
        }
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
        if (currentDirection == Direction.LEFT || currentDirection == Direction.UP) {
            if (animationFrameIndex == 0) targetSprite = Sprite.enemy_doll_left1;
            else if (animationFrameIndex == 1) targetSprite = Sprite.enemy_doll_left2;
            else targetSprite = Sprite.enemy_doll_left3;
        } else { 
            if (animationFrameIndex == 0) targetSprite = Sprite.enemy_doll_right1;
            else if (animationFrameIndex == 1) targetSprite = Sprite.enemy_doll_right2;
            else targetSprite = Sprite.enemy_doll_right3;
        }

        this.sprite = targetSprite;
        if (this.sprite == null) {
            System.err.println("Warning: Minvo sprite is null for direction " + currentDirection +
                    ", frame " + animationFrameIndex + ". Defaulting to left1.");
            this.sprite = Sprite.enemy_doll_left1; 
            if (this.sprite == null) {
                System.err.println("CRITICAL ERROR: Default Minvo sprite (enemy_doll_left1) is also null!");
            }
        }
        this.sprite = targetSprite;
        if (this.sprite == null) { this.sprite = Sprite.enemy_doll_right1; }
    }

    @Override
    public void destroy() {
        if (isAlive() && !isDying()) {
        }
    }
}
