package mundo;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.imageio.ImageIO;

import configuracion.Configuracion;
import audio.GestorAudio;
import colision.DetectorColisiones;
import entidad.*;
import estadisticas.Estadisticas;
import items.SuperObjeto;
import tiles.TileManager;
import utilidades.Herramientas;
import utilidades.Notificacion;

/**
 * Contiene TODA la data y lógica del mundo de juego.
 *
 * Responsabilidades:
 *  - Almacena arrays de entidades (jugador, npcs, objetos, proyectiles)
 *  - Ejecuta la lógica de update (movimiento, colisiones, respawn, muerte)
 *  - Mantiene el estado del juego (gameState)
 *  - Coordina subsistemas (TileManager, DetectorColisiones, GrillaEspacial, etc.)
 *
 * OPTIMIZACIONES PRESERVADAS:
 *  - Object Pool de 1000 NPCs pre-instanciados
 *  - Spatial Hash Grid para colisiones O(N)
 *  - Frustum Culling y Logical Culling
 *  - Pre-allocated rectangles (zero GC)
 */
public class MundoJuego {

    // ===== ESTADO DEL JUEGO =====
    public int gameState;

    // ===== ENTIDADES =====
    public Jugador jugador;
    public NPC[] npcs = new NPC[GestorRecursos.POOL_TOTAL];
    public int contadorNPCs = 0;
    public SuperObjeto[] objs = new SuperObjeto[Configuracion.MAX_OBJETOS];
    public Proyectil[] proyectiles = new Proyectil[Configuracion.MAX_PROYECTILES];

    // ===== SUBSISTEMAS =====
    public GrillaEspacial grillaEspacial;
    public TileManager tileManager;
    public DetectorColisiones dColisiones;
    public GestorRecursos gestorRecursos;
    public Estadisticas estadisticas;
    public GestorAudio musica;
    public GestorAudio efectoSonido;

    // ===== NOTIFICACIONES =====
    public ArrayList<Notificacion> notificaciones = new ArrayList<>();

    // ===== FONDO MENÚ =====
    public BufferedImage imagenFondoMenu;

    // ===== RESPAWN =====
    private int contadorRespawn = 0;
    private final int intervaloRespawn = 60;
    private int contadorVerificacionCercanos = 0;
    private final int intervaloVerificacionCercanos = 180;

    // ===== DELAY GAME OVER =====
    private boolean jugadorMuerto = false;
    private int contadorGameOver = 0;
    private final int delayGameOver = 180;

    // ===== CONSTRUCTOR =====

    public MundoJuego() {
        musica = new GestorAudio();
        efectoSonido = new GestorAudio();
        estadisticas = new Estadisticas();

        int tamanioCelda = Configuracion.TAMANO_TILE * 3;
        grillaEspacial = new GrillaEspacial(
                Configuracion.MUNDO_ANCHO, Configuracion.MUNDO_ALTO, tamanioCelda);

        tileManager = new TileManager(this);
        dColisiones = new DetectorColisiones(this);
        gestorRecursos = new GestorRecursos(this);
    }

    // ===== SETUP =====

    /**
     * Configuración inicial del mundo (llamado una sola vez al inicio).
     */
    public void setupJuego() {
        Estadisticas.cargarStats();

        // Cargar imagen de fondo
        try {
            imagenFondoMenu = ImageIO.read(getClass().getResourceAsStream("/bg/fonfoPantallaPrincipal.png"));
            Herramientas tool = new Herramientas();
            imagenFondoMenu = tool.escalarImagen(imagenFondoMenu,
                    Configuracion.ANCHO_PANTALLA, Configuracion.ALTO_PANTALLA);
        } catch (Exception e) {
            System.out.println("Error al cargar imagen de fondo: " + e.getMessage());
            imagenFondoMenu = null;
        }

        // Pre-instanciar Object Pool de NPCs
        gestorRecursos.inicializarPool();

        // Iniciar en menú principal
        gameState = Configuracion.ESTADO_MENU;
    }

    // ===== UPDATE =====

