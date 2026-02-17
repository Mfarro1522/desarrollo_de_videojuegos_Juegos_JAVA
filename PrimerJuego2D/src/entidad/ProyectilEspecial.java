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
 * Proyectil especial del DemonBat: calavera perseguidora (32x32).
 *
 * Comportamiento:
 * - Persigue al jugador con velocidad constante
 * - Tiene duración limitada (3 segundos / 180 frames)
 * - Al expirar: efecto de desvanecimiento y desactivación
 *
 * Usa Object Pooling (array dedicado en MundoJuego).
 */
public class ProyectilEspecial {

    public int worldX, worldY;
    public boolean activo = false;

    private MundoJuego mundo;
    private int dano = 5;
    private int tamano;

    // Persecución
    private float velocidadPersecucion = 4.5f;
    private int tiempoVida;
    private static final int TIEMPO_VIDA_MAX = 300; // 5 segundos

    // Objetivo
    private Jugador objetivo;

    // Sprites (caché estático compartido entre instancias)
    private static BufferedImage s_frame1, s_frame2, s_frame3, s_frame4;
    private static boolean spritesLoaded = false;

    // Animación
    private int frameActual = 0;
    private int contadorAnim = 0;
    private int velocidadAnim = 8;

    // Efecto de desvanecimiento
    private float opacidad = 1.0f;
    private boolean desvaneciendose = false;
    private static final int DURACION_DESVANECIMIENTO = 20; // frames
    private int contadorDesvanecimiento = 0;

    // Rectángulo pre-allocado para colisión
    private final Rectangle areaProyectil = new Rectangle();
    private final Rectangle areaJugador = new Rectangle();

    public ProyectilEspecial(MundoJuego mundo) {
        this.mundo = mundo;
        this.tamano = 32 * Configuracion.ESCALA;
        cargarSpritesEstaticos();
    }

    private synchronized void cargarSpritesEstaticos() {
        if (spritesLoaded) return;
        Herramientas tool = new Herramientas();
        try {
            String ruta = "/Npc/Bosses/DemonBat/ProyectilesQuieto/";
            int size = 32 * Configuracion.ESCALA;

            s_frame1 = tool.escalarImagen(
                    ImageIO.read(ProyectilEspecial.class.getResourceAsStream(ruta + "frame0000.png")),
                    size, size);
            s_frame2 = tool.escalarImagen(
                    ImageIO.read(ProyectilEspecial.class.getResourceAsStream(ruta + "frame0003.png")),
                    size, size);
            s_frame3 = tool.escalarImagen(
                    ImageIO.read(ProyectilEspecial.class.getResourceAsStream(ruta + "frame0005.png")),
                    size, size);
            s_frame4 = tool.escalarImagen(
                    ImageIO.read(ProyectilEspecial.class.getResourceAsStream(ruta + "frame0008.png")),
                    size, size);

            spritesLoaded = true;
            System.out.println("[ProyectilEspecial] Sprites cargados correctamente.");
        } catch (Exception e) {
            System.err.println("[ProyectilEspecial] Error al cargar sprites: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ===== OBJECT POOLING =====

    public void activar(int x, int y, Jugador objetivo) {
        this.worldX = x;
        this.worldY = y;
        this.objetivo = objetivo;
        this.activo = true;
        this.tiempoVida = TIEMPO_VIDA_MAX;
        this.opacidad = 1.0f;
        this.desvaneciendose = false;
        this.contadorDesvanecimiento = 0;
        this.frameActual = 0;
        this.contadorAnim = 0;
    }

    public void desactivar() {
        activo = false;
    }

    // ===== UPDATE =====

    public void update() {
        if (!activo) return;

        // Desvanecimiento
        if (desvaneciendose) {
            contadorDesvanecimiento++;
            opacidad = 1.0f - ((float) contadorDesvanecimiento / DURACION_DESVANECIMIENTO);
            if (contadorDesvanecimiento >= DURACION_DESVANECIMIENTO) {
                desactivar();
            }
            return;
        }

        // Temporizador de vida
        tiempoVida--;
        if (tiempoVida <= 0) {
            desvaneciendose = true;
            return;
        }

        // Persecución: vector hacia jugador normalizado
        if (objetivo != null) {
            double dx = objetivo.worldx - worldX;
            double dy = objetivo.worldy - worldY;
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist > 0) {
                worldX += (int) Math.round((dx / dist) * velocidadPersecucion);
                worldY += (int) Math.round((dy / dist) * velocidadPersecucion);
            }
        }

        // Colisión con jugador
        verificarColisionConJugador();

        // Animación
        contadorAnim++;
        if (contadorAnim > velocidadAnim) {
            frameActual = (frameActual + 1) % 4;
            contadorAnim = 0;
        }
    }

    private void verificarColisionConJugador() {
        if (objetivo == null) return;

        areaProyectil.setBounds(worldX, worldY, tamano, tamano);
        areaJugador.setBounds(
                objetivo.worldx + objetivo.AreaSolida.x,
                objetivo.worldy + objetivo.AreaSolida.y,
                objetivo.AreaSolida.width,
                objetivo.AreaSolida.height);

        if (areaProyectil.intersects(areaJugador)) {
            objetivo.recibirDanio(dano);
            desactivar();
        }
    }

    // ===== RENDERIZADO =====

    public void draw(Graphics2D g2) {
        if (!activo) return;

        int screenX = worldX - mundo.jugador.worldx + mundo.jugador.screenX;
        int screenY = worldY - mundo.jugador.worldy + mundo.jugador.screeny;

        BufferedImage sprite;
        switch (frameActual) {
            case 0: sprite = s_frame1; break;
            case 1: sprite = s_frame2; break;
            case 2: sprite = s_frame3; break;
            default: sprite = s_frame4; break;
        }

        if (sprite != null) {
            if (desvaneciendose && opacidad < 1.0f) {
                // Efecto de transparencia durante desvanecimiento
                java.awt.Composite originalComposite = g2.getComposite();
                g2.setComposite(java.awt.AlphaComposite.getInstance(
                        java.awt.AlphaComposite.SRC_OVER, Math.max(0f, opacidad)));
                g2.drawImage(sprite, screenX, screenY, null);
                g2.setComposite(originalComposite);
            } else {
                g2.drawImage(sprite, screenX, screenY, null);
            }
        }
    }

    public static void resetearCache() {
        spritesLoaded = false;
    }
}
