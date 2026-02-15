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

    private UtilityTool tool = new UtilityTool();

    public Slime(PanelJuego pj) {
        super(pj);

        // Estadísticas
        vidaMaxima = 20;
        vidaActual = vidaMaxima;
        ataque = 3;
        defensa = 0;
        vel = 1;
        direccion = "abajo";

        // IA
        radioDeteccion = 6 * pj.tamanioTile;
        experienciaAOtorgar = 10;

        cargarSprites();
    }

    private void cargarSprites() {
        try {
            rutaCarpeta = "/Npc/Slime/";

            // Cargar sprites de movimiento (2 frames por dirección)
            BufferedImage tempArriba1 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "Arriba01.png"));
            BufferedImage tempArriba2 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "Arriba02.png"));
            BufferedImage tempAbajo1 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "Abajo01.png"));
            BufferedImage tempAbajo2 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "Abajo02.png"));

            // Solo cargar sprites de derecha
            BufferedImage tempDer1 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "Derecha01.png"));
            BufferedImage tempDer2 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "Derecha02.png"));

            // Cargar sprites de muerte (3 frames)
            BufferedImage tempMuerte1 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "Muerte01.png"));
            BufferedImage tempMuerte2 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "Muerte02.png"));
            BufferedImage tempMuerte3 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "Muerte03.png"));

            // Escalar imágenes
            arriba1 = tool.escalarImagen(tempArriba1, pj.tamanioTile, pj.tamanioTile);
            arriba2 = tool.escalarImagen(tempArriba2, pj.tamanioTile, pj.tamanioTile);
            abajo1 = tool.escalarImagen(tempAbajo1, pj.tamanioTile, pj.tamanioTile);
            abajo2 = tool.escalarImagen(tempAbajo2, pj.tamanioTile, pj.tamanioTile);

            // Escalar sprites de derecha
            derecha1 = tool.escalarImagen(tempDer1, pj.tamanioTile, pj.tamanioTile);
            derecha2 = tool.escalarImagen(tempDer2, pj.tamanioTile, pj.tamanioTile);

            // Generar sprites de izquierda mediante espejo horizontal
            izquierda1 = tool.voltearImagenHorizontal(derecha1);
            izquierda2 = tool.voltearImagenHorizontal(derecha2);

            muerte1 = tool.escalarImagen(tempMuerte1, pj.tamanioTile, pj.tamanioTile);
            muerte2 = tool.escalarImagen(tempMuerte2, pj.tamanioTile, pj.tamanioTile);
            muerte3 = tool.escalarImagen(tempMuerte3, pj.tamanioTile, pj.tamanioTile);

        } catch (Exception e) {
            e.printStackTrace();
        }
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
}
