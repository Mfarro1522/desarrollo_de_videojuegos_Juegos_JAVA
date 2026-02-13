package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;

import javax.swing.JPanel;

import entidad.Jugador;
import entidad.NPC;
import entidad.PowerUpManager;
import entidad.Proyectil;
import objetos.superObjeto;
import tiles.TileManager;

/**
 * Panel principal del juego que maneja el bucle de juego, la renderización y la
 * lógica.
 * Implementa un sistema de tiles retro escalados (original 32x32, escalado x2).
 */
@SuppressWarnings("serial")
public class PanelJuego extends JPanel implements Runnable {
	int prueba;
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
	public final int maxWorldcol = 100;
	public final int maxWorldfilas = 100;
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

	// Sistema de NPCs
	public NPC[] npcs = new NPC[100]; // Máximo 100 NPCs simultáneos
	public int contadorNPCs = 0; // Contador actual de NPCs vivos

	// Sistema de proyectiles
	public Proyectil[] proyectiles = new Proyectil[100];
	public int contadorProyectiles = 0;

	// Sistema de estadísticas
	public GameStats stats = new GameStats();

	// Sistema de notificaciones
	public java.util.ArrayList<Notificacion> notificaciones = new java.util.ArrayList<>();

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
	public final int gameOverState = 4; // Game Over (nuevo)
	// Estado actual del juego
	public int gameState;

	// Sistema de medición de rendimiento
	private boolean chkTiempoDibujado = true; // debug
	private long iniciDibujo;
	private long TiempoDibujo;

	/**
	 * Configura el estado inicial del juego (coloca objetos, NPCs, etc).
	 */
	public void setupJuego() {
		aSetter.setObjetct();
		aSetter.setNPCs();
		reproducirMusicaFondo(0);

		// Iniciar estadísticas
		stats.setPanelJuego(this);
		stats.iniciar();

		// iniciar estado de juego
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

	// Sistema de respawn de enemigos
	private int contadorRespawn = 0;
	private final int intervaloRespawn = 60; // Cada 1 segundo (60 FPS)
	private int contadorVerificacionCercanos = 0;
	private final int intervaloVerificacionCercanos = 180; // Cada 3 segundos

	public void update() {
		if (gameState == playState) {
			// Estado : jugando
			jugador.update();

			// Actualizar estadísticas
			stats.actualizar();

			// Actualizar notificaciones
			for (int i = notificaciones.size() - 1; i >= 0; i--) {
				notificaciones.get(i).actualizar();
				if (!notificaciones.get(i).estaActiva()) {
					notificaciones.remove(i);
				}
			}

			// Actualizar NPCs
			for (int i = 0; i < npcs.length; i++) {
				if (npcs[i] != null) {
					if (npcs[i].estaVivo) {
						// NPC vivo: actualizar normalmente
						npcs[i].update();
					} else {
						// NPC muriendo: actualizar animación de muerte
						npcs[i].update();

						// Eliminar solo después de completar la animación de muerte
						if (npcs[i].frameMuerte >= 3) {
							npcs[i] = null;
							contadorNPCs--;
						}
					}
				}
			}

			// Sistema de respawn continuo de enemigos
			contadorRespawn++;
			if (contadorRespawn >= intervaloRespawn) {
				aSetter.respawnearEnemigos();
				contadorRespawn = 0;
			}

			// Verificar enemigos cercanos y spawnear si no hay
			contadorVerificacionCercanos++;
			if (contadorVerificacionCercanos >= intervaloVerificacionCercanos) {
				aSetter.verificarYSpawnearCercanos();
				contadorVerificacionCercanos = 0;
			}

			// Actualizar proyectiles
			for (int i = 0; i < proyectiles.length; i++) {
				if (proyectiles[i] != null && proyectiles[i].activo) {
					proyectiles[i].update();
				} else if (proyectiles[i] != null && !proyectiles[i].activo) {
					proyectiles[i] = null;
				}
			}

			// Verificar muerte del jugador
			if (!jugador.estaVivo) {
				stats.finalizarJuego();
				gameState = gameOverState;
			}
		} else {
			// Estado: en pausa o game over
			// aqui ira lo demas

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
		// NPCs
		for (int i = 0; i < npcs.length; i++) {
			if (npcs[i] != null) {
				// Dibujar NPC si está vivo O si está muriendo (para mostrar animación)
				npcs[i].draw(g2);
			}
		}
		// Proyectiles
		for (int i = 0; i < proyectiles.length; i++) {
			if (proyectiles[i] != null && proyectiles[i].activo) {
				proyectiles[i].draw(g2);
			}
		}
		// juagador
		jugador.draw(g2);

		// 4. UI (HUD) - ¡SIEMPRE AL FINAL!
		ui.draw(g2);

		TiempoDibujo = System.nanoTime() - iniciDibujo;

		if (chkTiempoDibujado) {
			/*
			 * Font arial_20 = new Font("Arial", Font.PLAIN, 20);
			 * g2.setFont(arial_20);
			 * g2.setColor(Color.WHITE);
			 * g2.drawString("Tiempo Dibujado : "+TiempoDibujo + ".ns ", 10 , 400);
			 * g2.drawString("Tiempo Dibujado : " + (TiempoDibujo/ 1000000.0) + "ms", 10,
			 * 440);
			 */
		}

		g2.dispose();
	}

	/**
	 * 
	 * Reproduce música de fondo en bucle.
	 * 
	 * @param i - Índice del archivo de música en Sound.soundURL[]
	 */
	public void reproducirMusicaFondo(int i) {
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
	 * 
	 * @param i - Índice del efecto de sonido en Sound.soundURL[]
	 */
	public void playSE(int i) {
		efectoSonido.setFile(i);
		efectoSonido.play();
	}

	/**
	 * Reinicia el juego a su estado inicial
	 */
	public void reiniciarJuego() {
		// Reiniciar jugador
		jugador.setValorePorDefecto();
		jugador.powerUps = new PowerUpManager();

		// Limpiar NPCs
		for (int i = 0; i < npcs.length; i++) {
			npcs[i] = null;
		}
		contadorNPCs = 0;

		// Limpiar proyectiles
		for (int i = 0; i < proyectiles.length; i++) {
			proyectiles[i] = null;
		}

		// Limpiar notificaciones
		notificaciones.clear();

		// Reiniciar estadísticas
		stats = new GameStats();
		stats.setPanelJuego(this);
		stats.iniciar();

		// Reiniciar objetos y NPCs
		aSetter.setObjetct();
		aSetter.setNPCs();

		// Volver al juego
		gameState = playState;
	}

	/**
	 * Agrega una notificación a la pantalla.
	 * 
	 * @param mensaje          El mensaje a mostrar
	 * @param color            El color del mensaje
	 * @param duracionSegundos Duración en segundos
	 */
	public void agregarNotificacion(String mensaje, Color color, int duracionSegundos) {
		notificaciones.add(new Notificacion(mensaje, color, duracionSegundos));
		// Limitar a 10 notificaciones máximo
		if (notificaciones.size() > 10) {
			notificaciones.remove(0);
		}
	}
}
