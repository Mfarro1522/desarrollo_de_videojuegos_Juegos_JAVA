package entidad;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import main.PanelJuego;
import main.UtilityTool;

/**
 * Enemigo básico tipo Slime.
 * Comportamiento: Persigue al jugador lentamente.
 */
public class Slime extends NPC {

    // ===== CACHE ESTÁTICO DE SPRITES =====
    private static BufferedImage s_arriba1, s_arriba2;
    private static BufferedImage s_abajo1, s_abajo2;
    private static BufferedImage s_derecha1, s_derecha2;
    private static BufferedImage s_izquierda1, s_izquierda2;
    private static BufferedImage s_muerte1, s_muerte2, s_muerte3;
    private static boolean spritesLoaded = false;

    public Slime(PanelJuego pj) {
        super(pj);
        tipoNPC = TipoNPC.SLIME;
        inicializarEstadisticas();
        cargarSpritesEstaticos(pj);
        asignarSprites();
    }

    private void inicializarEstadisticas() {
        vidaMaxima = 20;
        vidaActual = vidaMaxima;
        ataque = 3;
        defensa = 0;
        vel = 1;
        direccion = "abajo";
        radioDeteccion = 6 * pj.tamanioTile;
        experienciaAOtorgar = 10;
    }

    /**
     * Carga sprites UNA SOLA VEZ para todos los Slimes (Object Pooling).
     */
    private static synchronized void cargarSpritesEstaticos(PanelJuego pj) {
        if (spritesLoaded) return;
        UtilityTool tool = new UtilityTool();
        try {
            String ruta = "/Npc/Slime/";

            s_arriba1 = tool.escalarImagen(ImageIO.read(Slime.class.getResourceAsStream(ruta + "Arriba01.png")), pj.tamanioTile, pj.tamanioTile);
            s_arriba2 = tool.escalarImagen(ImageIO.read(Slime.class.getResourceAsStream(ruta + "Arriba02.png")), pj.tamanioTile, pj.tamanioTile);
            s_abajo1 = tool.escalarImagen(ImageIO.read(Slime.class.getResourceAsStream(ruta + "Abajo01.png")), pj.tamanioTile, pj.tamanioTile);
            s_abajo2 = tool.escalarImagen(ImageIO.read(Slime.class.getResourceAsStream(ruta + "Abajo02.png")), pj.tamanioTile, pj.tamanioTile);

            s_derecha1 = tool.escalarImagen(ImageIO.read(Slime.class.getResourceAsStream(ruta + "Derecha01.png")), pj.tamanioTile, pj.tamanioTile);
            s_derecha2 = tool.escalarImagen(ImageIO.read(Slime.class.getResourceAsStream(ruta + "Derecha02.png")), pj.tamanioTile, pj.tamanioTile);
            s_izquierda1 = tool.voltearImagenHorizontal(s_derecha1);
            s_izquierda2 = tool.voltearImagenHorizontal(s_derecha2);

            s_muerte1 = tool.escalarImagen(ImageIO.read(Slime.class.getResourceAsStream(ruta + "Muerte01.png")), pj.tamanioTile, pj.tamanioTile);
            s_muerte2 = tool.escalarImagen(ImageIO.read(Slime.class.getResourceAsStream(ruta + "Muerte02.png")), pj.tamanioTile, pj.tamanioTile);
            s_muerte3 = tool.escalarImagen(ImageIO.read(Slime.class.getResourceAsStream(ruta + "Muerte03.png")), pj.tamanioTile, pj.tamanioTile);

            spritesLoaded = true;
        } catch (Exception e) {
            System.err.println("[Slime] Error al cargar sprites: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void asignarSprites() {
        arriba1 = s_arriba1; arriba2 = s_arriba2;
        abajo1 = s_abajo1; abajo2 = s_abajo2;
        derecha1 = s_derecha1; derecha2 = s_derecha2;
        izquierda1 = s_izquierda1; izquierda2 = s_izquierda2;
        muerte1 = s_muerte1; muerte2 = s_muerte2; muerte3 = s_muerte3;
    }

    @Override
    public void resetearEstado() {
        // Slime no tiene estado adicional que resetear
    }

    @Override
    public void actualizarIA() {
        perseguirJugador();
    }

    @Override
    protected BufferedImage obtenerSprite() {
        // Retornar sprite según dirección y frame de animación
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
                return abajo1; // Default
        }
    }

    public static void resetearCache() {
        spritesLoaded = false;
    }
}
