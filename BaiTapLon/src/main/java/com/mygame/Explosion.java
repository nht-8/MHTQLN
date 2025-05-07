package com.mygame;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Explosion {
    private final double x, y;
    private final int gridX, gridY, size;
    private final Image image;
    private double duration = 0.4;
    private double timer;
    private boolean finished = false;

    // +++ THÊM THUỘC TÍNH SOURCE BOMB +++
    private final Bomb sourceBomb; // Tham chiếu đến quả bom gốc

    // Sửa Constructor để nhận sourceBomb
    public Explosion(int gX, int gY, int tS, Image img, Bomb sourceBomb) { // Thêm sourceBomb
        this.gridX = gX;
        this.gridY = gY;
        this.size = tS;
        this.x = gX * tS;
        this.y = gY * tS;
        this.image = img;
        this.timer = duration;
        this.sourceBomb = sourceBomb; // Lưu sourceBomb
    }

    // Giữ nguyên update() và render()
    public void update(double dT) {
        if (!finished) {
            timer -= dT;
            if (timer <= 0) finished = true;
        }
    }

    public void render(GraphicsContext gc) {
        if (!finished) {
            if (image != null) gc.drawImage(image, x, y, size, size);
            else {
                gc.setFill(Color.YELLOW);
                gc.fillRect(x, y, size, size);
            }
        }
    }

    // --- Getters ---
    public boolean isFinished() {
        return finished;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    // +++ THÊM GETTER CHO SOURCE BOMB +++
    public Bomb getSourceBomb() {
        return sourceBomb;
    }
}