package com.mygame;

import javafx.scene.image.Image;
import java.util.List;

public class BalloonEnemy extends Enemy {

    // Constructor nhận thêm deathFrames và truyền lên lớp cha
    public BalloonEnemy(double startGridX, double startGridY, int tileSize, double speed,
                        List<Image> walkFrames, List<Image> deathFrames) {
        super(startGridX, startGridY, tileSize, speed, walkFrames, deathFrames); // Truyền deathFrames
        this.timeToChangeDirection = 2.5;
    }

    @Override // Triển khai phương thức updateAI (tên mới)
    public void updateAI(double deltaTime, int[][] boardData, List<Bomb> bombs, Player player, GameController controller) {
        // Logic AI và di chuyển của Balloon đặt ở đây
        // (Không cần kiểm tra alive/dying ở đây vì hàm update() của lớp cha đã làm)

        // 1. AI: Đổi hướng ngẫu nhiên
        aiDecisionTimer -= deltaTime;
        if (aiDecisionTimer <= 0) {
            currentDirection = getRandomDirection(false);
            aiDecisionTimer = timeToChangeDirection + random.nextDouble() * 1.5;
        }

        // 2. Di chuyển
        double potentialX = x, potentialY = y, distance = speed * deltaTime;
        switch (currentDirection) { /* ... tính potentialX, potentialY ... */
            case UP: potentialY-=distance; break; case DOWN: potentialY+=distance; break;
            case LEFT: potentialX-=distance; break; case RIGHT: potentialX+=distance; break;
        }

        // 3. Kiểm tra va chạm và cập nhật vị trí
        if (currentDirection != Direction.NONE && GameController.isValidMove(potentialX, potentialY, size, boardData)) {
            x = potentialX; y = potentialY;
            // Không cần gọi updateWalkAnimation ở đây nữa, hàm update() của lớp cha sẽ gọi
        } else if (currentDirection != Direction.NONE) {
            currentDirection = getRandomDirection(false);
            aiDecisionTimer = timeToChangeDirection * 0.2;
        } else {
            // Nếu đang đứng yên mà bị chặn? Có thể cũng nên đổi hướng
            if(currentDirection == Direction.NONE) {
                currentDirection = getRandomDirection(false);
            }
        }
    }
}