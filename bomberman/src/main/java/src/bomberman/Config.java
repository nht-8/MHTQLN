// src/main/java/src/bomberman/Config.java
package src.bomberman; // HOẶC uet.oop.bomberman

public class Config {
    // Kích thước hiển thị của mỗi ô trên màn hình
    public static final int TILE_SIZE = 32; // Ví dụ: 32x32 pixel cho mỗi ô logic

    // Số lượng ô Tile hiển thị trên màn hình
    public static final int SCREEN_TILES_WIDTH = 21;
    public static final int SCREEN_TILES_HEIGHT = 15;

    // Kích thước màn hình game theo pixel (tự động tính)
    public static final int SCREEN_WIDTH = SCREEN_TILES_WIDTH * TILE_SIZE;
    public static final int SCREEN_HEIGHT = SCREEN_TILES_HEIGHT * TILE_SIZE;

    // Tốc độ di chuyển của Player
    public static final double PLAYER_SPEED = 1.5; // Điều chỉnh cho phù hợp với TILE_SIZE

    // Các hằng số game khác
    public static final int BOMB_TIMER = 120; // Khoảng 2 giây (nếu game chạy 60fps)
    public static final int BOMB_EXPLOSION_DURATION = 30; // Khoảng 0.5 giây
    public static final int PLAYER_INIT_BOMBS = 1;    // Số bom ban đầu
    public static final int PLAYER_INIT_FLAMES = 1;   // Độ dài lửa ban đầu (số ô)

    // Đường dẫn tới các file spritesheet (tính từ thư mục resources)
    public static final String SPRITESHEET1_PATH = "/images/sheet1.png";
    public static final String SPRITESHEET2_PATH = "/images/sheet2.png";

    // Đường dẫn tới thư mục chứa các file level
    public static final String LEVEL_PATH_PREFIX = "/levels/";
}