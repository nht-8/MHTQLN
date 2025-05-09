package src.bomberman.core; 

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import src.bomberman.Config;
import src.bomberman.entities.*;
import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet;
import src.bomberman.input.InputHandler; 

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static src.bomberman.graphics.SpriteSheet.Sheet2;

public class Level {
    private int width; 
    private int height; 
    private char[][] mapData;
    private Game game; 

    private Random random = new Random();

    public Level(String levelPath, Game game) {
        this.game = game;
        loadLevelFromFile(levelPath);
    }

    private void loadLevelFromFile(String path) {
        try (InputStream is = getClass().getResourceAsStream(path);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String firstLine = reader.readLine();
            if (firstLine == null) {
                System.err.println("ERROR: Level file is empty: " + path);
                return;
            }
            String[] dimensions = firstLine.trim().split("\\s+"); 
            if (dimensions.length < 2) {
                System.err.println("ERROR: Invalid dimensions format in level file (expected 'height width'): " + path);
                return;
            }
            this.height = Integer.parseInt(dimensions[0]);
            this.width = Integer.parseInt(dimensions[1]);

            if (this.width <= 0 || this.height <= 0) {
                System.err.println("ERROR: Invalid level dimensions (<=0) in " + path + ". Dimensions: " + width + "x" + height);
                this.width = 0; this.height = 0; 
                return;
            }

            mapData = new char[height][width];
           
            for (int y = 0; y < height; y++) {
                String line = reader.readLine();
                if (line == null) {
                    System.err.println("ERROR: Unexpected end of file while reading map data at line " + (y + 2) + " in: " + path);
                
                    for (int fillY = y; fillY < height; fillY++) {
                        java.util.Arrays.fill(mapData[fillY], ' ');
                    }
                    break;
                }
                for (int x = 0; x < width; x++) {
                    if (x < line.length()) {
                        mapData[y][x] = line.charAt(x);
                    } else {
                        mapData[y][x] = ' '; 
                    }
                }
            }
            System.out.println("Map data loaded: " + width + "x" + height + " from " + path);
            createEntitiesFromMap(); 

        } catch (NumberFormatException e) {
            System.err.println("ERROR: Invalid number format for dimensions in level file: " + path);
            this.width = 0; this.height = 0;
        } catch (Exception e) {
            System.err.println("ERROR: Could not load level file: " + path);
            e.printStackTrace();
            this.width = 0; this.height = 0;
        }
    }

