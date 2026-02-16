package audio;

import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Gestor de audio del juego. Maneja la reproducción de música de fondo y
 * efectos de sonido.
 */
public class GestorAudio {

    Clip clip;
    URL[] soundURL = new URL[30];

    public GestorAudio() {
        soundURL[0] = getClass().getResource("/sound/Doom.wav");
        soundURL[1] = getClass().getResource("/sound/coin.wav");
        soundURL[2] = getClass().getResource("/sound/powerup.wav");
        soundURL[3] = getClass().getResource("/sound/unlock.wav");
        soundURL[4] = getClass().getResource("/sound/fanfare.wav");
        soundURL[5] = getClass().getResource("/sound/MenuDoom.wav");
        // soundURL[6] = getClass().getResource("/sound/battle_music.wav");
        // soundURL[7] = getClass().getResource("/sound/attack.wav");
    }

    public void setFile(int i) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
            clip = AudioSystem.getClip();
            clip.open(ais);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (clip != null)
            clip.start();
    }

    public void loop() {
        if (clip != null)
            clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stop() {
        if (clip != null)
            clip.stop();
    }
}
