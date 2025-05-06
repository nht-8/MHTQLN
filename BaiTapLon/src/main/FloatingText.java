package src.main;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class FloatingText {

    private double x, y; // Tọa độ pixel ban đầu (tâm của text)
    private String text;
    private double duration = 0.7; // Thời gian hiển thị (giây)
    private double timer;
    private boolean finished = false;
    private Color textColor = Color.BLACK; // Màu chữ
    private Font textFont = Font.font("Arial", FontWeight.BOLD, 16); // Font chữ

    private double floatSpeed = -30; // Tốc độ trôi lên (pixel/giây, âm là đi lên)

    public FloatingText(String text, double centerX, double centerY) {
        this.text = text;
        this.x = centerX; // Lưu tọa độ tâm
        this.y = centerY;
        this.timer = duration;
    }

    public void update(double deltaTime) {
        if (!finished) {
            timer -= deltaTime;
            if (timer <= 0) {
                finished = true;
            } else {
                // Làm cho chữ trôi lên trên
                y += floatSpeed * deltaTime;
            }
        }
    }

    public void render(GraphicsContext gc) {
        if (!finished) {
            // Lưu trạng thái cũ của gc
            TextAlignment oldAlign = gc.getTextAlign();
            Font oldFont = gc.getFont();
            gc.setFill(textColor);
            gc.setFont(textFont);
            gc.setTextAlign(TextAlignment.CENTER); // Căn giữa chữ theo tọa độ x

            // Vẽ chữ tại vị trí hiện tại (đã cập nhật y)
            gc.fillText(text, x, y);

            // Khôi phục trạng thái gc
            gc.setTextAlign(oldAlign);
            gc.setFont(oldFont);
        }
    }

    public boolean isFinished() {
        return finished;
    }
}