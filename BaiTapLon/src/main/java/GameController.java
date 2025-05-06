package org.example.demo;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;         // Đảm bảo import này
import javafx.geometry.Rectangle2D;    // Đảm bảo import này
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.HashMap; // Import HashMap
import java.util.Iterator;
import java.util.List;
import java.util.Map; // Import Map
import java.util.Random;


public class GameController {

    // --- Constants ---
    private static final int TILE_SIZE = 32;
    private static final int BOARD_WIDTH_TILES = 20;
    private static final int BOARD_HEIGHT_TILES = 15;
    private static final int PATH = 0;
    private static final int WALL = 1;
    private static final int BRICK = 2;

    // --- Map Data ---
    private static final int[][] boardData = {
            {WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL},
            {WALL, PATH, PATH, BRICK, PATH, BRICK, PATH, BRICK, PATH, PATH, PATH, BRICK, PATH, PATH, BRICK, PATH, BRICK, PATH, PATH, WALL},
            {WALL, PATH, WALL, PATH, WALL, PATH, WALL, PATH, WALL, PATH, WALL, PATH, WALL, PATH, WALL, PATH, WALL, BRICK, PATH, WALL},
            {WALL, BRICK, PATH, BRICK, PATH, BRICK, PATH, BRICK, PATH, BRICK, PATH, BRICK, PATH, BRICK, PATH, BRICK, PATH, PATH, PATH, WALL},
            {WALL, PATH, WALL, PATH, WALL, PATH, WALL, PATH, WALL, PATH, WALL, PATH, WALL, PATH, WALL, PATH, WALL, BRICK, PATH, WALL},
            {WALL, PATH, BRICK, PATH, BRICK, PATH, PATH, PATH, PATH, PATH, PATH, PATH, BRICK, PATH, BRICK, PATH, BRICK, PATH, PATH, WALL},
            {WALL, BRICK, WALL, BRICK, WALL, PATH, WALL, PATH, WALL, PATH, WALL, PATH, WALL, BRICK, WALL, BRICK, WALL, BRICK, PATH, WALL},
            {WALL, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, WALL},
            {WALL, PATH, WALL, BRICK, WALL, BRICK, WALL, BRICK, WALL, BRICK, WALL, BRICK, WALL, BRICK, WALL, BRICK, WALL, BRICK, PATH, WALL},
            {WALL, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, WALL},
            {WALL, BRICK, WALL, BRICK, WALL, BRICK, WALL, BRICK, WALL, BRICK, WALL, BRICK, WALL, BRICK, WALL, BRICK, WALL, BRICK, PATH, WALL},
            {WALL, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, PATH, WALL},
            {WALL, PATH, WALL, PATH, WALL, PATH, WALL, PATH, WALL, PATH, WALL, PATH, WALL, PATH, WALL, PATH, WALL, BRICK, PATH, WALL},
            {WALL, PATH, BRICK, PATH, BRICK, PATH, BRICK, PATH, BRICK, PATH, BRICK, PATH, BRICK, PATH, BRICK, PATH, PATH, PATH, PATH, WALL},
            {WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL},
    };

    // --- Images ---
    private Image wallImage, pathImage, brickImage;
    private List<Image> bombAnimationFrames = new ArrayList<>();
    private List<Image> playerWalkDownFrames = new ArrayList<>(), playerWalkUpFrames = new ArrayList<>(),
            playerWalkLeftFrames = new ArrayList<>(), playerWalkRightFrames = new ArrayList<>();
    private List<Image> playerDeathFrames = new ArrayList<>();
    private List<Image> enemyBalloonFrames = new ArrayList<>();
    private List<Image> enemyOnealFrames = new ArrayList<>();
    private List<Image> enemyBalloonDeathFrames = new ArrayList<>();
    private List<Image> enemyOnealDeathFrames = new ArrayList<>();
    private List<Image> effectFrames = new ArrayList<>();
    private Image explosionCenterImage, explosionHorizontalImage, explosionVerticalImage,
            explosionEndUpImage, explosionEndDownImage, explosionEndLeftImage, explosionEndRightImage;
    private List<Image> brickDestroyFrames = new ArrayList<>();
    // PowerUp Images
    private Map<PowerUpType, Image> powerUpImages = new HashMap<>(); // <-- Đảm bảo khai báo này tồn tại

    // --- Game State ---
    private int currentLevel = 1;
    private double gameTimeRemaining = 180.0;
    private boolean isGameOver = false;

