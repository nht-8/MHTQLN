// src/main/java/src/bomberman/core/Game.java
package src.bomberman.core;

import javafx.geometry.Rectangle2D;
import src.bomberman.Config;
import src.bomberman.entities.*;
import src.bomberman.graphics.SpriteSheet;
import src.bomberman.input.InputHandler;
import src.bomberman.sound.SoundManager;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Game {
    private int playerInitialTileX = 1; // Mặc định nếu không có trong map
    private int playerInitialTileY = 1;

    private Level level;
    private Player player;
    private List<Enemy> enemies;
    private List<Bomb> bombs;
    private List<Explosion> explosions;
    private List<Entity> staticEntities;
    private List<PowerUp> powerUps;
    private InputHandler inputHandler;
    private Portal portal; // Từ phiên bản 1

    // Biến cho trạng thái game và HUD
    private int currentLevelNumber = 1; // Bắt đầu từ level 1
    private int playerScore = 0;
    private int playerLives;
    private static final int MAX_LEVELS = 5; // Từ phiên bản 1 (bạn có thể điều chỉnh)
    private boolean allEnemiesDefeatedAndPortalActive = false; // Cờ để kiểm tra điều kiện qua màn bằng Portal

    public Game(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
        this.playerLives = Config.PLAYER_INIT_LIVES;


        this.enemies = new ArrayList<>();
        this.bombs = new ArrayList<>();
        this.explosions = new ArrayList<>();
        this.staticEntities = new ArrayList<>();
        this.powerUps = new ArrayList<>();

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

        level = new Level(Config.LEVEL_PATH_PREFIX + "level" + levelNumber + ".txt", this);

        if (level.getWidth() == 0 || level.getHeight() == 0) {
            System.err.println("CRITICAL ERROR: Level " + levelNumber + " data is invalid (zero dimensions).");
            return;
        }

        if (player != null) {
            player.setInitialPosition(playerInitialTileX, playerInitialTileY); // playerInitialTileX/Y được Level cập nhật
            player.resetToStartPositionAndRevive(); // Đặt player về vị trí đầu màn và hồi sinh
            System.out.println("Player initial position for level " + levelNumber + " set to: (" + playerInitialTileX + "," + playerInitialTileY + ")");
        }


        SoundManager.getInstance().playSound(SoundManager.LEVEL_START);
        System.out.println("Level " + levelNumber + " loaded successfully.");

    }

    public void update(double deltaTime) {
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
                } else {
                    staticIterator.remove();
                    addScore(Config.POINTS_PER_BRICK);
                    // Xử lý spawn PowerUp nếu có
                    if (brick.getContainedPowerUpType() != PowerUp.PowerUpType.NONE) {
                        addPowerUp(new PowerUp(brick.getTileX(), brick.getTileY(), getNesSheet(), brick.getContainedPowerUpType()));
                    }
                    System.out.println("Brick removed. Current Score: " + playerScore);
                }
            } else if (entity != null && !entity.isAlive()) {
                staticIterator.remove();
            }
        }

        // 6. Cập nhật PowerUps
        Iterator<PowerUp> powerUpIterator = powerUps.iterator();
        while (powerUpIterator.hasNext()) {
            PowerUp pu = powerUpIterator.next();
            if (pu.isAlive()) {
                pu.update(deltaTime, null);
            } else {
                powerUpIterator.remove();
            }
        }

    }


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



    public void addPlayer(Player p) {
        this.player = p;
        // Khi Player được thêm (thường là từ Level), đặt vị trí ban đầu cho nó
        if (this.player != null) {
            this.player.setInitialPosition(this.playerInitialTileX, this.playerInitialTileY);
        }
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

    public void addPortal(Portal p) { // Từ phiên bản 1
        this.portal = p;
        System.out.println("[Game] Portal object added to game at (" + p.getTileX() + "," + p.getTileY() + ")");
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

    public Portal getPortal() {
        return portal;
    } // Từ phiên bản 1

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
                    loadLevel(this.currentLevelNumber);
                }
            }
        }
    }

    private void handleGameOver() {
        System.out.println("GAME OVER - Final Score: " + playerScore);
        SoundManager.getInstance().playSound(SoundManager.GAME_OVER);
        if (player != null) {
            player.setPermanentlyDeadNoUpdates(); // Ngăn Player update
        }
        // Thông báo cho BombermanApp để chuyển sang màn hình Game Over
        // mainApp.showGameOverScreen(playerScore); // Điều này sẽ được gọi từ GameHUDController
    }


    private void handleGameWin() {
        System.out.println("YOU WIN! - Final Score: " + playerScore);
        SoundManager.getInstance().playSound(SoundManager.GAME_WIN); // Thêm âm thanh thắng game
        if (player != null) {
            player.setPermanentlyDeadNoUpdates(); // Ngăn player update
        }
        // TODO: Thông báo cho BombermanApp để chuyển sang màn hình Win
        // mainApp.showGameWinScreen(playerScore); // Nếu có tham chiếu mainApp
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