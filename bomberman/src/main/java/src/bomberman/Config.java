package src.bomberman;

public class Config {

    public static final int TILE_SIZE = 32;

    public static final int SCREEN_TILES_WIDTH = 21;
    public static final int SCREEN_TILES_HEIGHT = 15;

    public static final int GAME_AREA_WIDTH = SCREEN_TILES_WIDTH * TILE_SIZE;
    public static final int GAME_AREA_HEIGHT = SCREEN_TILES_HEIGHT * TILE_SIZE;

    public static final int HUD_HEIGHT = 35;

    public static final int WINDOW_WIDTH = GAME_AREA_WIDTH;
    public static final int WINDOW_HEIGHT = GAME_AREA_HEIGHT + HUD_HEIGHT;

    public static final int POINTS_PER_ENEMY = 200;

    public static final double PLAYER_SPEED = 1.5;

    public static final int BOMB_TIMER = 120;
    public static final int BOMB_EXPLOSION_DURATION = 30;
    public static final int PLAYER_INIT_BOMBS = 1;
    public static final int PLAYER_INIT_FLAMES = 1;
    public static final int PLAYER_INIT_LIVES = 3;

    public static final String SPRITESHEET1_PATH = "/images/sheet1.png";
    public static final String SPRITESHEET2_PATH = "/images/sheet2.png";

    public static final String LEVEL_PATH_PREFIX = "/levels/";

    public static final String MENU_BACKGROUND_IMAGE_PATH = "/images/menu_background.png";

    public static final int MAX_LEVELS = 5;
    public static final int POINTS_PER_BRICK = 10;
}