    /**
     * Actualiza toda la lógica del juego (solo en playState).
     */
    public void update() {
        if (gameState != Configuracion.ESTADO_JUGANDO) return;

        jugador.update();
        estadisticas.actualizar();

        // Notificaciones
        for (int i = notificaciones.size() - 1; i >= 0; i--) {
            notificaciones.get(i).actualizar();
            if (!notificaciones.get(i).estaActiva()) {
                notificaciones.remove(i);
            }
        }

        // Poblar Spatial Hash Grid
        grillaEspacial.limpiar();
        for (int i = 0; i < npcs.length; i++) {
            if (npcs[i] != null && npcs[i].activo) {
                grillaEspacial.insertar(i, npcs[i].worldx, npcs[i].worldy);
            }
        }

        // Actualizar NPCs activos
        for (int i = 0; i < npcs.length; i++) {
            if (npcs[i] != null && npcs[i].activo) {
                npcs[i].update();
            }
        }

        // Respawn de enemigos
        contadorRespawn++;
        if (contadorRespawn >= intervaloRespawn) {
            gestorRecursos.respawnearEnemigos();
            contadorRespawn = 0;
        }

        contadorVerificacionCercanos++;
        if (contadorVerificacionCercanos >= intervaloVerificacionCercanos) {
            gestorRecursos.verificarYSpawnearCercanos();
            contadorVerificacionCercanos = 0;
        }

        // Proyectiles
        for (int i = 0; i < proyectiles.length; i++) {
            if (proyectiles[i] != null && proyectiles[i].activo) {
                proyectiles[i].update();
            } else if (proyectiles[i] != null && !proyectiles[i].activo) {
                proyectiles[i] = null;
            }
        }

        // Muerte del jugador (delay para animación)
        if (!jugador.estaVivo && !jugadorMuerto) {
            estadisticas.finalizarJuego();
            jugadorMuerto = true;
            contadorGameOver = 0;
        }
        if (jugadorMuerto) {
            contadorGameOver++;
            if (contadorGameOver >= delayGameOver) {
                gameState = Configuracion.ESTADO_GAME_OVER;
            }
        }
    }

    // ===== INICIAR / REINICIAR =====

    /**
     * Inicia una partida con el personaje seleccionado.
     */
    public void iniciarJuego(String tipoPersonaje, entrada.GestorEntrada entrada) {
        // Crear jugador con referencia al mundo y entrada
        jugador = new Jugador(this, entrada);
        jugador.configurarPersonaje(tipoPersonaje);
        jugador.powerUps = new PowerUpManager();

        // Limpiar objetos
        for (int i = 0; i < objs.length; i++) objs[i] = null;

        // Desactivar pool (NO null)
        gestorRecursos.desactivarTodos();

        // Limpiar proyectiles
        for (int i = 0; i < proyectiles.length; i++) proyectiles[i] = null;

        // Limpiar notificaciones
        notificaciones.clear();

        // Reiniciar estadísticas
        estadisticas = new Estadisticas();
        estadisticas.setCallbackNotificacion(this::agregarNotificacion);
        estadisticas.iniciar();

        // Reset flags de muerte
        jugadorMuerto = false;
        contadorGameOver = 0;

        // Colocar objetos y NPCs
        gestorRecursos.setObjetct();
        gestorRecursos.setNPCs();

        // Música
        stopMusic();
        reproducirMusicaFondo(0);

        // ¡A jugar!
        gameState = Configuracion.ESTADO_JUGANDO;
    }

    /**
     * Vuelve al menú principal (reinicia).
     */
    public void reiniciarJuego() {
        gameState = Configuracion.ESTADO_MENU;
        stopMusic();
    }

    // ===== AUDIO =====

    public void reproducirMusicaFondo(int i) {
        musica.setFile(i);
        musica.play();
        musica.loop();
    }

    public void stopMusic() { musica.stop(); }

    public void playSE(int i) {
        efectoSonido.setFile(i);
        efectoSonido.play();
    }

    // ===== NOTIFICACIONES =====

    public void agregarNotificacion(String mensaje, Color color, int duracionSegundos) {
        notificaciones.add(new Notificacion(mensaje, color, duracionSegundos));
        if (notificaciones.size() > 10) notificaciones.remove(0);
    }
}