    // --- Game Objects ---
    private Player player;
    private List<Bomb> bombs = new ArrayList<>();
    private List<Explosion> explosions = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private List<FloatingText> floatingTexts = new ArrayList<>();
    private List<PowerUp> powerups = new ArrayList<>(); // <-- Đảm bảo khai báo này tồn tại
    private List<DestroyedBrick> destroyedBricks = new ArrayList<>();


    // --- FXML ---
    @FXML
    private Canvas gameCanvas;
    @FXML
    private Label levelLabel;
    @FXML
    private Label scoreLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private Label livesLabel;

    // --- Internal ---
    private GraphicsContext gc;
    private GameLoop gameLoop;
    private static final Random random = new Random();

    // --- Initialization ---
    @FXML
    public void initialize() {
        System.out.println("GameController initializing...");
        if (gameCanvas == null) {
            System.err.println("CRITICAL: gameCanvas is null!");
            return;
        }
        gc = gameCanvas.getGraphicsContext2D();
        gameCanvas.setWidth(BOARD_WIDTH_TILES * TILE_SIZE);
        gameCanvas.setHeight(BOARD_HEIGHT_TILES * TILE_SIZE);
        loadResources();
        if (!playerWalkDownFrames.isEmpty() && !playerWalkUpFrames.isEmpty() && !playerWalkLeftFrames.isEmpty() && !playerWalkRightFrames.isEmpty() && !bombAnimationFrames.isEmpty() && !playerDeathFrames.isEmpty()) {
            player = new Player(1.0, 1.0, TILE_SIZE, playerWalkDownFrames, playerWalkUpFrames, playerWalkLeftFrames, playerWalkRightFrames, playerDeathFrames);
        } else {
            System.err.println("CRITICAL: Missing essential frames. Cannot start.");
            isGameOver = true;
            updateUI();
            return;
        }
        spawnInitialEnemies();
        if (explosionCenterImage == null /*||...*/) {
            System.err.println("WARN: Missing explosion images.");
        }
        updateUI();
        if (!isGameOver) {
            gameLoop = new GameLoop();
            gameLoop.start();
            gameCanvas.requestFocus();
            System.out.println("Game Started!");
        } else {
            System.out.println("Game cannot start.");
            renderGame();
        }
    }

    // --- Load Resources ---
    private void loadResources() {
        System.out.println("Loading resources...");
        wallImage = loadImage("images/wall.png");
        pathImage = loadImage("images/path.png");
        brickImage = loadImage("images/brick.png");
        loadBombFrames();
        loadExplosionImages();
        loadPlayerFrames(playerWalkDownFrames, "d", 4);
        loadPlayerFrames(playerWalkUpFrames, "u", 4);
        loadPlayerFrames(playerWalkLeftFrames, "l", 4);
        loadPlayerFrames(playerWalkRightFrames, "r", 4);
        loadGeneralFrames(playerDeathFrames, "dead", 3);
        loadEnemyFrames(enemyBalloonFrames, "balloon", 7);
        loadEnemyFrames(enemyOnealFrames, "oneal", 7);
        loadEnemyDeathFrames(enemyBalloonDeathFrames, "balloon");
        loadEnemyDeathFrames(enemyOnealDeathFrames, "oneal");
        effectFrames.clear();
        Image ef0 = loadImage("images/effect.png");
        if (ef0 != null) effectFrames.add(ef0);
        loadGeneralFrames(effectFrames, "effect", 4, 1);
        // Load PowerUp Images
        System.out.println("Loading powerup images...");
        powerUpImages.clear();
        powerUpImages.put(PowerUpType.BOMBS, loadImage("images/powerups/powerup_bombs.png"));
        powerUpImages.put(PowerUpType.FLAMES, loadImage("images/powerups/powerup_flames.png"));
        powerUpImages.put(PowerUpType.SPEED, loadImage("images/powerups/powerup_speed.png"));
        powerUpImages.put(PowerUpType.WALL_PASS, loadImage("images/powerups/powerup_wallpass.png"));
        for (PowerUpType type : PowerUpType.values()) {
            if (powerUpImages.get(type) == null) {
                System.err.println("WARN: Missing image for PowerUp: " + type);
            }
        }
        loadBrickDestroyFrames();
        System.out.println("Resource loading finished.");
    }

