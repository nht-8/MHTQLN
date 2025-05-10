package src.bomberman.graphics; // Hoặc một package phù hợp

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import src.bomberman.Config;

public class FloatingText {
    private String text;
    private double x, y;
    private Color color;
    private Font font;
    private long startTime;
    private long durationMillis;
    private double ySpeed;
    private boolean alive = true;

    public FloatingText(String text, double x, double y, Color color, Font font, long durationMillis, double ySpeed) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color;
        this.font = (font != null) ? font : Font.font("Arial", FontWeight.BOLD, 16); // Font mặc định
        this.startTime = System.currentTimeMillis();
        this.durationMillis = durationMillis;
        this.ySpeed = ySpeed;
    }

    public FloatingText(String text, double x, double y, Color color, long durationMillis) {
        this(text, x, y, color, null, durationMillis, -30); // ySpeed mặc định là -30 (đi lên)
    }

    public void update(double deltaTime) {
        if (!alive) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - startTime > durationMillis) {
            alive = false;
            return;
        }

        this.y -= ySpeed * deltaTime; // deltaTime tính bằng giây

    }

    public void render(GraphicsContext gc) {
        if (!alive) return;

        gc.setFont(font);
        gc.setFill(color);
        gc.fillText(text, x, y);
    }

    public String getText() {
        return text;
    }

    public boolean isAlive() {
        return alive;
    }
}