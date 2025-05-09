// src/main/java/src/bomberman/core/Level.java
package src.bomberman.core; // HOẶC uet.oop.bomberman.core

// Import các lớp cần thiết
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import src.bomberman.Config;
import src.bomberman.entities.*;
import src.bomberman.entities.Portal;
import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet;
import src.bomberman.input.InputHandler; // Cần cho Player

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random; // Để tạo PowerUp ngẫu nhiên

import static src.bomberman.graphics.SpriteSheet.Sheet2;

/**
 * Quản lý bản đồ (map) của game, bao gồm việc tải dữ liệu map từ file
 * và tạo ra các thực thể ban đầu (Wall, Brick, Player, Enemies).
 */
public class Level {
    private int width; // Số ô chiều rộng của map
    private int height; // Số ô chiều cao của map
    private char[][] mapData; // Mảng 2D lưu trữ ký tự từ file map
    private Game game; // Tham chiếu ngược lại đối tượng Game

    private Random random = new Random(); // Để tạo powerup ngẫu nhiên

    /**
     * Constructor cho Level.
     * @param levelPath Đường dẫn đến file text chứa dữ liệu map.
     * @param game Tham chiếu đến đối tượng Game.
     */
    public Level(String levelPath, Game game) {
        this.game = game;
        System.out.println("[Level Constructor] Attempting to load level: " + levelPath);
        loadLevelFromFile(levelPath);
    }

    /**
     * Tải dữ liệu map từ một file text.
     * Dòng đầu tiên của file chứa chiều cao và chiều rộng.
     * Các dòng tiếp theo là các ký tự đại diện cho từng ô trên map.
     * @param path Đường dẫn đến file map.
     */
    private void loadLevelFromFile(String path) {
        System.out.println("[loadLevelFromFile] Loading from path: " + path);
        try (InputStream is = getClass().getResourceAsStream(path);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            if (is == null) { // <<< KIỂM TRA QUAN TRỌNG
                System.err.println("CRITICAL ERROR [loadLevelFromFile]: Could not find resource file: " + path + ". Check path and ensure file is in resources/levels and marked as resource.");
                this.width = 0; this.height = 0; // Đánh dấu là không load được
                return;
            }

            // Đọc dòng đầu tiên: height width
            String firstLine = reader.readLine();
            if (firstLine == null) {
                System.err.println("ERROR: Level file is empty: " + path);
                return;
            }
            String[] dimensions = firstLine.trim().split("\\s+"); // Tách bằng khoảng trắng
            if (dimensions.length < 2) {
                System.err.println("ERROR: Invalid dimensions format in level file (expected 'height width'): " + path);
                return;
            }
            this.height = Integer.parseInt(dimensions[0]);
            this.width = Integer.parseInt(dimensions[1]);
            System.out.println("[loadLevelFromFile] Successfully read dimensions: " + this.height + "x" + this.width);

            if (this.width <= 0 || this.height <= 0) {
                System.err.println("ERROR: Invalid level dimensions (<=0) in " + path + ". Dimensions: " + width + "x" + height);
                this.width = 0; this.height = 0; // Đặt về 0 để báo lỗi
                return;
            }

            mapData = new char[height][width];
            // Đọc các dòng tiếp theo của map
            for (int y = 0; y < height; y++) {
                String line = reader.readLine();
                if (line == null) {
                    System.err.println("ERROR: Unexpected end of file while reading map data at line " + (y + 2) + " in: " + path);
                    // Điền phần còn lại của map bằng ô trống nếu file bị thiếu dòng
                    for (int fillY = y; fillY < height; fillY++) {
                        java.util.Arrays.fill(mapData[fillY], ' ');
                    }
                    break; // Thoát vòng lặp đọc dòng
                }
                for (int x = 0; x < width; x++) {
                    if (x < line.length()) {
                        mapData[y][x] = line.charAt(x);
                    } else {
                        mapData[y][x] = ' '; // Nếu dòng map ngắn hơn chiều rộng, điền ô trống
                    }
                }
            }
            System.out.println("Map data loaded: " + width + "x" + height + " from " + path);
            createEntitiesFromMap(); // Tạo các thực thể dựa trên mapData

        } catch (NumberFormatException e) {
            System.err.println("ERROR: Invalid number format for dimensions in level file: " + path);
            this.width = 0; this.height = 0;
        } catch (Exception e) {
            System.err.println("ERROR: Could not load level file: " + path);
            e.printStackTrace();
            this.width = 0; this.height = 0; // Đặt về 0 để báo lỗi
        }
    }

