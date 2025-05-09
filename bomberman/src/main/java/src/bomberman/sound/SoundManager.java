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
    public static final String LEVEL_CLEAR = "clear"; 
    public static final String PLAYER_DEATH = "dead1";
    public static final String ENEMY_DEATH = "dead2"; 
    public static final String GET_ITEM = "getitem";
    public static final String PLACE_BOMB = "putbomb";
    public static final String GAMEOVER = "gameover";

    public static final String GAME_BGM = "gameaudio"; 
    public static final String TITLE_BGM = "homestart"; 

    private SoundManager() {
        soundEffects = new HashMap<>();
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

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

    private void loadSoundEffect(String name, String filePath) {
        try {
            URL resourceUrl = getClass().getResource(filePath);
            if (resourceUrl == null) {
                System.err.println("ERROR: Cannot find sound resource: " + filePath);
                soundEffects.put(name, null);
                return;
            }
            AudioClip clip = new AudioClip(resourceUrl.toExternalForm());
            soundEffects.put(name, clip);
            System.out.println("Loaded sound: " + name + " from " + filePath);
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load sound effect '" + name + "' from " + filePath);
            e.printStackTrace();
            soundEffects.put(name, null); 
        }
    }

    public void playSound(String name) {
        AudioClip clip = soundEffects.get(name);
        if (clip != null) {
            
            clip.play();
        } else {
            System.err.println("Warning: Sound effect not found or failed to load: " + name);
        }
    }

    public void playBackgroundMusic(String musicName, boolean loop) {

        String filePath = "/sounds/" + musicName + ".wav"; 

        if (backgroundMusicPlayer != null && filePath.equals(currentBackgroundMusicPath)) {
            if (backgroundMusicPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                return;
            } else {
                
                backgroundMusicPlayer.seek(Duration.ZERO);
                backgroundMusicPlayer.play();
                return;
            }
        }

        stopBackgroundMusic();
        currentBackgroundMusicPath = null; 

        try {
            URL resourceUrl = getClass().getResource(filePath);
            if (resourceUrl == null) {
                System.err.println("ERROR: Cannot find background music resource: " + filePath);
                return;
            }
            Media media = new Media(resourceUrl.toExternalForm());
            backgroundMusicPlayer = new MediaPlayer(media);

            backgroundMusicPlayer.setOnError(() -> {
                System.err.println("MediaPlayer Error: " + backgroundMusicPlayer.getError());
                backgroundMusicPlayer.stop();
                backgroundMusicPlayer.dispose();
                backgroundMusicPlayer = null;
                currentBackgroundMusicPath = null;
            });

            backgroundMusicPlayer.setOnReady(() -> {
                if (loop) {
                    backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE); 
                } else {
                    backgroundMusicPlayer.setCycleCount(1); 
                }
                
                backgroundMusicPlayer.play();
                currentBackgroundMusicPath = filePath; 
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
