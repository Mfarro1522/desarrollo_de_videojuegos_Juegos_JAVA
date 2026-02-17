package entidad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import configuracion.Configuracion;
import mundo.MundoJuego;
import utilidades.Herramientas;

/**
 * Boss "DemonBat" — Murciélago demonio gigante (71x80 sprite).
 *
 * MÁQUINA DE ESTADOS:
 * 1. PERSEGUIR_Y_ATACAR (15-20s): Persigue al jugador + dispara proyectiles normales
 * 2. QUIETO_DISPARANDO (5-10 proyectiles especiales): Se detiene, dispara calaveras perseguidoras
 * 3. MUERTO: Animación de muerte → fin de boss fight
 *
 * NO usa Object Pool (instancia única gestionada por MundoJuego.bossActivo).
 */
public class DemonBat extends NPC {

    // ===== ESTADOS DEL BOSS =====
    public enum EstadoBoss {
        PERSEGUIR_Y_ATACAR,
        QUIETO_DISPARANDO,
        MUERTO
    }

    public EstadoBoss estadoBoss = EstadoBoss.PERSEGUIR_Y_ATACAR;

    // ===== TIMERS Y CONTADORES =====
    private int tiempoEnFase = 0;
    private int proyectilesEspecialesLanzados = 0;
    private int maxProyectilesEspeciales;

    // Fase 1: 15-20 segundos (aleatorio)
    private int duracionFase1;
    private static final int DURACION_FASE1_MIN = 60 * 15; // 15s
    private static final int DURACION_FASE1_MAX = 60 * 20; // 20s

    // Proyectil normal cada 45 frames (0.75s)
    private static final int COOLDOWN_PROYECTIL_NORMAL = 45;
    private int contadorProyectilNormal = 0;

    // Proyectil especial cada 30 frames (0.5s)
    private static final int COOLDOWN_PROYECTIL_ESPECIAL = 30;
    private int contadorProyectilEspecial = 0;

    // ===== SPRITES (CACHÉ ESTÁTICO) =====
    private static BufferedImage s_idle1, s_idle2, s_idle3, s_idle4;
    private static BufferedImage s_idle1_izq, s_idle2_izq, s_idle3_izq, s_idle4_izq;
    private static BufferedImage s_ataque1, s_ataque2, s_ataque3, s_ataque4;
    private static BufferedImage s_ataque5, s_ataque6, s_ataque7;
    private static BufferedImage s_ataque1_izq, s_ataque2_izq, s_ataque3_izq, s_ataque4_izq;
    private static BufferedImage s_ataque5_izq, s_ataque6_izq, s_ataque7_izq;
    private static BufferedImage s_golpe1, s_golpe2, s_golpe3, s_golpe4;
    private static BufferedImage s_golpe1_izq, s_golpe2_izq, s_golpe3_izq, s_golpe4_izq;
    private static BufferedImage s_muerte1_boss, s_muerte2_boss, s_muerte3_boss, s_muerte4_boss;
    private static BufferedImage s_proyectilNormal;
    private static boolean spritesLoaded = false;

    // ===== ANIMACIÓN =====
    private String ultimaDireccionHorizontal = "derecha";
    private int frameActual = 1;
    private int contadorAnim = 0;
    private int velocidadAnim = 8;
    private int frameAtaque = 1;
    private int contadorAnimAtk = 0;
    private int velocidadAnimAtk = 6;

    // Dimensiones reales del sprite escalado
    private int spriteAncho;
    private int spriteAlto;

    // ===== CONSTRUCTOR =====
    public DemonBat(MundoJuego mundo) {
        super(mundo);
        tipoNPC = TipoNPC.BAT; // Usa slot BAT para compatibilidad de tipo
        inicializarEstadisticas();
        cargarSpritesEstaticos();

        // Escalar dimensiones del sprite (71x80 original → escalado x2)
        spriteAncho = 71 * Configuracion.ESCALA;
        spriteAlto = 80 * Configuracion.ESCALA;

        // Hitbox reducida al cuerpo central (~50% del sprite, ignora alas y transparencia)
        // Sprite escalado: 142x160. Cuerpo centrado: ~70x80
        AreaSolida = new Rectangle(36, 45, 70, 75);
        AreaSolidaDefaultX = AreaSolida.x;
        AreaSolidaDefaultY = AreaSolida.y;

        vuela = true;
    }

