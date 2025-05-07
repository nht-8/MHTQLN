package com.mygame;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Enemy {

    protected double x, y;
    protected final int size;
    protected double speed;
    protected boolean alive = true;
    protected boolean dying = false;

    protected List<Image> walkFrames;
    protected int currentWalkFrameIndex = 0;
    protected double walkAnimationTimer = 0;
    protected double walkAnimationFrameDuration = 0.18;

    protected List<Image> deathFrames;
    protected int currentDeathFrameIndex = 0;
    protected double deathAnimationTimer = 0;
    protected double deathFrameDuration = 0.15;
    protected double deathAnimationTotalTime;


    protected Direction currentDirection = Direction.NONE;
    protected double aiDecisionTimer = 0;
    protected double timeToChangeDirection = 2.0;
    protected static Random random = new Random();

    public Enemy(double startGridX, double startGridY, int tileSize, double initialSpeed,
                 List<Image> walkFrames, List<Image> deathFrames) {
        this.size = tileSize;
        this.x = startGridX * tileSize;
        this.y = startGridY * tileSize;
        this.speed = initialSpeed;
        this.walkFrames = ensureList(walkFrames);
        this.deathFrames = ensureList(deathFrames);
        this.deathAnimationTotalTime = this.deathFrames.isEmpty() ? 0.5 : this.deathFrames.size() * this.deathFrameDuration;

        if (this.walkFrames.isEmpty())
            System.err.println("WARN: Enemy " + getClass().getSimpleName() + " has no walk frames!");
        if (this.deathFrames.isEmpty())
            System.err.println("WARN: Enemy " + getClass().getSimpleName() + " has no death frames!");

        this.currentDirection = getRandomDirection(true);
        this.aiDecisionTimer = random.nextDouble() * timeToChangeDirection;
    }

    private List<Image> ensureList(List<Image> list) {
        return (list != null) ? list : new ArrayList<>();
    }

    public abstract void updateAI(double deltaTime, int[][] boardData, List<Bomb> bombs, Player player, GameController controller);

    public void update(double deltaTime, int[][] boardData, List<Bomb> bombs, Player player, GameController controller) {
        if (dying) {
            updateDeathAnimation(deltaTime);
        } else if (alive) {
            updateAI(deltaTime, boardData, bombs, player, controller);
            if (currentDirection != Direction.NONE) updateWalkAnimation(deltaTime);
            else {
                currentWalkFrameIndex = 0;
                walkAnimationTimer = 0;
            }
        }
    }

    protected void updateWalkAnimation(double deltaTime) {
        if (walkFrames == null || walkFrames.isEmpty()) return;
        walkAnimationTimer += deltaTime;
        if (walkAnimationTimer >= walkAnimationFrameDuration) {
            walkAnimationTimer -= walkAnimationFrameDuration;
            currentWalkFrameIndex = (currentWalkFrameIndex + 1) % walkFrames.size();
        }
    }

    protected void updateDeathAnimation(double deltaTime) {
        deathAnimationTimer += deltaTime;
        if (deathAnimationTimer < deathAnimationTotalTime && !deathFrames.isEmpty()) {
            currentDeathFrameIndex = Math.min((int) (deathAnimationTimer / deathFrameDuration), deathFrames.size() - 1);
        } else {
            dying = false;
            alive = false;
        }
    }

    public void render(GraphicsContext gc) {
        if (dying) {
            renderDeathAnimation(gc);
        } else if (alive) {
            renderWalkingAnimation(gc);
        }
    }

    protected void renderWalkingAnimation(GraphicsContext gc) {
        Image cF = null;
        if (walkFrames != null && !walkFrames.isEmpty()) cF = walkFrames.get(currentWalkFrameIndex % walkFrames.size());
        if (cF != null) gc.drawImage(cF, x, y, size, size);

    }

    protected void renderDeathAnimation(GraphicsContext gc) {
        if (deathFrames != null && !deathFrames.isEmpty() && currentDeathFrameIndex >= 0 && currentDeathFrameIndex < deathFrames.size()) {
            Image dF = deathFrames.get(currentDeathFrameIndex);
            if (dF != null) gc.drawImage(dF, x, y, size, size);

        } 
    }

    public void die() {
        if (alive && !dying) {
            System.out.println("Enemy dying " + getClass().getSimpleName() + "(" + getGridX() + "," + getGridY() + ")");
            this.dying = true;
            this.currentDirection = Direction.NONE;
            this.currentDeathFrameIndex = 0;
            this.deathAnimationTimer = 0;
        }
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getGridX() {
        return (int) ((x + size / 2.0) / size);
    }

    public int getGridY() {
        return (int) ((y + size / 2.0) / size);
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean isDying() {
        return dying;
    }

    public Rectangle2D getHitbox() {
        double s = size * 0.7;
        double o = (size - s) / 2.0;
        return new Rectangle2D(x + o, y + o, s, s);
    }

    protected Direction getRandomDirection(boolean allowNone) {
        int b = allowNone ? 5 : 4;
        int p = random.nextInt(b);
        switch (p) {
            case 0:
                return Direction.UP;
            case 1:
                return Direction.DOWN;
            case 2:
                return Direction.LEFT;
            case 3:
                return Direction.RIGHT;
            case 4:
                return Direction.NONE;
            default:
                return Direction.NONE;
        }
    }
}