package src.bomberman.entities; 

import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet;
import src.bomberman.sound.SoundManager;

import java.util.List;

public class PowerUp extends Entity {

    public enum PowerUpType {
        BOMBS, FLAMES, SPEED, NONE
    }

    private PowerUpType type;
    private boolean collected = false;
    private int duration = 300;

    public PowerUp(double xTile, double yTile, SpriteSheet sheet, PowerUpType type) {
        super(xTile, yTile, sheet);
        this.type = type;
      
        switch (type) {
            case BOMBS: this.sprite = Sprite.powerup_bombs; break;
            case FLAMES: this.sprite = Sprite.powerup_flames; break;
            case SPEED: this.sprite = Sprite.powerup_speed; break;

            default:
                System.err.println("Warning: Unknown or NONE PowerUpType, attempting to use bombs sprite.");
                this.sprite = Sprite.powerup_bombs; 
                break;
        }
        if (this.sprite == null) {
            System.err.println("CRITICAL WARNING: PowerUp sprite is null for type: " + type);
        }
    }

    @Override
    public void update(double deltaTime, List<Entity> entities) {
        if (collected) {
            alive = false;
            return;
        }
    }

    public void collect(Player player) {
        if (!collected && player != null) {
            System.out.println("Player collected powerup: " + type);
            applyEffect(player);
            collected = true;
            alive = false; 
            SoundManager.getInstance().playSound(SoundManager.GET_ITEM);
        }
    }


    private void applyEffect(Player player) {
        switch (type) {
            case BOMBS: player.addBombCapacity(1); break;
            case FLAMES: player.addFlameLength(1); break;
            case SPEED: player.addSpeed(0.3); break; 
            
            default:
                break;
        }
    }

    public PowerUpType getType() {
        return type;
    }

    @Override
    public boolean isSolid() {
        return false;
    }
}
