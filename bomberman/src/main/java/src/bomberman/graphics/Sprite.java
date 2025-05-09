// src/main/java/uet/oop/bomberman/graphics/Sprite.java
package src.bomberman.graphics; // << GIỮ NGUYÊN PACKAGE CỦA BẠN

/**
 * Đại diện cho một sprite cụ thể trên SpriteSheet.
 * Lưu tọa độ (pixel) và kích thước (pixel) của sprite trên sheet gốc.
 * Phần khởi tạo tĩnh sẽ lấy thông tin từ cấu trúc cũ (col, row, realWidth, realHeight).
 */
public class Sprite {

    // Kích thước cơ bản của ô lưới trên spritesheet NES
    // (Quan trọng để tính tọa độ pixel từ col/row)
    public static final int DEFAULT_SIZE = 32;
    // Kích thước hiển thị sau khi scale (nếu dùng getFxImage, nhưng chúng ta không dùng)
    // public static final int SCALED_SIZE = DEFAULT_SIZE * 2;
    // Màu trong suốt (không cần thiết với cách vẽ mới)
    // private static final int TRANSPARENT_COLOR = 0xffff00ff;

    // --- Các trường dữ liệu của đối tượng Sprite ---
    public final double x; // Tọa độ X trên spritesheet (pixel)
    public final double y; // Tọa độ Y trên spritesheet (pixel)
    public final double width; // Chiều rộng thực của sprite này (pixel)
    public final double height; // Chiều cao thực của sprite này (pixel)
    public final SpriteSheet sheet; // Tham chiếu tới sheet chứa sprite này

    // --- Định nghĩa các Sprite tĩnh --- //

    /* Board */
    public static Sprite grass;
    public static Sprite brick;
    public static Sprite wall;

    /* Bomber (Sẽ lấy từ sheet modern) */
    public static Sprite player_u1, player_u2, player_u3, player_u4;
    public static Sprite player_d1, player_d2, player_d3, player_d4;
    public static Sprite player_l1, player_l2, player_l3, player_l4;
    public static Sprite player_r1, player_r2, player_r3, player_r4;
    public static Sprite player_dead1, player_dead2, player_dead3;

    /* Enemies */
    // BALLOM
    public static Sprite enemy_ballom_left1, enemy_ballom_left2, enemy_ballom_left3;
    public static Sprite enemy_ballom_right1, enemy_ballom_right2, enemy_ballom_right3;
    public static Sprite enemy_ballom_dead;
    // ONEAL
    public static Sprite enemy_oneal_left1, enemy_oneal_left2, enemy_oneal_left3;
    public static Sprite enemy_oneal_right1, enemy_oneal_right2, enemy_oneal_right3;
    public static Sprite enemy_oneal_dead;
    // DOLL
    public static Sprite enemy_doll_left1, enemy_doll_left2, enemy_doll_left3;
    public static Sprite enemy_doll_right1, enemy_doll_right2, enemy_doll_right3;
    public static Sprite enemy_doll_dead;
    // MINVO
    public static Sprite enemy_minvo_left1, enemy_minvo_left2, enemy_minvo_left3;
    public static Sprite enemy_minvo_right1, enemy_minvo_right2, enemy_minvo_right3;
    public static Sprite enemy_minvo_dead;
    // KONDORIA
    public static Sprite enemy_kondoria_left1, enemy_kondoria_left2, enemy_kondoria_left3;
    public static Sprite enemy_kondoria_right1, enemy_kondoria_right2, enemy_kondoria_right3;
    public static Sprite enemy_kondoria_dead;
    // ALL Enemies
    public static Sprite mob_dead1, mob_dead2, mob_dead3;

    /* Bomb */
    public static Sprite bomb, bomb_1, bomb_2;

