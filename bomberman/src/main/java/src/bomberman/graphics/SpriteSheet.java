package src.bomberman.graphics; 

import javafx.scene.image.Image;
import src.bomberman.Config;

import java.io.InputStream;

public class SpriteSheet {
    private final Image sheet; 

    public static SpriteSheet Sheet1;   
    public static SpriteSheet Sheet2; 

    public SpriteSheet(String path) {
        Image tempSheet = null;
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                System.err.println("ERROR: Cannot find spritesheet resource at: " + path);
            } else {
                tempSheet = new Image(is);
                if (tempSheet.isError()) {
                    System.err.println("ERROR: Failed to load or process image from: " + path);
                    System.err.println("Exception: " + tempSheet.getException().getMessage());
                    tempSheet = null;
                }
            }
        } catch (Exception e) {
            System.err.println("ERROR: Exception while loading spritesheet: " + path);
            e.printStackTrace();
            tempSheet = null;
        }
        this.sheet = tempSheet;
    }

    public Image getSheet() {
        return sheet;
    }
    
    public static void loadAllSheets() {
        System.out.println("Loading sprite sheets...");
        Sheet2 = new SpriteSheet(Config.SPRITESHEET2_PATH);
        Sheet1 = new SpriteSheet(Config.SPRITESHEET1_PATH);
        System.out.println("Sprite sheets loading finished.");
      
        if (Sheet1.getSheet() == null) {
            System.err.println("CRITICAL: Spritesheet failed to load.");
        }
        if (Sheet2.getSheet() == null) {
            System.err.println("CRITICAL: Spritesheet failed to load.");
        }
    }
}
