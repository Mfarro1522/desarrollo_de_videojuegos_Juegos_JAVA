package entidad;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import configuracion.Configuracion;
import mundo.MundoJuego;
import utilidades.Herramientas;

/**
 * Enemigo básico tipo Slime. Persigue al jugador lentamente.
 * CACHE ESTÁTICO de sprites compartido.
 */
public class Slime extends NPC {

    private static BufferedImage s_arriba1, s_arriba2;
    private static BufferedImage s_abajo1, s_abajo2;
    private static BufferedImage s_derecha1, s_derecha2;
    private static BufferedImage s_izquierda1, s_izquierda2;
    private static BufferedImage s_muerte1, s_muerte2, s_muerte3;
    private static boolean spritesLoaded = false;

    public Slime(MundoJuego mundo) {
        super(mundo);
        tipoNPC = TipoNPC.SLIME;
        inicializarEstadisticas();
        cargarSpritesEstaticos();
        asignarSprites();
    }

    private void inicializarEstadisticas() {
        vidaMaxima = 30; // Intermedio
        vidaActual = vidaMaxima;
        ataque = 3;
        defensa = 0;
        vel = 1;
        direccion = "abajo";
        radioDeteccion = 6 * Configuracion.TAMANO_TILE;
        experienciaAOtorgar = 10;
    }

    private synchronized void cargarSpritesEstaticos() {
        if (spritesLoaded)
            return;
        Herramientas tool = new Herramientas();
        int tile = Configuracion.TAMANO_TILE;
        try {
            String ruta = "/Npc/Slime/";

            s_arriba1 = tool.escalarImagen(ImageIO.read(Slime.class.getResourceAsStream(ruta + "Arriba01.png")), tile,
                    tile);
            s_arriba2 = tool.escalarImagen(ImageIO.read(Slime.class.getResourceAsStream(ruta + "Arriba02.png")), tile,
                    tile);
            s_abajo1 = tool.escalarImagen(ImageIO.read(Slime.class.getResourceAsStream(ruta + "Abajo01.png")), tile,
                    tile);
            s_abajo2 = tool.escalarImagen(ImageIO.read(Slime.class.getResourceAsStream(ruta + "Abajo02.png")), tile,
                    tile);

            s_derecha1 = tool.escalarImagen(ImageIO.read(Slime.class.getResourceAsStream(ruta + "Derecha01.png")), tile,
                    tile);
            s_derecha2 = tool.escalarImagen(ImageIO.read(Slime.class.getResourceAsStream(ruta + "Derecha02.png")), tile,
                    tile);
            s_izquierda1 = tool.voltearImagenHorizontal(s_derecha1);
            s_izquierda2 = tool.voltearImagenHorizontal(s_derecha2);

            s_muerte1 = tool.escalarImagen(ImageIO.read(Slime.class.getResourceAsStream(ruta + "Muerte01.png")), tile,
                    tile);
            s_muerte2 = tool.escalarImagen(ImageIO.read(Slime.class.getResourceAsStream(ruta + "Muerte02.png")), tile,
                    tile);
            s_muerte3 = tool.escalarImagen(ImageIO.read(Slime.class.getResourceAsStream(ruta + "Muerte03.png")), tile,
                    tile);

            spritesLoaded = true;
        } catch (Exception e) {
            System.err.println("[Slime] Error al cargar sprites: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void asignarSprites() {
        arriba1 = s_arriba1;
        arriba2 = s_arriba2;
        abajo1 = s_abajo1;
        abajo2 = s_abajo2;
        derecha1 = s_derecha1;
        derecha2 = s_derecha2;
        izquierda1 = s_izquierda1;
        izquierda2 = s_izquierda2;
        muerte1 = s_muerte1;
        muerte2 = s_muerte2;
        muerte3 = s_muerte3;
    }

    @Override
    public void resetearEstado() {
    }

    @Override
    public void actualizarIA() {
        perseguirJugador();
    }

    @Override
    protected BufferedImage obtenerSprite() {
        switch (direccion) {
            case "arriba":
                return (numeroSpites == 1) ? arriba1 : arriba2;
            case "abajo":
                return (numeroSpites == 1) ? abajo1 : abajo2;
            case "izquierda":
                return (numeroSpites == 1) ? izquierda1 : izquierda2;
            case "derecha":
                return (numeroSpites == 1) ? derecha1 : derecha2;
            default:
                return abajo1;
        }
    }

    public static void resetearCache() {
        spritesLoaded = false;
    }
}