    private void loadBrickDestroyFrames() {
        brickDestroyFrames.clear();
        System.out.println("Loading brick destroy frames (boom22-24)...");
        for (int i = 22; i <= 24; i++) {
            String path = "images/boom" + i + ".png"; // Giả sử ảnh nằm trong thư mục images chính
            Image frame = loadImage(path);
            if (frame != null) {
                brickDestroyFrames.add(frame);
            } else {
                System.err.println("ERROR: Missing brick destroy frame: " + path);
            }
        }
        if (brickDestroyFrames.size() < 3) { // Cần đủ ảnh
            System.err.println("WARN: Not enough brick destroy frames loaded!");
        }
    }

    private Image loadImage(String p) {
        Image i = null;
        try {
            i = new Image(getClass().getResourceAsStream(p));
        } catch (Exception e) {
            System.err.println("ERR load " + p + e);
        }
        if (i == null) System.err.println("WARN: null img " + p);
        return i;
    }

    private void loadBombFrames() {
        bombAnimationFrames.clear();
        for (int i = 1; i <= 6; i++) {
            Image f = loadImage("images/bom" + i + ".png");
            if (f != null) bombAnimationFrames.add(f);
        }
        if (bombAnimationFrames.isEmpty()) System.err.println("ERR: No bomb frames!");
    }

    private void loadExplosionImages() {
        explosionCenterImage = loadImage("images/boom1.png");
        explosionHorizontalImage = loadImage("images/boom6.png");
        explosionVerticalImage = loadImage("images/boom3.png");
        explosionEndUpImage = loadImage("images/boom2.png");
        explosionEndDownImage = loadImage("images/boom4.png");
        explosionEndLeftImage = loadImage("images/boom5.png");
        explosionEndRightImage = loadImage("images/boom7.png");
        if (explosionCenterImage == null || explosionEndRightImage == null)
            System.err.println("WARN: Expl images missing!");
    }

    private void loadPlayerFrames(List<Image> l, String p, int c) {
        l.clear();
        for (int i = 1; i <= c; i++) {
            Image f = loadImage("images/" + p + i + ".png");
            if (f != null) l.add(f);
        }
        if (l.isEmpty()) System.err.println("ERR: No player frames " + p);
    }

    private void loadEnemyFrames(List<Image> l, String eT, int c) {
        l.clear();
        String bP = "images/enemies/" + eT + "/";
        int ld = 0;
        for (int i = 1; i <= c; i++) {
            Image f = loadImage(bP + "mob" + i + ".png");
            if (f != null) {
                l.add(f);
                ld++;
            }
        }
        System.out.println(" -> Loaded " + ld + " WALK frames for " + eT);
        if (l.isEmpty()) System.err.println("ERR: No WALK frames for " + eT);
    }

    private void loadEnemyDeathFrames(List<Image> l, String eT) {
        l.clear();
        String bP = "images/enemies/" + eT + "/";
        int ld = 0;
        for (int i = 8; i <= 10; i++) {
            Image f = loadImage(bP + "mob" + i + ".png");
            if (f != null) {
                l.add(f);
                ld++;
            } else System.err.println("ERR: Missing DEATH " + eT + " frame: " + bP + "mob" + i + ".png");
        }
        System.out.println(" -> Loaded " + ld + " DEATH frames for " + eT);
        if (l.size() < 3) System.err.println("WARN: Not enough DEATH frames for " + eT);
    }

    private void loadGeneralFrames(List<Image> l, String p, int c) {
        loadGeneralFrames(l, p, c, 1);
    }

    private void loadGeneralFrames(List<Image> l, String p, int c, int s) {
        l.clear();
        if (s == 0) {
            Image f0 = loadImage("images/" + p + ".png");
            if (f0 != null) l.add(f0);
        }
        for (int i = s; i < s + c; i++) {
            int fI = (s == 0) ? i + 1 : i;
            Image f = loadImage("images/" + p + fI + ".png");
            if (f != null) l.add(f);
        }
    }

    private List<Point2D> findValidSpawnPoints() {
        List<Point2D> vP = new ArrayList<>();
        for (int y = 1; y < BOARD_HEIGHT_TILES - 1; y++) {
            for (int x = 1; x < BOARD_WIDTH_TILES - 1; x++) {
                if (boardData[y][x] == PATH) {
                    if (!((x == 1 && y == 1) || (x == 1 && y == 2) || (x == 2 && y == 1))) {
                        boolean oc = false;
                        for (Enemy eE : enemies) {
                            if (eE.getGridX() == x && eE.getGridY() == y) {
                                oc = true;
                                break;
                            }
                        }
                        if (!oc) vP.add(new Point2D(x, y));
                    }
                }
            }
        }
        System.out.println("Found " + vP.size() + " valid spawn points.");
        return vP;
    }

