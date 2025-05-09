
package src.bomberman.entities;

// Import các lớp cần thiết
import src.bomberman.graphics.Sprite;
import src.bomberman.graphics.SpriteSheet;
import src.bomberman.core.Game;           // Import Game nếu cần để spawn powerup
// Bỏ import GraphicsContext vì không ghi đè render
import java.util.List;
import java.util.Random; // Để quyết định có powerup không

/**
 * Đại diện cho một viên gạch có thể bị phá hủy trong game.
 * Khi bị nổ, nó sẽ chạy animation vỡ và sau đó biến mất, có thể rơi ra PowerUp.
 */
public class Brick extends Entity {

    private boolean breaking = false; // Trạng thái đang vỡ
    private int breakAnimationCounter = 0; // Bộ đếm cho animation vỡ
    // Tốc độ animation: Số frame update cho mỗi sprite vỡ
    private final int BREAK_ANIMATION_SPEED = 15; // Khoảng 1/4 giây cho mỗi frame ở 60fps

    // (Tùy chọn) Loại PowerUp chứa bên trong
    private PowerUp.PowerUpType containedPowerUpType = PowerUp.PowerUpType.NONE;
    private Game game; // Tham chiếu Game để spawn powerup

    /**
     * Constructor cho Brick.
     * @param xTile Tọa độ ô X ban đầu.
     * @param yTile Tọa độ ô Y ban đầu.
     * @param sheet SpriteSheet chứa hình ảnh gạch (thường là nesSheet).
     * @param game Tham chiếu đến đối tượng Game (để spawn powerup).
     */
    public Brick(double xTile, double yTile, SpriteSheet sheet, Game game) {
        super(xTile, yTile, sheet);
        this.game = game; // Lưu tham chiếu Game
        this.sprite = Sprite.brick; // Sprite ban đầu là gạch nguyên vẹn
        if (this.sprite == null) {
            System.err.println("CRITICAL WARNING: Sprite.brick is null during Brick construction!");
        }

        // Quyết định ngẫu nhiên xem gạch này có chứa powerup không
        // Bạn có thể điều chỉnh tỉ lệ này
        Random random = new Random();
        // Ví dụ: 30% cơ hội có powerup (có thể tùy chỉnh tỉ lệ hoặc đọc từ map)
        if (random.nextDouble() < 0.7) {
            // Lấy ngẫu nhiên một loại powerup từ enum (trừ NONE)
            PowerUp.PowerUpType[] allPowerUps = PowerUp.PowerUpType.values();
            // Chọn ngẫu nhiên 1 trong các loại (BOMBS, FLAMES, SPEED,...)
            // Bỏ qua NONE nếu nó là phần tử cuối cùng
            int randomIndex = random.nextInt(allPowerUps.length - (allPowerUps[allPowerUps.length-1] == PowerUp.PowerUpType.NONE ? 1:0) );
            this.containedPowerUpType = allPowerUps[randomIndex];
        }
    }

    /**
     * Cập nhật trạng thái của Brick. Chỉ xử lý animation khi đang vỡ.
     */
    @Override
    public void update(double deltaTime, List<Entity> entities) {
        if (!alive) return; // Không làm gì nếu đã bị hủy

        if (breaking) {
            breakAnimationCounter++;

            // Chọn sprite animation vỡ dựa trên tiến trình
            // Có 3 frame animation vỡ: brick_exploded, brick_exploded1, brick_exploded2
            int totalAnimationFrames = 3;
            int currentFrameIndex = breakAnimationCounter / BREAK_ANIMATION_SPEED;

            if (currentFrameIndex == 0) {
                this.sprite = Sprite.brick;
            } else if (currentFrameIndex == 1) {
                this.sprite = Sprite.brick_exploded;
            } else if (currentFrameIndex == 2) {
                this.sprite = Sprite.brick_exploded1;
            } else { // currentFrameIndex >= 3
                // Animation hoàn tất: Đánh dấu là đã chết
                this.alive = false; // Sẽ được Game loại bỏ
                this.breaking = false; // Dừng trạng thái breaking

                // Tạo đối tượng PowerUp tại vị trí này nếu gạch có chứa powerup
                if (containedPowerUpType != PowerUp.PowerUpType.NONE && game != null && this.entitySheet != null) {
                    // Tạo PowerUp tại vị trí ô của Brick
                    PowerUp pu = new PowerUp(getTileX(), getTileY(), this.entitySheet, containedPowerUpType);
                    game.addPowerUp(pu); // Yêu cầu Game thêm PowerUp vào danh sách
                    System.out.println("Spawned PowerUp: " + containedPowerUpType + " at (" + getTileX() + "," + getTileY() + ")");
                }

                return; // Không cần làm gì thêm sau khi chết
            }

            // Fallback phòng trường hợp sprite animation bị null
            if (sprite == null) {
                System.err.println("Warning: Brick explosion sprite is null. Defaulting to normal brick.");
                sprite = Sprite.brick; // Quay về sprite gạch thường nếu lỗi
                if(sprite == null) {
                    System.err.println("CRITICAL ERROR: Default brick sprite (Sprite.brick) is also null!");
                }
            }
        }
        // Nếu không phải trạng thái breaking, không làm gì cả.
    }
    // Trong Brick.java
    public PowerUp.PowerUpType getContainedPowerUpType() {
        return containedPowerUpType;
    }

    /**
     * Bắt đầu quá trình phá hủy gạch. Được gọi khi bị Explosion chạm vào.
     */
    public void startBreaking() {
        if (!breaking && alive) { // Chỉ bắt đầu nếu chưa vỡ và còn sống
            breaking = true;
            breakAnimationCounter = 0; // Reset bộ đếm animation
            // TODO: Có thể phát âm thanh gạch vỡ ở đây
            // SoundManager.play("brick_break");
        }
    }

    /**
     * Phương thức được gọi khi có tác nhân muốn phá hủy gạch (ví dụ: vụ nổ).
     * Thay vì xóa ngay, nó sẽ bắt đầu animation vỡ.
     */
    @Override
    public void destroy() {
        startBreaking();
    }

    /**
     * Gạch là vật cản rắn khi chưa bị phá hủy.
     */
    @Override
    public boolean isSolid() {
        return alive && !breaking; // Chỉ rắn khi còn sống (chưa vỡ hẳn)
    }

    /**
     * Trả về true nếu gạch đang trong quá trình chạy animation vỡ.
     */
    @Override
    public boolean isDying() {
        return breaking && alive; // Đang vỡ và chưa bị xóa hẳn
    }

    public void setContainedPowerUpType(PowerUp.PowerUpType type) {
        this.containedPowerUpType = type;

    }

    // (Tùy chọn) Getter để Game có thể kiểm tra loại powerup (nếu cần)
    // public PowerUp.PowerUpType getContainedPowerUpType() {
    //     return containedPowerUpType;
    // }
}