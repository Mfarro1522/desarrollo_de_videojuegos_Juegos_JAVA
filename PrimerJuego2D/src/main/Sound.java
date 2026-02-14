package main;

import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Gestor de audio del juego. Maneja la reproducción de música de fondo y
 * efectos de sonido.
 */
public class Sound {

	Clip clip;
	URL[] soundURL = new URL[30]; // Array para almacenar rutas de audio

	/**
	 * Constructor: Inicializa las rutas de los archivos de sonido.
	 */
	public Sound() {

		// Índice 0: Música de fondo (batalla)
		soundURL[0] = getClass().getResource("/sound/Doom.wav");
		// Índice 1: Efecto de recoger llave
		soundURL[1] = getClass().getResource("/sound/coin.wav");
		// Índice 2: Efecto de recoger power-up (botas)
		soundURL[2] = getClass().getResource("/sound/powerup.wav");
		// Índice 3: Efecto de abrir puerta
		soundURL[3] = getClass().getResource("/sound/unlock.wav");
		// Índice 4: Efecto de abrir cofre
		soundURL[4] = getClass().getResource("/sound/fanfare.wav");
		
		// TODO: Agregar nuevos archivos de audio
		// Índice 5: Música del menú principal
		// 1. Colocar archivo en: res/sound/menu_music.wav
		// 2. Descomentar la siguiente línea:
		// soundURL[5] = getClass().getResource("/sound/menu_music.wav");
		
		// Índice 6: Música de batalla alternativa (más intensa)
		// 1. Colocar archivo en: res/sound/battle_music.wav
		// 2. Descomentar la siguiente línea:
		// soundURL[6] = getClass().getResource("/sound/battle_music.wav");
		
		// Índice 7: Efecto de sonido al atacar
		// 1. Colocar archivo en: res/sound/attack.wav (sonido corto, como "swoosh" o impacto)
		// 2. Descomentar la siguiente línea:
		// soundURL[7] = getClass().getResource("/sound/attack.wav");

	}

	/**
	 * Carga un archivo de audio en memoria.
	 * 
	 * @param i - Índice del sonido en el array soundURL.
	 */

	public void setFile(int i) {
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
			clip = AudioSystem.getClip();
			clip.open(ais);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Inicia la reproducción del clip cargado.
	 */

	public void play() {
		if (clip != null)
			clip.start();
	}

	/**
	 * Reproduce el clip en bucle infinito (para música de fondo).
	 */
	public void loop() {
		if (clip != null)
			clip.loop(Clip.LOOP_CONTINUOUSLY);
	}

	/**
	 * Detiene la reproducción del clip.
	 */
	public void stop() {
		if (clip != null)
			clip.stop();
	}

}