    private void spawnInitialEnemies() {
        System.out.println("Spawning enemies...");
        List<Point2D> vP = findValidSpawnPoints();
        enemies.clear();
        int nB = 3, nO = 3, tS = nB + nO;
        if (vP.size() < tS) {
            System.err.println("WARN: Only " + vP.size() + " points. Spawning fewer.");
            nO = Math.min(nO, vP.size() / 2);
            nB = vP.size() - nO;
            tS = nB + nO;
        }
        boolean cSB = !enemyBalloonFrames.isEmpty() && !enemyBalloonDeathFrames.isEmpty();
        boolean cSO = !enemyOnealFrames.isEmpty() && !enemyOnealDeathFrames.isEmpty();
        if (!cSB) System.err.println("WARN: Cannot spawn Balloon");
        if (!cSO) System.err.println("WARN: Cannot spawn Oneal");
        for (int i = 0; i < nB && cSB && !vP.isEmpty(); i++) {
            int rI = random.nextInt(vP.size());
            Point2D p = vP.remove(rI);
            int gX = (int) p.getX(), gY = (int) p.getY();
            enemies.add(new BalloonEnemy(gX, gY, TILE_SIZE, 60.0 + random.nextInt(10), enemyBalloonFrames, enemyBalloonDeathFrames));
        }
        for (int i = 0; i < nO && cSO && !vP.isEmpty(); i++) {
            int rI = random.nextInt(vP.size());
            Point2D p = vP.remove(rI);
            int gX = (int) p.getX(), gY = (int) p.getY();
            enemies.add(new OnealEnemy(gX, gY, TILE_SIZE, 75.0 + random.nextInt(15), enemyOnealFrames, enemyOnealDeathFrames, this));
        }
        System.out.println("Spawned: " + enemies.size() + " enemies.");
    }


    // --- Game Loop ---
    private class GameLoop extends AnimationTimer {
        private long lastNanoTime = System.nanoTime();

        @Override
        public void handle(long now) {
            if (isGameOver) {
                this.stop();
                renderGame();
                return;
            }
            double dT = (now - lastNanoTime) / 1e9;
            dT = Math.min(dT, 0.1);
            lastNanoTime = now;
            updateGame(dT);
            renderGame();
        }
    }

    // --- Update Game Logic ---
    private void updateGame(double dT) {
        // 1. Kiểm tra Game Over hoặc Hết Thời Gian
        if (isGameOver) {
            return; // Không cập nhật gì nếu game đã kết thúc
        }
        if (gameTimeRemaining > 0) {
            gameTimeRemaining -= dT;
            if (gameTimeRemaining <= 0) {
                gameTimeRemaining = 0;
                System.out.println("Time's up!");
                handleGameOver(); // Hết giờ -> Game Over
                return; // Dừng cập nhật frame này khi hết giờ
            }
        }

        // 2. Cập nhật Player (bao gồm animation đi bộ và animation chết)
        if (player != null) { // Chỉ cập nhật nếu Player tồn tại
            // Hàm update của Player sẽ tự xử lý trạng thái alive/dying
            player.update(dT, boardData);
        }

        // 3. Cập nhật Bom (đếm ngược, animation, kiểm tra nổ)
        Iterator<Bomb> bombIterator = bombs.iterator();
        while (bombIterator.hasNext()) {
            Bomb bomb = bombIterator.next();
            bomb.update(dT); // Cập nhật timer và animation của bom
            if (bomb.isExploded()) {
                createExplosion(bomb.getGridX(), bomb.getGridY(), bomb); // Tạo vụ nổ (truyền bom gốc)
                bombIterator.remove(); // Xóa bom đã nổ
            }
        }

        // 4. Cập nhật Vụ Nổ (timer tồn tại, kiểm tra va chạm)
        Iterator<Explosion> explosionIterator = explosions.iterator();
        while (explosionIterator.hasNext()) {
            Explosion explosion = explosionIterator.next();
            explosion.update(dT); // Cập nhật timer của vụ nổ
            if (explosion.isFinished()) {
                explosionIterator.remove(); // Xóa vụ nổ khi hết hạn
            } else {
                // Kiểm tra va chạm của vụ nổ này với các đối tượng khác
                checkExplosionCollision(explosion); // Với Player
                checkExplosionEnemyCollision(explosion); // Với Enemies (kiểm tra owner)
            }
        }

        // 5. Cập nhật Kẻ Địch (AI, di chuyển, animation đi/chết)
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            enemy.update(dT, boardData, bombs, player, this); // Gọi hàm update chung của Enemy
            // Chỉ xóa enemy khi nó đã chết hẳn (animation chết đã xong)
            if (!enemy.isAlive() && !enemy.isDying()) {
                enemyIterator.remove();
                System.out.println("DEBUG: Removed dead enemy from list."); // Log nếu cần
            }
        }