    private void inicializarEstadisticas() {
        vidaMaxima = 300;
        vidaActual = vidaMaxima;
        ataque = 8;
        defensa = 3;
        vel = 2;
        direccion = "izquierda";
        radioDeteccion = 30 * Configuracion.TAMANO_TILE; // Siempre persigue
        experienciaAOtorgar = 200;
    }

    // ===== CARGA DE SPRITES =====
    private synchronized void cargarSpritesEstaticos() {
        if (spritesLoaded) return;
        Herramientas tool = new Herramientas();
        try {
            String ruta = "/Npc/Bosses/DemonBat/DemonBat/";

            // Idle/Movimiento (demonBat01-04)
            s_idle1 = tool.escalarImagen(
                    ImageIO.read(DemonBat.class.getResourceAsStream(ruta + "demonBat01.png")),
                    71 * Configuracion.ESCALA, 80 * Configuracion.ESCALA);
            s_idle2 = tool.escalarImagen(
                    ImageIO.read(DemonBat.class.getResourceAsStream(ruta + "demonBat02.png")),
                    71 * Configuracion.ESCALA, 80 * Configuracion.ESCALA);
            s_idle3 = tool.escalarImagen(
                    ImageIO.read(DemonBat.class.getResourceAsStream(ruta + "demonBat03.png")),
                    71 * Configuracion.ESCALA, 80 * Configuracion.ESCALA);
            s_idle4 = tool.escalarImagen(
                    ImageIO.read(DemonBat.class.getResourceAsStream(ruta + "demonBat04.png")),
                    71 * Configuracion.ESCALA, 80 * Configuracion.ESCALA);

            // Izquierda (voltear)
            s_idle1_izq = tool.voltearImagenHorizontal(s_idle1);
            s_idle2_izq = tool.voltearImagenHorizontal(s_idle2);
            s_idle3_izq = tool.voltearImagenHorizontal(s_idle3);
            s_idle4_izq = tool.voltearImagenHorizontal(s_idle4);

            // Ataque (Ataque01-07: abrir boca progresivamente)
            s_ataque1 = tool.escalarImagen(
                    ImageIO.read(DemonBat.class.getResourceAsStream(ruta + "Ataque01.png")),
                    71 * Configuracion.ESCALA, 80 * Configuracion.ESCALA);
            s_ataque2 = tool.escalarImagen(
                    ImageIO.read(DemonBat.class.getResourceAsStream(ruta + "Ataque02.png")),
                    71 * Configuracion.ESCALA, 80 * Configuracion.ESCALA);
            s_ataque3 = tool.escalarImagen(
                    ImageIO.read(DemonBat.class.getResourceAsStream(ruta + "Ataque03.png")),
                    71 * Configuracion.ESCALA, 80 * Configuracion.ESCALA);
            s_ataque4 = tool.escalarImagen(
                    ImageIO.read(DemonBat.class.getResourceAsStream(ruta + "Ataque04.png")),
                    71 * Configuracion.ESCALA, 80 * Configuracion.ESCALA);
            s_ataque5 = tool.escalarImagen(
                    ImageIO.read(DemonBat.class.getResourceAsStream(ruta + "Ataque05.png")),
                    71 * Configuracion.ESCALA, 80 * Configuracion.ESCALA);
            s_ataque6 = tool.escalarImagen(
                    ImageIO.read(DemonBat.class.getResourceAsStream(ruta + "Ataque06.png")),
                    71 * Configuracion.ESCALA, 80 * Configuracion.ESCALA);
            s_ataque7 = tool.escalarImagen(
                    ImageIO.read(DemonBat.class.getResourceAsStream(ruta + "Ataque07.png")),
                    71 * Configuracion.ESCALA, 80 * Configuracion.ESCALA);

            // Ataque izquierda
            s_ataque1_izq = tool.voltearImagenHorizontal(s_ataque1);
            s_ataque2_izq = tool.voltearImagenHorizontal(s_ataque2);
            s_ataque3_izq = tool.voltearImagenHorizontal(s_ataque3);
            s_ataque4_izq = tool.voltearImagenHorizontal(s_ataque4);
            s_ataque5_izq = tool.voltearImagenHorizontal(s_ataque5);
            s_ataque6_izq = tool.voltearImagenHorizontal(s_ataque6);
            s_ataque7_izq = tool.voltearImagenHorizontal(s_ataque7);

            // Recibir golpe (RecibirGolpe01-04)
            s_golpe1 = tool.escalarImagen(
                    ImageIO.read(DemonBat.class.getResourceAsStream(ruta + "RecibirGolpe01.png")),
                    71 * Configuracion.ESCALA, 80 * Configuracion.ESCALA);
            s_golpe2 = tool.escalarImagen(
                    ImageIO.read(DemonBat.class.getResourceAsStream(ruta + "RecibirGolpe02.png")),
                    71 * Configuracion.ESCALA, 80 * Configuracion.ESCALA);
            s_golpe3 = tool.escalarImagen(
                    ImageIO.read(DemonBat.class.getResourceAsStream(ruta + "RecibirGolpe03.png")),
                    71 * Configuracion.ESCALA, 80 * Configuracion.ESCALA);
            s_golpe4 = tool.escalarImagen(
                    ImageIO.read(DemonBat.class.getResourceAsStream(ruta + "RecibirGolpe04.png")),
                    71 * Configuracion.ESCALA, 80 * Configuracion.ESCALA);

            s_golpe1_izq = tool.voltearImagenHorizontal(s_golpe1);
            s_golpe2_izq = tool.voltearImagenHorizontal(s_golpe2);
            s_golpe3_izq = tool.voltearImagenHorizontal(s_golpe3);
            s_golpe4_izq = tool.voltearImagenHorizontal(s_golpe4);

            // Muerte (Muerte01-04)
            s_muerte1_boss = tool.escalarImagen(
                    ImageIO.read(DemonBat.class.getResourceAsStream(ruta + "Muerte01.png")),
                    71 * Configuracion.ESCALA, 80 * Configuracion.ESCALA);
            s_muerte2_boss = tool.escalarImagen(
                    ImageIO.read(DemonBat.class.getResourceAsStream(ruta + "Muerte02.png")),
                    71 * Configuracion.ESCALA, 80 * Configuracion.ESCALA);
            s_muerte3_boss = tool.escalarImagen(
                    ImageIO.read(DemonBat.class.getResourceAsStream(ruta + "Muerte03.png")),
                    71 * Configuracion.ESCALA, 80 * Configuracion.ESCALA);
            s_muerte4_boss = tool.escalarImagen(
                    ImageIO.read(DemonBat.class.getResourceAsStream(ruta + "Muerte04.png")),
                    71 * Configuracion.ESCALA, 80 * Configuracion.ESCALA);

            // Proyectil normal
            s_proyectilNormal = tool.escalarImagen(
                    ImageIO.read(DemonBat.class.getResourceAsStream(ruta + "proyectil.png")),
                    16 * Configuracion.ESCALA, 16 * Configuracion.ESCALA);

            // Asignar sprites base heredados para compatibilidad con NPC.draw()
            spritesLoaded = true;
            System.out.println("[DemonBat] Sprites cargados correctamente.");

        } catch (Exception e) {
            System.err.println("[DemonBat] Error al cargar sprites: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ===== ACTIVACIÓN / DESACTIVACIÓN =====

    @Override
    public void activar(int x, int y) {
        worldx = x;
        worldy = y;
        vidaActual = vidaMaxima;
        estaVivo = true;
        activo = true;
        estado = EstadoEntidad.IDLE;
        estadoBoss = EstadoBoss.PERSEGUIR_Y_ATACAR;
        direccion = "izquierda";
        frameMuerte = 0;
        contadorMuerte = 0;
        contadorInvulnerabilidad = 0;
        contadorMovimiento = 0;
        hayColision = false;
        resetearContadorDanio(); // Resetear cooldown de contacto

        // Randomizar duración de fase 1
        duracionFase1 = DURACION_FASE1_MIN +
                (int) (Math.random() * (DURACION_FASE1_MAX - DURACION_FASE1_MIN));
        maxProyectilesEspeciales = 5 + (int) (Math.random() * 6); // 5-10
        tiempoEnFase = 0;
        proyectilesEspecialesLanzados = 0;
        contadorProyectilNormal = 0;
        contadorProyectilEspecial = 0;
        frameActual = 1;
        contadorAnim = 0;
        frameAtaque = 1;
        contadorAnimAtk = 0;

        // NO incrementar contadorNPCs (boss es independiente del pool)
    }

    @Override
    public void desactivar() {
        activo = false;
        // NO decrementar contadorNPCs
    }

    @Override
    public void resetearEstado() {
        ultimaDireccionHorizontal = "derecha";
        frameActual = 1;
        contadorAnim = 0;
        frameAtaque = 1;
        contadorAnimAtk = 0;
    }

    // ===== UPDATE PRINCIPAL (MÁQUINA DE ESTADOS) =====

    @Override
    public void update() {
        if (!estaVivo) {
            // Animación de muerte
            contadorMuerte++;
            if (contadorMuerte >= duracionFrameMuerte) {
                frameMuerte++;
                contadorMuerte = 0;
                if (frameMuerte >= 4) {
                    // Boss muerto completamente → terminar boss fight
                    desactivar();
                    mundo.terminarBossFight();
                }
            }
            return;
        }

        tiempoEnFase++;
        actualizarInvulnerabilidad();
        actualizarContadorDanio(); // Decrementar cooldown de daño por contacto

        switch (estadoBoss) {
            case PERSEGUIR_Y_ATACAR:
                actualizarFasePersecucion();
                break;
            case QUIETO_DISPARANDO:
                actualizarFaseProyectilesEspeciales();
                break;
            case MUERTO:
                break;
        }

        // Colisión con jugador (contacto directo)
        verificarColisionConJugador();

        // Animación idle
        contadorAnim++;
        if (contadorAnim > velocidadAnim) {
            frameActual++;
            if (frameActual > 4) frameActual = 1;
            contadorAnim = 0;
        }
    }

    // ===== FASE 1: PERSEGUIR Y ATACAR =====

    private void actualizarFasePersecucion() {
        // Perseguir al jugador
        perseguirJugador();

        // Rastrear dirección horizontal
        if (direccion.equals("izquierda") || direccion.equals("derecha")) {
            ultimaDireccionHorizontal = direccion;
        }

        // Aplicar movimiento con soft collisions
        aplicarMovimientoBoss();

        // Disparar proyectil normal periódicamente
        contadorProyectilNormal++;
        if (contadorProyectilNormal >= COOLDOWN_PROYECTIL_NORMAL) {
            dispararProyectilNormal();
            contadorProyectilNormal = 0;
        }

        // Transición a fase 2
        if (tiempoEnFase >= duracionFase1) {
            cambiarAFase2();
        }
    }

    // ===== FASE 2: QUIETO DISPARANDO ESPECIALES =====

    private void actualizarFaseProyectilesEspeciales() {
        // Boss quieto — solo actualizar dirección hacia jugador (para orientar boca)
        int dx = mundo.jugador.worldx - worldx;
        ultimaDireccionHorizontal = (dx < 0) ? "izquierda" : "derecha";
        direccion = ultimaDireccionHorizontal;

        // Animación de ataque
        contadorAnimAtk++;
        if (contadorAnimAtk > velocidadAnimAtk) {
            frameAtaque++;
            if (frameAtaque > 7) frameAtaque = 1;
            contadorAnimAtk = 0;
        }

        // Disparar ProyectilEspecial periódicamente
        contadorProyectilEspecial++;
        if (contadorProyectilEspecial >= COOLDOWN_PROYECTIL_ESPECIAL
                && proyectilesEspecialesLanzados < maxProyectilesEspeciales) {
            dispararProyectilEspecialDesdeBoca();
            proyectilesEspecialesLanzados++;
            contadorProyectilEspecial = 0;
        }

        // Volver a fase 1 tras completar ráfaga
        if (proyectilesEspecialesLanzados >= maxProyectilesEspeciales) {
            cambiarAFase1();
        }
    }

    // ===== TRANSICIONES DE FASE =====

    private void cambiarAFase2() {
        estadoBoss = EstadoBoss.QUIETO_DISPARANDO;
        tiempoEnFase = 0;
        proyectilesEspecialesLanzados = 0;
        maxProyectilesEspeciales = 5 + (int) (Math.random() * 6);
        contadorProyectilEspecial = 0;
        frameAtaque = 1;
        contadorAnimAtk = 0;
    }

    private void cambiarAFase1() {
        estadoBoss = EstadoBoss.PERSEGUIR_Y_ATACAR;
        tiempoEnFase = 0;
        duracionFase1 = DURACION_FASE1_MIN +
                (int) (Math.random() * (DURACION_FASE1_MAX - DURACION_FASE1_MIN));
        contadorProyectilNormal = 0;
    }

    // ===== MOVIMIENTO =====

    private void aplicarMovimientoBoss() {
        // Vector hacia jugador
        double trackX = 0, trackY = 0;
        switch (direccion) {
            case "arriba":    trackY = -1; break;
            case "abajo":     trackY =  1; break;
            case "izquierda": trackX = -1; break;
            case "derecha":   trackX =  1; break;
        }

        double len = Math.sqrt(trackX * trackX + trackY * trackY);
        if (len > 0) {
            int nextX = worldx + (int) Math.round((trackX / len) * vel);
            int nextY = worldy + (int) Math.round((trackY / len) * vel);

            // Verificar colisión con tiles
            if (!verificarColisionTileBoss(nextX, worldy)) worldx = nextX;
            if (!verificarColisionTileBoss(worldx, nextY)) worldy = nextY;
        }
    }

    private boolean verificarColisionTileBoss(int nextX, int nextY) {
        if (vuela) return false; // Boss vuela, no choca con tiles
        int left = nextX + AreaSolida.x;
        int right = nextX + AreaSolida.x + AreaSolida.width;
        int top = nextY + AreaSolida.y;
        int bottom = nextY + AreaSolida.y + AreaSolida.height;

        int col1 = left / Configuracion.TAMANO_TILE;
        int col2 = right / Configuracion.TAMANO_TILE;
        int row1 = top / Configuracion.TAMANO_TILE;
        int row2 = bottom / Configuracion.TAMANO_TILE;

        if (col1 < 0 || col2 >= Configuracion.MUNDO_COLUMNAS ||
            row1 < 0 || row2 >= Configuracion.MUNDO_FILAS) return true;

        return false;
    }

    // ===== DISPARO DE PROYECTILES =====

    private void dispararProyectilNormal() {
        // Dirección hacia jugador
        int dx = mundo.jugador.worldx - worldx;
        int dy = mundo.jugador.worldy - worldy;
        String dir;
        if (Math.abs(dx) > Math.abs(dy)) {
            dir = (dx > 0) ? "derecha" : "izquierda";
        } else {
            dir = (dy > 0) ? "abajo" : "arriba";
        }

        // Posición de disparo (centro del sprite)
        int spawnX = worldx + spriteAncho / 2 - 8;
        int spawnY = worldy + spriteAlto / 2 - 8;

        // Buscar slot libre en pool de proyectiles normales
        for (int i = 0; i < mundo.proyectiles.length; i++) {
            if (mundo.proyectiles[i] == null || !mundo.proyectiles[i].activo) {
                Proyectil p = new Proyectil(mundo, spawnX, spawnY, dir, ataque);
                p.esDelJugador = false; // Marca como proyectil del boss
                mundo.proyectiles[i] = p;
                break;
            }
        }
    }

    private void dispararProyectilEspecialDesdeBoca() {
        // Posición de la boca: centro-izquierdo si mira izquierda, centro-derecho si mira derecha
        boolean mirandoIzquierda = ultimaDireccionHorizontal.equals("izquierda");
        int bocaX = worldx + (mirandoIzquierda ? 15 : spriteAncho - 30);
        int bocaY = worldy + spriteAlto / 2;

        // Buscar slot libre en pool de ProyectilEspecial
        ProyectilEspecial pe = mundo.obtenerProyectilEspecialLibre();
        if (pe != null) {
            pe.activar(bocaX, bocaY, mundo.jugador);
        }
    }

    // ===== OVERRIDE IA (requerido por NPC abstract) =====

    @Override
    public void actualizarIA() {
        // La IA del boss se gestiona en update() con la máquina de estados
    }

    // ===== RENDERIZADO =====

    @Override
    public void draw(Graphics2D g2) {
        int screenX = worldx - mundo.jugador.worldx + mundo.jugador.screenX;
        int screenY = worldy - mundo.jugador.worldy + mundo.jugador.screeny;

        BufferedImage sprite = obtenerSpriteBoss();

        if (sprite != null) {
            g2.drawImage(sprite, screenX, screenY, null);
        }

        // Barra de vida del boss (más grande que la de NPCs normales)
        if (estaVivo) {
            dibujarBarraVidaBoss(g2, screenX, screenY);
        }

        // Efecto de daño (tinte rojo)
        if (contadorInvulnerabilidad > 0 && contadorInvulnerabilidad % 10 < 5 && estaVivo && sprite != null) {
            BufferedImage tinted = new Herramientas().tintImage(sprite, new Color(255, 0, 0, 100));
            g2.drawImage(tinted, screenX, screenY, null);
        }
    }

    private BufferedImage obtenerSpriteBoss() {
        boolean izq = ultimaDireccionHorizontal.equals("izquierda");

        if (!estaVivo) {
            // Muerte (4 frames)
            switch (frameMuerte) {
                case 0: return s_muerte1_boss;
                case 1: return s_muerte2_boss;
                case 2: return s_muerte3_boss;
                default: return s_muerte4_boss;
            }
        }

        // Si recibió golpe reciente, mostrar sprite de golpe
        if (contadorInvulnerabilidad > duracionInvulnerabilidad - 20) {
            int fGolpe = ((duracionInvulnerabilidad - contadorInvulnerabilidad) / 5) + 1;
            if (fGolpe < 1) fGolpe = 1;
            if (fGolpe > 4) fGolpe = 4;
            switch (fGolpe) {
                case 1: return izq ? s_golpe1_izq : s_golpe1;
                case 2: return izq ? s_golpe2_izq : s_golpe2;
                case 3: return izq ? s_golpe3_izq : s_golpe3;
                default: return izq ? s_golpe4_izq : s_golpe4;
            }
        }

        // Fase 2: animación de ataque (boca abierta)
        if (estadoBoss == EstadoBoss.QUIETO_DISPARANDO) {
            switch (frameAtaque) {
                case 1: return izq ? s_ataque1_izq : s_ataque1;
                case 2: return izq ? s_ataque2_izq : s_ataque2;
                case 3: return izq ? s_ataque3_izq : s_ataque3;
                case 4: return izq ? s_ataque4_izq : s_ataque4;
                case 5: return izq ? s_ataque5_izq : s_ataque5;
                case 6: return izq ? s_ataque6_izq : s_ataque6;
                default: return izq ? s_ataque7_izq : s_ataque7;
            }
        }

        // Fase 1 / Idle: animación de movimiento
        switch (frameActual) {
            case 1: return izq ? s_idle1_izq : s_idle1;
            case 2: return izq ? s_idle2_izq : s_idle2;
            case 3: return izq ? s_idle3_izq : s_idle3;
            default: return izq ? s_idle4_izq : s_idle4;
        }
    }

    @Override
    protected BufferedImage obtenerSprite() {
        // Delegamos al método propio del boss
        return obtenerSpriteBoss();
    }

    private void dibujarBarraVidaBoss(Graphics2D g2, int screenX, int screenY) {
        int anchoBarraMax = spriteAncho;
        int altoBarra = 8;
        int yBarra = screenY - 15;

        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(screenX, yBarra, anchoBarraMax, altoBarra);

        int anchoVida = (int) ((double) vidaActual / vidaMaxima * anchoBarraMax);
        g2.setColor(new Color(200, 0, 0));
        g2.fillRect(screenX, yBarra, anchoVida, altoBarra);

        g2.setColor(Color.WHITE);
        g2.drawRect(screenX, yBarra, anchoBarraMax, altoBarra);
    }

    public static void resetearCache() {
        spritesLoaded = false;
    }
}
