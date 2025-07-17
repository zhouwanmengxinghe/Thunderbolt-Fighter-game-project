//-------------------------------------------------------
// Yifu Hou 23009975
// Yuze Liang 23010065
// Xuran Li 23041
// Shanfei Mo 23010080
//-------------------------------------------------------
package src;

import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioSystem;
import java.io.File;

public class AudioManager {
    // Audio file constants
    public static final String GAME_MUSIC = "game.wav";
    public static final String BUTTON_SOUND = "button.wav";
    public static final String SHOOT_SOUND = "shoot.wav";
    public static final String EXPLOSION_SOUND = "explosion.wav";
    public static final String POWER_UP_SOUND = "get_goods.wav";

    // Background music clip
    private Clip backgroundMusicClip;

    // Initialize audio system
    public void initAudio() {
        // Load and play background music in a new thread
        new Thread(() -> {
            try {
                backgroundMusicClip = AudioSystem.getClip();
                backgroundMusicClip.open(AudioSystem.getAudioInputStream(new File(GAME_MUSIC)));
                backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Start playing background music
        playSound(GAME_MUSIC);
    }

    // Play button sound effect
    public void playButtonSound() {
        playSound(BUTTON_SOUND);
    }

    // Play shooting sound effect
    public void playShootSound() {
        playSound(SHOOT_SOUND);
    }

    // Play explosion sound effect
    public void playExplosionSound() {
        playSound(EXPLOSION_SOUND);
    }

    // Play power-up sound effect
    public void playPowerUpSound() {
        playSound(POWER_UP_SOUND);
    }

    // General method to play sound effects
    private void playSound(String soundFile) {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(new File(soundFile)));
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Stop background music
    public void stopBackgroundMusic() {
        if (backgroundMusicClip != null && backgroundMusicClip.isRunning()) {
            backgroundMusicClip.stop();
            backgroundMusicClip.close();
        }
    }
}