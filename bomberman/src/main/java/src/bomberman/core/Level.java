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

    private void createEntitiesFromMap() {
        System.out.println("[createEntitiesFromMap] Creating entities...");
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
                            game.addPlayer(new Player(x, y, Sheet1, input, game));
                        } else {
                            System.err.println("Warning: Multiple player ('p') start positions found in map file. Using the first one.");
                        }
                        mapData[y][x] = ' '; 
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
                    case 'x':
                        System.out.println("[Level DEBUG] Found 'x' for Portal at (" + x + "," + y + ")");
                        if (game.getPortal() == null) {
                            game.addPortal(new Portal(x, y, nesSheet)); 
                            
                            mapData[y][x] = ' '; 
                        } else {
                            System.err.println("[Level DEBUG] Portal already exists in this level, not creating new one.");
                        }
                        break;
                    default:
                        break;
                }
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
