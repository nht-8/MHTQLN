// src/main/java/src/bomberman/input/InputHandler.java
package src.bomberman.input; // HOẶC uet.oop.bomberman.input

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.HashSet;
import java.util.Set;

public class InputHandler {
    private Scene scene;
    private final Set<KeyCode> pressedKeys = new HashSet<>();
    // (Tùy chọn) Có thể thêm một Set cho các phím vừa được nhấn trong frame này (one-shot)
    // private final Set<KeyCode> justPressedKeys = new HashSet<>();

    public InputHandler(Scene scene) {
        this.scene = scene;
    }

    public void init() {
        if (scene == null) {
            System.err.println("ERROR: Scene is null in InputHandler.init()");
            return;
        }
        // Gắn sự kiện vào Scene
        scene.setOnKeyPressed(this::handleKeyPressed);
        scene.setOnKeyReleased(this::handleKeyReleased);
        System.out.println("InputHandler initialized for scene.");
    }

    private void handleKeyPressed(KeyEvent event) {
        pressedKeys.add(event.getCode());
        // justPressedKeys.add(event.getCode()); // Nếu dùng one-shot
    }

    private void handleKeyReleased(KeyEvent event) {
        pressedKeys.remove(event.getCode());
    }

    /**
     * Kiểm tra xem một phím có đang được giữ nhấn hay không.
     */
    public boolean isPressed(KeyCode code) {
        return pressedKeys.contains(code);
    }

    /**
     * (Tùy chọn) Kiểm tra xem một phím có vừa được nhấn trong frame này không.
     * Cần gọi clearJustPressedKeys() cuối mỗi frame update.
     */
    // public boolean isJustPressed(KeyCode code) {
    //     return justPressedKeys.contains(code);
    // }

    /**
     * (Tùy chọn) Xóa danh sách các phím vừa được nhấn.
     * Gọi cuối mỗi vòng lặp game nếu dùng isJustPressed.
     */
    // public void clearJustPressedKeys() {
    //     justPressedKeys.clear();
    // }

    /**
     * Dùng khi muốn xử lý 1 lần nhấn (ví dụ đặt bom) và không muốn
     * nó được coi là đang nhấn ở frame tiếp theo ngay lập tức.
     */
    public void releaseKey(KeyCode code) {
        pressedKeys.remove(code);
        // justPressedKeys.remove(code); // Nếu dùng one-shot
    }

    /**
     * Xóa tất cả các phím đang được nhấn (ví dụ: khi chuyển màn hình).
     */
    public void clearAllPressedKeys() {
        pressedKeys.clear();
        // justPressedKeys.clear(); // Nếu dùng one-shot
    }
}