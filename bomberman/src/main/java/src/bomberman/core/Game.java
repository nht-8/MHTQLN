// src/main/java/src/bomberman/core/Game.java
package src.bomberman.core;

import javafx.geometry.Rectangle2D;
import javafx.scene.input.KeyCode;
import src.bomberman.Config;
import src.bomberman.entities.*;
import src.bomberman.graphics.SpriteSheet;
import src.bomberman.input.InputHandler;
import src.bomberman.sound.SoundManager;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Game {
<<<<<<< Updated upstream
    private int playerInitialTileX = 1; // Mặc định nếu không có trong map
=======

    public enum GameState { LEVEL_STARTING, PLAYING, LEVEL_COMPLETE, GAME_OVER, GAME_WON, PAUSED }

    private GameState currentState = GameState.LEVEL_STARTING;
    private int playerInitialTileX = 1;
>>>>>>> Stashed changes
    private int playerInitialTileY = 1;

    private Level level;
    private Player player;
    private List<Enemy> enemies;
    private List<Bomb> bombs;
    private List<Explosion> explosions;
    private List<Entity> staticEntities;
    private List<PowerUp> powerUps;
    private InputHandler inputHandler;
<<<<<<< Updated upstream
    private Portal portal; // Từ phiên bản 1
=======
    private Portal currentLevelPortal = null;
>>>>>>> Stashed changes

    // Biến cho trạng thái game và HUD
    private int currentLevelNumber = 1; // Bắt đầu từ level 1
    private int playerScore = 0;
<<<<<<< Updated upstream
    private int playerLives;
    private static final int MAX_LEVELS = 5; // Từ phiên bản 1 (bạn có thể điều chỉnh)
    private boolean allEnemiesDefeatedAndPortalActive = false; // Cờ để kiểm tra điều kiện qua màn bằng Portal
=======
    private int playerLives = 3;
    private int maxLevels = 5;
>>>>>>> Stashed changes

    public Game(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
        this.playerLives = Config.PLAYER_INIT_LIVES;


        this.enemies = new ArrayList<>();
        this.bombs = new ArrayList<>();
        this.explosions = new ArrayList<>();
        this.staticEntities = new ArrayList<>();
        this.powerUps = new ArrayList<>();
<<<<<<< Updated upstream

        loadLevel(this.currentLevelNumber);
    }

    public void loadLevel(int levelNumber) {
        System.out.println("Loading level " + levelNumber + "...");
        this.currentLevelNumber = levelNumber;
        this.allEnemiesDefeatedAndPortalActive = false; // Reset cờ khi load level mới

        enemies.clear();
        bombs.clear();
        explosions.clear();
        staticEntities.clear();
        powerUps.clear();
        portal = null; // Reset portal khi load level mới
=======
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
>>>>>>> Stashed changes

            level = new Level(Config.LEVEL_PATH_PREFIX + "level" + levelNumber + ".txt", this);

            if (level.getWidth() == 0 || level.getHeight() == 0) {
                currentState = GameState.GAME_OVER;
                return;
            }

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
<<<<<<< Updated upstream

        if (player != null) {
            player.setInitialPosition(playerInitialTileX, playerInitialTileY); // playerInitialTileX/Y được Level cập nhật
            player.resetToStartPositionAndRevive(); // Đặt player về vị trí đầu màn và hồi sinh
            System.out.println("Player initial position for level " + levelNumber + " set to: (" + playerInitialTileX + "," + playerInitialTileY + ")");
        }




        System.out.println("Level " + levelNumber + " loaded successfully.");

=======
        System.out.println("Level setup. Current state: " + currentState);
>>>>>>> Stashed changes
    }


    public void update(double deltaTime) {
<<<<<<< Updated upstream
        // Kiểm tra Game Over ngay từ đầu nếu không còn mạng và Player đã hoàn toàn chết
        if (playerLives <= 0 && (player == null || (!player.isAlive() && !player.isDying()))) {
            // handleGameOver() đã được gọi bởi playerLoseLife() khi lives <= 0
            // Chỉ cần return để không update gì nữa
            return;
        }

        // 1. Cập nhật Player
        if (player != null) {
            if (player.isAlive()) {
                player.update(deltaTime, getAllEntities());
                checkPlayerCollectPowerUps();

            } else if (player.isDying()) {
                player.update(deltaTime, null); // Chỉ chạy animation chết
            } else { // Player không alive và không dying, nghĩa là vừa chết xong (animation đã hoàn tất)
                if (player.isJustPermanentlyDeadAndDecrementLife()) {
                    player.consumePermanentlyDeadFlag(); // QUAN TRỌNG: Reset cờ
                    playerLoseLife(); // Xử lý mất mạng
                }
            }
        }
        boolean enemiesWerePresentLastFrame = !enemies.isEmpty(); // Kiểm tra trước khi cập nhật
=======

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
>>>>>>> Stashed changes


        // 2. Cập nhật Enemies
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
<<<<<<< Updated upstream
                addScore(Config.POINTS_PER_ENEMY); // Sử dụng Config cho điểm số
                System.out.println("Enemy removed. Current Score: " + playerScore);
            }
        }
        if (enemies.isEmpty()) {
            System.out.println("[Game DEBUG Update] Enemies are empty.");
            if (this.portal != null) {
                System.out.println("[Game DEBUG Update] Portal object in Game is NOT NULL. Actual Type: " + this.portal.getClass().getName());
                System.out.println("[Game DEBUG Update] Portal revealed status: " + this.portal.isRevealed());
            } else {
                System.out.println("[Game DEBUG Update] Portal object in Game IS NULL.");
            }
            System.out.println("[Game DEBUG Update] allEnemiesDefeatedAndPortalActive flag: " + this.allEnemiesDefeatedAndPortalActive);
        }

        // Kiểm tra sau khi cập nhật enemies, nếu tất cả enemies đã bị tiêu diệt
        // và portal đã được thêm vào game (tức là nó tồn tại trên bản đồ).
        if (enemiesWerePresentLastFrame && enemies.isEmpty() && player != null && player.isAlive()) {
            SoundManager.getInstance().playSound(SoundManager.LEVEL_CLEAR); // Phát âm thanh hoàn thành màn
            System.out.println("All enemies defeated in level " + currentLevelNumber + "!");

            int nextLevel = currentLevelNumber + 1;
            if (nextLevel > Config.MAX_LEVELS) {
                System.out.println("Congratulations! You have completed all " + Config.MAX_LEVELS + " levels!");
                handleGameWin();
            } else {
                System.out.println("Proceeding to level " + nextLevel);
                loadLevel(nextLevel); // TỰ ĐỘNG TẢI LEVEL MỚI
            }
        }

        // 3. Cập nhật Bombs
