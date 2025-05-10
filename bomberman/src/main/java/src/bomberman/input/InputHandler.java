package src.bomberman.input;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.HashSet;
import java.util.Set;

public class InputHandler {
    private Scene scene;
    private final Set<KeyCode> pressedKeys = new HashSet<>();

    public InputHandler(Scene scene) {
        this.scene = scene;
    }

    public void init() {
        if (scene == null) {
            System.err.println("ERROR: Scene is null in InputHandler.init()");
            return;
        }

        scene.setOnKeyPressed(this::handleKeyPressed);
        scene.setOnKeyReleased(this::handleKeyReleased);
        System.out.println("InputHandler initialized for scene.");
    }

    private void handleKeyPressed(KeyEvent event) {
        pressedKeys.add(event.getCode());

    }

    private void handleKeyReleased(KeyEvent event) {
        pressedKeys.remove(event.getCode());
    }

    public boolean isPressed(KeyCode code) {
        return pressedKeys.contains(code);
    }

    public void releaseKey(KeyCode code) {
        pressedKeys.remove(code);
    }

    public void clearAllPressedKeys() {
        pressedKeys.clear();
    }
}
