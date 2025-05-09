// src/main/java/src/bomberman/core/Game.java
package src.bomberman.core; // HOẶC uet.oop.bomberman.core

// Import các lớp cần thiết
import javafx.geometry.Rectangle2D;
import src.bomberman.Config;
import src.bomberman.entities.*; // Import tất cả các lớp Entity
import src.bomberman.entities.Portal;
import src.bomberman.graphics.SpriteSheet; // Import SpriteSheet để dùng các sheet tĩnh
import src.bomberman.input.InputHandler;
import src.bomberman.sound.SoundManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Lớp chính quản lý logic của trò chơi Bomberman.
 * Bao gồm quản lý level, các thực thể (player, enemies, bombs, explosions),
 * vòng lặp cập nhật game, xử lý va chạm và các sự kiện game.
 */
public class Game {
    private Level level;        // Level hiện tại của game
    private Player player;      // Đối tượng người chơi
    private List<Enemy> enemies;    // Danh sách các Enemy
    private List<Bomb> bombs;       // Danh sách các Bomb đang hoạt động
    private List<Explosion> explosions; // Danh sách các đoạn lửa Explosion
    private List<Entity> staticEntities; // Danh sách các thực thể tĩnh (Wall, Brick)
    private List<PowerUp> powerUps;     // Danh sách các PowerUp trên bản đồ
    private Portal portal;                 // Đối tượng Portal của level hiện tại
    private int currentLevelNumber = 1;    // Bắt đầu từ level 1
    private static final int MAX_LEVELS = 5;
    // Không cần lưu trữ instance của SpriteSheet ở đây nữa,
    // vì chúng ta sẽ dùng các biến tĩnh từ lớp SpriteSheet.
    private InputHandler inputHandler; // Đối tượng xử lý input

    /**
     * Constructor cho lớp Game.
     * Khởi tạo các danh sách thực thể và tải level đầu tiên.
     * @param input InputHandler để xử lý điều khiển của người chơi.
     */
    public Game(InputHandler input) {
        this.inputHandler = input;

        // Khởi tạo các danh sách để chứa các thực thể của game
        enemies = new ArrayList<>();
        bombs = new ArrayList<>();
        explosions = new ArrayList<>();
        staticEntities = new ArrayList<>();
        powerUps = new ArrayList<>();

        // Tải level đầu tiên khi game bắt đầu
        loadLevel(1); // Giả sử level bắt đầu từ 1
    }

    /**
     * Tải một level mới dựa trên số thứ tự của level.
     * Xóa tất cả các thực thể của level cũ trước khi tải level mới.
     * @param levelNumber Số thứ tự của level cần tải.
     */
    public void loadLevel(int levelNumber) {
        System.out.println("Loading level " + levelNumber + "...");
        // Xóa sạch các thực thể của level trước đó
        enemies.clear();
        bombs.clear();
        explosions.clear();
        staticEntities.clear();
        powerUps.clear();
        player = null; // Reset player
        portal = null; // Reset portal cho level mới

        this.currentLevelNumber = levelNumber; // Cập nhật số level hiện tại

        // Tạo đối tượng Level mới, truyền vào đường dẫn file map và tham chiếu 'this' (Game)
        // Level sẽ tự động đọc map và gọi các phương thức addEntity của Game để tạo thực thể
        level = new Level(Config.LEVEL_PATH_PREFIX + "level" + levelNumber + ".txt", this);

        if (level.getWidth() == 0 || level.getHeight() == 0) {
            System.err.println("CRITICAL ERROR: Level " + levelNumber + " data is invalid (zero dimensions).");
            // Xử lý lỗi, ví dụ: thoát game hoặc load level mặc định an toàn
            return;
        }

        SoundManager.getInstance().playSound(SoundManager.LEVEL_START);

        System.out.println("Level " + levelNumber + " loaded successfully.");
        // level.printMap(); // Bỏ comment nếu muốn in map ra console để debug
    }

    /**
     * Vòng lặp cập nhật chính của game.
     * Được gọi liên tục bởi AnimationTimer trong BombermanApp.
     * @param deltaTime Thời gian (thường là 1.0/60.0 giây) trôi qua kể từ lần cập nhật trước.
     */
    public void update(double deltaTime) {
        // 1. Cập nhật Player
        if (player != null) {
            if (player.isAlive()) {
                player.update(deltaTime, getAllEntities());
                checkPlayerCollectPowerUps(); // Kiểm tra nhặt powerup
            } else if (player.isDying()) { // Nếu player đang chạy animation chết
                player.update(deltaTime, null); // Chỉ update animation
            }
            // Nếu player chết hẳn (isAlive()=false, isDying()=false), không update nữa
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
            } else if (enemy.isDying()){ // Nếu enemy đang chạy animation chết
                enemy.update(deltaTime, null); // Chỉ update animation
            }
            else { // Enemy đã chết hẳn
                enemyIterator.remove();
                // TODO: Thêm logic tính điểm khi Enemy chết
                System.out.println("Enemy removed.");
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
                if (brick.isAlive() || brick.isDying()) { // Cập nhật cả khi đang vỡ
                    brick.update(deltaTime, null);
                } else { // Gạch đã vỡ hoàn toàn
                    staticIterator.remove();
                    // TODO: Xử lý spawn PowerUp tại vị trí gạch vỡ
                    // if (brick.getContainedPowerUpType() != PowerUp.PowerUpType.NONE) {
                    //     addPowerUp(new PowerUp(brick.getTileX(), brick.getTileY(), getNesSheet(), brick.getContainedPowerUpType()));
                    // }
                    System.out.println("Brick removed.");
                }
            } else if (!entity.isAlive()) {
                staticIterator.remove();
            }
        }

