package org.example.demo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import java.util.List;

public class DestroyedBrick {

    private double x, y; // Tọa độ pixel
    private int size;
    private List<Image> animationFrames;
    private int currentFrameIndex = 0;
    private double animationTimer = 0;
    private double frameDuration = 0.1; // Thời gian mỗi frame của animation vỡ (giây)
    private boolean finished = false;
    private double totalAnimationTime;

    // Constructor nhận vị trí grid và list ảnh animation
    public DestroyedBrick(int gridX, int gridY, int tileSize, List<Image> frames) {
        this.x = gridX * tileSize;
        this.y = gridY * tileSize;
        this.size = tileSize;
        this.animationFrames = frames;
        // Tính tổng thời gian dựa trên số frame và duration
        this.totalAnimationTime = (frames != null) ? frames.size() * frameDuration : 0.3; // 0.3s nếu không có frame
        if (this.animationFrames == null || this.animationFrames.isEmpty()) {
            System.err.println("WARN: DestroyedBrick created with no animation frames!");
        }
    }

    public void update(double deltaTime) {
        if (finished || animationFrames == null || animationFrames.isEmpty()) return;

        animationTimer += deltaTime;

        // Nếu animation đã chạy hết tổng thời gian
        if (animationTimer >= totalAnimationTime) {
            finished = true;
            return;
        }

        // Tính frame hiện tại (chỉ tăng khi đủ thời gian frame)
        currentFrameIndex = Math.min((int)(animationTimer / frameDuration), animationFrames.size() - 1);
    }

    public void render(GraphicsContext gc) {
        if (!finished && animationFrames != null && !animationFrames.isEmpty()) {
            // Đảm bảo index hợp lệ
            int frameIndex = Math.max(0, Math.min(currentFrameIndex, animationFrames.size() - 1));
            Image currentFrame = animationFrames.get(frameIndex);
            if (currentFrame != null) {
                gc.drawImage(currentFrame, x, y, size, size);
            } else { // Fallback nếu ảnh frame bị null
                gc.setFill(Color.SADDLEBROWN); // Màu gạch vỡ dự phòng
                gc.fillRect(x + size*0.1, y + size*0.1, size*0.8, size*0.8);
            }
        }
        // Không vẽ gì nếu đã finished
    }

    public boolean isFinished() {
        return finished;
    }
}