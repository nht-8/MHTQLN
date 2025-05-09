package src.bomberman.graphics; 

public class Sprite {

    public static final int DEFAULT_SIZE = 32;
    public static Sprite portal;
    
    public final double x; 
    public final double y; 
    public final double width; 
    public final double height; 
    public final SpriteSheet sheet; 

 
    public static Sprite grass;
    public static Sprite brick;
    public static Sprite wall;
    public static Sprite door;

    public static Sprite player_u1, player_u2, player_u3, player_u4;
    public static Sprite player_d1, player_d2, player_d3, player_d4;
    public static Sprite player_l1, player_l2, player_l3, player_l4;
    public static Sprite player_r1, player_r2, player_r3, player_r4;
    public static Sprite player_dead1, player_dead2, player_dead3;

    public static Sprite enemy_ballom_left1, enemy_ballom_left2, enemy_ballom_left3;
    public static Sprite enemy_ballom_right1, enemy_ballom_right2, enemy_ballom_right3;
    public static Sprite enemy_ballom_dead;
  
    public static Sprite enemy_oneal_left1, enemy_oneal_left2, enemy_oneal_left3;
    public static Sprite enemy_oneal_right1, enemy_oneal_right2, enemy_oneal_right3;
    public static Sprite enemy_oneal_dead;
   
    public static Sprite enemy_doll_left1, enemy_doll_left2, enemy_doll_left3;
    public static Sprite enemy_doll_right1, enemy_doll_right2, enemy_doll_right3;
    public static Sprite enemy_doll_dead;

    public static Sprite enemy_minvo_left1, enemy_minvo_left2, enemy_minvo_left3;
    public static Sprite enemy_minvo_right1, enemy_minvo_right2, enemy_minvo_right3;
    public static Sprite enemy_minvo_dead;
  
    public static Sprite enemy_kondoria_left1, enemy_kondoria_left2, enemy_kondoria_left3;
    public static Sprite enemy_kondoria_right1, enemy_kondoria_right2, enemy_kondoria_right3;
    public static Sprite enemy_kondoria_dead;
  
    public static Sprite mob_dead1, mob_dead2, mob_dead3;
    
    public static Sprite bomb, bomb_1, bomb_2;

    public static Sprite explosion_center, explosion_center1, explosion_center2; // Đổi tên bomb_exploded*
    public static Sprite explosion_vertical, explosion_vertical1, explosion_vertical2;
    public static Sprite explosion_horizontal, explosion_horizontal1, explosion_horizontal2;
    public static Sprite explosion_left, explosion_left1, explosion_left2;
    public static Sprite explosion_right, explosion_right1, explosion_right2;
    public static Sprite explosion_top, explosion_top1, explosion_top2;
    public static Sprite explosion_bottom, explosion_bottom1, explosion_bottom2;

    public static Sprite brick_exploded, brick_exploded1;
 
    public static Sprite powerup_bombs;
    public static Sprite powerup_flames;
    public static Sprite powerup_speed;
    public static Sprite powerup_heart;

