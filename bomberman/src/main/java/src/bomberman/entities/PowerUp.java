// src/main/java/src/bomberman/entities/PowerUp.java
package src.bomberman.entities; // HOẶC uet.oop.bomberman.entities

import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet;
import src.bomberman.sound.SoundManager;

import java.util.List;

public class PowerUp extends Entity {

    public enum PowerUpType {
        BOMBS, FLAMES, SPEED, WALLPASS, DETONATOR, BOMBPASS, FLAMEPASS, NONE
        // Thêm phương thức getRandom() nếu muốn
    }

    private PowerUpType type;
    private boolean collected = false;
    private int duration = 300; // Thời gian tồn tại trên map trước khi biến mất (tùy chọn)

    public PowerUp(double xTile, double yTile, SpriteSheet sheet, PowerUpType type) {
        super(xTile, yTile, sheet);
        this.type = type;
        // Gán sprite dựa trên type
        switch (type) {
            case BOMBS: this.sprite = Sprite.powerup_bombs; break;
            case FLAMES: this.sprite = Sprite.powerup_flames; break;
            case SPEED: this.sprite = Sprite.powerup_speed; break;
            // ... thêm case cho các loại powerup khác ...
            default:
                System.err.println("Warning: Unknown or NONE PowerUpType, attempting to use bombs sprite.");
                this.sprite = Sprite.powerup_bombs; // Fallback
                break;
        }
        if (this.sprite == null) {
            System.err.println("CRITICAL WARNING: PowerUp sprite is null for type: " + type);
        }
    }

    @Override
    public void update(double deltaTime, List<Entity> entities) {
        if (collected) {
            alive = false; // Nếu đã được nhặt, đánh dấu để xóa
            return;
        }
        // (Tùy chọn) Giảm thời gian tồn tại
        // duration--;
        // if (duration <= 0) alive = false;
    }

    public void collect(Player player) {
        if (!collected && player != null) {
            System.out.println("Player collected powerup: " + type);
            applyEffect(player);
            collected = true;
            alive = false; // Biến mất ngay sau khi nhặt
            SoundManager.getInstance().playSound(SoundManager.GET_ITEM);
        }
    }

    private void applyEffect(Player player) {
        switch (type) {
            case BOMBS: player.addBombCapacity(1); break;
            case FLAMES: player.addFlameLength(1); break;
            case SPEED: player.addSpeed(0.3); break; // Điều chỉnh giá trị tăng
            // ... thêm logic cho các powerup khác ...
            default:
                break;
        }
    }

    public PowerUpType getType() {
        return type;
    }

    // PowerUp không phải là vật cản rắn
    @Override
    public boolean isSolid() {
        return false;
    }
}