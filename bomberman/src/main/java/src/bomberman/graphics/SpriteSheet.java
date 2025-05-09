// src/main/java/src/bomberman/graphics/SpriteSheet.java
package src.bomberman.graphics; // Hoặc uet.oop.bomberman.graphics

import javafx.scene.image.Image;
import src.bomberman.Config;

import java.io.InputStream;

/**
 * Quản lý việc tải một file ảnh spritesheet.
 */
public class SpriteSheet {
    private final Image sheet; // Ảnh spritesheet đã tải

    // --- Các sheet tĩnh ---
    public static SpriteSheet Sheet1;    // Sheet chứa sprite (explosion, playeer, bombs...)
    public static SpriteSheet Sheet2; // Sheet chứa sprite (enemy)

    /**
     * Constructor tải ảnh từ đường dẫn resource.
     * @param path Đường dẫn đến file ảnh trong thư mục resources (ví dụ: "/images/nes_spritesheet.png").
     */
    public SpriteSheet(String path) {
        Image tempSheet = null; // Biến tạm để xử lý lỗi
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                System.err.println("ERROR: Cannot find spritesheet resource at: " + path);
            } else {
                tempSheet = new Image(is);
                if (tempSheet.isError()) {
                    System.err.println("ERROR: Failed to load or process image from: " + path);
                    System.err.println("Exception: " + tempSheet.getException().getMessage());
                    tempSheet = null; // Đặt là null nếu có lỗi
                }
            }
        } catch (Exception e) {
            System.err.println("ERROR: Exception while loading spritesheet: " + path);
            e.printStackTrace();
            tempSheet = null;
        }
        this.sheet = tempSheet; // Gán sheet (có thể là null nếu lỗi)
    }

    /**
     * Trả về đối tượng Image của spritesheet.
     * @return Image hoặc null nếu tải lỗi.
     */
    public Image getSheet() {
        return sheet;
    }

    /**
     * Phương thức tĩnh để tải tất cả các spritesheet cần thiết cho game.
     * Nên được gọi một lần khi game khởi động.
     */
    public static void loadAllSheets() {
        System.out.println("Loading sprite sheets...");
        Sheet2 = new SpriteSheet(Config.SPRITESHEET2_PATH);
        Sheet1 = new SpriteSheet(Config.SPRITESHEET1_PATH);
        System.out.println("Sprite sheets loading finished.");
        // Kiểm tra lỗi sau khi load
        if (Sheet1.getSheet() == null) {
            System.err.println("CRITICAL: Spritesheet failed to load.");
        }
        if (Sheet2.getSheet() == null) {
            System.err.println("CRITICAL: Spritesheet failed to load.");
        }
    }
}