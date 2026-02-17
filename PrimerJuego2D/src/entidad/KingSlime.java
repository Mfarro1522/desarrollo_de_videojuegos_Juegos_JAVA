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
 * Boss "KingSlime" — Slime gigante coronado (64x64 sprite).
 * Aparecen 3 simultáneamente al llegar a nivel 7.
 *
 * FASES:
 * 1. PERSEGUIR: Persigue al jugador a velocidad normal, daño por contacto.
 *    Cada 3 segundos da un "salto" (dash corto hacia el jugador).
 * 2. FURIA (< 40% HP): Se tiñe rojo, velocidad x2, daño x1.5,
 *    salta cada 1.5 segundos (más agresivo y errático).
 *
 * Solo daña por contacto, no tiene proyectiles.
 * Instancia independiente del pool de NPCs.
 */
public class KingSlime extends NPC {

    // ===== ESTADOS =====
    public enum EstadoKing {
        PERSEGUIR,     // Fase normal
        FURIA,         // Fase enrabiada (< 40% HP)
        MUERTO
    }

    public EstadoKing estadoKing = EstadoKing.PERSEGUIR;

    // ===== SALTO / DASH =====
    private int contadorSalto = 0;
    private int cooldownSaltoNormal = 60 * 3;   // Cada 3s
    private int cooldownSaltoFuria = 60 * 1;     // Cada 1.5s en furia (90 frames)
    private boolean saltando = false;
    private int duracionSalto = 20;              // Frames que dura el dash (más largo)
    private int contadorDuracionSalto = 0;
    private double saltoX, saltoY;               // Dirección del dash
    private static final int VELOCIDAD_SALTO = 10;// Velocidad durante el dash (más rápido)

    // ===== SPRITES (CACHÉ ESTÁTICO) =====
    private static BufferedImage s_idle1, s_idle2, s_idle3, s_idle4;
    private static BufferedImage s_move1, s_move2, s_move3;
    private static BufferedImage s_atk1, s_atk2, s_atk3, s_atk4, s_atk5;
    private static boolean spritesLoaded = false;

    // ===== ANIMACIÓN =====
    private int frameActual = 1;
    private int contadorAnim = 0;
    private int velocidadAnim = 8;
    private int frameAtaque = 1;
    private int contadorAnimAtk = 0;
    private int velocidadAnimAtk = 5;

    // ===== FURIA =====
    private boolean enFuria = false;
    private int velBase;
    private int ataqueBase;
    private int contadorParpadeoFuria = 0;

    // ===== ID para manejo múltiple =====
    public int indiceBoss = 0; // 0, 1 o 2 (para saber cuál de los 3 es)

    // ===== CONSTRUCTOR =====
    public KingSlime(MundoJuego mundo, int indice) {
        super(mundo);
        this.indiceBoss = indice;
        tipoNPC = TipoNPC.SLIME; // Tipo compatible
        inicializarEstadisticas();
        cargarSpritesEstaticos();

        // Hitbox 20% más pequeña y centrada (sprite 64x64 escalado a TAMANO_TILE)
        // 64 * 0.8 = ~51, offset = (64-51)/2 ≈ 6
        int tile = Configuracion.TAMANO_TILE;
        int hitboxSize = (int) (tile * 0.8);
        int offset = (tile - hitboxSize) / 2;
        AreaSolida = new Rectangle(offset, offset, hitboxSize, hitboxSize);
        AreaSolidaDefaultX = AreaSolida.x;
        AreaSolidaDefaultY = AreaSolida.y;

        vuela = false; // Los slimes no vuelan
    }

    private void inicializarEstadisticas() {
        vidaMaxima = 300;
        vidaActual = vidaMaxima;
        ataque = 10;
        defensa = 3;
        vel = 5;
        velBase = vel;
        ataqueBase = ataque;
        direccion = "abajo";
        radioDeteccion = 25 * Configuracion.TAMANO_TILE;
        experienciaAOtorgar = 300;
    }

