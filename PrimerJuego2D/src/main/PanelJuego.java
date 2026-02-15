package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

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
	MouseHandler mh;
	Thread threadJuego;

	// jugador
	public Jugador jugador = new Jugador(this, kh);

	// FPS
	int FPS = 60;

	public superObjeto[] objs = new superObjeto[15];

	// Sistema de NPCs (Object Pool pre-instanciado)
	public NPC[] npcs = new NPC[AssetSetter.POOL_TOTAL];
	public int contadorNPCs = 0; // Contador actual de NPCs activos

	// Sistema de proyectiles
	public Proyectil[] proyectiles = new Proyectil[100];
	public int contadorProyectiles = 0;

	// ===== SPATIAL HASH GRID para colisiones eficientes =====
	public SpatialHashGrid spatialGrid;

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
	public final int gameOverState = 4; // Game Over
	public final int menuState = 5; // Menú principal
	public final int seleccionState = 6; // Selección de personaje
	public final int creditosState = 7; // Créditos
	public final int ayudaState = 8; // Ayuda
	public final int logrosState = 9; // Logros
	// Estado actual del juego
	public int gameState;

	// Fondo centralizado para menús
	public BufferedImage imagenFondoMenu;

	// Sistema de medición de rendimiento
	private boolean chkTiempoDibujado = true; // debug
	private long iniciDibujo;
	private long TiempoDibujo;

	/**
	 * Configura el estado inicial del juego (coloca objetos, NPCs, etc).
	 */
	public void setupJuego() {
		// Cargar estadísticas guardadas
		GameStats.cargarStats();

		// Cargar imagen de fondo centralizada
		try {
			imagenFondoMenu = ImageIO.read(getClass().getResourceAsStream("/bg/fonfoPantallaPrincipal.png"));
			UtilityTool tool = new UtilityTool();
			imagenFondoMenu = tool.escalarImagen(imagenFondoMenu, anchoPantalla, altoPantalla);
		} catch (Exception e) {
			System.out.println("Error al cargar la imagen de fondo centralizada: " + e.getMessage());
			imagenFondoMenu = null;
		}

		// ===== Inicializar Spatial Hash Grid =====
		int tamanioCelda = tamanioTile * 3; // Cada celda = 3x3 tiles
		spatialGrid = new SpatialHashGrid(maxWorldAncho, maxWorldAlto, tamanioCelda);

		// ===== Pre-instanciar Object Pool de NPCs =====
		aSetter.inicializarPool();

		// Iniciar en el menú principal
		gameState = menuState;
	}

	TileManager tileManager = new TileManager(this);
	public detectorColisiones dColisiones = new detectorColisiones(this);

	public PanelJuego() {
		this.setPreferredSize(new Dimension(anchoPantalla, altoPantalla));
		this.setBackground(Color.BLACK);
		// MEJORA EL RENDIMIENTO
		this.setDoubleBuffered(true);
		this.addKeyListener(kh);

		// Agregar soporte para mouse
		mh = new MouseHandler(this, kh);
		this.addMouseListener(mh);
		this.addMouseMotionListener(mh);

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

	// Sistema de delay para Game Over (para apreciar animación de muerte)
	private boolean jugadorMuerto = false;
	private int contadorGameOver = 0;
	private final int delayGameOver = 180; // 3 segundos (180 frames a 60 FPS)

	public void update() {
		// Solo actualizar lógica de juego en playState
		if (gameState != playState) {
			return;
		}

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

		// ===== SPATIAL HASH GRID: poblar grilla con NPCs activos =====
		spatialGrid.limpiar();
		for (int i = 0; i < npcs.length; i++) {
			if (npcs[i] != null && npcs[i].activo) {
				spatialGrid.insertar(i, npcs[i].worldx, npcs[i].worldy);
			}
		}

		// ===== Actualizar NPCs activos (Object Pool) =====
		for (int i = 0; i < npcs.length; i++) {
			if (npcs[i] != null && npcs[i].activo) {
				npcs[i].update();
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

		// Verificar muerte del jugador (con delay para apreciar animación de muerte)
		if (!jugador.estaVivo && !jugadorMuerto) {
			stats.finalizarJuego();
			jugadorMuerto = true;
			contadorGameOver = 0;
		}

		// Contar frames después de la muerte antes de mostrar Game Over
		if (jugadorMuerto) {
			contadorGameOver++;
			if (contadorGameOver >= delayGameOver) {
				gameState = gameOverState;
			}
		}
	}

	/**
	 * Dibuja todos los componentes del juego (Tiles, Objetos, Jugador) en el panel.
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		iniciDibujo = System.nanoTime();

		Graphics2D g2 = (Graphics2D) g;

		// Solo dibujar mundo del juego en estados de juego
		if (gameState == playState || gameState == pauseState || gameState == gameOverState) {
			// tiles
			tileManager.draw(g2);
			// objetos
			for (int i = 0; i < objs.length; i++) {
				if (objs[i] != null) {
					objs[i].draw(g2, this);
				}
			}

			// ===== NPCs con FRUSTUM CULLING =====
			int margenCullX = jugador.screenX + tamanioTile;
			int margenCullY = jugador.screeny + tamanioTile;
			for (int i = 0; i < npcs.length; i++) {
				if (npcs[i] != null && npcs[i].activo) {
					// Solo dibujar si está dentro de la cámara
					int dx = npcs[i].worldx - jugador.worldx;
					int dy = npcs[i].worldy - jugador.worldy;
					if (dx > -margenCullX && dx < margenCullX
							&& dy > -margenCullY && dy < margenCullY) {
						npcs[i].draw(g2);
					}
				}
			}

			// Proyectiles con Frustum Culling
			for (int i = 0; i < proyectiles.length; i++) {
				if (proyectiles[i] != null && proyectiles[i].activo) {
					int dx = proyectiles[i].worldX - jugador.worldx;
					int dy = proyectiles[i].worldY - jugador.worldy;
					if (dx > -margenCullX && dx < margenCullX
							&& dy > -margenCullY && dy < margenCullY) {
						proyectiles[i].draw(g2);
					}
				}
			}
			// jugador
			jugador.draw(g2);
		}

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
		// Volver al menú principal
		gameState = menuState;

		// Detener música de batalla y reproducir música del menú
		stopMusic();
		// TODO: Descomentar cuando agregues res/sound/menu_music.wav
		// reproducirMusicaFondo(5); // Música del menú
	}

	/**
	 * Inicia el juego con el personaje seleccionado.
	 * Configura al jugador, coloca objetos/NPCs y arranca la música.
	 * 
	 * @param tipoPersonaje - "Sideral", "Mago" o "Doom"
	 */
	public void iniciarJuego(String tipoPersonaje) {
		// Configurar personaje
		jugador.configurarPersonaje(tipoPersonaje);

		// Reiniciar jugador
		jugador.powerUps = new PowerUpManager();

		// Limpiar objetos
		for (int i = 0; i < objs.length; i++) {
			objs[i] = null;
		}

		// ===== OBJECT POOL: desactivar todos los NPCs (NO null) =====
		aSetter.desactivarTodos();

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

		// Resetear flag de muerte
		jugadorMuerto = false;
		contadorGameOver = 0;

		// Colocar objetos y spawnear NPCs (usando pool)
		aSetter.setObjetct();
		aSetter.setNPCs();

		// Sistema de música
		stopMusic();
		reproducirMusicaFondo(0); // Doom.wav

		// Iniciar juego
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
