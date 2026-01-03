package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;

import javax.swing.JPanel;

import entidad.Jugador;
import objetos.superObjeto;
import tiles.TileManager;

/**
 * Panel principal del juego que maneja el bucle de juego, la renderización y la
 * lógica.
 * Implementa un sistema de tiles retro escalados (original 32x32, escalado x2).
 */
@SuppressWarnings("serial")
public class PanelJuego extends JPanel implements Runnable {
	int prueba ;
	int prueba2;
	final int OriginalTile = 32; // cambie el juego de 16x16 a 32x32
	final int scale = 2;

	public final int tamanioTile = OriginalTile * scale; // 64x64
	// relacion 4*3 clasica
	public final int maxPantallaColumnas = 16;
	public final int maxPantallaFilas = 12;
	// tamaño de la panatalla
	public final int anchoPantalla = tamanioTile * maxPantallaColumnas;
	public final int altoPantalla = tamanioTile * maxPantallaFilas;

	// ajustede del mundo
	public final int maxWorldcol = 50;
	public final int maxWorldfilas = 50;
	public final int maxWorldAncho = maxWorldcol * tamanioTile;
	public final int maxWorldAlto = maxWorldfilas * tamanioTile;

	// lsitener
	keyHandler kh = new keyHandler(this);
	Thread threadJuego;

	// jugador
	public Jugador jugador = new Jugador(this, kh);

	// FPS
	int FPS = 60;

	public superObjeto[] objs = new superObjeto[15];
	AssetSetter aSetter = new AssetSetter(this);
	
	// Sistema de sonido
	Sound musica = new Sound();
	Sound efectoSonido = new Sound();
	
	// Interfaz de Usuario (HUD)
	public UI ui = new UI(this);
	
	
	// SISTEMA DE ESTADOS DEL JUEGO (Game State)
	public final int tituloState = 0; // Pantalla de título
	public final int playState = 1; // Jugando
	public final int pauseState = 2; // Pausado
	public final int dialogoState = 3; // En diálogo (futuro)
	// Estado actual del juego
	public int gameState;
	
	
	// Sistema de medición de rendimiento
	private boolean chkTiempoDibujado = true; //debug
	private long iniciDibujo;
	private long TiempoDibujo;

	/**
	 * Configura el estado inicial del juego (coloca objetos, NPCs, etc).
	 */
	public void setupJuego() {
		aSetter.setObjetct();
		reproducirMusicaFondo(0);
		
		//iniciar estado de juego
		gameState = playState;
	}

	TileManager tileManager = new TileManager(this);
	public detectorColisiones dColisiones = new detectorColisiones(this);

	public PanelJuego() {
		this.setPreferredSize(new Dimension(anchoPantalla, altoPantalla));
		this.setBackground(Color.BLACK);
		// MEJORA EL RENDIMIENTO
		this.setDoubleBuffered(true);
		this.addKeyListener(kh);
		setFocusable(true);

	}

	public void iniciarHiloJuego() {
		threadJuego = new Thread(this);
		threadJuego.start();
	}

	/**
	 * Bucle principal del juego (Game Loop).
	 * Controla la actualización de lógica y el repintado a 60 FPS.
	 */
	public void run() {
		double intervaloDibujo = 1000000000 / FPS;
		double delta = 0;
		double tiempoFinal = System.nanoTime();
		double tiempoActual;
		double temporizador = 0;
		double contador = 0;

		while (threadJuego != null) {

			tiempoActual = System.nanoTime();
			delta += (tiempoActual - tiempoFinal) / intervaloDibujo;
			temporizador += (tiempoActual - tiempoFinal);
			tiempoFinal = tiempoActual;

			while (delta >= 1) {
				update();
				repaint();
				// si estas en linux agrega esta linea
				Toolkit.getDefaultToolkit().sync();
				delta--;
				contador++;

			}

			if (temporizador >= 1000000000) {
				temporizador = 0;
				contador = 0;
			}

		}
	}

	public void update() {
		if (gameState == playState) {
			//Estado : el jugando
			jugador.update();
		} else {
			//Estado: en pausa
			//aqui ira lo demas

		}
	}

	/**
	 * Dibuja todos los componentes del juego (Tiles, Objetos, Jugador) en el panel.
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		iniciDibujo = System.nanoTime();
		
		Graphics2D g2 = (Graphics2D) g;
		// tiles
		tileManager.draw(g2);
		// objetos
		for (int i = 0; i < objs.length; i++) {
			if (objs[i] != null) {
				objs[i].draw(g2, this);
			}
		}
		// juagador
		jugador.draw(g2);
		
		// 4. UI (HUD) - ¡SIEMPRE AL FINAL!
		 ui.draw(g2);
		 
		 TiempoDibujo = System.nanoTime() - iniciDibujo;
		 
		 if(chkTiempoDibujado) {
			 /*Font arial_20 = new Font("Arial", Font.PLAIN, 20);
			 g2.setFont(arial_20);
			 g2.setColor(Color.WHITE);
			 g2.drawString("Tiempo Dibujado : "+TiempoDibujo + ".ns ", 10 , 400);
			 g2.drawString("Tiempo Dibujado : " + (TiempoDibujo/ 1000000.0) + "ms", 10, 440);*/
		 }
		 
		 
		 
		g2.dispose();
	}
	
	/**

	* Reproduce música de fondo en bucle.
	* @param i - Índice del archivo de música en Sound.soundURL[]
	*/
	public void reproducirMusicaFondo (int i) {
		musica.setFile(i);
		musica.play();
		musica.loop();
	}
	/**
	 * detener musica
	 */
	public void stopMusic() {
		 musica.stop();
		}
	
	/**
	* Reproduce un efecto de sonido puntual.
	* @param i - Índice del efecto de sonido en Sound.soundURL[]
	*/
	public void playSE(int i) {
	 efectoSonido.setFile(i);
	 efectoSonido.play();
	}
}