    public Sprite(SpriteSheet sheet, double x, double y, double width, double height) {
        this.sheet = sheet;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

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

    public static void loadSprites(SpriteSheet Sheet1, SpriteSheet Sheet2) {

        if (Sheet1 != null && Sheet1.getSheet() != null && !Sheet1.getSheet().isError()) {

            // Board
            grass = new Sprite(Sheet1, 299, 47, 32, 32);
            wall = new Sprite(Sheet1, 299, 5, 32, 32);
            brick = new Sprite(Sheet1, 107, 257, 32, 32);
            // Bomb
            bomb = new Sprite(Sheet1, 47, 131, 32, 32); 
            bomb_1 = new Sprite(Sheet1, 89, 131, 32, 32); 
            bomb_2 = new Sprite(Sheet1, 131, 131, 32, 32); 

            brick_exploded = new Sprite(Sheet1, 191, 257, 32, 32);
            brick_exploded1 = new Sprite(Sheet1, 233, 257, 32, 32);

            player_d1 = new Sprite(Sheet1,5,173,24,32);
            player_d2 = new Sprite(Sheet1, 39, 173, 24, 32); 
            player_d3 = new Sprite(Sheet1, 73, 173, 24, 32); 
            player_d4 = new Sprite(Sheet1, 107, 173, 24, 32); 

            player_l1 = new Sprite(Sheet1, 243, 173, 24, 32);
            player_l2 = new Sprite(Sheet1, 5, 215, 24, 32);
            player_l3 = new Sprite(Sheet1, 39, 215, 24, 32);
            player_l4 = new Sprite(Sheet1, 73, 215, 24, 32);

            player_r1 = new Sprite(Sheet1, 107, 215, 24, 32);
            player_r2 = new Sprite(Sheet1, 141, 215, 24, 32);
            player_r3 = new Sprite(Sheet1, 175, 215, 24, 32);
            player_r4 = new Sprite(Sheet1, 209, 215, 24, 32);

            player_u1= new Sprite(Sheet1, 243, 215, 24, 32);
            player_u2 = new Sprite(Sheet1, 5, 257, 24, 32);
            player_u3 = new Sprite(Sheet1, 39, 257, 24, 32);
            player_u4 = new Sprite(Sheet1, 73, 257, 24, 32);

            player_dead1 = new Sprite(Sheet1, 141, 173, 24, 32);
            player_dead2 = new Sprite(Sheet1, 175, 173, 24, 32);
            player_dead3 = new Sprite(Sheet1, 209, 173, 24, 32);

            explosion_center = new Sprite(Sheet1, 47, 5, 32, 32); 
            explosion_center1 = new Sprite(Sheet1, 47, 47, 32, 32); 
            explosion_center2 = new Sprite(Sheet1, 47, 89, 32, 32); 
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

            portal = new Sprite(Sheet1,215,5,32,32);
        } else {
            System.err.println("Failed to initialize player sprites from modern sheet. Player may not render correctly.");
        }

        if (Sheet2 != null && Sheet2.getSheet() != null && !Sheet2.getSheet().isError()) {
           

            enemy_ballom_left1 = new Sprite(Sheet2, 5, 5, 32, 32);
            enemy_ballom_left2 = new Sprite(Sheet2, 5, 47, 32, 32);
            enemy_ballom_left3 = new Sprite(Sheet2, 5, 89, 32, 32);
            enemy_ballom_right1 = new Sprite(Sheet2, 5, 131, 32, 32);
            enemy_ballom_right2 = new Sprite(Sheet2, 5, 173, 32, 32);
            enemy_ballom_right3 = new Sprite(Sheet2, 5, 215, 32, 32);
            enemy_ballom_dead = new Sprite(Sheet2, 5, 257, 32, 32);
          
            enemy_oneal_left1 = new Sprite(Sheet2, 173, 5, 32, 32);
            enemy_oneal_left2 = new Sprite(Sheet2, 173, 47, 32, 32);
            enemy_oneal_left3 = new Sprite(Sheet2, 173, 89, 32, 32);
            enemy_oneal_right1 = new Sprite(Sheet2, 173, 131, 32, 32);
            enemy_oneal_right2 = new Sprite(Sheet2, 173,173, 32, 32);
            enemy_oneal_right3 = new Sprite(Sheet2, 173, 215, 32, 32);
            enemy_oneal_dead = new Sprite(Sheet2, 173, 257, 32, 32);
           
            enemy_doll_left1 = new Sprite(Sheet2,  47, 5, 32, 32);
            enemy_doll_left2 = new Sprite(Sheet2,  47, 47, 32, 32);
            enemy_doll_left3 = new Sprite(Sheet2,  47, 89, 32, 32);
            enemy_doll_right1 = new Sprite(Sheet2,  47, 131, 32, 32);
            enemy_doll_right2 = new Sprite(Sheet2,  47, 173, 32, 32);
            enemy_doll_right3 = new Sprite(Sheet2,  47, 215, 32, 32);
            enemy_doll_dead = new Sprite(Sheet2,  47, 257, 32, 32);
        
            enemy_minvo_left1 = new Sprite(Sheet2, 131, 5, 32, 32);
            enemy_minvo_left2 = new Sprite(Sheet2, 131, 47, 32, 32);
            enemy_minvo_left3 = new Sprite(Sheet2, 131, 89, 32, 32);
            enemy_minvo_right1 = new Sprite(Sheet2, 131, 131, 32, 32);
            enemy_minvo_right2 = new Sprite(Sheet2, 131, 173, 32, 32);
            enemy_minvo_right3 = new Sprite(Sheet2, 131, 215, 32, 32);
            enemy_minvo_dead = new Sprite(Sheet2, 131, 257, 32, 32);
           
            enemy_kondoria_left1 = new Sprite(Sheet2, 89, 5, 32, 32);
            enemy_kondoria_left2 = new Sprite(Sheet2, 89, 47, 32, 32);
            enemy_kondoria_left3 = new Sprite(Sheet2, 89, 89, 32, 32);
            enemy_kondoria_right1 = new Sprite(Sheet2, 89, 131, 32, 32);
            enemy_kondoria_right2 = new Sprite(Sheet2, 89, 173, 32, 32);
            enemy_kondoria_right3 = new Sprite(Sheet2, 89, 215, 32, 32);
            enemy_kondoria_dead = new Sprite(Sheet2, 89, 257, 32, 32);
         
            mob_dead1 = new Sprite(Sheet2, 299, 89, 32, 32);
            mob_dead2 = new Sprite(Sheet2, 299, 131, 32, 32);
            mob_dead3 = new Sprite(Sheet2, 299, 173, 32, 32);

           
            powerup_bombs = new Sprite(Sheet2, 299, 257 , 32, 32);
            powerup_flames = new Sprite(Sheet2, 89, 299 , 32, 32);
            powerup_speed = new Sprite(Sheet2, 131, 299 , 32, 32);
            powerup_heart = new Sprite(Sheet2, 215, 299 , 32, 32);

            door = new Sprite(Sheet2,299, 257,32,32);

        } else {
            System.err.println("Failed to initialize sprites from NES sheet.");
        }
    }

    public static Sprite movingSprite(Sprite normal, Sprite x1, Sprite x2, int animate, int time) {
        int calc = animate % time;
        int diff = time / 3;

        if (calc < diff) {
            return normal; 
        }
        if (calc < diff * 2) {
            return x1;     
        }
        return x2;         
    }

    public static Sprite movingSprite(Sprite x1, Sprite x2, int animate, int time) {
        int diff = time / 2;
        return (animate % time > diff) ? x1 : x2;
    }
}