=======
                addScore(Config.POINTS_PER_ENEMY);
            }
        }


>>>>>>> Stashed changes
        Iterator<Bomb> bombIterator = bombs.iterator();
        while (bombIterator.hasNext()) {
            Bomb bomb = bombIterator.next();
            if (bomb.isAlive()) {
                bomb.update(deltaTime, null);
            } else {
                bombIterator.remove();
            }
        }

        // 4. Cập nhật Explosions
        Iterator<Explosion> explosionIterator = explosions.iterator();
        while (explosionIterator.hasNext()) {
            Explosion explosion = explosionIterator.next();
            if (explosion.isAlive()) {
                explosion.update(deltaTime, null);
            } else {
                explosionIterator.remove();
            }
        }

        // 5. Cập nhật các thực thể tĩnh (Brick)
        Iterator<Entity> staticIterator = staticEntities.iterator();
        while (staticIterator.hasNext()) {
            Entity entity = staticIterator.next();
            if (entity instanceof Brick) {
                Brick brick = (Brick) entity;
                if (brick.isAlive() || brick.isDying()) {
                    brick.update(deltaTime, null);
                } else { // Gạch đã vỡ hoàn toàn (alive=false, dying=false)
                    staticIterator.remove();
<<<<<<< Updated upstream
                    addScore(Config.POINTS_PER_BRICK);
                    // Xử lý spawn PowerUp nếu có
                    if (brick.getContainedPowerUpType() != PowerUp.PowerUpType.NONE) {
                        addPowerUp(new PowerUp(brick.getTileX(), brick.getTileY(), getNesSheet(), brick.getContainedPowerUpType()));
=======
                    // ===>>> KIỂM TRA VÀ KÍCH HOẠT PORTAL NẾU NÓ TỒN TẠI <<<===
                    if (currentLevelPortal != null && !currentLevelPortal.isRevealed()) {
                        // Nếu tọa độ ô của Brick trùng với tọa độ ô của Portal
                        if (brick.getTileX() == currentLevelPortal.getTileX() &&
                                brick.getTileY() == currentLevelPortal.getTileY()) {
                            System.out.println("Brick covering portal AT TILE ("+ currentLevelPortal.getTileX() + "," + currentLevelPortal.getTileY() + ") destroyed.");
                            currentLevelPortal.setRevealed(true); // Kích hoạt Portal
                        }
>>>>>>> Stashed changes
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


        // 6. Cập nhật PowerUps
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

<<<<<<< Updated upstream
    }

=======
        checkWinCondition();
        if (player != null && !player.isAlive() && !player.isDying()) { // Player đã chết hẳn
            handlePlayerDeathOutcomeInternal(); // Đổi tên để tránh nhầm lẫn
        }
    }

    private void handleNonPlayingStateInput() {
        if (inputHandler.isPressed(KeyCode.ENTER)) {
            inputHandler.releaseKey(KeyCode.ENTER);
            if (currentState == GameState.LEVEL_COMPLETE) {
                if (currentLevelNumber >= maxLevels) { // Thực ra nên là GAME_WON
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
        if (currentLevelNumber >= maxLevels) {
            currentState = GameState.GAME_WON; // Chuyển sang trạng thái thắng game
            System.out.println("**************** YOU WON THE GAME! ****************");
        } else {
            System.out.println("Press ENTER for Next Level...");
        }
    }
>>>>>>> Stashed changes

    private void checkPlayerCollectPowerUps() {
        if (player == null || !player.isAlive()) return;
        Rectangle2D playerBounds = player.getBounds();
        Iterator<PowerUp> powerUpIterator = powerUps.iterator();
        while (powerUpIterator.hasNext()) {
            PowerUp pu = powerUpIterator.next();
            if (pu.isAlive() && playerBounds.intersects(pu.getBounds())) {
                pu.collect(player);
            }
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
<<<<<<< Updated upstream
        // Khi Player được thêm (thường là từ Level), đặt vị trí ban đầu cho nó
        if (this.player != null) {
            this.player.setInitialPosition(this.playerInitialTileX, this.playerInitialTileY);
        }
=======
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
    public void addPortal(Portal p) { // Từ phiên bản 1
        this.portal = p;
        System.out.println("[Game] Portal object added to game at (" + p.getTileX() + "," + p.getTileY() + ")");
    }

=======
>>>>>>> Stashed changes

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

    public void playerLoseLife() {
        if (this.playerLives > 0) { // Chỉ xử lý nếu còn mạng
            this.playerLives--;
            System.out.println("Player lost a life. Lives remaining: " + this.playerLives);

            if (this.playerLives <= 0) {
                handleGameOver();
            } else {
                // Nếu còn mạng, hồi sinh người chơi
                if (player != null) {
                    player.resetToStartPositionAndRevive();
                } else {
                    // Trường hợp hiếm, Player null nhưng mất mạng -> tải lại màn
                    System.err.println("Player is null but a life was lost. Attempting to reload level " + this.currentLevelNumber);
                    loadLevel(this.currentLevelNumber, false);
                }
            }
        }
    }

    private void handleGameOver() {
        if (currentState != GameState.GAME_OVER) {
            SoundManager.getInstance().stopBackgroundMusic();
            System.out.println("GAME OVER - Final Score: " + playerScore);
            SoundManager.getInstance().playSound(SoundManager.GAME_OVER);
        }
        if (player != null) {
<<<<<<< Updated upstream
            player.setPermanentlyDeadNoUpdates(); // Ngăn Player update
=======
            player.setPermanentlyDeadNoUpdates();
>>>>>>> Stashed changes
        }
    }


    private void handleGameWin() {
        System.out.println("YOU WIN! - Final Score: " + playerScore);
        if (player != null) {
            player.setPermanentlyDeadNoUpdates(); // Ngăn player update
        }
        // TODO: Thông báo cho BombermanApp để chuyển sang màn hình Win
        // mainApp.showGameWinScreen(playerScore); // Nếu có tham chiếu mainApp
    }

    public void setCurrentLevelPortal(Portal p) {
        if (this.currentLevelPortal == null) this.currentLevelPortal = p;
        else System.err.println("Attempted to set multiple portals.");
    }
    public Portal getCurrentLevelPortal() { return currentLevelPortal; }

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
        // Portal không cần trong list này nếu nó không có tương tác vật lý phức tạp ngoài việc player chạm vào
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


}