        // 6. Cập nhật Animation Gạch Vỡ
        Iterator<DestroyedBrick> brickIterator = destroyedBricks.iterator();
        while (brickIterator.hasNext()) {
            DestroyedBrick db = brickIterator.next();
            db.update(dT); // Cập nhật animation timer của gạch vỡ
            if (db.isFinished()) {
                brickIterator.remove(); // Xóa animation khi hoàn tất
                // System.out.println("DEBUG: Removed finished destroyed brick animation."); // Log nếu cần
            }
        }

        // 7. Cập nhật Hiệu ứng Text nổi
        Iterator<FloatingText> textIterator = floatingTexts.iterator();
        while (textIterator.hasNext()) {
            FloatingText text = textIterator.next();
            text.update(dT); // Cập nhật vị trí và timer của text
            if (text.isFinished()) {
                textIterator.remove(); // Xóa text khi hết hạn
            }
        }

        // 8. Kiểm tra các va chạm khác
        checkPlayerPowerUpCollision(); // Player nhặt vật phẩm
        checkPlayerEnemyCollisions(); // Player va chạm kẻ địch

        // 9. Kiểm tra lại điều kiện Game Over cuối cùng (phòng trường hợp Player chết do va chạm)
        if (player != null && !player.isAlive() && !player.isDying()) {
            handleGameOver();
        }

