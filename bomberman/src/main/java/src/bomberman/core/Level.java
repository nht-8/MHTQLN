package src.bomberman.core;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import src.bomberman.Config;
import src.bomberman.entities.*;
import src.bomberman.entities.Portal;
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

import static src.bomberman.graphics.SpriteSheet.Sheet1;
import static src.bomberman.graphics.SpriteSheet.Sheet2;

public class Level {
    private int width;
    private int height;
    private char[][] mapData;
    private Game game;

    private Random random = new Random();

    private int portalTileX = -1;
    private int portalTileY = -1;

    private int playerStartX = 1; // Mặc định nếu không tìm thấy 'p'
    private int playerStartY = 1;

    public Level(String levelPath, Game game) {
        this.game = game;
        System.out.println("[Level Constructor] Attempting to load level: " + levelPath);
        loadLevelFromFile(levelPath);
    }

    private void loadLevelFromFile(String path) {
        System.out.println("[loadLevelFromFile] Loading from path: " + path);
        try (InputStream is = getClass().getResourceAsStream(path);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            if (is == null) {
                System.err.println("CRITICAL ERROR [loadLevelFromFile]: Could not find resource file: " + path + ". Check path and ensure file is in resources/levels and marked as resource.");
                this.width = 0; this.height = 0;
                return;
            }

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
            System.out.println("[loadLevelFromFile] Successfully read dimensions: " + this.height + "x" + this.width);

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

    public int getPortalTileY() {
        return portalTileY;
    }

    public int getPortalTileX() {
        return portalTileX;
    }

    public int getPlayerStartX() {
        return playerStartX;
    }

    public int getPlayerStartY() {
        return playerStartY;
    }

    private void createEntitiesFromMap() {

        boolean playerMarkerFound = false;
        System.out.println("[createEntitiesFromMap] Creating entities...");
        if (mapData == null || game == null || width == 0 || height == 0) {
            System.err.println("Cannot create entities from map: mapData, game is null, or dimensions are zero.");
            return;
        }

        SpriteSheet modernSheet = game.getModernSheet();
        SpriteSheet nesSheet = game.getNesSheet();
        InputHandler input = game.getInputHandler();
        boolean portalMarkerProcessed = false;

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
                    case 'x':
                        if (!portalMarkerProcessed) {
                            this.portalTileX = x; // Lưu tọa độ logic của Portal cho Level
                            this.portalTileY = y;

                            // 1. Tạo đối tượng Portal
                            Portal gamePortal = new Portal(x, y, nesSheet, this.game);
                            game.setCurrentLevelPortal(gamePortal); // Thông báo cho Game về Portal này

                            // 2. Tạo một Brick để che Portal này
                            Brick portalCoverBrick = new Brick(x, y, nesSheet, this.game);
                            portalCoverBrick.setContainedPowerUpType(PowerUp.PowerUpType.NONE); // Gạch che Portal không chứa PowerUp
                            game.addBrick(portalCoverBrick); // Thêm gạch này vào danh sách như bình thường

                            System.out.println("Portal location defined at TILE (" + x + "," + y + ") and is covered by a Brick.");
                            portalMarkerProcessed = true;
                            // Giữ nguyên ký tự '*' hoặc ' ' trong mapData ở đây để logic isSolidTile
                            // của Level vẫn coi ô đó là gạch/trống cho đến khi gạch bị phá.
                            // Hoặc bạn có thể thay mapData[y][x] = '*' nếu muốn nó chắc chắn là gạch.
                            // Hiện tại Level.isSolidTile dựa vào map ký tự, nên đặt '*' là an toàn.
                            mapData[y][x] = '*'; // Đảm bảo ô Portal được coi là gạch ban đầu

                        } else {
                            System.err.println("Warning: Multiple portal markers ('X') found. Using first one. Treating extra 'X' at TILE (" + x + "," + y + ") as a normal Brick.");
                            Brick extraBrick = new Brick(x, y, nesSheet, this.game);
                            game.addBrick(extraBrick);
                            mapData[y][x] = '*'; // Coi như gạch thường
                        }
                        break;
                    case 'p':
                        if (!playerMarkerFound) { // Chỉ xử lý ký tự 'p' đầu tiên
                            this.playerStartX = x; // Lưu tọa độ Player Start
                            this.playerStartY = y;
                            game.addPlayer(new Player(x, y, modernSheet, input, game));
                            playerMarkerFound = true;
                        } else {
                            System.err.println("Warning: Multiple player ('p') start positions. Using first one.");
                        }
                        mapData[y][x] = ' '; // Ô đó trở thành nền cỏ
                        break;
                    case '1':
                        game.addEnemy(new Ballom(x, y, Sheet2, game));
                        mapData[y][x] = ' ';
                        break;
                    case '2':
                        game.addEnemy(new Kondoria(x, y, Sheet2, game));
                        mapData[y][x] = ' ';
                        break;
                    case '3':
                        game.addEnemy(new Doll(x, y, Sheet2, game));
                        mapData[y][x] = ' ';
                        break;
                    case '4':
                        game.addEnemy(new Oneal(x, y, Sheet2, game));
                        mapData[y][x] = ' ';
                        break;
                    case '5':
                        game.addEnemy(new Pass(x, y, Sheet2, game));
                        mapData[y][x] = ' ';
                        break;
                    case '6':
                        game.addEnemy(new Ovapi(x, y, Sheet2, game));
                        mapData[y][x] = ' ';
                        break;
                    case '7':
                        game.addEnemy(new Minvo(x, y, Sheet2, game));
                        mapData[y][x] = ' ';
                        break;
                    default:
                        break;
                }
            }
        }

        if (!playerMarkerFound) {
            System.err.println("Warning: No player start ('p') in map. Adding default player at (" + playerStartX + "," + playerStartY + ").");
            if (input != null && modernSheet != null && modernSheet.getSheet() != null) {
                game.addPlayer(new Player(playerStartX, playerStartY, modernSheet, input, game));
            }
        }

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

    public void renderBackground(GraphicsContext gc) {
        Sprite grassSprite = Sprite.grass;

        if (mapData == null || width == 0 || height == 0 || grassSprite == null ||
                grassSprite.sheet == null || grassSprite.sheet.getSheet() == null ||
                grassSprite.sheet.getSheet().isError()) {

            gc.setFill(Color.DARKSLATEGRAY);
            gc.fillRect(0, 0, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
            System.err.println("Cannot render background: Grass sprite, its Sheet is invalid/null, or map dimensions are zero.");
            return;
        }

        SpriteSheet sheet = grassSprite.sheet;

        for (int yTile = 0; yTile < height; yTile++) {
            for (int xTile = 0; xTile < width; xTile++) {

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
                        Config.TILE_SIZE,
                        Config.TILE_SIZE
                );
            }
        }
    }


    public int getWidth() { return width; }

    public int getHeight() { return height; }

    public char getTileChar(int tileX, int tileY) {
        if (mapData == null || tileX < 0 || tileX >= width || tileY < 0 || tileY >= height) {
            return '#';
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
