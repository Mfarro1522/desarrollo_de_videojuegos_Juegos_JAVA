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
 * - Almacena arrays de entidades (jugador, npcs, objetos, proyectiles)
 * - Ejecuta la lógica de update (movimiento, colisiones, respawn, muerte)
 * - Mantiene el estado del juego (gameState)
 * - Coordina subsistemas (TileManager, DetectorColisiones, GrillaEspacial,
 * etc.)
 *
 * OPTIMIZACIONES PRESERVADAS:
 * - Object Pool de 1000 NPCs pre-instanciados
 * - Spatial Hash Grid para colisiones O(N)
 * - Frustum Culling y Logical Culling
 * - Pre-allocated rectangles (zero GC)
 */
public class MundoJuego {

    // ===== ESTADO DEL JUEGO =====
    public int gameState;
    public int estadoAntesPausa = Configuracion.ESTADO_JUGANDO;

    // ===== ENTIDADES =====
    public Jugador jugador;
    public NPC[] npcs = new NPC[GestorRecursos.POOL_TOTAL];
    public int contadorNPCs = 0;
    public SuperObjeto[] objs = new SuperObjeto[Configuracion.MAX_OBJETOS];
    public Proyectil[] proyectiles = new Proyectil[Configuracion.MAX_PROYECTILES];

    // ===== BOSS =====
    public DemonBat bossActivo = null;
    public KingSlime[] kingSlimes = new KingSlime[3]; // 3 KingSlimes simultáneos
    public int kingSlimesVivos = 0;
    public ProyectilEspecial[] proyectilesEspeciales = new ProyectilEspecial[Configuracion.MAX_PROYECTILES_ESPECIALES];
    private boolean bossYaSpawneado = false; // Evita re-spawn del DemonBat
    private boolean kingSlimeYaSpawneado = false; // Evita re-spawn de KingSlimes

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

        // Pre-instanciar pool de proyectiles especiales del boss
        inicializarProyectilesEspeciales();

