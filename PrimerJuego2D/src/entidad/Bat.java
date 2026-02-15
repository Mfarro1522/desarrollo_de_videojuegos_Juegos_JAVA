package entidad;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import configuracion.Configuracion;
import mundo.MundoJuego;
import utilidades.Herramientas;

/**
 * Enemigo tipo Murciélago (Bat). 4 frames de animación por dirección.
 * CACHE ESTÁTICO de sprites compartido entre todas las instancias.
 */
public class Bat extends NPC {

    private static BufferedImage s_izq1, s_izq2, s_izq3, s_izq4;
    private static BufferedImage s_der1, s_der2, s_der3, s_der4;
    private static BufferedImage s_muerte1, s_muerte2, s_muerte3;
    private static boolean spritesLoaded = false;

    private BufferedImage izq1, izq2, izq3, izq4;
    private BufferedImage der1, der2, der3, der4;

    private String ultimaDireccionHorizontal = "derecha";
    private int frameActual = 1;
    private int contadorAnim = 0;
    private int velocidadAnim = 8;

    public Bat(MundoJuego mundo) {
        super(mundo);
        tipoNPC = TipoNPC.BAT;
        inicializarEstadisticas();
        cargarSpritesEstaticos();
        asignarSprites();
    }

    private void inicializarEstadisticas() {
        vidaMaxima = 12;
        vidaActual = vidaMaxima;
        ataque = 4;
        defensa = 0;
        vel = 2;
        direccion = "izquierda";
        radioDeteccion = 8 * Configuracion.TAMANO_TILE;
        experienciaAOtorgar = 15;
    }

    private synchronized void cargarSpritesEstaticos() {
        if (spritesLoaded) return;
        Herramientas tool = new Herramientas();
        int tile = Configuracion.TAMANO_TILE;
        try {
            String ruta = "/Npc/Bats/";

            BufferedImage tempDer1 = ImageIO.read(Bat.class.getResourceAsStream(ruta + "der01.png"));
            BufferedImage tempDer2 = ImageIO.read(Bat.class.getResourceAsStream(ruta + "der02.png"));
            BufferedImage tempDer3 = ImageIO.read(Bat.class.getResourceAsStream(ruta + "der03.png"));
            BufferedImage tempDer4 = ImageIO.read(Bat.class.getResourceAsStream(ruta + "der04.png"));

            s_der1 = tool.escalarImagen(tempDer1, tile, tile);
            s_der2 = tool.escalarImagen(tempDer2, tile, tile);
            s_der3 = tool.escalarImagen(tempDer3, tile, tile);
            s_der4 = tool.escalarImagen(tempDer4, tile, tile);

            s_izq1 = tool.voltearImagenHorizontal(s_der1);
            s_izq2 = tool.voltearImagenHorizontal(s_der2);
            s_izq3 = tool.voltearImagenHorizontal(s_der3);
            s_izq4 = tool.voltearImagenHorizontal(s_der4);

            BufferedImage tempMuerte1 = ImageIO.read(Bat.class.getResourceAsStream(ruta + "muerte01.png"));
            BufferedImage tempMuerte2 = ImageIO.read(Bat.class.getResourceAsStream(ruta + "muerte02.png"));

            s_muerte1 = tool.escalarImagen(tempMuerte1, tile, tile);
            s_muerte2 = tool.escalarImagen(tempMuerte2, tile, tile);
            s_muerte3 = s_muerte2;

            spritesLoaded = true;
        } catch (Exception e) {
            System.err.println("[Bat] Error al cargar sprites: " + e.getMessage());
            e.printStackTrace();
        }
    }

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
        if (direccion.equals("izquierda") || direccion.equals("derecha")) {
            ultimaDireccionHorizontal = direccion;
        }
    }

    @Override
    protected void mover() {
        super.mover();
        contadorAnim++;
        if (contadorAnim > velocidadAnim) {
            frameActual++;
            if (frameActual > 4) frameActual = 1;
            contadorAnim = 0;
        }
    }

    @Override
    protected BufferedImage obtenerSprite() {
        boolean usarIzquierda;
        switch (direccion) {
            case "izquierda": usarIzquierda = true; break;
            case "derecha":   usarIzquierda = false; break;
            default:          usarIzquierda = ultimaDireccionHorizontal.equals("izquierda"); break;
        }

        if (usarIzquierda) {
            switch (frameActual) {
                case 1: return izq1;
                case 2: return izq2;
                case 3: return izq3;
                case 4: return izq4;
                default: return izq1;
            }
        } else {
            switch (frameActual) {
                case 1: return der1;
                case 2: return der2;
                case 3: return der3;
                case 4: return der4;
                default: return der1;
            }
        }
    }

    public static void resetearCache() { spritesLoaded = false; }
}
