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

    
    private int currentLevelNumber = 0;
    private int playerScore = 0;
    private int playerLives; 

    public Game(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
        this.playerLives = Config.PLAYER_INIT_LIVES;

       
        this.enemies = new ArrayList<>();
        this.bombs = new ArrayList<>();
        this.explosions = new ArrayList<>();
        this.staticEntities = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        
        loadLevel(1);
    }
    
    public void loadLevel(int levelNumber) {
        System.out.println("Loading level " + levelNumber + "...");
        this.currentLevelNumber = levelNumber;

        enemies.clear();
        bombs.clear();
        explosions.clear();
        staticEntities.clear();
        powerUps.clear();
        player = null;

        level = new Level(Config.LEVEL_PATH_PREFIX + "level" + levelNumber + ".txt", this);

        if (level.getWidth() == 0 || level.getHeight() == 0) {
            System.err.println("CRITICAL ERROR: Level " + levelNumber + " data is invalid (zero dimensions).");
        
            return;
        }

        SoundManager.getInstance().playSound(SoundManager.LEVEL_START);
        System.out.println("Level " + levelNumber + " loaded successfully.");
    }

    public void update(double deltaTime) {
        
        if (playerLives <= 0 && (player == null || (!player.isAlive() && !player.isDying()))) {
            return; 
        }
        
        if (player != null) {
            if (player.isAlive()) {
                player.update(deltaTime, getAllEntities());
                checkPlayerCollectPowerUps();
            } else if (player.isDying()) {
                player.update(deltaTime, null); ]
            } else { 
                
                if (player.isJustPermanentlyDeadAndDecrementLife()) {
                    playerLoseLife();
                }
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
            } else if (enemy.isDying()){
                enemy.update(deltaTime, null); 
            } else { 
                enemyIterator.remove();
                addScore(100);
                System.out.println("Enemy removed. Current Score: " + playerScore);
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
                } else { 
                    staticIterator.remove();
                    addScore(10); 
                    System.out.println("Brick removed. Current Score: " + playerScore);
                }
            } else if (entity != null && !entity.isAlive()) { 
                staticIterator.remove();
            }
        }

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

        createFlameSegments(tileX, tileY,  1,  0, flameLength, sheet, newExplosionsThisTurn); 
        createFlameSegments(tileX, tileY, -1,  0, flameLength, sheet, newExplosionsThisTurn); 
        createFlameSegments(tileX, tileY,  0,  1, flameLength, sheet, newExplosionsThisTurn); 
        createFlameSegments(tileX, tileY,  0, -1, flameLength, sheet, newExplosionsThisTurn); 

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

    public int getCurrentLevelNumber() { return currentLevelNumber; }
    public int getPlayerScore() { return playerScore; }
    public int getPlayerLives() { return playerLives; }

    public void addScore(int points) {
        if (playerLives > 0) { 
            this.playerScore += points;
        }
    }

    public void playerLoseLife() {
        if (this.playerLives > 0) { 
            this.playerLives--;
        
            System.out.println("Player lost a life. Lives remaining: " + this.playerLives);

            if (this.playerLives <= 0) {
                handleGameOver();
            } else {
            
                if(player != null) {
                    player.resetToStartPositionAndRevive();
                } else {
                    
                    System.err.println("Player is null but a life was lost. Attempting to reload level.");
                    loadLevel(this.currentLevelNumber);
                }
            }
        }
    }

    private void handleGameOver() {
        System.out.println("GAME OVER (from Game.java) - Final Score: " + playerScore);
        if (player != null) {
            player.setPermanentlyDeadNoUpdates(); 
        }
    }

    public Level getLevel() { return level; }
    public Player getPlayer() { return player; }
    public List<Enemy> getEnemies() { return enemies; }
    public List<Bomb> getBombs() { return bombs; }
    public List<Explosion> getExplosions() { return explosions; }
    public List<Entity> getStaticEntities() { return staticEntities; }
    public List<PowerUp> getPowerUps() { return powerUps; }

    public List<Entity> getAllEntities() {
        List<Entity> all = new ArrayList<>();
        all.addAll(staticEntities);
        if (player != null && (player.isAlive() || player.isDying())) {
            all.add(player);
        }
     
        for(Enemy e : enemies) { if(e.isAlive() || e.isDying()) all.add(e); }
        
        for(Bomb b : bombs) { if(b.isAlive() && b.isSolid()) all.add(b); }
       
        for(PowerUp pu : powerUps) { if(pu.isAlive()) all.add(pu); }
        
        return all;
    }

    
    public SpriteSheet getModernSheet() { return SpriteSheet.Sheet1; }
    public SpriteSheet getNesSheet() { return SpriteSheet.Sheet2; }
    public InputHandler getInputHandler() { return inputHandler; }
}
