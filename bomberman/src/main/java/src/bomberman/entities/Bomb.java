package src.bomberman.entities; 

import src.bomberman.core.Game;
import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet;
import src.bomberman.Config;
import src.bomberman.sound.SoundManager;

import java.util.List;

public class Bomb extends Entity {

    private int timer;
    private final int flameLength; 
    private Player owner; 
    private Game game;

    private int animationCounter = 0; 
    private final int ANIMATION_SPEED = 20; 

    public Bomb(double xTile, double yTile, SpriteSheet sheet, int timer, int flameLength, Player owner, Game game) {
        super(xTile, yTile, sheet); 
        this.timer = timer;
        this.flameLength = flameLength;
        this.owner = owner;
        this.game = game;
        this.sprite = Sprite.bomb; 
        if (this.sprite == null) {
            System.err.println("CRITICAL WARNING: Sprite.bomb is null during Bomb construction!");
        }
    }

    @Override
    public void update(double deltaTime, List<Entity> entities) {
        if (!alive) return;g

        timer--;

        animationCounter++;
        int totalAnimationFrames = 3;
        if (animationCounter >= ANIMATION_SPEED * totalAnimationFrames) {
            animationCounter = 0; 
        }

        int currentFrameIndex = animationCounter / ANIMATION_SPEED; 

        switch (currentFrameIndex) {
            case 0: sprite = Sprite.bomb; break;
            case 1: sprite = Sprite.bomb_1; break;
            case 2: sprite = Sprite.bomb_2; break;
            default: sprite = Sprite.bomb; break; 
        }

        if (sprite == null) {
            System.err.println("Warning: Bomb sprite is null during update. Defaulting.");
            sprite = Sprite.bomb;
            if (sprite == null) System.err.println("CRITICAL ERROR: Default bomb sprite is also null!");
        }

        if (timer <= 0) {
            explode();
        }
    }

    public void explode() {
        if (!alive) return; 
        alive = false;

        SoundManager.getInstance().playSound(SoundManager.EXPLOSION);

        if (owner != null) {
            owner.bombExploded();
        }

        if (game != null) {
         
            game.addExplosion(getTileX(), getTileY(), this.flameLength, this.entitySheet);
        } else {
            System.err.println("Bomb cannot explode: Game reference is null. Bomb at (" + getTileX() + "," + getTileY() + ")");
        }
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    public void triggerExplosion() {
        if (!alive) return;
       
        if (this.timer > 1) {
            this.timer = 1;
        }
    }
}
