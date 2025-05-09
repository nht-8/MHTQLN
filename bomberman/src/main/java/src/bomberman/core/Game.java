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
    private Level level;
    private Player player;
    private List<Enemy> enemies;
    private List<Bomb> bombs;
    private List<Explosion> explosions;
    private List<Entity> staticEntities;
    private List<PowerUp> powerUps;
    private InputHandler inputHandler;

    // Biến cho trạng thái game và HUD
    private int currentLevelNumber = 0;
    private int playerScore = 0;
    private int playerLives; // Sẽ được khởi tạo trong constructor

    public Game(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
        this.playerLives = Config.PLAYER_INIT_LIVES;

        // Khởi tạo các danh sách
        this.enemies = new ArrayList<>();
        this.bombs = new ArrayList<>();
        this.explosions = new ArrayList<>();
        this.staticEntities = new ArrayList<>();
        this.powerUps = new ArrayList<>();

        // Tải level đầu tiên
        loadLevel(1);
    }

    public void loadLevel(int levelNumber) {
        System.out.println("Loading level " + levelNumber + "...");
        this.currentLevelNumber = levelNumber;

        // Xóa các thực thể của level cũ
        enemies.clear();
        bombs.clear();
        explosions.clear();
        staticEntities.clear();
        powerUps.clear();
        player = null; // Reset player

        // Tạo đối tượng Level mới
        level = new Level(Config.LEVEL_PATH_PREFIX + "level" + levelNumber + ".txt", this);

        if (level.getWidth() == 0 || level.getHeight() == 0) {
            System.err.println("CRITICAL ERROR: Level " + levelNumber + " data is invalid (zero dimensions).");
            // Có thể thêm xử lý thoát game hoặc load level mặc định ở đây
            return;
        }

//        SoundManager.getInstance().playSound(SoundManager.LEVEL_START);
        System.out.println("Level " + levelNumber + " loaded successfully.");
    }

    public void update(double deltaTime) {
        // Nếu đã hết mạng và player không còn tồn tại hoặc đã chết hẳn, không update nữa
        // Điều này sẽ được GameHUDController hoặc BombermanApp kiểm tra để dừng game loop hoặc chuyển màn hình
        if (playerLives <= 0 && (player == null || (!player.isAlive() && !player.isDying()))) {
            return; // Game đã kết thúc logic, chờ controller xử lý
        }

        // 1. Cập nhật Player
        if (player != null) {
            if (player.isAlive()) {
                player.update(deltaTime, getAllEntities());
                checkPlayerCollectPowerUps();
            } else if (player.isDying()) {
                player.update(deltaTime, null); // Chỉ update animation chết
            } else { // Player đã chết hẳn (alive = false, dying = false)
                // Kiểm tra xem có phải vừa mới chết hẳn không để xử lý mất mạng
                if (player.isJustPermanentlyDeadAndDecrementLife()) {
                    playerLoseLife();
                }
            }
        }

        // 2. Cập nhật Enemies
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            if (enemy.isAlive()) {
                enemy.update(deltaTime, getAllEntities());
                // Kiểm tra va chạm giữa Enemy và Player còn sống
                if (player != null && player.isAlive() && enemy.intersects(player)) {
                    player.destroy(); // Player chết nếu chạm Enemy
                }
            } else if (enemy.isDying()){
                enemy.update(deltaTime, null); // Chỉ update animation chết
            } else { // Enemy đã chết hẳn
                enemyIterator.remove();
                addScore(100); // Ví dụ: cộng 100 điểm khi Enemy chết
                System.out.println("Enemy removed. Current Score: " + playerScore);
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

        // 5. Cập nhật các thực thể tĩnh (chủ yếu là Brick đang vỡ)
        Iterator<Entity> staticIterator = staticEntities.iterator();
        while (staticIterator.hasNext()) {
            Entity entity = staticIterator.next();
            if (entity instanceof Brick) {
                Brick brick = (Brick) entity;
                if (brick.isAlive() || brick.isDying()) {
                    brick.update(deltaTime, null);
                } else { // Gạch đã vỡ hoàn toàn
                    staticIterator.remove();
                    addScore(10); // Ví dụ: cộng 10 điểm khi phá gạch
                    System.out.println("Brick removed. Current Score: " + playerScore);
                }
            } else if (entity != null && !entity.isAlive()) { // Đảm bảo entity không null
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

        // TODO: Kiểm tra các điều kiện thắng level (ví dụ: hết enemies và đến portal)
        // if (isLevelWon()) {
        //    handleLevelWin();
        // }
    }

    private void checkPlayerCollectPowerUps() {
        if (player == null || !player.isAlive()) return;

        Rectangle2D playerBounds = player.getBounds();
        Iterator<PowerUp> powerUpIterator = powerUps.iterator();
        while (powerUpIterator.hasNext()) {
            PowerUp pu = powerUpIterator.next();
            if (pu.isAlive() && playerBounds.intersects(pu.getBounds())) {
                pu.collect(player); // Player nhặt powerup, powerup sẽ tự đánh dấu !alive
            }
        }
    }

    // --- Các phương thức được gọi bởi Level để thêm thực thể vào game ---
    public void addPlayer(Player p) { this.player = p; }
    public void addEnemy(Enemy e) { this.enemies.add(e); }
    public void addWall(Wall w) { this.staticEntities.add(w); }
    public void addBrick(Brick b) { this.staticEntities.add(b); }
    public void addPowerUp(PowerUp pu) { this.powerUps.add(pu); }
    public void addBomb(Bomb b) { this.bombs.add(b); }

    public void addExplosion(int tileX, int tileY, int flameLength, SpriteSheet sheet) {
        List<Explosion> newExplosionsThisTurn = new ArrayList<>();
        Explosion centerExplosion = new Explosion(tileX, tileY, sheet, Explosion.ExplosionType.CENTER);
        explosions.add(centerExplosion);
        newExplosionsThisTurn.add(centerExplosion);

        createFlameSegments(tileX, tileY,  1,  0, flameLength, sheet, newExplosionsThisTurn); // Phải
        createFlameSegments(tileX, tileY, -1,  0, flameLength, sheet, newExplosionsThisTurn); // Trái
        createFlameSegments(tileX, tileY,  0,  1, flameLength, sheet, newExplosionsThisTurn); // Xuống
        createFlameSegments(tileX, tileY,  0, -1, flameLength, sheet, newExplosionsThisTurn); // Lên

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

    // --- Getters cho HUD và trạng thái game ---
    public int getCurrentLevelNumber() { return currentLevelNumber; }
    public int getPlayerScore() { return playerScore; }
    public int getPlayerLives() { return playerLives; }

    public void addScore(int points) {
        if (playerLives > 0) { // Chỉ cộng điểm nếu chưa game over
            this.playerScore += points;
        }
    }

    public void playerLoseLife() {
        if (this.playerLives > 0) { // Chỉ xử lý nếu còn mạng để mất
            this.playerLives--;
            // Âm thanh chết nên được gọi bởi Player.destroy() để đồng bộ với animation
            System.out.println("Player lost a life. Lives remaining: " + this.playerLives);

            if (this.playerLives <= 0) {
                handleGameOver();
            } else {
                // Nếu còn mạng, hồi sinh player
                if(player != null) {
                    player.resetToStartPositionAndRevive();
                } else {
                    // Trường hợp hiếm: player null nhưng vẫn mất mạng?
                    // Có thể cần load lại level nếu player không thể hồi sinh
                    System.err.println("Player is null but a life was lost. Attempting to reload level.");
                    loadLevel(this.currentLevelNumber);
                }
            }
        }
    }

    private void handleGameOver() {
        System.out.println("GAME OVER (from Game.java) - Final Score: " + playerScore);
        if (player != null) {
            player.setPermanentlyDeadNoUpdates(); // Ngăn player update thêm
        }
        SoundManager.getInstance().playSound(SoundManager.GAMEOVER);
        // GameHUDController sẽ kiểm tra playerLives <= 0 và xử lý việc chuyển màn hình
        // hoặc dừng game loop. Game.java chỉ cần cập nhật trạng thái.
    }

    // --- Getters cho các thành phần game khác ---
    public Level getLevel() { return level; }
    public Player getPlayer() { return player; }
    public List<Enemy> getEnemies() { return enemies; }
    public List<Bomb> getBombs() { return bombs; }
    public List<Explosion> getExplosions() { return explosions; }
    public List<Entity> getStaticEntities() { return staticEntities; }
    public List<PowerUp> getPowerUps() { return powerUps; }

    public List<Entity> getAllEntities() {
        List<Entity> all = new ArrayList<>();
        all.addAll(staticEntities); // Nên thêm các entity tĩnh trước
        if (player != null && (player.isAlive() || player.isDying())) { // Thêm cả khi đang dying để render
            all.add(player);
        }
        // Thêm enemies đang sống hoặc đang dying
        for(Enemy e : enemies) { if(e.isAlive() || e.isDying()) all.add(e); }
        // Thêm bomb đang hoạt động và là vật rắn
        for(Bomb b : bombs) { if(b.isAlive() && b.isSolid()) all.add(b); }
        // Thêm powerups đang hoạt động
        for(PowerUp pu : powerUps) { if(pu.isAlive()) all.add(pu); }
        // Explosions không cần trong list này vì chúng không phải là đối tượng va chạm vật lý
        // và được render riêng.
        return all;
    }

    // Getters cho SpriteSheet và InputHandler (Renderer không còn dùng trực tiếp)
    public SpriteSheet getModernSheet() { return SpriteSheet.Sheet1; }
    public SpriteSheet getNesSheet() { return SpriteSheet.Sheet2; }
    public InputHandler getInputHandler() { return inputHandler; }
}