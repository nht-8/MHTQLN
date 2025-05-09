package src.bomberman;

public class Config {
    // Kích thước hiển thị của mỗi ô trên màn hình
    public static final int TILE_SIZE = 32;

    // Số lượng ô Tile hiển thị trên màn hình (cho vùng game)
    public static final int SCREEN_TILES_WIDTH = 21;
    public static final int SCREEN_TILES_HEIGHT = 15;

    // Kích thước vùng game (Canvas) theo pixel
    public static final int GAME_AREA_WIDTH = SCREEN_TILES_WIDTH * TILE_SIZE;  // 672
    public static final int GAME_AREA_HEIGHT = SCREEN_TILES_HEIGHT * TILE_SIZE; // 480

    // Chiều cao của thanh HUD
    public static final int HUD_HEIGHT = 35; // Bạn có thể điều chỉnh

    // Tổng kích thước cửa sổ (bao gồm cả HUD nếu nó nằm ngoài vùng game Canvas)
    // Nếu game-hud-view.fxml dùng BorderPane với HUD ở top và Canvas ở center,
    // thì prefWidth và prefHeight của BorderPane sẽ là tổng kích thước.
    public static final int WINDOW_WIDTH = GAME_AREA_WIDTH;
    public static final int WINDOW_HEIGHT = GAME_AREA_HEIGHT + HUD_HEIGHT;

    public static final int POINTS_PER_ENEMY = 200; // Hoặc một giá trị điểm bạn muốn


    // Tốc độ di chuyển của Player
    public static final double PLAYER_SPEED = 1.5;

    // Các hằng số game khác
    public static final int BOMB_TIMER = 120;
    public static final int BOMB_EXPLOSION_DURATION = 30;
    public static final int PLAYER_INIT_BOMBS = 1;
    public static final int PLAYER_INIT_FLAMES = 1;
    public static final int PLAYER_INIT_LIVES = 3;

    // Đường dẫn tới các file spritesheet
    public static final String SPRITESHEET1_PATH = "/images/sheet1.png";
    public static final String SPRITESHEET2_PATH = "/images/sheet2.png";

  
    public static final String LEVEL_PATH_PREFIX = "/levels/";

    public static final String MENU_BACKGROUND_IMAGE_PATH = "/images/menu_background.png"; 

    public static final int MAX_LEVELS = 5;
    public static final int POINTS_PER_BRICK = 10; 
}
