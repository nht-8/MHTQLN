package src.bomberman.entities; 

import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet;
import java.util.List;

public class Wall extends Entity {

    public Wall(double xTile, double yTile, SpriteSheet sheet) {
        super(xTile, yTile, sheet);
        this.sprite = Sprite.wall; 
        if (this.sprite == null) {
            System.err.println("CRITICAL WARNING: Sprite.wall is null during Wall construction!");
        }
    }

    @Override
    public void update(double deltaTime, List<Entity> entities) {

    }

    @Override
    public boolean isSolid() {
        return true;
    }
}
