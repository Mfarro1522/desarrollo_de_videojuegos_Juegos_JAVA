package audio;

import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Gestor de audio del juego. Maneja la reproducción de música de fondo y
 * efectos de sonido.
 *
 * Los clips se pre-cargan una sola vez al inicio para evitar leer
 * archivos de disco en cada reproducción (causa de lag grave).
 */
public class GestorAudio {

    /** Clips pre-cargados (uno por índice de sonido). */
    private Clip[] clips = new Clip[30];
    private URL[] soundURL = new URL[30];

    /** Índice del clip activo (para música con loop). */
    private int clipActivo = -1;

    // ===== ÍNDICES DE SONIDO =====
    // Música
    public static final int MUSICA_JUEGO = 0;
    public static final int MUSICA_MENU = 5;

    // Efectos existentes
    public static final int SE_COIN = 1;
    public static final int SE_POWERUP = 2;
    public static final int SE_UNLOCK = 3;
    public static final int SE_FANFARE = 4;

    // Efectos de jugador
    public static final int SE_ATAQUE = 6;
    public static final int SE_GOLPE_JUGADOR = 7;
    public static final int SE_MUERTE_JUGADOR = 8;
    public static final int SE_LEVEL_UP = 9;

    // Efectos de cofre / items
    public static final int SE_COFRE_ABRIR = 10;
    public static final int SE_AMULETO_EQUIPAR = 11;
    public static final int SE_USAR_ITEM = 12;

    // Power-ups
    public static final int SE_HEAL = 13;
    public static final int SE_SPEED_BOOST = 14;
    public static final int SE_INVENCIBILIDAD = 15;
    public static final int SE_EXPLOSION = 16;

    // Boss
    public static final int SE_BOSS_ROAR = 17;
    public static final int SE_BOSS_HIT = 18;
    public static final int SE_BOSS_DEATH = 19;
    public static final int SE_FURY_ACTIVATE = 20;

    // UI
    public static final int SE_PAUSA = 21;
    public static final int SE_DESPAUSA = 22;
    public static final int SE_CONFIRM = 23;

    public GestorAudio() {
        // Registrar URLs
        soundURL[MUSICA_JUEGO] = getClass().getResource("/sound/Doom.wav");
        soundURL[MUSICA_MENU] = getClass().getResource("/sound/MenuDoom.wav");
        soundURL[SE_COIN] = getClass().getResource("/sound/coin.wav");
        soundURL[SE_POWERUP] = getClass().getResource("/sound/powerup.wav");
        soundURL[SE_UNLOCK] = getClass().getResource("/sound/unlock.wav");
        soundURL[SE_FANFARE] = getClass().getResource("/sound/fanfare.wav");
        soundURL[SE_ATAQUE] = getClass().getResource("/sound/Attack.wav");
        soundURL[SE_GOLPE_JUGADOR] = getClass().getResource("/sound/Hit.wav");
        soundURL[SE_MUERTE_JUGADOR] = getClass().getResource("/sound/player_death.wav");
        soundURL[SE_LEVEL_UP] = getClass().getResource("/sound/level_up.wav");
        soundURL[SE_COFRE_ABRIR] = getClass().getResource("/sound/chest_open.wav");
        soundURL[SE_AMULETO_EQUIPAR] = getClass().getResource("/sound/amuleto_equip.wav");
        soundURL[SE_USAR_ITEM] = getClass().getResource("/sound/use_item.wav");
        soundURL[SE_HEAL] = getClass().getResource("/sound/heal.wav");
        soundURL[SE_SPEED_BOOST] = getClass().getResource("/sound/speed_boost.wav");
        soundURL[SE_INVENCIBILIDAD] = getClass().getResource("/sound/invincibility.wav");
        soundURL[SE_EXPLOSION] = getClass().getResource("/sound/explosion.wav");
        soundURL[SE_BOSS_ROAR] = getClass().getResource("/sound/boss_roar.wav");
        soundURL[SE_BOSS_HIT] = getClass().getResource("/sound/boss_hit.wav");
        soundURL[SE_BOSS_DEATH] = getClass().getResource("/sound/boss_death.wav");
        soundURL[SE_FURY_ACTIVATE] = getClass().getResource("/sound/fury_activate.wav");
        soundURL[SE_PAUSA] = getClass().getResource("/sound/Pause.wav");
        soundURL[SE_DESPAUSA] = getClass().getResource("/sound/Unpause.wav");
        soundURL[SE_CONFIRM] = getClass().getResource("/sound/Confirm.wav");

        // Pre-cargar TODOS los clips una sola vez
        for (int i = 0; i < soundURL.length; i++) {
            if (soundURL[i] != null) {
                try {
                    AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
                    clips[i] = AudioSystem.getClip();
                    clips[i].open(ais);
                    ais.close();
                } catch (Exception e) {
                    System.err.println("No se pudo cargar sonido [" + i + "]: " + e.getMessage());
                    clips[i] = null;
                }
            }
        }
    }

    /**
     * Reproduce un efecto de sonido (sin loop). Si el clip ya se estaba
     * reproduciendo, lo reinicia desde el principio.
     */
    public void play(int i) {
        if (i < 0 || i >= clips.length || clips[i] == null) return;
        clips[i].stop();
        clips[i].setFramePosition(0);
        clips[i].start();
    }

    /**
     * Reproduce música en loop continuo.
     */
    public void loop(int i) {
        if (i < 0 || i >= clips.length || clips[i] == null) return;
        // Detener música anterior si había
        if (clipActivo >= 0 && clipActivo < clips.length && clips[clipActivo] != null) {
            clips[clipActivo].stop();
        }
        clipActivo = i;
        clips[i].setFramePosition(0);
        clips[i].loop(Clip.LOOP_CONTINUOUSLY);
    }

    /**
     * Detiene la música activa.
     */
    public void stop() {
        if (clipActivo >= 0 && clipActivo < clips.length && clips[clipActivo] != null) {
            clips[clipActivo].stop();
        }
    }

    /**
     * Detiene un clip específico.
     */
    public void stop(int i) {
        if (i >= 0 && i < clips.length && clips[i] != null) {
            clips[i].stop();
        }
    }
}