        // 6. Cập nhật PowerUps (ví dụ: biến mất sau một thời gian)
        Iterator<PowerUp> powerUpIterator = powerUps.iterator();
        while (powerUpIterator.hasNext()) {
            PowerUp pu = powerUpIterator.next();
            if (pu.isAlive()) {
                pu.update(deltaTime, null); // PowerUp có thể có logic update riêng (ví dụ: nhấp nháy, timer)
            } else {
                powerUpIterator.remove(); // Xóa PowerUp đã được nhặt hoặc hết hạn
            }
        }

        // 7. Kiểm tra điều kiện hoàn thành level (thêm vào cuối phương thức update)
        checkLevelCompletion();
    } // Kết thúc phương thức update

    /**
     * Kiểm tra xem người chơi đã hoàn thành level hiện tại chưa.
     * Điều kiện: Tất cả enemies bị tiêu diệt VÀ người chơi chạm vào Portal.
     */
    private void checkLevelCompletion() {
        if (portal != null && player != null && player.isAlive() && enemies.isEmpty()) {
            if (player.getBounds().intersects(portal.getBounds())) {
                System.out.println("Level " + currentLevelNumber + " cleared!");
                SoundManager.getInstance().playSound(SoundManager.LEVEL_CLEAR); // Phát âm thanh qua màn

                currentLevelNumber++;
                if (currentLevelNumber > MAX_LEVELS) {
                    // Đã hoàn thành tất cả các level
                    System.out.println("Congratulations! You have completed all levels!");
                    // TODO: Hiển thị màn hình chiến thắng hoặc xử lý kết thúc game
                    // Ví dụ: Dừng game hoặc quay lại level 1
                    // BombermanApp.getInstance().showGameWinScreen(); // (Cần sửa BombermanApp để có instance)
                    // Hoặc đơn giản là load lại level 1:
                    // loadLevel(1);
                    // Hiện tại, chúng ta sẽ dừng ở đây, bạn có thể mở rộng sau.
                    // Để dừng hẳn game, BombermanApp cần kiểm tra một trạng thái nào đó từ Game.
                    // Ví dụ, bạn có thể thêm: public boolean isGameFinished() { return currentLevelNumber > MAX_LEVELS; }
                    // và BombermanApp sẽ gọi gameLoop.stop();
                } else {
                    loadLevel(currentLevelNumber); // Tải level tiếp theo
                }
            }
        }
    }

        // TODO: Kiểm tra các điều kiện thắng/thua của game
        // if (enemies.isEmpty() && player đã đến portal) -> thắng level
        // if (player hết mạng và animation chết đã xong) -> game over
    }

    /**
     * Kiểm tra xem Player có nhặt được PowerUp nào không.
     */
    private void checkPlayerCollectPowerUps() {
        if (player == null || !player.isAlive()) return;

        Rectangle2D playerBounds = player.getBounds();
        Iterator<PowerUp> powerUpIterator = powerUps.iterator();
        while (powerUpIterator.hasNext()) {
            PowerUp pu = powerUpIterator.next();
            if (pu.isAlive() && playerBounds.intersects(pu.getBounds())) {
                pu.collect(player); // Player nhặt powerup, powerup sẽ tự đánh dấu !alive
                // Không cần xóa ở đây vì vòng lặp update powerup sẽ xóa
            }
        }
    }

    // --- Các phương thức được gọi bởi Level để thêm thực thể vào game ---
    public void addPlayer(Player p) { this.player = p; }
    public void addEnemy(Enemy e) { this.enemies.add(e); }
    public void addWall(Wall w) { this.staticEntities.add(w); }
    public void addBrick(Brick b) { this.staticEntities.add(b); }
    public void addPowerUp(PowerUp pu) { this.powerUps.add(pu); }
    public void addPortal(Portal p) { this.portal = p; }

    /**
     * Thêm một Bomb mới vào danh sách quản lý của game.
     * Được gọi từ Player.placeBomb().
     * @param b Đối tượng Bomb cần thêm.
     */
    public void addBomb(Bomb b) {
        this.bombs.add(b);
    }

    /**
     * Xử lý việc tạo ra các đoạn lửa (Explosion) khi một Bomb nổ.
     * Được gọi từ Bomb.explode().
     * @param tileX Tọa độ ô X của tâm vụ nổ.
     * @param tileY Tọa độ ô Y của tâm vụ nổ.
     * @param flameLength Độ dài của tia lửa (số ô).
     * @param sheet SpriteSheet chứa hình ảnh của vụ nổ (thường là nesSheet).
     */
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

    /**
     * Helper method để tạo các đoạn lửa (Explosion segments) theo một hướng nhất định.
     */
    private void createFlameSegments(int startX, int startY, int dx, int dy, int length,
                                     SpriteSheet sheet, List<Explosion> newExplosionsList) {
        for (int i = 1; i <= length; i++) {
            int currentX = startX + i * dx;
            int currentY = startY + i * dy;

            Entity blockingEntity = getSolidEntityAtTile(currentX, currentY);
            if (blockingEntity instanceof Wall) {
                break; // Tia lửa dừng lại nếu gặp Wall
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
                break; // Tia lửa dừng lại sau khi chạm gạch (gạch sẽ bị phá hủy)
            }
            // Kiểm tra ngoài biên bản đồ
            if (currentX < 0 || currentX >= level.getWidth() || currentY < 0 || currentY >= level.getHeight()) {
                break;
            }
        }
    }

    /**
     * Helper method để lấy Entity rắn (Wall, Brick, Bomb chưa nổ) tại một vị trí ô cụ thể.
     * @param tileX Tọa độ ô X.
     * @param tileY Tọa độ ô Y.
     * @return Entity rắn tại vị trí đó, hoặc null nếu không có hoặc ô ngoài map.
     */
    public Entity getSolidEntityAtTile(int tileX, int tileY) {
        if (level == null || tileX < 0 || tileX >= level.getWidth() || tileY < 0 || tileY >= level.getHeight()) {
            return null; // Ngoài map hoặc level chưa load
        }
        // Kiểm tra staticEntities (Wall, Brick)
        for (Entity entity : staticEntities) {
            if (entity.getTileX() == tileX && entity.getTileY() == tileY && entity.isAlive()) {
                if (entity instanceof Wall || entity instanceof Brick) {
                    return entity;
                }
            }
        }
        // Kiểm tra Bombs
        for (Bomb bomb : bombs) {
            if (bomb.getTileX() == tileX && bomb.getTileY() == tileY && bomb.isSolid()) {
                return bomb;
            }
        }
        return null; // Không có entity rắn nào tại ô đó
    }


    /**
     * Kiểm tra xem có thể đặt bom tại một vị trí ô cụ thể hay không.
     */
    public boolean canPlaceBombAt(int tileX, int tileY) {
        // Không cho đặt bom nếu ô đó đã có bom hoặc là tường/gạch
        Entity entityAtTarget = getSolidEntityAtTile(tileX, tileY);
        if (entityAtTarget instanceof Bomb || entityAtTarget instanceof Wall || entityAtTarget instanceof Brick) {
            return false;
        }
        // TODO: (Tùy chọn) Không cho đặt nếu Player đang đứng ở đó
        return true;
    }


    // --- Getters để các lớp khác truy cập thông tin ---
    public Level getLevel() { return level; }
    public Player getPlayer() { return player; }
    public Portal getPortal() { return portal; }
    public List<Enemy> getEnemies() { return enemies; }
    public List<Bomb> getBombs() { return bombs; }
    public List<Explosion> getExplosions() { return explosions; }
    public List<Entity> getStaticEntities() { return staticEntities; }
    public List<PowerUp> getPowerUps() { return powerUps; }


    /**
     * Trả về danh sách tất cả các thực thể đang hoạt động trong game.
     * Dùng cho việc kiểm tra va chạm.
     */
    public List<Entity> getAllEntities() {
        List<Entity> all = new ArrayList<>();
        all.addAll(staticEntities);
        if (player != null && player.isAlive()) { // Chỉ thêm player nếu còn sống
            all.add(player);
        }
        for(Enemy e : enemies) { if(e.isAlive()) all.add(e); }
        for(Bomb b : bombs) { if(b.isAlive() && b.isSolid()) all.add(b); } // Chỉ thêm bom rắn
        all.addAll(powerUps); // PowerUp không rắn nhưng player cần kiểm tra va chạm để nhặt
        return all;
    }

    // Getters cho SpriteSheet và InputHandler
    public SpriteSheet getModernSheet() { return SpriteSheet.Sheet1; }
    public SpriteSheet getNesSheet() { return SpriteSheet.Sheet2; }
    public InputHandler getInputHandler() { return inputHandler; }
}