        // 10. Cập nhật Giao Diện Người Dùng (UI Labels)
        updateUI();
    }

    // --- Render Game ---
    private void renderGame() {
        if (gc == null) return;
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        drawBoard(gc);
        for (DestroyedBrick db : destroyedBricks) {
            db.render(gc); // Vẽ animation gạch đang vỡ
        }
        for (Bomb b : bombs) b.render(gc);
        for (Explosion e : explosions) e.render(gc);
        for (Enemy e : enemies) e.render(gc);
        for (PowerUp p : powerups) p.render(gc); // Render powerups
        if (player != null) player.render(gc);
        for (FloatingText t : floatingTexts) t.render(gc); // Render floating texts last
        if (isGameOver) {
            gc.setFill(Color.web("rgba(0,0,0,0.7)"));
            gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
            gc.setFill(Color.RED);
            gc.setFont(new Font("Arial Bold", 60));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("GAME OVER", gameCanvas.getWidth() / 2, gameCanvas.getHeight() / 2);
            gc.setTextAlign(TextAlignment.LEFT);
        }
    }

    // --- Draw Board ---
    private void drawBoard(GraphicsContext gc) {
        if (gc == null) return;
        for (int y = 0; y < BOARD_HEIGHT_TILES; y++) {
            for (int x = 0; x < BOARD_WIDTH_TILES; x++) {
                if (pathImage != null) gc.drawImage(pathImage, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                else {
                    gc.setFill(Color.LIGHTGREEN);
                    gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
                int tT = boardData[y][x];
                if (tT == WALL) {
                    if (wallImage != null) gc.drawImage(wallImage, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    else {
                        gc.setFill(Color.GRAY);
                        gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    }
                } else if (tT == BRICK) {
                    if (brickImage != null)
                        gc.drawImage(brickImage, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    else {
                        gc.setFill(Color.ORANGE);
                        gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    }
                }
            }
        }
    }

    // --- Create Explosion ---
    private void createExplosion(int startX, int startY, Bomb sourceBomb) {
        // Kiểm tra ảnh cơ bản đã load
        if (explosionCenterImage == null || explosionHorizontalImage == null || explosionVerticalImage == null ||
                explosionEndLeftImage == null || explosionEndRightImage == null || explosionEndUpImage == null || explosionEndDownImage == null ) {
            System.err.println("ERROR: Cannot create explosion - Essential explosion images missing!");
            return;
        }

        // Lấy bán kính nổ
        int radius = 1;
        if (sourceBomb.getOwner() instanceof Player) {
            radius = ((Player) sourceBomb.getOwner()).getExplosionRadius();
        }
        // (Thêm logic cho Enemy nếu cần)

        // 1. Tạo tâm nổ
        explosions.add(new Explosion(startX, startY, TILE_SIZE, explosionCenterImage, sourceBomb));
        destroyBrickIfPresent(startX, startY);

        Image middleImage, endImage;

        // 2. Tạo tia NGANG (Trái/Phải)
        // --- Trái ---
        middleImage = explosionHorizontalImage; // Ảnh ngang
        endImage = explosionEndLeftImage;
        for (int i = 1; i <= radius; i++) {
            if (!processExplosionSegment(startX - i, startY, i, radius, middleImage, endImage, sourceBomb)) break;
        }
        // --- Phải ---
        middleImage = explosionHorizontalImage; // Ảnh ngang
        endImage = explosionEndRightImage;
        for (int i = 1; i <= radius; i++) {
            if (!processExplosionSegment(startX + i, startY, i, radius, middleImage, endImage, sourceBomb)) break;
        }

        // 3. Tạo tia DỌC (Lên/Xuống) - Bỏ if(radius > 1)
        // --- Lên ---
        middleImage = explosionVerticalImage;   // Ảnh dọc
        endImage = explosionEndUpImage;
        for (int i = 1; i <= radius; i++) {
            if (!processExplosionSegment(startX, startY - i, i, radius, middleImage, endImage, sourceBomb)) break;
        }
        // --- Xuống ---
        middleImage = explosionVerticalImage;   // Ảnh dọc
        endImage = explosionEndDownImage;
        for (int i = 1; i <= radius; i++) {
            if (!processExplosionSegment(startX, startY + i, i, radius, middleImage, endImage, sourceBomb)) break;
        }
    }




    // --- Process Explosion Segment ---
    // Trong lớp GameController.java

    /**
     * Xử lý việc tạo ra một ô (segment) của tia nổ tại tọa độ (gridX, gridY).
     * Kiểm tra va chạm với tường/gạch và quyết định xem tia lửa có tiếp tục lan tỏa hay không.
     *
     * @param gridX Tọa độ X của ô cần xử lý.
     * @param gridY Tọa độ Y của ô cần xử lý.
     * @param stepIndex Chỉ số bước hiện tại của tia lửa (1 là ô gần tâm nhất, tăng dần ra xa).
     * @param maxRadius Bán kính tối đa của vụ nổ này.
     * @param middleImage Ảnh dùng cho phần giữa của tia lửa.
     * @param endImage Ảnh dùng cho phần đầu mút (cuối cùng) của tia lửa.
     * @param sourceBomb Quả bom gốc đã gây ra vụ nổ.
     * @return true nếu tia lửa có thể tiếp tục lan tỏa qua ô này, false nếu tia lửa bị chặn.
     */
    private boolean processExplosionSegment(int gridX, int gridY, int stepIndex, int maxRadius,
                                            Image middleImage, Image endImage, Bomb sourceBomb) {
        // 1. Kiểm tra biên bản đồ
        if (gridX < 0 || gridX >= BOARD_WIDTH_TILES || gridY < 0 || gridY >= BOARD_HEIGHT_TILES) {
            return false;
        }

        // 2. Lấy loại ô
        if (boardData == null || gridY >= boardData.length || boardData[gridY] == null || gridX >= boardData[gridY].length) {
            System.err.println("Error: Accessing invalid boardData coordinates (" + gridX + ", " + gridY + ")");
            return false;
        }
        int tileType = boardData[gridY][gridX];

        // 3. ---> LOGIC CHỌN ẢNH (QUAY LẠI PHIÊN BẢN GỐC) <---
        // Luôn sử dụng ảnh đầu mút (endImage) nếu đây là ô cuối cùng của tia lửa (stepIndex == maxRadius).
        // Điều này tự động xử lý trường hợp maxRadius == 1, vì lúc đó stepIndex cũng là 1.
        Image imageToUse = (stepIndex == maxRadius) ? endImage : middleImage;
        // ---> KẾT THÚC CẬP NHẬT LOGIC CHỌN ẢNH <---

        // 4. Tạo đối tượng Explosion
        if (imageToUse == null) {
            System.err.println("WARN: imageToUse is null for explosion segment at (" + gridX + "," + gridY + "). Skipping segment.");
            return false; // Dừng tia lửa nếu thiếu ảnh
        }
        Explosion currentExplosion = new Explosion(gridX, gridY, TILE_SIZE, imageToUse, sourceBomb);
        explosions.add(currentExplosion);

        // 5. Xử lý va chạm tường/gạch
        if (tileType == WALL) {
            return false;
        }
        if (tileType == BRICK) {
            destroyBrickIfPresent(gridX, gridY);
            return false;
        }

        // 6. Tiếp tục tia lửa
        return true;
    }

    // --- Destroy Brick & Spawn PowerUp ---
    private void destroyBrickIfPresent(int gX, int gY) {
        if (gX < 0 || gX >= BOARD_WIDTH_TILES || gY < 0 || gY >= BOARD_HEIGHT_TILES) {
            return;
        }
        if (boardData[gY][gX] == BRICK) {
            boardData[gY][gX] = PATH;
            if (!brickDestroyFrames.isEmpty()) {
                destroyedBricks.add(new DestroyedBrick(gX, gY, TILE_SIZE, brickDestroyFrames));
            }
            if (player != null && player.isAlive()) {
                player.addScore(10); // Ví dụ
            }
            double dropChance = 0.4;
            if (random.nextDouble() < dropChance) {
                spawnRandomPowerUp(gX, gY);
            }
        }
    }

    // --- Spawn Random PowerUp ---
    private void spawnRandomPowerUp(int gridX, int gridY) {
        PowerUpType[] possibleTypes = PowerUpType.values();
        if (possibleTypes.length == 0) return;
        PowerUpType randomType = possibleTypes[random.nextInt(possibleTypes.length)];
        Image powerUpImage = powerUpImages.get(randomType); // Sử dụng biến thành viên powerUpImages
        if (powerUpImage != null) {
            boolean occupied = false;
            for (PowerUp p : powerups) { // Sử dụng biến thành viên powerups
                if (p.getGridX() == gridX && p.getGridY() == gridY) {
                    occupied = true;
                    break;
                }
            }
            if (!occupied) {
                PowerUp newPowerUp = new PowerUp(gridX, gridY, TILE_SIZE, randomType, powerUpImage);
                powerups.add(newPowerUp); // Sử dụng biến thành viên powerups
                System.out.println("Spawned PowerUp: " + randomType + " at (" + gridX + "," + gridY + ")");
            }
        } else {
            System.err.println("WARN: Cannot spawn PowerUp - Image missing for " + randomType);
        }
    }

    // --- Check Collision Methods ---
    private void checkExplosionCollision(Explosion e) {
        if (player == null || !player.isAlive() || player.isDying() || player.isInvincible()) return;
        Rectangle2D pR = player.getHitbox();
        Rectangle2D eR = new Rectangle2D(e.getGridX() * TILE_SIZE, e.getGridY() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        if (pR.intersects(eR)) {
            if (player.loseLife()) {
                spawnFloatingText("-1 Life", player.getX() + TILE_SIZE / 2.0, player.getY());
            }
        }
    }

    private void checkExplosionEnemyCollision(Explosion explosion) {
        int eGX = explosion.getGridX();
        int eGY = explosion.getGridY();
        Bomb sB = explosion.getSourceBomb();
        for (Enemy e : enemies) {
            if (e.isAlive() && !e.isDying() && e.getGridX() == eGX && e.getGridY() == eGY) {
                if (sB != null && sB.getOwner() == e) {
                } else {
                    e.die();
                    if (player != null && player.isAlive()) player.addScore(100);
                }
            }
        }
    }

    private void checkPlayerEnemyCollisions() {
        if (player == null || !player.isAlive() || player.isDying() || player.isInvincible()) return;
        Rectangle2D pR = player.getHitbox();
        for (Enemy e : enemies) {
            if (e.isAlive() && !e.isDying() && pR.intersects(e.getHitbox())) {
                if (player.loseLife()) {
                    spawnFloatingText("-1 Life", player.getX() + TILE_SIZE / 2.0, player.getY());
                }
                break;
            }
        }
    }

    private void checkPlayerPowerUpCollision() {
        if (player == null || !player.isAlive() || player.isDying()) return;
        Rectangle2D pR = player.getHitbox();
        Iterator<PowerUp> pI = powerups.iterator();
        while (pI.hasNext()) {
            PowerUp p = pI.next();
            if (!p.isCollected() && pR.intersects(p.getHitbox())) {
                player.applyPowerUp(p.getType());
                p.collect();
                pI.remove();
            }
        }
    }

    // --- Place Player Bomb ---
    public void placeBomb() {
        if (player == null || !player.isAlive() || player.isDying() || bombAnimationFrames.isEmpty()) return;
        int pBC = 0;
        for (Bomb b : bombs) if (b.getOwner() == player) pBC++;
        if (pBC >= player.getMaxBombs()) return;
        double pCX = player.getX() + TILE_SIZE / 2.0;
        double pCY = player.getY() + TILE_SIZE / 2.0;
        int gX = (int) (pCX / TILE_SIZE);
        int gY = (int) (pCY / TILE_SIZE);
        for (Bomb b : bombs) if (b.getGridX() == gX && b.getGridY() == gY) return;
        bombs.add(new Bomb(gX, gY, TILE_SIZE, bombAnimationFrames, player));
    }

    // --- Add Enemy Bomb ---
    public void addEnemyBomb(int gX, int gY, Enemy placer) {
        if (gX < 0 || gX >= BOARD_WIDTH_TILES || gY < 0 || gY >= BOARD_HEIGHT_TILES || isSolidTile(gX, gY, boardData))
            return;
        for (Bomb b : bombs) if (b.getGridX() == gX && b.getGridY() == gY) return;
        if (bombAnimationFrames.isEmpty()) {
            System.err.println("ERR: No bomb frames for Enemy!");
            return;
        }
        bombs.add(new Bomb(gX, gY, TILE_SIZE, bombAnimationFrames, placer));
    }

    // --- Input Handling Methods ---
    public void setMoveUp(boolean a) {
        if (pCanMove()) player.setMovingUp(a);
    }

    public void setMoveDown(boolean a) {
        if (pCanMove()) player.setMovingDown(a);
    }

    public void setMoveLeft(boolean a) {
        if (pCanMove()) player.setMovingLeft(a);
    }

    public void setMoveRight(boolean a) {
        if (pCanMove()) player.setMovingRight(a);
    }

    private boolean pCanMove() {
        return player != null && player.isAlive() && !player.isDying();
    }

    // --- Collision Checks (Static) ---
    public static boolean isValidMove(double nX, double nY, int oS, int[][] bD, boolean cPW) {
        double hBI = oS * 0.1;
        double hBX = nX + hBI;
        double hBY = nY + hBI;
        double hBS = oS - 2 * hBI;
        int tlX = (int) (hBX / TILE_SIZE), tlY = (int) (hBY / TILE_SIZE);
        int trX = (int) ((hBX + hBS) / TILE_SIZE), trY = tlY;
        int blX = tlX, blY = (int) ((hBY + hBS) / TILE_SIZE);
        int brX = trX, brY = blY;
        if (isSolidTile(tlX, tlY, bD, cPW) || isSolidTile(trX, trY, bD, cPW) || isSolidTile(blX, blY, bD, cPW) || isSolidTile(brX, brY, bD, cPW))
            return false;
        return true;
    }

    public static boolean isValidMove(double nX, double nY, int oS, int[][] bD) {
        return isValidMove(nX, nY, oS, bD, false);
    } // Overload for non-wallpass

    private static boolean isSolidTile(int tX, int tY, int[][] bD, boolean cPW) {
        if (tX < 0 || tX >= BOARD_WIDTH_TILES || tY < 0 || tY >= BOARD_HEIGHT_TILES) return true;
        if (bD == null || tY >= bD.length || bD[tY] == null || tX >= bD[tY].length) return true;
        int tT = bD[tY][tX];
        if (tT == WALL) return true;
        if (tT == BRICK && !cPW) return true;
        return false;
    }

    private static boolean isSolidTile(int tX, int tY, int[][] bD) {
        return isSolidTile(tX, tY, bD, false);
    } // Overload for non-wallpass

    // --- Game Over ---
    private void handleGameOver() {
        if (!isGameOver) {
            System.out.println("GAME OVER!");
            this.isGameOver = true;
        }
    }

    // --- Update UI Method ---
    private void updateUI() {
        if (levelLabel != null) levelLabel.setText("Level: " + currentLevel);
        if (scoreLabel != null && player != null) scoreLabel.setText("Score: " + player.getScore());
        if (timeLabel != null) timeLabel.setText("Time: " + (int) Math.max(0, Math.round(gameTimeRemaining)));
        if (livesLabel != null && player != null) livesLabel.setText("Lives: " + Math.max(0, player.getLives()));
    }

    // --- Helper để tạo Floating Text ---
    private void spawnFloatingText(String text, double centerX, double centerY) {
        FloatingText ft = new FloatingText(text, centerX, centerY);
        floatingTexts.add(ft);
    }

} // End of GameController class