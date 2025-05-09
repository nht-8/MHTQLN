package src.bomberman.sound;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration; 

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    private static SoundManager instance;

    private Map<String, AudioClip> soundEffects;
    private MediaPlayer backgroundMusicPlayer;
    private String currentBackgroundMusicPath = null;


    public static final String EXPLOSION = "boom";
    public static final String LEVEL_CLEAR = "clear"; // Có thể dùng khi thắng level
    public static final String PLAYER_DEATH = "dead1"; // Hoặc dead2
    public static final String ENEMY_DEATH = "dead2"; // Hoặc dùng chung mob_dead nếu có
    public static final String GET_ITEM = "getitem";
    public static final String PLACE_BOMB = "putbomb"; // Hoặc putbomb2
    public static final String GAMEOVER = "gameover";

    // Các tên khác bạn có thể thêm: "flash", "intro", "homestart"

    // Tên file nhạc nền
    public static final String GAME_BGM = "gameaudio"; // Nhạc nền chính
    public static final String TITLE_BGM = "homestart"; // Nhạc màn hình tiêu đề

    // Constructor riêng tư cho Singleton
    private SoundManager() {
        soundEffects = new HashMap<>();
    }

    /**
     * Lấy thể hiện duy nhất của SoundManager (Singleton).
     */
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    /**
     * Tải tất cả các hiệu ứng âm thanh cần thiết vào bộ nhớ.
     * Nên gọi một lần khi game khởi động.
     */
    public void loadSounds() {
        System.out.println("Loading sound effects...");
        loadSoundEffect(EXPLOSION, "/sounds/boom.wav");
        loadSoundEffect(LEVEL_CLEAR, "/sounds/clear.wav");
        loadSoundEffect(PLAYER_DEATH, "/sounds/dead1.wav");
        loadSoundEffect(ENEMY_DEATH, "/sounds/dead2.wav");
        loadSoundEffect(GET_ITEM, "/sounds/getitem.wav");
        loadSoundEffect(PLACE_BOMB, "/sounds/putbomb.wav");
        loadSoundEffect(GAMEOVER, "/sounds/gameover.wav");

        System.out.println("Sound effects loading finished.");
    }

    /**
     * Helper method để tải một file âm thanh và lưu vào map.
     * @param name Tên định danh cho âm thanh (dùng khi playSound).
     * @param filePath Đường dẫn resource đến file âm thanh.
     */
    private void loadSoundEffect(String name, String filePath) {
        try {
            URL resourceUrl = getClass().getResource(filePath);
            if (resourceUrl == null) {
                System.err.println("ERROR: Cannot find sound resource: " + filePath);
                soundEffects.put(name, null); // Đánh dấu là không load được
                return;
            }
            AudioClip clip = new AudioClip(resourceUrl.toExternalForm());
            soundEffects.put(name, clip);
            System.out.println("Loaded sound: " + name + " from " + filePath);
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load sound effect '" + name + "' from " + filePath);
            e.printStackTrace();
            soundEffects.put(name, null); // Đánh dấu là không load được
        }
    }

    /**
     * Phát một hiệu ứng âm thanh đã được tải.
     * @param name Tên định danh của âm thanh (ví dụ: SoundManager.EXPLOSION).
     */
    public void playSound(String name) {
        AudioClip clip = soundEffects.get(name);
        if (clip != null) {
            // clip.setVolume(0.5); // Có thể điều chỉnh âm lượng nếu muốn
            clip.play();
        } else {
            System.err.println("Warning: Sound effect not found or failed to load: " + name);
        }
    }

    /**
     * Phát nhạc nền. Nếu đang có nhạc khác phát, nó sẽ bị dừng.
     * @param musicName Tên file nhạc nền (không có đuôi, ví dụ: SoundManager.GAME_BGM).
     * @param loop true nếu muốn lặp lại nhạc nền, false nếu chỉ phát một lần.
     */
    public void playBackgroundMusic(String musicName, boolean loop) {

        String filePath = "/sounds/" + musicName + ".wav"; // Hoặc .wav

        // Nếu đang phát đúng nhạc này rồi thì không làm gì cả
        if (backgroundMusicPlayer != null && filePath.equals(currentBackgroundMusicPath)) {
            if (backgroundMusicPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                return;
            } else {
                // Nếu đang dừng hoặc hết thì phát lại từ đầu
                backgroundMusicPlayer.seek(Duration.ZERO);
                backgroundMusicPlayer.play();
                return;
            }
        }

        // Dừng nhạc nền cũ nếu có
        stopBackgroundMusic();
        currentBackgroundMusicPath = null; // Reset đường dẫn

        try {
            URL resourceUrl = getClass().getResource(filePath);
            if (resourceUrl == null) {
                System.err.println("ERROR: Cannot find background music resource: " + filePath);
                return;
            }
            Media media = new Media(resourceUrl.toExternalForm());
            backgroundMusicPlayer = new MediaPlayer(media);

            // Xử lý lỗi khi media không sẵn sàng
            backgroundMusicPlayer.setOnError(() -> {
                System.err.println("MediaPlayer Error: " + backgroundMusicPlayer.getError());
                backgroundMusicPlayer.stop();
                backgroundMusicPlayer.dispose();
                backgroundMusicPlayer = null;
                currentBackgroundMusicPath = null;
            });

            // Đảm bảo media sẵn sàng trước khi cài đặt và play
            backgroundMusicPlayer.setOnReady(() -> {
                if (loop) {
                    backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Lặp vô hạn
                } else {
                    backgroundMusicPlayer.setCycleCount(1); // Chỉ phát 1 lần
                }
                // backgroundMusicPlayer.setVolume(0.7); // Đặt âm lượng mặc định nếu muốn
                backgroundMusicPlayer.play();
                currentBackgroundMusicPath = filePath; // Lưu lại đường dẫn nhạc đang phát
                System.out.println("Playing background music: " + musicName);
            });


        } catch (Exception e) {
            System.err.println("ERROR: Failed to play background music from " + filePath);
            e.printStackTrace();
            if (backgroundMusicPlayer != null) {
                backgroundMusicPlayer.dispose(); 
                backgroundMusicPlayer = null;
            }
            currentBackgroundMusicPath = null;
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusicPlayer != null) {
            try {
                if (backgroundMusicPlayer.getStatus() != MediaPlayer.Status.DISPOSED) {
                    backgroundMusicPlayer.stop();
                    System.out.println("Stopped background music.");
            
                }
            } catch (Exception e) {
                System.err.println("Error stopping background music: " + e.getMessage());
            } finally {
              
                currentBackgroundMusicPath = null;
            }

        }
    }

    public void setBackgroundMusicVolume(double volume) {
        if (backgroundMusicPlayer != null && backgroundMusicPlayer.getStatus() != MediaPlayer.Status.DISPOSED) {
        
            volume = Math.max(0.0, Math.min(1.0, volume));
            backgroundMusicPlayer.setVolume(volume);
        }
    }

    public void cleanup() {
        System.out.println("Cleaning up SoundManager...");
        stopBackgroundMusic();
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.dispose(); 
        }
        soundEffects.clear(); 
        instance = null; 
    }
<<<<<<< HEAD
    public static final String GAME_OVER = "GameOverSound";
    public static final String GAME_WIN = "GameWinSound"; 

=======

    public static final String GAME_OVER = "gameover"; 
>>>>>>> 049d609ca10667fbbf060b092f596aee531d74e5
}
