package com.mygame;

import com.mygame.GameController;
import javafx.scene.image.Image;
import java.util.List;

public class OnealEnemy extends Enemy {

    private double bombPlaceCooldown = 5.0;
    private double bombPlaceTimer;
    private GameController gameControllerRef;

    // Constructor nhận thêm deathFrames
    public OnealEnemy(double startGridX, double startGridY, int tileSize, double speed,
                      List<Image> walkFrames, List<Image> deathFrames, // Thêm deathFrames
                      GameController controller) {
        super(startGridX, startGridY, tileSize, speed, walkFrames, deathFrames); // Truyền deathFrames
        this.gameControllerRef = controller;
        this.timeToChangeDirection = 1.2;
        this.bombPlaceTimer = bombPlaceCooldown + random.nextDouble() * 2.0;
    }

    @Override // Triển khai phương thức updateAI
    public void updateAI(double deltaTime, int[][] boardData, List<Bomb> bombs, Player player, GameController controller) {
        // Logic AI và di chuyển của Oneal

        // 1. AI: Di chuyển ngẫu nhiên nhanh hơn
        aiDecisionTimer -= deltaTime;
        if (aiDecisionTimer <= 0) { currentDirection = getRandomDirection(false); aiDecisionTimer = timeToChangeDirection + random.nextDouble() * 0.8; }
        double potentialX=x, potentialY=y, distance=speed*deltaTime;
        switch (currentDirection) { /* ... tính potentialX, potentialY ... */
            case UP: potentialY-=distance; break; case DOWN: potentialY+=distance; break;
            case LEFT: potentialX-=distance; break; case RIGHT: potentialX+=distance; break;
        }
        if (currentDirection != Direction.NONE && GameController.isValidMove(potentialX, potentialY, size, boardData)) { x = potentialX; y = potentialY; }
        else if (currentDirection != Direction.NONE){ currentDirection = getRandomDirection(false); aiDecisionTimer = timeToChangeDirection * 0.3; }
        else { if(currentDirection == Direction.NONE) currentDirection = getRandomDirection(false); }


        // 2. Logic Đặt Bom
        bombPlaceTimer -= deltaTime;
        if (bombPlaceTimer <= 0) { tryPlaceBomb(); bombPlaceTimer = bombPlaceCooldown + random.nextDouble() * 3.0; }
    }

    private void tryPlaceBomb() {
        if (gameControllerRef != null) {
            // --- TRUYỀN 'THIS' VÀO addEnemyBomb ---
            gameControllerRef.addEnemyBomb(getGridX(), getGridY(), this); // Truyền chính đối tượng Oneal này
        } else {
            System.err.println("ERROR: OnealEnemy missing Controller ref!");
        }
    }
}