        // Iniciar en menú principal
        gameState = Configuracion.ESTADO_MENU;
        reproducirMusicaFondo(5);
    }

    // ===== UPDATE =====

    /**
     * Actualiza toda la lógica del juego (solo en playState).
     */
    public void update() {
        if (gameState != Configuracion.ESTADO_JUGANDO
                && gameState != Configuracion.ESTADO_BOSS_FIGHT)
            return;

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

        // ===== Generación de Enemigos (Anillo Dinámico) =====
        // Solo spawnear si NO hay boss fight
        if (gameState != Configuracion.ESTADO_BOSS_FIGHT) {
            gestorRecursos.spawnearEnAnillo();
        }

        // ===== BOSS =====
        // Verificar si hay que spawnear boss (nivel 4 = DemonBat, nivel 7 = KingSlime)
        if (!bossYaSpawneado) {
            gestorRecursos.verificarSpawnBoss();
        }
        if (!kingSlimeYaSpawneado) {
            gestorRecursos.verificarSpawnKingSlime();
        }

        // Actualizar boss DemonBat
        if (bossActivo != null && bossActivo.activo) {
            bossActivo.update();
        }

        // Actualizar KingSlimes
        for (int i = 0; i < kingSlimes.length; i++) {
            if (kingSlimes[i] != null && kingSlimes[i].activo) {
                kingSlimes[i].update();
            }
        }

        // Actualizar proyectiles especiales del boss
        for (int i = 0; i < proyectilesEspeciales.length; i++) {
            if (proyectilesEspeciales[i] != null && proyectilesEspeciales[i].activo) {
                proyectilesEspeciales[i].update();
            }
        }

        // Colisión de proyectiles del JUGADOR con el boss DemonBat
        if (bossActivo != null && bossActivo.activo && bossActivo.estaVivo) {
            verificarProyectilesContraBoss();
        }

        // Colisión de proyectiles del JUGADOR con KingSlimes
        for (int k = 0; k < kingSlimes.length; k++) {
            if (kingSlimes[k] != null && kingSlimes[k].activo && kingSlimes[k].estaVivo) {
                verificarProyectilesContraKingSlime(k);
            }
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
        for (int i = 0; i < objs.length; i++)
            objs[i] = null;

        // Desactivar pool (NO null)
        gestorRecursos.desactivarTodos();

        // Limpiar proyectiles
        for (int i = 0; i < proyectiles.length; i++)
            proyectiles[i] = null;

        // Limpiar notificaciones
        notificaciones.clear();

        // Reiniciar estadísticas
        estadisticas = new Estadisticas();
        estadisticas.setCallbackNotificacion(this::agregarNotificacion);
        // CRITICAL: Conectar lógica de nivel
        estadisticas.setCallbackLevelUp(nivel -> {
            if (jugador != null)
                jugador.subirNivel();
        });
        estadisticas.iniciar();

        // Reset flags de muerte
        jugadorMuerto = false;
        contadorGameOver = 0;

        // Reset boss
        bossActivo = null;
        bossYaSpawneado = false;
        for (int i = 0; i < kingSlimes.length; i++) {
            kingSlimes[i] = null;
        }
        kingSlimesVivos = 0;
        kingSlimeYaSpawneado = false;
        for (int i = 0; i < proyectilesEspeciales.length; i++) {
            if (proyectilesEspeciales[i] != null) {
                proyectilesEspeciales[i].desactivar();
            }
        }

        // Colocar objetos y NPCs
        gestorRecursos.setObjetct();
        gestorRecursos.setNPCs();

        // Música
        stopMusic();
        reproducirMusicaFondo(0);

        gameState = Configuracion.ESTADO_JUGANDO;
    }

    /**
     * Vuelve al menú principal (reinicia).
     */
    public void reiniciarJuego() {
        gameState = Configuracion.ESTADO_MENU;
        stopMusic();
        reproducirMusicaFondo(5);
    }

    // ===== BOSS FIGHT =====

    private void inicializarProyectilesEspeciales() {
        for (int i = 0; i < proyectilesEspeciales.length; i++) {
            proyectilesEspeciales[i] = new ProyectilEspecial(this);
        }
        System.out.println("[Pool] ProyectilesEspeciales: " + proyectilesEspeciales.length + " pre-instanciados.");
    }

    public ProyectilEspecial obtenerProyectilEspecialLibre() {
        for (ProyectilEspecial pe : proyectilesEspeciales) {
            if (pe != null && !pe.activo) {
                return pe;
            }
        }
        return null; // Pool lleno
    }

    public void iniciarBossFight() {
        gameState = Configuracion.ESTADO_BOSS_FIGHT;
        bossYaSpawneado = true;

        // 1. Desactivar TODOS los NPCs normales
        for (int i = 0; i < npcs.length; i++) {
            if (npcs[i] != null && npcs[i].activo) {
                npcs[i].desactivar();
            }
        }

        // 2. Crear boss si no existe
        if (bossActivo == null) {
            bossActivo = new DemonBat(this);
        }

        // 3. Posicionar boss frente al jugador
        int offsetX = 400;
        int spawnX = jugador.worldx + offsetX;
        int spawnY = jugador.worldy;
        bossActivo.activar(spawnX, spawnY);

        // 4. Notificación
        agregarNotificacion("¡BOSS: DemonBat ha aparecido!", Color.RED, 4);
        System.out.println("[Boss] DemonBat spawneado en (" + spawnX + ", " + spawnY + ")");
    }

    public void terminarBossFight() {
        gameState = Configuracion.ESTADO_JUGANDO;

        // Desactivar proyectiles especiales restantes
        for (int i = 0; i < proyectilesEspeciales.length; i++) {
            if (proyectilesEspeciales[i] != null && proyectilesEspeciales[i].activo) {
                proyectilesEspeciales[i].desactivar();
            }
        }

        // Recompensar al jugador
        estadisticas.registrarEnemigoEliminado();
        estadisticas.ganarExperiencia(bossActivo.experienciaAOtorgar);

        agregarNotificacion("¡DemonBat derrotado! +" + bossActivo.experienciaAOtorgar + " XP", 
                new Color(255, 215, 0), 5);
        System.out.println("[Boss] DemonBat derrotado. Volviendo a JUGANDO.");
    }

    // ===== KING SLIME BOSS FIGHT =====

    public void iniciarKingSlimeFight() {
        gameState = Configuracion.ESTADO_BOSS_FIGHT;
        kingSlimeYaSpawneado = true;

        // 1. Desactivar TODOS los NPCs normales
        for (int i = 0; i < npcs.length; i++) {
            if (npcs[i] != null && npcs[i].activo) {
                npcs[i].desactivar();
            }
        }

        // 2. Crear 3 KingSlimes en formación triangular alrededor del jugador
        int dist = 300; // Distancia de spawn
        double[][] offsets = {
            { dist,  0 },           // Derecha
            { -dist * 0.5,  dist * 0.87 },  // Abajo-izquierda
            { -dist * 0.5, -dist * 0.87 }   // Arriba-izquierda
        };

        kingSlimesVivos = 3;
        for (int i = 0; i < 3; i++) {
            kingSlimes[i] = new KingSlime(this, i);
            int spawnX = jugador.worldx + (int) offsets[i][0];
            int spawnY = jugador.worldy + (int) offsets[i][1];
            kingSlimes[i].activar(spawnX, spawnY);
        }

        agregarNotificacion("¡BOSS: 3 KingSlimes han aparecido!", new Color(0, 200, 0), 4);
        System.out.println("[Boss] 3 KingSlimes spawneados.");
    }

    public void notificarKingSlimeMuerto(int indice) {
        kingSlimesVivos--;
        estadisticas.registrarEnemigoEliminado();
        estadisticas.ganarExperiencia(kingSlimes[indice].experienciaAOtorgar);

        agregarNotificacion("¡KingSlime #" + (indice + 1) + " derrotado! +"
                + kingSlimes[indice].experienciaAOtorgar + " XP",
                new Color(255, 215, 0), 3);

        System.out.println("[Boss] KingSlime #" + indice + " muerto. Quedan: " + kingSlimesVivos);

        if (kingSlimesVivos <= 0) {
            terminarKingSlimeFight();
        }
    }

    private void terminarKingSlimeFight() {
        gameState = Configuracion.ESTADO_JUGANDO;
        agregarNotificacion("¡Todos los KingSlimes derrotados!", new Color(255, 215, 0), 5);
        System.out.println("[Boss] KingSlime fight terminada. Volviendo a JUGANDO.");
    }

    private void verificarProyectilesContraKingSlime(int k) {
        KingSlime ks = kingSlimes[k];
        java.awt.Rectangle ksArea = new java.awt.Rectangle(
                ks.worldx + ks.AreaSolida.x,
                ks.worldy + ks.AreaSolida.y,
                ks.AreaSolida.width,
                ks.AreaSolida.height);

        for (int i = 0; i < proyectiles.length; i++) {
            if (proyectiles[i] != null && proyectiles[i].activo && proyectiles[i].esDelJugador) {
                java.awt.Rectangle pArea = new java.awt.Rectangle(
                        proyectiles[i].worldX, proyectiles[i].worldY, 16, 16);
                if (pArea.intersects(ksArea)) {
                    ks.recibirDanio(jugador.ataque);
                    proyectiles[i].activo = false;
                }
            }
        }
    }

    private void verificarProyectilesContraBoss() {
        java.awt.Rectangle bossArea = new java.awt.Rectangle(
                bossActivo.worldx + bossActivo.AreaSolida.x,
                bossActivo.worldy + bossActivo.AreaSolida.y,
                bossActivo.AreaSolida.width,
                bossActivo.AreaSolida.height);

        for (int i = 0; i < proyectiles.length; i++) {
            // Solo proyectiles DEL JUGADOR pueden dañar al boss
            if (proyectiles[i] != null && proyectiles[i].activo && proyectiles[i].esDelJugador) {
                java.awt.Rectangle pArea = new java.awt.Rectangle(
                        proyectiles[i].worldX, proyectiles[i].worldY, 16, 16);
                if (pArea.intersects(bossArea)) {
                    bossActivo.recibirDanio(jugador.ataque);
                    proyectiles[i].activo = false;
                }
            }
        }
    }

    // ===== AUDIO =====

    public void reproducirMusicaFondo(int i) {
        musica.setFile(i);
        musica.play();
        musica.loop();
    }

    public void stopMusic() {
        musica.stop();
    }

    public void playSE(int i) {
        efectoSonido.setFile(i);
        efectoSonido.play();
    }

    // ===== NOTIFICACIONES =====

    public void agregarNotificacion(String mensaje, Color color, int duracionSegundos) {
        notificaciones.add(new Notificacion(mensaje, color, duracionSegundos));
        if (notificaciones.size() > 10)
            notificaciones.remove(0);
    }
}