    private void createEntitiesFromMap() {
        if (mapData == null || game == null || width == 0 || height == 0) {
            System.err.println("Cannot create entities from map: mapData, game is null, or dimensions are zero.");
            return;
        }

        SpriteSheet modernSheet = game.getModernSheet();
        SpriteSheet nesSheet = game.getNesSheet();
        InputHandler input = game.getInputHandler();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char tileChar = mapData[y][x];
               
                switch (tileChar) {
                    case '#': 
                        game.addWall(new Wall(x, y, Sheet2));
                        break;
                    case '*': 
                        Brick brick = new Brick(x, y, Sheet2, this.game);
                        
                        game.addBrick(brick);
                        break;
                    case 'p': 
                        if (game.getPlayer() == null) { 
                            game.addPlayer(new Player(x, y, modernSheet, input, game));
                        } else {
                            System.err.println("Warning: Multiple player ('p') start positions found in map file. Using the first one.");
                        }
                        mapData[y][x] = ' '; 
                        break;
                    case '1':
                        game.addEnemy(new Ballom(x, y, nesSheet, game)); 
                        mapData[y][x] = ' '; // Ô đó trở thành nền cỏ
                        break;
                    case '2':
                         game.addEnemy(new Kondoria(x, y, nesSheet, game));
                        mapData[y][x] = ' ';
                        break;
                    // TODO: Thêm các case cho các loại Enemy khác và PowerUp đặt sẵn trên map
                    // case 'b': game.addPowerUp(new PowerUp(x,y,nesSheet,PowerUp.PowerUpType.BOMBS)); mapData[y][x] = ' '; break;
                    // case 'f': game.addPowerUp(new PowerUp(x,y,nesSheet,PowerUp.PowerUpType.FLAMES)); mapData[y][x] = ' '; break;
                    // case 's': game.addPowerUp(new PowerUp(x,y,nesSheet,PowerUp.PowerUpType.SPEED)); mapData[y][x] = ' '; break;
                    default:
                        // Ký tự không xác định hoặc ' ' (ô trống) sẽ là nền cỏ (được vẽ bởi renderBackground)
                        break;
                }
            }
        }

        // Đảm bảo có Player nếu map không định nghĩa vị trí 'p'
        if (game.getPlayer() == null) {
            System.err.println("Warning: No player start ('p') found in map. Adding default player at (1,1).");
            if(input != null && modernSheet != null && modernSheet.getSheet() != null) {
                game.addPlayer(new Player(1, 1, modernSheet, input, game));
            } else {
                System.err.println("ERROR: Cannot add default player due to missing input or modernSheet.");
            }
        }
    }

    /**
     * Vẽ lớp nền (background) của Level lên màn hình.
     * Lặp qua từng ô của map và vẽ sprite nền (ví dụ: cỏ).
     * Kích thước vẽ ra của mỗi sprite nền sẽ bằng `Config.TILE_SIZE`.
     * @param gc GraphicsContext của Canvas (đã được dịch chuyển nếu cần).
     */
    public void renderBackground(GraphicsContext gc) {
        Sprite grassSprite = Sprite.grass; // Lấy sprite nền đã được load

        // Kiểm tra xem sprite nền và sheet của nó có hợp lệ không
        if (mapData == null || width == 0 || height == 0 || grassSprite == null ||
                grassSprite.sheet == null || grassSprite.sheet.getSheet() == null ||
                grassSprite.sheet.getSheet().isError()) {

            // Nếu có lỗi, vẽ màu nền mặc định và báo lỗi
            // Sử dụng kích thước vùng game từ Config đã cập nhật
            gc.setFill(Color.DARKSLATEGRAY); // Một màu nền tối để dễ debug
            gc.fillRect(0, 0, Config.GAME_AREA_WIDTH, Config.GAME_AREA_HEIGHT); // <<< SỬA Ở ĐÂY
            System.err.println("Cannot render background: Grass sprite, its Sheet is invalid/null, or map dimensions are zero.");
            return; // Không vẽ gì thêm
        }

        SpriteSheet sheet = grassSprite.sheet; // Lấy sheet chứa sprite cỏ

        // Vẽ nền cỏ cho toàn bộ map game (không bao gồm HUD)
        // Vòng lặp này dựa trên width và height của level (số ô), không phải kích thước màn hình pixel
        for (int yTile = 0; yTile < height; yTile++) { // Lặp qua hàng (ô tile)
            for (int xTile = 0; xTile < width; xTile++) { // Lặp qua cột (ô tile)
                // Tính toán tọa độ pixel để vẽ (tọa độ này là tương đối với gốc của gc,
                // mà gốc này có thể đã được dịch chuyển xuống dưới HUD bởi Renderer)
                double dx = xTile * Config.TILE_SIZE;
                double dy = yTile * Config.TILE_SIZE;

                gc.drawImage(
                        sheet.getSheet(),
                        grassSprite.getSourceX(),
                        grassSprite.getSourceY(),
                        grassSprite.getSourceWidth(),
                        grassSprite.getSourceHeight(),
                        dx,
                        dy,
                        Config.TILE_SIZE, // Vẽ mỗi ô cỏ với kích thước chuẩn
                        Config.TILE_SIZE
                );
            }
        }
    }

    // --- Getters ---
    /** Lấy số ô chiều rộng của map. */
    public int getWidth() { return width; }
    /** Lấy số ô chiều cao của map. */
    public int getHeight() { return height; }

    /**
     * Lấy ký tự tại một vị trí ô (tileX, tileY) trên bản đồ.
     * @param tileX Chỉ số cột của ô.
     * @param tileY Chỉ số hàng của ô.
     * @return Ký tự tại ô đó, hoặc '#' (Wall) nếu ra ngoài biên hoặc mapData null.
     */
    public char getTileChar(int tileX, int tileY) {
        if (mapData == null || tileX < 0 || tileX >= width || tileY < 0 || tileY >= height) {
            return '#'; // Coi như là tường nếu ra ngoài map hoặc map chưa load
        }
        return mapData[tileY][tileX];
    }

    public boolean isSolidTile(int tx, int ty) {
    
        if (tx < 0 || tx >= width || ty < 0 || ty >= height || mapData == null) {
            return true; 
        }
     
        char tileChar = mapData[ty][tx];
       
        return tileChar == '#' || tileChar == '*';
    }

    public void printMap() {
        System.out.println("--- Level Map (" + width + "x" + height + ") ---");
        if (mapData == null) {
            System.out.println("Map data is null.");
            return;
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                System.out.print(mapData[y][x]);
            }
            System.out.println();
        }
        System.out.println("--------------------------");
    }
}
