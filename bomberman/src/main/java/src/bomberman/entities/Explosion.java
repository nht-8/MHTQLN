package src.bomberman.entities; 

import javafx.geometry.Rectangle2D; 
import src.bomberman.Config;       
import src.bomberman.graphics.Sprite; 
import src.bomberman.graphics.SpriteSheet; 

import java.util.List;

public class Explosion extends Entity {

    private int duration; 
    private final ExplosionType type; 
    private int animationCounter = 0; 

    private final int ANIMATION_SPEED = Math.max(1, Config.BOMB_EXPLOSION_DURATION / 3);

    public enum ExplosionType {
        CENTER,                 
        HORIZONTAL_MIDDLE,      
        VERTICAL_MIDDLE,       
        END_UP,                
        END_DOWN,               
        END_LEFT,              
        END_RIGHT               
    }

    public Explosion(double xTile, double yTile, SpriteSheet sheet, ExplosionType type) {
        super(xTile, yTile, sheet); 
        this.duration = Config.BOMB_EXPLOSION_DURATION; 
        this.type = type;
        setSpriteForFrame(0); 

        if (this.sprite == null) {
            System.err.println("CRITICAL WARNING: Initial Explosion sprite is null for type: " + type +
                    ". Ensure Sprite.loadSprites() ran correctly and sprites exist.");
        }
    }

    private void setSpriteForFrame(int frameIndex) {
        frameIndex = Math.max(0, Math.min(2, frameIndex));
        Sprite targetSprite = null; 

        switch (type) {
            case CENTER:
                if (frameIndex == 0) targetSprite = Sprite.explosion_center;
                else if (frameIndex == 1) targetSprite = Sprite.explosion_center1;
                else targetSprite = Sprite.explosion_center2;
                break;
            case HORIZONTAL_MIDDLE:
                if (frameIndex == 0) targetSprite = Sprite.explosion_horizontal;
                else if (frameIndex == 1) targetSprite = Sprite.explosion_horizontal1;
                else targetSprite = Sprite.explosion_horizontal2;
                break;
            case VERTICAL_MIDDLE:
                if (frameIndex == 0) targetSprite = Sprite.explosion_vertical;
                else if (frameIndex == 1) targetSprite = Sprite.explosion_vertical1;
                else targetSprite = Sprite.explosion_vertical2;
                break;
            case END_UP:
                if (frameIndex == 0) targetSprite = Sprite.explosion_top;
                else if (frameIndex == 1) targetSprite = Sprite.explosion_top1;
                else targetSprite = Sprite.explosion_top2;
                break;
            case END_DOWN:
                if (frameIndex == 0) targetSprite = Sprite.explosion_bottom;
                else if (frameIndex == 1) targetSprite = Sprite.explosion_bottom1;
                else targetSprite = Sprite.explosion_bottom2;
                break;
            case END_LEFT: 
                if (frameIndex == 0) targetSprite = Sprite.explosion_left;
                else if (frameIndex == 1) targetSprite = Sprite.explosion_left1;
                else targetSprite = Sprite.explosion_left2;
                break;
            case END_RIGHT:
                if (frameIndex == 0) targetSprite = Sprite.explosion_right;
                else if (frameIndex == 1) targetSprite = Sprite.explosion_right1;
                else targetSprite = Sprite.explosion_right2;
                break;
            default: 
                System.err.println("Warning: Unknown ExplosionType: " + type);
                targetSprite = Sprite.explosion_center; 
                break;
        }

        this.sprite = targetSprite;
        if (this.sprite == null) {
            System.err.println("Warning: Explosion sprite is null after assignment for type: " + type +
                    ", frameIndex: " + frameIndex + ". Defaulting to center.");
            this.sprite = Sprite.explosion_center; 
            if (this.sprite == null) {
                System.err.println("CRITICAL ERROR: Default explosion sprite (explosion_center) is also null!");
            }
        }
    }

    @Override
    public void update(double deltaTime, List<Entity> entities) {
        if (!alive) return; 

        duration--;
        if (duration <= 0) {
            alive = false; 
            return;
        }

        animationCounter++;
        int totalAnimationTime = ANIMATION_SPEED * 3; 

        int elapsedCounter = Config.BOMB_EXPLOSION_DURATION - duration; 
        int currentFrameIndex = elapsedCounter / ANIMATION_SPEED;
        if (currentFrameIndex > 2) currentFrameIndex = 2; 

        setSpriteForFrame(currentFrameIndex); 
    }

    public void checkInitialCollisions(List<Entity> entities) {
    
        Rectangle2D explosionBounds = this.getBounds();
        
        for (Entity entity : entities) {
         
            if (entity == this || !entity.isAlive()) continue;
a
            if (entity instanceof Player || entity instanceof Enemy || entity instanceof Brick || entity instanceof Bomb) {
               
                if (explosionBounds.intersects(entity.getBounds())) {
                  
                    if (entity instanceof Player) {
                        ((Player) entity).destroy(); 
                    } else if (entity instanceof Enemy) {
                        ((Enemy) entity).destroy();  
                    } else if (entity instanceof Brick) {
                        ((Brick) entity).startBreaking(); 
                    } else if (entity instanceof Bomb) {
                        ((Bomb) entity).triggerExplosion(); 
                    }
                }
            }
        }
    }

    @Override
    public boolean isSolid() {
        return false;
    }
}
