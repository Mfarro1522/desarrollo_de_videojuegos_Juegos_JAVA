package entidad;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import main.PanelJuego;
import main.UtilityTool;

/**
 * Enemigo tipo Murciélago (Bat).
 * Se mueve en las 4 direcciones pero solo tiene animaciones de izquierda y
 * derecha (4 frames por lado). Al moverse arriba/abajo usa el sprite de la
 * última dirección horizontal. Daño por colisión.
 */
public class Bat extends NPC {

    // ===== CACHE ESTÁTICO DE SPRITES (cargados una sola vez, compartidos) =====
    private static BufferedImage s_izq1, s_izq2, s_izq3, s_izq4;
    private static BufferedImage s_der1, s_der2, s_der3, s_der4;
    private static BufferedImage s_muerte1, s_muerte2, s_muerte3;
    private static boolean spritesLoaded = false;

    // Referencias de instancia (apuntan al caché estático)
    private BufferedImage izq1, izq2, izq3, izq4;
    private BufferedImage der1, der2, der3, der4;

    // Dirección horizontal para reutilizar sprites al ir arriba/abajo
    private String ultimaDireccionHorizontal = "derecha";

    // Animación de 4 frames
    private int frameActual = 1; // 1-4
    private int contadorAnim = 0;
    private int velocidadAnim = 8; // Frames entre cambios de sprite

    public Bat(PanelJuego pj) {
        super(pj);
        tipoNPC = TipoNPC.BAT;
        inicializarEstadisticas();
        cargarSpritesEstaticos(pj);
        asignarSprites();
    }

    private void inicializarEstadisticas() {
        vidaMaxima = 12;
        vidaActual = vidaMaxima;
        ataque = 4;
        defensa = 0;
        vel = 2;
        direccion = "izquierda";
        radioDeteccion = 8 * pj.tamanioTile;
        experienciaAOtorgar = 15;
    }

    /**
     * Carga sprites UNA SOLA VEZ para todos los Bats (Object Pooling).
     * Elimina carga duplicada de imágenes al pre-instanciar el pool.
     */
    private static synchronized void cargarSpritesEstaticos(PanelJuego pj) {
        if (spritesLoaded) return;
        UtilityTool tool = new UtilityTool();
        try {
            String ruta = "/Npc/Bats/";

            BufferedImage tempDer1 = ImageIO.read(Bat.class.getResourceAsStream(ruta + "der01.png"));
            BufferedImage tempDer2 = ImageIO.read(Bat.class.getResourceAsStream(ruta + "der02.png"));
            BufferedImage tempDer3 = ImageIO.read(Bat.class.getResourceAsStream(ruta + "der03.png"));
            BufferedImage tempDer4 = ImageIO.read(Bat.class.getResourceAsStream(ruta + "der04.png"));

            s_der1 = tool.escalarImagen(tempDer1, pj.tamanioTile, pj.tamanioTile);
            s_der2 = tool.escalarImagen(tempDer2, pj.tamanioTile, pj.tamanioTile);
            s_der3 = tool.escalarImagen(tempDer3, pj.tamanioTile, pj.tamanioTile);
            s_der4 = tool.escalarImagen(tempDer4, pj.tamanioTile, pj.tamanioTile);

            s_izq1 = tool.voltearImagenHorizontal(s_der1);
            s_izq2 = tool.voltearImagenHorizontal(s_der2);
            s_izq3 = tool.voltearImagenHorizontal(s_der3);
            s_izq4 = tool.voltearImagenHorizontal(s_der4);

            BufferedImage tempMuerte1 = ImageIO.read(Bat.class.getResourceAsStream(ruta + "muerte01.png"));
            BufferedImage tempMuerte2 = ImageIO.read(Bat.class.getResourceAsStream(ruta + "muerte02.png"));

            s_muerte1 = tool.escalarImagen(tempMuerte1, pj.tamanioTile, pj.tamanioTile);
            s_muerte2 = tool.escalarImagen(tempMuerte2, pj.tamanioTile, pj.tamanioTile);
            s_muerte3 = s_muerte2; // Reutilizar frame 2

            spritesLoaded = true;
        } catch (Exception e) {
            System.err.println("[Bat] Error al cargar sprites: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Asigna las referencias estáticas a los campos de instancia. */
    private void asignarSprites() {
        izq1 = s_izq1; izq2 = s_izq2; izq3 = s_izq3; izq4 = s_izq4;
        der1 = s_der1; der2 = s_der2; der3 = s_der3; der4 = s_der4;
        izquierda1 = s_izq1; izquierda2 = s_izq2;
        derecha1 = s_der1; derecha2 = s_der2;
        arriba1 = s_der1; arriba2 = s_der2;
        abajo1 = s_der1; abajo2 = s_der2;
        muerte1 = s_muerte1; muerte2 = s_muerte2; muerte3 = s_muerte3;
    }

    @Override
    public void resetearEstado() {
        ultimaDireccionHorizontal = "derecha";
        frameActual = 1;
        contadorAnim = 0;
    }

    @Override
    public void actualizarIA() {
        perseguirJugador();

        // Rastrear dirección horizontal para sprites arriba/abajo
        if (direccion.equals("izquierda") || direccion.equals("derecha")) {
            ultimaDireccionHorizontal = direccion;
        }
    }

    /**
     * Override del movimiento: se mueve en las 4 direcciones normalmente.
     * El método heredado de NPC ya maneja las 4 direcciones.
     */
    @Override
    protected void mover() {
        // Usar movimiento estándar de NPC (4 direcciones)
        super.mover();

        // Actualizar animación de 4 frames
        contadorAnim++;
        if (contadorAnim > velocidadAnim) {
            frameActual++;
            if (frameActual > 4) {
                frameActual = 1;
            }
            contadorAnim = 0;
        }
    }

    @Override
    protected BufferedImage obtenerSprite() {
        // Determinar qué set usar: izquierda o derecha
        boolean usarIzquierda;

        switch (direccion) {
            case "izquierda":
                usarIzquierda = true;
                break;
            case "derecha":
                usarIzquierda = false;
                break;
            case "arriba":
            case "abajo":
            default:
                // Cuando va arriba/abajo, usar la última dirección horizontal
                usarIzquierda = ultimaDireccionHorizontal.equals("izquierda");
                break;
        }

        // Seleccionar frame según animación de 4 frames
        if (usarIzquierda) {
            switch (frameActual) {
                case 1:
                    return izq1;
                case 2:
                    return izq2;
                case 3:
                    return izq3;
                case 4:
                    return izq4;
                default:
                    return izq1;
            }
        } else {
            switch (frameActual) {
                case 1:
                    return der1;
                case 2:
                    return der2;
                case 3:
                    return der3;
                case 4:
                    return der4;
                default:
                    return der1;
            }
        }
    }

    /**
     * Resetea el caché estático (útil para cambiar resolución).
     */
    public static void resetearCache() {
        spritesLoaded = false;
    }
}
