package puzzle;

import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Loads and plays short .wav sound effects and looping background music.
 * Reuses a single Clip and releases the previous clip's resources before
 * starting a new one, so effects don't leak audio resources or overlap.
 */
public class SoundPlayer {

    private static final String VALID_MOVE_PATH = "src/media/validMove.wav";
    private static final String WRONG_MOVE_PATH = "src/media/wrongMove.wav";
    private static final String BACKGROUND_MUSIC_PATH = "src/media/backgroundMusic.wav";

    private Clip clip;

    public void playValidMove() {
        play(VALID_MOVE_PATH, false);
    }

    public void playWrongMove() {
        play(WRONG_MOVE_PATH, false);
    }

    public void playBackgroundMusic() {
        play(BACKGROUND_MUSIC_PATH, true);
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
        }
    }

    private void play(String filePath, boolean loop) {
        try {
            if (clip != null) {
                clip.stop();
                clip.close();
            }
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            if (loop) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
