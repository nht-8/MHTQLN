package src.bomberman.core;

import javafx.geometry.Rectangle2D;
import javafx.scene.input.KeyCode;
import src.bomberman.Config;
import src.bomberman.entities.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import src.bomberman.graphics.FloatingText;
import src.bomberman.graphics.SpriteSheet;
import src.bomberman.input.InputHandler;
import src.bomberman.sound.SoundManager;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Game {

    public enum GameState {LEVEL_STARTING, PLAYING, LEVEL_COMPLETE, GAME_OVER, GAME_WON, PAUSED}

    private GameState currentState = GameState.LEVEL_STARTING;
    private int playerInitialTileX = 1;
    private int playerInitialTileY = 1;

    private Level level;
    private Player player;
    private List<Enemy> enemies;
    private List<Bomb> bombs;
    private List<Explosion> explosions;
    private List<Entity> staticEntities;
    private List<PowerUp> powerUps;
    private InputHandler inputHandler;
    private Portal currentLevelPortal = null;
    private GameState previousStateBeforePause;
    private List<FloatingText> floatingTexts;

    private int currentLevelNumber = 1;
    private int playerScore = 0;
    private int playerLives = 3;
    private int maxLevels = 5;

    public Game(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
        this.playerLives = Config.PLAYER_INIT_LIVES;
        this.enemies = new ArrayList<>();
        this.bombs = new ArrayList<>();
        this.floatingTexts = new CopyOnWriteArrayList<>();
        this.explosions = new ArrayList<>();
        this.staticEntities = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        loadLevel(currentLevelNumber, true);
    }

    public void loadLevel(int levelNumber, boolean fullReset) {
        if (fullReset) {
            System.out.println("Loading level " + levelNumber + "...");
            this.currentLevelNumber = levelNumber;

            enemies.clear();
            bombs.clear();
            explosions.clear();
            staticEntities.clear();
            powerUps.clear();
            currentLevelPortal = null;
            player = null;
            if(floatingTexts!=null) floatingTexts.clear();

            level = new Level(Config.LEVEL_PATH_PREFIX + "level" + levelNumber + ".txt", this);

            if (level.getWidth() == 0 || level.getHeight() == 0) {
                currentState = GameState.GAME_OVER;
                return;
            }

            currentState = GameState.LEVEL_STARTING;


            SoundManager.getInstance().playBackgroundMusic(SoundManager.GAME_BGM,true);

            if (player != null) {
                player = new Player(1, 1, getModernSheet(), inputHandler, this);
                addPlayer(player);
            }

            currentState = GameState.LEVEL_STARTING;
        } else { // Chỉ hồi sinh Player (fullReset = false)
            if (level == null || player == null) { // Không thể hồi sinh nếu chưa có level hoặc player ban đầu
                System.err.println("ERROR: Cannot respawn player. Level or initial player is null. Forcing full reset.");
                loadLevel(this.currentLevelNumber, true); // Buộc full reset nếu có lỗi
                return;
            }
            System.out.println("Respawning Player in current Level " + this.currentLevelNumber + "...");
            // Không clear enemies, staticEntities, powerUps, currentLevelPortal
            // Chỉ xóa bom và vụ nổ hiện tại (có thể là của lần chơi trước)
            bombs.clear();
            explosions.clear();

            player = null; // Xóa instance Player cũ

            // Tạo Player mới tại vị trí bắt đầu đã lưu của level hiện tại
            player = new Player(level.getPlayerStartX(), level.getPlayerStartY(),
                    getModernSheet(), inputHandler, this);
            // addPlayer(player); // Không cần gọi lại nếu this.player đã được gán trực tiếp

            currentState = GameState.PLAYING; // Có thể vào game ngay hoặc có delay hồi sinh ngắn
            System.out.println("Player respawned at (" + player.getTileX() + "," + player.getTileY() + ")");
        }
        System.out.println("Level setup. Current state: " + currentState);
    }


    public void update(double deltaTime) {
        handleNonPlayingStateInput();

        // 1. Xử lý input cho các trạng thái không phải là PLAYING
        if (currentState != GameState.PLAYING && currentState != GameState.LEVEL_STARTING) {
            if (inputHandler.isPressed(KeyCode.ENTER)) {
                inputHandler.releaseKey(KeyCode.ENTER); // Tránh lặp lại
                if (currentState == GameState.LEVEL_COMPLETE) {
                    prepareNextLevel();
                } else if (currentState == GameState.GAME_OVER || currentState == GameState.GAME_WON) {
                    restartGame();
                }
            }
            // TODO: Xử lý input cho PAUSE nếu có
            return; // Không làm gì thêm nếu không phải PLAYING hoặc LEVEL_STARTING
        }

        // 2. Xử lý trạng thái chờ bắt đầu level
        if (currentState == GameState.LEVEL_STARTING) {
            currentState = GameState.PLAYING;
            return; // Chỉ vẽ, không update logic động
        }

        if (currentState != GameState.PLAYING) {
            handleNonPlayingStateInput();
            return;
        }


        // --- Từ đây trở đi, currentState chắc chắn là PLAYING ---

        // 3. Cập nhật Player
        if (player != null) {
            if (player.isAlive() || player.isDying()) {
                player.update(deltaTime, getAllEntities()); // Player update và tự xử lý va chạm movement
            }
        }

        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            if (enemy.isAlive()) {
                enemy.update(deltaTime, getAllEntities());
                if (player != null && player.isAlive() && enemy.intersects(player)) {
                    player.destroy();
                }
            } else if (enemy.isDying()) {
                enemy.update(deltaTime, null);
            } else {
                enemyIterator.remove();
                addScore(Config.POINTS_PER_ENEMY);
            }
        }


        Iterator<Bomb> bombIterator = bombs.iterator();
        while (bombIterator.hasNext()) {
            Bomb bomb = bombIterator.next();
            if (bomb.isAlive()) {
                bomb.update(deltaTime, null);
            } else {
                bombIterator.remove();
            }
        }

        Iterator<Explosion> explosionIterator = explosions.iterator();
        while (explosionIterator.hasNext()) {
            Explosion explosion = explosionIterator.next();
            if (explosion.isAlive()) {
                explosion.update(deltaTime, null);
            } else {
                explosionIterator.remove();
            }
        }


        Iterator<Entity> staticIterator = staticEntities.iterator();
        while (staticIterator.hasNext()) {
            Entity entity = staticIterator.next();
            if (entity instanceof Brick) {
                Brick brick = (Brick) entity;
                if (brick.isAlive() || brick.isDying()) {
                    brick.update(deltaTime, null);
                } else { // Gạch đã vỡ hoàn toàn (alive=false, dying=false)
                    staticIterator.remove();
                    // ===>>> KIỂM TRA VÀ KÍCH HOẠT PORTAL NẾU NÓ TỒN TẠI <<<===
                    if (currentLevelPortal != null && !currentLevelPortal.isRevealed()) {
                        // Nếu tọa độ ô của Brick trùng với tọa độ ô của Portal
                        if (brick.getTileX() == currentLevelPortal.getTileX() &&
                                brick.getTileY() == currentLevelPortal.getTileY()) {
                            System.out.println("Brick covering portal AT TILE (" + currentLevelPortal.getTileX() + "," + currentLevelPortal.getTileY() + ") destroyed.");
                            currentLevelPortal.setRevealed(true); // Kích hoạt Portal
                        }
                    }
                    // ===>>> KẾT THÚC KIỂM TRA PORTAL <<<===
                    // Logic spawn PowerUp giữ nguyên
                }
            } else if (!entity.isAlive()) {
                staticIterator.remove();
            }
        }
        // ... (Cập nhật PowerUps) ...

        // ... (Xử lý player chết) ...


        Iterator<PowerUp> powerUpIterator = powerUps.iterator();
        while (powerUpIterator.hasNext()) {
            PowerUp pu = powerUpIterator.next();
            if (pu.isAlive()) {
                pu.update(deltaTime, null);
                if (player != null && player.isAlive() && player.getBounds().intersects(pu.getBounds())) {
                    pu.collect(player); // PowerUp tự xử lý hiệu ứng và đặt alive=false
                }
            } else {
                powerUpIterator.remove();
            }
        }

        checkWinCondition();
        if (player != null && !player.isAlive() && !player.isDying()) { // Player đã chết hẳn
            handlePlayerDeathOutcomeInternal(); // Đổi tên để tránh nhầm lẫn
        }


        Iterator<FloatingText> ftIterator = floatingTexts.iterator();
        while (ftIterator.hasNext()) {
            FloatingText ft = ftIterator.next();
            if (ft.isAlive()) {
                ft.update(deltaTime);
            }
        }
        floatingTexts.removeIf(ft -> !ft.isAlive());
        handleNonPlayingStateInput();

    }

    private void handleNonPlayingStateInput() {
        if (inputHandler.isPressed(KeyCode.P)) {
            inputHandler.releaseKey(KeyCode.P);
            togglePause();
        }
        if (inputHandler.isPressed(KeyCode.ENTER)) {
            inputHandler.releaseKey(KeyCode.ENTER);
            if (currentState == GameState.LEVEL_COMPLETE) {
                if (currentLevelNumber >= maxLevels) {
                    restartGame();
                } else {
                    prepareNextLevel();
                }
            } else if (currentState == GameState.GAME_OVER || currentState == GameState.GAME_WON) {
                restartGame();
            }
        }
    }

    private void checkWinCondition() {
        if (currentState != GameState.PLAYING || player == null || !player.isAlive()) return;

        // Điều kiện thắng: Tất cả enemies đã bị tiêu diệt VÀ Portal đã xuất hiện VÀ Player đang đứng trên Portal
        if (enemies.isEmpty()) {
            if (currentLevelPortal != null && currentLevelPortal.isAlive() && currentLevelPortal.isRevealed()) {
                // Kiểm tra Player có đứng trên ô của currentLevelPortal không
                if (player.getTileX() == currentLevelPortal.getTileX() && player.getTileY() == currentLevelPortal.getTileY()) {
                    levelComplete();

                }
            } else if (level != null && level.getPortalTileX() == -1) {
                // Nếu map không định nghĩa Portal và hết enemy -> thắng
                System.out.println("Level Won (No Portal Defined & All Enemies Cleared).");
                levelComplete();
            }
        }
    }

    private void handlePlayerDeathOutcomeInternal() {
        if (currentState == GameState.PLAYING && player != null && !player.isAlive() && !player.isDying()) {
            playerLives--;
            System.out.println("Player permanently died. Lives remaining: " + playerLives);

            double textX = player.getX(); // Hoặc Config.WINDOW_WIDTH / 2.0 - 50;
            double textY = player.getY() - 20; // Hoặc Config.WINDOW_HEIGHT / 2.0 - 50;
            Font playerDeathFont = Font.font("Arial", FontWeight.BOLD, 24);
            addFloatingText("-1 LIFE", textX, textY, Color.RED, playerDeathFont, 2000, -20);

            if (playerLives <= 0) {
                handleGameOver();
            } else {
                System.out.println("Respawning player (map state preserved)...");
                // ===>>> GỌI loadLevel VỚI fullReset = false <<<===
                loadLevel(currentLevelNumber, false);
            }
        }
    }

    private void levelComplete() {
        System.out.println("LEVEL " + currentLevelNumber + " COMPLETE!");
        currentState = GameState.LEVEL_COMPLETE; // Chuyển trạng thái game
        SoundManager.getInstance().playSound(SoundManager.LEVEL_COMPLETED); // Phát âm thanh hoàn thành level
        SoundManager.getInstance().stopBackgroundMusic(); // Dừng nhạc nền hiện tại
        double screenCenterX = Config.WINDOW_WIDTH / 2.0;
        double screenCenterY = Config.WINDOW_HEIGHT / 2.0;
        Font levelCompleteFont = Font.font("Arial", FontWeight.BOLD, 48);
        addFloatingText("LEVEL " + currentLevelNumber + " COMPLETE!", screenCenterX - 250,
                screenCenterY - 50, Color.LIGHTGREEN, levelCompleteFont, 3000, 0);
        if (currentLevelNumber >= maxLevels) {
            currentState = GameState.GAME_WON; // Chuyển sang trạng thái thắng game
            Font gameWonFont = Font.font("Arial", FontWeight.BOLD, 60);
            addFloatingText("YOU WON!", screenCenterX - 180, screenCenterY + 30, Color.GOLD, gameWonFont, 5000, 0);
            addFloatingText("Press ENTER to Restart", screenCenterX - 150, screenCenterY + 100, Color.WHITE, Font.font("Arial", 20), 5000, 0);
        } else {
            addFloatingText("Press ENTER for Next Level", screenCenterX - 200, screenCenterY + 30, Color.WHITE, Font.font("Arial", 20), 3000, 0);
        }
    }

    public void prepareNextLevel() {
        if (currentState == GameState.LEVEL_COMPLETE) {
            if (currentLevelNumber < maxLevels) {
                // ===>>> GỌI loadLevel VỚI fullReset = true <<<===
                loadLevel(currentLevelNumber + 1, true);
            } else {
                currentState = GameState.GAME_WON;
                System.out.println("All levels completed! Game Won!");
            }
        }
    }

    private void restartGame() {
        System.out.println("Restarting game from Level 1...");
        this.playerScore = 0;
        this.playerLives = 3;
        // ===>>> GỌI loadLevel VỚI fullReset = true <<<===
        loadLevel(1, true);
    }

    public void addPlayer(Player p) {
        this.player = p;
    }

    public void addEnemy(Enemy e) {
        this.enemies.add(e);
    }

    public void addWall(Wall w) {
        this.staticEntities.add(w);
    }

    public void addBrick(Brick b) {
        this.staticEntities.add(b);
    }

    public void addPowerUp(PowerUp pu) {
        this.powerUps.add(pu);
    }

    public void addBomb(Bomb b) {
        this.bombs.add(b);
    }


    public void addExplosion(int tileX, int tileY, int flameLength, SpriteSheet sheet) {
        List<Explosion> newExplosionsThisTurn = new ArrayList<>();
        Explosion centerExplosion = new Explosion(tileX, tileY, sheet, Explosion.ExplosionType.CENTER);
        explosions.add(centerExplosion);
        newExplosionsThisTurn.add(centerExplosion);

        createFlameSegments(tileX, tileY, 1, 0, flameLength, sheet, newExplosionsThisTurn);
        createFlameSegments(tileX, tileY, -1, 0, flameLength, sheet, newExplosionsThisTurn);
        createFlameSegments(tileX, tileY, 0, 1, flameLength, sheet, newExplosionsThisTurn);
        createFlameSegments(tileX, tileY, 0, -1, flameLength, sheet, newExplosionsThisTurn);

        List<Entity> allCurrentEntities = getAllEntities();
        for (Explosion exp : newExplosionsThisTurn) {
            if (exp.isAlive()) {
                exp.checkInitialCollisions(allCurrentEntities);
            }
        }
    }

    private void createFlameSegments(int startX, int startY, int dx, int dy, int length,
                                     SpriteSheet sheet, List<Explosion> newExplosionsList) {
        for (int i = 1; i <= length; i++) {
            int currentX = startX + i * dx;
            int currentY = startY + i * dy;

            Entity blockingEntity = getSolidEntityAtTile(currentX, currentY);
            if (blockingEntity instanceof Wall) {
                break;
            }

            Explosion.ExplosionType type;
            if (i < length) {
                type = (dx != 0) ? Explosion.ExplosionType.HORIZONTAL_MIDDLE : Explosion.ExplosionType.VERTICAL_MIDDLE;
            } else {
                if (dx > 0) type = Explosion.ExplosionType.END_RIGHT;
                else if (dx < 0) type = Explosion.ExplosionType.END_LEFT;
                else if (dy > 0) type = Explosion.ExplosionType.END_DOWN;
                else type = Explosion.ExplosionType.END_UP;
            }
            Explosion segment = new Explosion(currentX, currentY, sheet, type);
            explosions.add(segment);
            newExplosionsList.add(segment);

            if (blockingEntity instanceof Brick) {
                break;
            }
            if (currentX < 0 || currentX >= level.getWidth() || currentY < 0 || currentY >= level.getHeight()) {
                break;
            }
        }
    }

    public Entity getSolidEntityAtTile(int tileX, int tileY) {
        if (level == null || tileX < 0 || tileX >= level.getWidth() || tileY < 0 || tileY >= level.getHeight()) {
            return null;
        }
        for (Entity entity : staticEntities) {
            if (entity.getTileX() == tileX && entity.getTileY() == tileY && entity.isAlive()) {
                if (entity instanceof Wall || entity instanceof Brick) {
                    return entity;
                }
            }
        }
        for (Bomb bomb : bombs) {
            if (bomb.getTileX() == tileX && bomb.getTileY() == tileY && bomb.isSolid()) {
                return bomb;
            }
        }
        return null;
    }

    public boolean canPlaceBombAt(int tileX, int tileY) {
        Entity entityAtTarget = getSolidEntityAtTile(tileX, tileY);
        return !(entityAtTarget instanceof Bomb || entityAtTarget instanceof Wall || entityAtTarget instanceof Brick);
    }

    public int getCurrentLevelNumber() {
        return currentLevelNumber;
    }

    public int getPlayerScore() {
        return playerScore;
    }

    public int getPlayerLives() {
        return playerLives;
    }

    public void addScore(int points) {
        if (playerLives > 0) {
            this.playerScore += points;
        }
    }

    private void handleGameOver() {
        if (currentState != GameState.GAME_OVER) {
            SoundManager.getInstance().stopBackgroundMusic();
            System.out.println("GAME OVER - Final Score: " + playerScore);
            SoundManager.getInstance().playSound(SoundManager.GAME_OVER);
        }
        if (player != null) {
            player.setPermanentlyDeadNoUpdates();
        }
    }


    private void handleGameWin() {
        System.out.println("YOU WIN! - Final Score: " + playerScore);
        if (player != null) {
            player.setPermanentlyDeadNoUpdates();
        }
    }

    public void setCurrentLevelPortal(Portal p) {
        if (this.currentLevelPortal == null) this.currentLevelPortal = p;
        else System.err.println("Attempted to set multiple portals.");
    }

    public Portal getCurrentLevelPortal() {
        return currentLevelPortal;
    }

    public Level getLevel() {
        return level;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Bomb> getBombs() {
        return bombs;
    }

    public List<Explosion> getExplosions() {
        return explosions;
    }

    public List<Entity> getStaticEntities() {
        return staticEntities;
    }

    public List<PowerUp> getPowerUps() {
        return powerUps;
    }

    public List<Entity> getAllEntities() {
        List<Entity> all = new ArrayList<>();
        all.addAll(staticEntities);
        if (player != null && (player.isAlive() || player.isDying())) {
            all.add(player);
        }
        for (Enemy e : enemies) {
            if (e.isAlive() || e.isDying()) all.add(e);
        }
        for (Bomb b : bombs) {
            if (b.isAlive() && b.isSolid()) all.add(b);
        }
        for (PowerUp pu : powerUps) {
            if (pu.isAlive()) all.add(pu);
        }

        return all;
    }

    public SpriteSheet getModernSheet() {
        return SpriteSheet.Sheet1;
    }

    public SpriteSheet getNesSheet() {
        return SpriteSheet.Sheet2;
    }

    public InputHandler getInputHandler() {
        return inputHandler;
    }

    public void setPlayerInitialPosition(int tileX, int tileY) {
        this.playerInitialTileX = tileX;
        this.playerInitialTileY = tileY;
        if (this.player != null) {
            this.player.setInitialPosition(tileX, tileY);
        }
    }

    public void togglePause() {
        if (currentState == GameState.PAUSED) {
            // Nếu đang PAUSED, quay lại trạng thái trước đó (thường là PLAYING)
            if (previousStateBeforePause != null) {
                currentState = previousStateBeforePause;
            } else {
                currentState = GameState.PLAYING; // Fallback nếu không có trạng thái trước
            }
            floatingTexts.removeIf(ft -> ft.getText().equals("PAUSED"));
            System.out.println("Game Resumed. Current state: " + currentState);
        } else if (currentState == GameState.PLAYING || currentState == GameState.LEVEL_STARTING) {
            previousStateBeforePause = currentState; // Lưu lại trạng thái hiện tại
            currentState = GameState.PAUSED;
            System.out.println("Game Paused. Press P to Resume.");
            double screenCenterX = Config.WINDOW_WIDTH / 2.0;
            double screenCenterY = Config.WINDOW_HEIGHT / 2.0;
            Font pauseFont = Font.font("Arial", FontWeight.BOLD, 72);
            addFloatingText("PAUSED", screenCenterX - 150, screenCenterY, Color.YELLOW, pauseFont, Long.MAX_VALUE, 0);
        }
    }

    public void addFloatingText(String text, double xPixel, double yPixel, Color color, Font font, long durationMillis, double ySpeed) {
        this.floatingTexts.add(new FloatingText(text, xPixel, yPixel, color, font, durationMillis, ySpeed));
    }

    public List<FloatingText> getFloatingTexts() {
        return floatingTexts;
    }
}
