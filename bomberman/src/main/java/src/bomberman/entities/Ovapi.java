package src.bomberman.entities;

import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet;
import src.bomberman.core.Game;

public class Ovapi extends Enemy {

    private final int ANIMATION_SPEED = 15;
    private int animationFrameIndex = 0;

    public Ovapi(double xTile, double yTile, SpriteSheet nesSheet, Game game) {
        super(xTile, yTile, nesSheet, game);
        this.speed = 0.5;
        setSpriteBasedOnDirectionAndFrame();
        if (this.sprite == null) {
            System.err.println("CRITICAL WARNING: Initial Ovapi sprite is null!");
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
        private void setSpriteBasedOnDirectionAndFrame () {
            Sprite targetSprite = null;

            if (currentDirection == Direction.LEFT || currentDirection == Direction.UP) {
                if (animationFrameIndex == 0) targetSprite = Sprite.enemy_ballom_left1;
                else if (animationFrameIndex == 1) targetSprite = Sprite.enemy_ballom_left2;
                else targetSprite = Sprite.enemy_ballom_left3;
            } else {
                if (animationFrameIndex == 0) targetSprite = Sprite.enemy_ballom_right1;
                else if (animationFrameIndex == 1) targetSprite = Sprite.enemy_ballom_right2;
                else targetSprite = Sprite.enemy_ballom_right3;
            }

            this.sprite = targetSprite;
            if (this.sprite == null) {
                System.err.println("Warning: Ovapi sprite is null for direction " + currentDirection +
                        ", frame " + animationFrameIndex + ". Defaulting to left1.");
                this.sprite = Sprite.enemy_ballom_left1;
                if (this.sprite == null) {
                    System.err.println("CRITICAL ERROR: Default Ovapi sprite (enemy_ballom_left1) is also null!");
                }
            }
        }

        @Override
        public void destroy () {
            if (isAlive() && !isDying()) {
                super.destroy();
            }
        }
    }