    /**
     * Tạo ra các đối tượng Entity (Wall, Brick, Player, Enemy) dựa trên
     * các ký tự trong `mapData` và thêm chúng vào đối tượng Game.
     */
    private void createEntitiesFromMap() {
        System.out.println("[createEntitiesFromMap] Creating entities...");
        if (mapData == null || game == null || width == 0 || height == 0) {
            System.err.println("Cannot create entities from map: mapData, game is null, or dimensions are zero.");
            return;
        }

        // Lấy các tài nguyên cần thiết từ Game
        SpriteSheet modernSheet = game.getModernSheet();
        SpriteSheet nesSheet = game.getNesSheet();
        InputHandler input = game.getInputHandler(); // Cần cho Player

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char tileChar = mapData[y][x];
                // Tọa độ ô (x, y) sẽ được Entity constructor chuyển thành pixel
                switch (tileChar) {
                    case '#': // Wall - Tường cứng
                        game.addWall(new Wall(x, y, Sheet2));
                        break;
                    case '*': // Brick - Gạch vỡ được
                        // Brick sẽ tự quyết định có chứa powerup không
                        Brick brick = new Brick(x, y, Sheet2, this.game);
                        // TODO: Thêm logic quyết định PowerUp cho Brick ở đây hoặc trong constructor Brick
                        // Ví dụ: if (random.nextDouble() < 0.3) { // 30% cơ hội
                        //           PowerUp.PowerUpType type = PowerUp.PowerUpType.values()[random.nextInt(PowerUp.PowerUpType.values().length-1)]; // Trừ NONE
                        //           brick.setContainedPowerUp(type);
                        //        }
                        game.addBrick(brick);
                        break;
                    case 'p': // Player - Vị trí bắt đầu của người chơi
                        if (game.getPlayer() == null) { // Chỉ tạo Player nếu chưa có
                            game.addPlayer(new Player(x, y, modernSheet, input, game));
                        } else {
                            System.err.println("Warning: Multiple player ('p') start positions found in map file. Using the first one.");
                        }
                        mapData[y][x] = ' '; // Sau khi tạo Player, ô đó trở thành nền cỏ
                        break;
                    case '1':
                        game.addEnemy(new Ballom(x, y, nesSheet, game)); // Truyền game vào Enemy
                        mapData[y][x] = ' '; // Ô đó trở thành nền cỏ
                        break;
                    case '2':
                        game.addEnemy(new Kondoria(x, y, nesSheet, game));
                        mapData[y][x] = ' ';
                        break;
                    case 'x':
                        if (game.getPortal() == null) {
                            game.addPortal(new Portal(x, y, nesSheet));
                            System.out.println("[Level] Portal created at (" + x + "," + y + ")");
                        } else {
                            System.err.println("Warning: Multiple portal ('x') positions found. Using the first one.");
                        }
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
        System.out.println("[createEntitiesFromMap] Finished creating entities.");
    }

    /**
     * Vẽ lớp nền (background) của Level lên màn hình.
     * Lặp qua từng ô của map và vẽ sprite nền (ví dụ: cỏ).
     * Kích thước vẽ ra của mỗi sprite nền sẽ bằng `Config.TILE_SIZE`.
     * @param gc GraphicsContext của Canvas.
     */
    public void renderBackground(GraphicsContext gc) {
        Sprite grassSprite = Sprite.grass; // Lấy sprite nền đã được load

        // Kiểm tra xem sprite nền và sheet của nó có hợp lệ không
        if (mapData == null || width == 0 || height == 0 || grassSprite == null ||
                grassSprite.sheet == null || grassSprite.sheet.getSheet() == null ||
                grassSprite.sheet.getSheet().isError()) {

            // Nếu có lỗi, vẽ màu nền mặc định và báo lỗi
            gc.setFill(Color.DARKSLATEGRAY); // Một màu nền tối để dễ debug
            gc.fillRect(0, 0, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
            System.err.println("Cannot render background: Grass sprite, its Sheet is invalid/null, or map dimensions are zero.");
            return; // Không vẽ gì thêm
        }

        SpriteSheet sheet = grassSprite.sheet; // Lấy sheet chứa sprite cỏ


        // Vẽ nền cỏ cho toàn bộ map
        for (int yTile = 0; yTile < height; yTile++) { // Lặp qua hàng (ô tile)
            for (int xTile = 0; xTile < width; xTile++) { // Lặp qua cột (ô tile)
                // Tính toán tọa độ pixel để vẽ
                double dx = xTile * Config.TILE_SIZE;
                double dy = yTile * Config.TILE_SIZE;

                // Vẽ sprite nền cỏ tại vị trí ô map (xTile, yTile)
                // Vẽ nó với kích thước của một ô Tile chuẩn (Config.TILE_SIZE)
                gc.drawImage(
                        sheet.getSheet(),                 // Ảnh nguồn (spritesheet)
                        grassSprite.getSourceX(),         // sx: Tọa độ X của sprite cỏ trên sheet
                        grassSprite.getSourceY(),         // sy: Tọa độ Y của sprite cỏ trên sheet
                        grassSprite.getSourceWidth(),     // sw: Chiều rộng nguồn của sprite cỏ (ví dụ: 16)
                        grassSprite.getSourceHeight(),    // sh: Chiều cao nguồn của sprite cỏ (ví dụ: 16)
                        dx,                               // xTile * Config.TILE_SIZE
                        dy,                               // yTile * Config.TILE_SIZE
                        Config.TILE_SIZE,                 // dw
                        Config.TILE_SIZE                  // dh
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

    /**
     * Kiểm tra xem một ô tại tọa độ tile (tx, ty) có phải là vật cản rắn hay không.
     * Vật cản rắn bao gồm Wall ('#') và Brick ('*').
     * @param tx Chỉ số cột (tile X).
     * @param ty Chỉ số hàng (tile Y).
     * @return true nếu ô là Wall hoặc Brick, false nếu là ô trống hoặc ngoài bản đồ.
     */
    public boolean isSolidTile(int tx, int ty) {
        // Kiểm tra xem tọa độ có nằm trong bản đồ không
        if (tx < 0 || tx >= width || ty < 0 || ty >= height || mapData == null) {
            return true; // Coi như ngoài bản đồ là tường (vật cản rắn)
        }
        // Lấy ký tự tại ô đó
        char tileChar = mapData[ty][tx];
        // Kiểm tra xem có phải là Wall hoặc Brick không
        return tileChar == '#' || tileChar == '*';
    }

    /**
     * In bản đồ (mapData) ra console để debug.
     */
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