    /* Explosion */
    public static Sprite explosion_center, explosion_center1, explosion_center2; // Đổi tên bomb_exploded*
    public static Sprite explosion_vertical, explosion_vertical1, explosion_vertical2;
    public static Sprite explosion_horizontal, explosion_horizontal1, explosion_horizontal2;
    public static Sprite explosion_left, explosion_left1, explosion_left2;
    public static Sprite explosion_right, explosion_right1, explosion_right2;
    public static Sprite explosion_top, explosion_top1, explosion_top2;
    public static Sprite explosion_bottom, explosion_bottom1, explosion_bottom2;

    /* Brick Explosion */
    public static Sprite brick_exploded, brick_exploded1;

    /* Powerups */
    public static Sprite powerup_bombs;
    public static Sprite powerup_flames;
    public static Sprite powerup_speed;
    public static Sprite powerup_wallpass;
    public static Sprite powerup_detonator;
    public static Sprite powerup_bombpass;
    public static Sprite powerup_flamepass;


    /**
     * Constructor mới nhận tọa độ và kích thước pixel.
     * Được dùng trong loadSprites để tạo các đối tượng Sprite tĩnh.
     */
    public Sprite(SpriteSheet sheet, double x, double y, double width, double height) {
        this.sheet = sheet;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // Các phương thức helper để lấy thông tin nguồn
    public double getSourceX() {
        return x;
    }

    public double getSourceY() {
        return y;
    }

    public double getSourceWidth() {
        return width;
    }

    public double getSourceHeight() {
        return height;
    }


    /**
     * Khởi tạo tất cả các biến Sprite tĩnh.
     * Phương thức này sẽ được gọi một lần khi game bắt đầu.
     * Nó lấy thông tin từ cấu trúc định nghĩa sprite cũ (col, row, realWidth, realHeight)
     * và tạo các đối tượng Sprite mới với tọa độ và kích thước pixel chính xác.
     *
     * @param Sheet2 SpriteSheet chứa các sprite kiểu NES (được tham chiếu là SpriteSheet.tiles trong code cũ).
     * @param Sheet1 SpriteSheet chứa các sprite kiểu modern (dùng cho Player).
     */
    public static void loadSprites(SpriteSheet Sheet1, SpriteSheet Sheet2) {

        if (Sheet1 != null && Sheet1.getSheet() != null && !Sheet1.getSheet().isError()) {

            // Board
            grass = new Sprite(Sheet1, 299, 47, 32, 32);
            wall = new Sprite(Sheet1, 299, 5, 32, 32);
            brick = new Sprite(Sheet1, 107, 257, 32, 32);

            // Bomb
            bomb = new Sprite(Sheet1, 47, 131, 32, 32); // col=0, row=3, rw=15, rh=15
            bomb_1 = new Sprite(Sheet1, 89, 131, 32, 32); // col=1, row=3, rw=13, rh=15
            bomb_2 = new Sprite(Sheet1, 131, 131, 32, 32); // col=2, row=3, rw=12, rh=14

            // Brick Explosion
            brick_exploded = new Sprite(Sheet1, 191, 257, 32, 32);
            brick_exploded1 = new Sprite(Sheet1, 233, 257, 32, 32);

            // Player Down (24x32)
            player_d1 = new Sprite(Sheet1, 5, 173, 24, 32); // Frame đứng yên + frame 1
            player_d2 = new Sprite(Sheet1, 39, 173, 24, 32); // Frame 2
            player_d3 = new Sprite(Sheet1, 73, 173, 24, 32); // Frame 3 + 4 (nếu có)
            player_d4 = new Sprite(Sheet1, 107, 173, 24, 32); // Frame 3 + 4 (nếu có)

            // Player Left (24x32)
            player_l1 = new Sprite(Sheet1, 243, 173, 24, 32);
            player_l2 = new Sprite(Sheet1, 5, 215, 24, 32);
            player_l3 = new Sprite(Sheet1, 39, 215, 24, 32);
            player_l4 = new Sprite(Sheet1, 73, 215, 24, 32);

            // Player Right (24x32)
            player_r1 = new Sprite(Sheet1, 107, 215, 24, 32);
            player_r2 = new Sprite(Sheet1, 141, 215, 24, 32);
            player_r3 = new Sprite(Sheet1, 175, 215, 24, 32);
            player_r4 = new Sprite(Sheet1, 209, 215, 24, 32);

            // Player Up (24x32)
            player_u1= new Sprite(Sheet1, 243, 215, 24, 32);
            player_u2 = new Sprite(Sheet1, 5, 257, 24, 32);
            player_u3 = new Sprite(Sheet1, 39, 257, 24, 32);
            player_u4 = new Sprite(Sheet1, 73, 257, 24, 32);

            // Player Dead (24x32)
            player_dead1 = new Sprite(Sheet1, 141, 173, 24, 32);
            player_dead2 = new Sprite(Sheet1, 175, 173, 24, 32);
            player_dead3 = new Sprite(Sheet1, 209, 173, 24, 32);

            // Explosion
            explosion_center = new Sprite(Sheet1, 47, 5, 32, 32); // bomb_exploded
            explosion_center1 = new Sprite(Sheet1, 47, 47, 32, 32); // bomb_exploded1
            explosion_center2 = new Sprite(Sheet1, 47, 89, 32, 32); // bomb_exploded2
            explosion_vertical = new Sprite(Sheet1, 257, 5, 32, 32);
            explosion_vertical1 = new Sprite(Sheet1, 257, 47, 32, 32);
            explosion_vertical2 = new Sprite(Sheet1, 257, 89, 32, 32);
            explosion_horizontal = new Sprite(Sheet1, 89, 5, 32, 32);
            explosion_horizontal1 = new Sprite(Sheet1, 89, 47, 32, 32);
            explosion_horizontal2 = new Sprite(Sheet1, 89, 89, 32, 32);
            explosion_left = new Sprite(Sheet1, 131, 5, 32, 32);
            explosion_left1 = new Sprite(Sheet1, 131, 47, 32, 32);
            explosion_left2 = new Sprite(Sheet1, 131, 89, 32, 32);
            explosion_right = new Sprite(Sheet1, 173, 5, 32, 32);
            explosion_right1 = new Sprite(Sheet1, 173, 47, 32, 32);
            explosion_right2 = new Sprite(Sheet1, 173, 89, 32, 32);
            explosion_top = new Sprite(Sheet1, 215, 5, 32, 32);
            explosion_top1 = new Sprite(Sheet1, 215, 47, 32, 32);
            explosion_top2 = new Sprite(Sheet1, 215, 89, 32, 32);
            explosion_bottom = new Sprite(Sheet1, 5, 5, 32, 32);
            explosion_bottom1 = new Sprite(Sheet1, 5, 47, 32, 32);
            explosion_bottom2 = new Sprite(Sheet1, 5, 89, 32, 32);

        } else {
            System.err.println("Failed to initialize player sprites from modern sheet. Player may not render correctly.");
        }

        if (Sheet2 != null && Sheet2.getSheet() != null && !Sheet2.getSheet().isError()) {
            // Enemies
            // BALLOM
            enemy_ballom_left1 = new Sprite(Sheet2, 5, 5, 32, 32);
            enemy_ballom_left2 = new Sprite(Sheet2, 5, 47, 32, 32);
            enemy_ballom_left3 = new Sprite(Sheet2, 5, 89, 32, 32);
            enemy_ballom_right1 = new Sprite(Sheet2, 5, 131, 32, 32);
            enemy_ballom_right2 = new Sprite(Sheet2, 5, 173, 32, 32);
            enemy_ballom_right3 = new Sprite(Sheet2, 5, 215, 32, 32);
            enemy_ballom_dead = new Sprite(Sheet2, 5, 257, 32, 32);
            // ONEAL
            enemy_oneal_left1 = new Sprite(Sheet2, 173, 5, 32, 32);
            enemy_oneal_left2 = new Sprite(Sheet2, 173, 47, 32, 32);
            enemy_oneal_left3 = new Sprite(Sheet2, 173, 89, 32, 32);
            enemy_oneal_right1 = new Sprite(Sheet2, 173, 131, 32, 32);
            enemy_oneal_right2 = new Sprite(Sheet2, 173,173, 32, 32);
            enemy_oneal_right3 = new Sprite(Sheet2, 173, 215, 32, 32);
            enemy_oneal_dead = new Sprite(Sheet2, 173, 257, 32, 32);
            // DOLL
            enemy_doll_left1 = new Sprite(Sheet2,  47, 5, 32, 32);
            enemy_doll_left2 = new Sprite(Sheet2,  47, 47, 32, 32);
            enemy_doll_left3 = new Sprite(Sheet2,  47, 89, 32, 32);
            enemy_doll_right1 = new Sprite(Sheet2,  47, 131, 32, 32);
            enemy_doll_right2 = new Sprite(Sheet2,  47, 173, 32, 32);
            enemy_doll_right3 = new Sprite(Sheet2,  47, 215, 32, 32);
            enemy_doll_dead = new Sprite(Sheet2,  47, 257, 32, 32);
            // MINVO
            enemy_minvo_left1 = new Sprite(Sheet2, 131, 5, 32, 32);
            enemy_minvo_left2 = new Sprite(Sheet2, 131, 47, 32, 32);
            enemy_minvo_left3 = new Sprite(Sheet2, 131, 89, 32, 32);
            enemy_minvo_right1 = new Sprite(Sheet2, 131, 131, 32, 32);
            enemy_minvo_right2 = new Sprite(Sheet2, 131, 173, 32, 32);
            enemy_minvo_right3 = new Sprite(Sheet2, 131, 215, 32, 32);
            enemy_minvo_dead = new Sprite(Sheet2, 131, 257, 32, 32);
            // KONDORIA
            enemy_kondoria_left1 = new Sprite(Sheet2, 89, 5, 32, 32);
            enemy_kondoria_left2 = new Sprite(Sheet2, 89, 47, 32, 32);
            enemy_kondoria_left3 = new Sprite(Sheet2, 89, 89, 32, 32);
            enemy_kondoria_right1 = new Sprite(Sheet2, 89, 131, 32, 32);
            enemy_kondoria_right2 = new Sprite(Sheet2, 89, 173, 32, 32);
            enemy_kondoria_right3 = new Sprite(Sheet2, 89, 215, 32, 32);
            enemy_kondoria_dead = new Sprite(Sheet2, 89, 257, 32, 32);
            // Mob Dead Animation
            mob_dead1 = new Sprite(Sheet2, 299, 89, 32, 32);
            mob_dead2 = new Sprite(Sheet2, 299, 131, 32, 32);
            mob_dead3 = new Sprite(Sheet2, 299, 173, 32, 32);

            // Powerups
            powerup_bombs = new Sprite(Sheet2, 0 , 10 , 32, 32);
            powerup_flames = new Sprite(Sheet2, 1 , 10 , 32, 32);
            powerup_speed = new Sprite(Sheet2, 2 , 10 , 32, 32);
            powerup_wallpass = new Sprite(Sheet2, 3 , 10 , 32, 32);
            powerup_detonator = new Sprite(Sheet2, 4 , 10 , 32, 32);
            powerup_bombpass = new Sprite(Sheet2, 5 , 10 , 32, 32);
            powerup_flamepass = new Sprite(Sheet2, 6 , 10 , 32, 32);

        } else {
            System.err.println("Failed to initialize sprites from NES sheet.");
        }
    }


    // --- Các phương thức tiện ích (có thể giữ lại nếu bạn dùng animation kiểu cũ) ---

    public static Sprite movingSprite(Sprite normal, Sprite x1, Sprite x2, int animate, int time) {
        int calc = animate % time;
        int diff = time / 3; // Chia thời gian cho 3 frame

        if (calc < diff) {
            return normal; // Frame 1
        }
        if (calc < diff * 2) {
            return x1;     // Frame 2
        }
        return x2;         // Frame 3
    }

    public static Sprite movingSprite(Sprite x1, Sprite x2, int animate, int time) {
        int diff = time / 2; // Chia thời gian cho 2 frame
        return (animate % time > diff) ? x1 : x2;
    }
}