    // ===== SPRITES =====
    private synchronized void cargarSpritesEstaticos() {
        if (spritesLoaded) return;
        Herramientas tool = new Herramientas();
        int tile = Configuracion.TAMANO_TILE;
        try {
            String ruta = "/Npc/Bosses/KingSlime/";

            // Idle (4 frames)
            s_idle1 = tool.escalarImagen(
                    ImageIO.read(KingSlime.class.getResourceAsStream(ruta + "KingSlime_Idle1.png")), tile, tile);
            s_idle2 = tool.escalarImagen(
                    ImageIO.read(KingSlime.class.getResourceAsStream(ruta + "KingSlime_Idle2.png")), tile, tile);
            s_idle3 = tool.escalarImagen(
                    ImageIO.read(KingSlime.class.getResourceAsStream(ruta + "KingSlime_Idle3.png")), tile, tile);
            s_idle4 = tool.escalarImagen(
                    ImageIO.read(KingSlime.class.getResourceAsStream(ruta + "KingSlime_Idle4.png")), tile, tile);

            // Moving (3 frames)
            s_move1 = tool.escalarImagen(
                    ImageIO.read(KingSlime.class.getResourceAsStream(ruta + "KingSlime_Moving1.png")), tile, tile);
            s_move2 = tool.escalarImagen(
                    ImageIO.read(KingSlime.class.getResourceAsStream(ruta + "KingSlime_Moving2.png")), tile, tile);
            s_move3 = tool.escalarImagen(
                    ImageIO.read(KingSlime.class.getResourceAsStream(ruta + "KingSlime_Moving3.png")), tile, tile);

            // Attack (5 frames)
            s_atk1 = tool.escalarImagen(
                    ImageIO.read(KingSlime.class.getResourceAsStream(ruta + "KingSlimeAttack1.png")), tile, tile);
            s_atk2 = tool.escalarImagen(
                    ImageIO.read(KingSlime.class.getResourceAsStream(ruta + "KingSlimeAttack2.png")), tile, tile);
            s_atk3 = tool.escalarImagen(
                    ImageIO.read(KingSlime.class.getResourceAsStream(ruta + "KingSlimeAttack3.png")), tile, tile);
            s_atk4 = tool.escalarImagen(
                    ImageIO.read(KingSlime.class.getResourceAsStream(ruta + "KingSlimeAttack4.png")), tile, tile);
            s_atk5 = tool.escalarImagen(
                    ImageIO.read(KingSlime.class.getResourceAsStream(ruta + "KingSlimeAttack5.png")), tile, tile);

            spritesLoaded = true;
            System.out.println("[KingSlime] Sprites cargados correctamente.");

        } catch (Exception e) {
            System.err.println("[KingSlime] Error al cargar sprites: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ===== ACTIVACIÓN =====
    @Override
    public void activar(int x, int y) {
        worldx = x;
        worldy = y;
        vidaActual = vidaMaxima;
        estaVivo = true;
        activo = true;
        estado = EstadoEntidad.IDLE;
        estadoKing = EstadoKing.PERSEGUIR;
        direccion = "abajo";
        frameMuerte = 0;
        contadorMuerte = 0;
        contadorInvulnerabilidad = 0;
        contadorMovimiento = 0;
        hayColision = false;
        resetearContadorDanio();

        // Reset salto
        contadorSalto = 0;
        saltando = false;
        contadorDuracionSalto = 0;

        // Reset furia
        enFuria = false;
        vel = velBase;
        ataque = ataqueBase;
        contadorParpadeoFuria = 0;

        // Reset animación
        frameActual = 1;
        contadorAnim = 0;
        frameAtaque = 1;
        contadorAnimAtk = 0;
    }

    @Override
    public void desactivar() {
        activo = false;
    }

    @Override
    public void resetearEstado() {
        frameActual = 1;
        contadorAnim = 0;
        frameAtaque = 1;
        contadorAnimAtk = 0;
    }

    // ===== UPDATE =====
    @Override
    public void update() {
        if (!estaVivo) {
            contadorMuerte++;
            if (contadorMuerte >= duracionFrameMuerte) {
                frameMuerte++;
                contadorMuerte = 0;
                if (frameMuerte >= 3) {
                    desactivar();
                    mundo.notificarKingSlimeMuerto(indiceBoss);
                }
            }
            return;
        }

        actualizarInvulnerabilidad();
        actualizarContadorDanio();

        // Verificar entrada a FURIA (< 40% HP)
        if (!enFuria && vidaActual <= vidaMaxima * 0.4) {
            entrarEnFuria();
        }

        switch (estadoKing) {
            case PERSEGUIR:
            case FURIA:
                actualizarPersecucion();
                break;
            case MUERTO:
                break;
        }

        // Colisión con jugador
        verificarColisionConJugador();

        // Animación idle
        contadorAnim++;
        if (contadorAnim > velocidadAnim) {
            frameActual++;
            int maxFrames = saltando ? 5 : (estado == EstadoEntidad.MOVIENDO ? 3 : 4);
            if (frameActual > maxFrames) frameActual = 1;
            contadorAnim = 0;
        }
    }

    @Override
    public void recibirDanio(int cantidad) {
        super.recibirDanio(cantidad);
        if (estaVivo) {
            mundo.playSE(audio.GestorAudio.SE_BOSS_HIT);
        }
    }

    // ===== FURIA =====
    private void entrarEnFuria() {
        enFuria = true;
        estadoKing = EstadoKing.FURIA;
        vel = velBase * 2;
        ataque = (int) (ataqueBase * 1.5);
        contadorParpadeoFuria = 0;
        mundo.playSE(audio.GestorAudio.SE_FURY_ACTIVATE);
        System.out.println("[KingSlime #" + indiceBoss + "] ¡ENTRÓ EN FURIA!");
    }

    // ===== PERSECUCIÓN + SALTO =====
    private void actualizarPersecucion() {
        // Si está en un salto/dash
        if (saltando) {
            contadorDuracionSalto++;
            // Movimiento rápido en la dirección del salto
            int nextX = worldx + (int) Math.round(saltoX * VELOCIDAD_SALTO);
            int nextY = worldy + (int) Math.round(saltoY * VELOCIDAD_SALTO);

            // Verificar colisión con tiles antes de mover
            if (!verificarColisionTileKS(nextX, worldy)) worldx = nextX;
            if (!verificarColisionTileKS(worldx, nextY)) worldy = nextY;

            // Animación de ataque durante salto
            contadorAnimAtk++;
            if (contadorAnimAtk > velocidadAnimAtk) {
                frameAtaque++;
                if (frameAtaque > 5) frameAtaque = 1;
                contadorAnimAtk = 0;
            }

            if (contadorDuracionSalto >= duracionSalto) {
                saltando = false;
                contadorDuracionSalto = 0;
            }
            return;
        }

        // Perseguir jugador normalmente
        perseguirJugador();
        aplicarMovimientoKS();

        // Cooldown de salto
        contadorSalto++;
        int cooldown = enFuria ? cooldownSaltoFuria : cooldownSaltoNormal;
        if (contadorSalto >= cooldown) {
            iniciarSalto();
            contadorSalto = 0;
        }
    }

    private void iniciarSalto() {
        // Calcular dirección normalizada hacia el jugador
        double dx = mundo.jugador.worldx - worldx;
        double dy = mundo.jugador.worldy - worldy;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist > 0) {
            saltoX = dx / dist;
            saltoY = dy / dist;
        } else {
            saltoX = 0;
            saltoY = -1;
        }

        // En furia, añadir componente aleatorio para movimiento errático
        if (enFuria) {
            saltoX += (Math.random() - 0.5) * 0.6;
            saltoY += (Math.random() - 0.5) * 0.6;
            double len = Math.sqrt(saltoX * saltoX + saltoY * saltoY);
            if (len > 0) {
                saltoX /= len;
                saltoY /= len;
            }
        }

        saltando = true;
        contadorDuracionSalto = 0;
        frameAtaque = 1;
        contadorAnimAtk = 0;
    }

    // ===== MOVIMIENTO =====
    private void aplicarMovimientoKS() {
        // Vector diagonal directo hacia el jugador (evita quedar trabado en esquinas)
        double trackX = 0, trackY = 0;
        double dx = mundo.jugador.worldx - worldx;
        double dy = mundo.jugador.worldy - worldy;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) {
            trackX = dx / dist;
            trackY = dy / dist;
        }

        if (trackX != 0 || trackY != 0) {
            int nextX = worldx + (int) Math.round(trackX * vel);
            int nextY = worldy + (int) Math.round(trackY * vel);

            if (!verificarColisionTileKS(nextX, worldy)) worldx = nextX;
            if (!verificarColisionTileKS(worldx, nextY)) worldy = nextY;
        }
    }

    private boolean verificarColisionTileKS(int nextX, int nextY) {
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

        // Verificar tiles sólidos
        try {
            int t1 = mundo.tileManager.mapaPorNumeroTile[col1][row1];
            int t2 = mundo.tileManager.mapaPorNumeroTile[col2][row1];
            int t3 = mundo.tileManager.mapaPorNumeroTile[col1][row2];
            int t4 = mundo.tileManager.mapaPorNumeroTile[col2][row2];

            if ((mundo.tileManager.tiles[t1] != null && mundo.tileManager.tiles[t1].colision) ||
                (mundo.tileManager.tiles[t2] != null && mundo.tileManager.tiles[t2].colision) ||
                (mundo.tileManager.tiles[t3] != null && mundo.tileManager.tiles[t3].colision) ||
                (mundo.tileManager.tiles[t4] != null && mundo.tileManager.tiles[t4].colision)) {
                return true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return true;
        }
        return false;
    }

    // ===== IA (requerido por abstract) =====
    @Override
    public void actualizarIA() {
        // Gestionado en update() directamente
    }

    // ===== RENDERIZADO =====
    @Override
    public void draw(Graphics2D g2) {
        int screenX = worldx - mundo.jugador.worldx + mundo.jugador.screenX;
        int screenY = worldy - mundo.jugador.worldy + mundo.jugador.screeny;

        BufferedImage sprite = obtenerSpriteKing();

        if (sprite != null) {
            // En furia: tinte rojo parpadeante
            if (enFuria && estaVivo) {
                contadorParpadeoFuria++;
                if (contadorParpadeoFuria % 20 < 10) {
                    Herramientas tool = new Herramientas();
                    sprite = tool.tintImage(sprite, new Color(255, 30, 30, 80));
                }
            }
            g2.drawImage(sprite, screenX, screenY, null);
        }

        // Barra de vida individual sobre cada KingSlime
        if (estaVivo) {
            dibujarBarraVidaKing(g2, screenX, screenY);
        }

        // Efecto de daño (tinte rojo)
        if (contadorInvulnerabilidad > 0 && contadorInvulnerabilidad % 10 < 5 && estaVivo && sprite != null) {
            BufferedImage tinted = new Herramientas().tintImage(sprite, new Color(255, 0, 0, 120));
            g2.drawImage(tinted, screenX, screenY, null);
        }
    }

    private BufferedImage obtenerSpriteKing() {
        if (!estaVivo) {
            // Reusar últimos frames de ataque como muerte
            switch (frameMuerte) {
                case 0: return s_atk3;
                case 1: return s_atk4;
                default: return s_atk5;
            }
        }

        // Si está saltando → sprites de ataque
        if (saltando) {
            switch (frameAtaque) {
                case 1: return s_atk1;
                case 2: return s_atk2;
                case 3: return s_atk3;
                case 4: return s_atk4;
                default: return s_atk5;
            }
        }

        // Movimiento
        if (estado == EstadoEntidad.MOVIENDO) {
            switch (frameActual) {
                case 1: return s_move1;
                case 2: return s_move2;
                default: return s_move3;
            }
        }

        // Idle
        switch (frameActual) {
            case 1: return s_idle1;
            case 2: return s_idle2;
            case 3: return s_idle3;
            default: return s_idle4;
        }
    }

    @Override
    protected BufferedImage obtenerSprite() {
        return obtenerSpriteKing();
    }

    private void dibujarBarraVidaKing(Graphics2D g2, int screenX, int screenY) {
        int anchoBarraMax = Configuracion.TAMANO_TILE;
        int altoBarra = 6;
        int yBarra = screenY - 12;

        // Fondo
        g2.setColor(enFuria ? new Color(100, 0, 0) : Color.DARK_GRAY);
        g2.fillRect(screenX, yBarra, anchoBarraMax, altoBarra);

        // Vida
        int anchoVida = (int) ((double) vidaActual / vidaMaxima * anchoBarraMax);
        g2.setColor(enFuria ? new Color(255, 50, 50) : new Color(50, 200, 50));
        g2.fillRect(screenX, yBarra, anchoVida, altoBarra);

        // Borde
        g2.setColor(Color.WHITE);
        g2.drawRect(screenX, yBarra, anchoBarraMax, altoBarra);
    }

    public static void resetearCache() {
        spritesLoaded = false;
    }
}
