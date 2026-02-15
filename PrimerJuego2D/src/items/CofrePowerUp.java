package items;

import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Cofre que otorga power-ups al jugador.
 */
public class CofrePowerUp extends SuperObjeto {

    public enum TipoPowerUp {
        INVENCIBILIDAD, VELOCIDAD, ATAQUE, CURACION
    }

    public TipoPowerUp tipoPowerUp;

    public CofrePowerUp(int tamanioTile, TipoPowerUp tipo) {
        this.tipoPowerUp = tipo;
        nombre = "Cofre Power-Up";

        try {
            String path = "/objetos/cofre_Normal.png";
            switch (tipoPowerUp) {
                case INVENCIBILIDAD:
                    path = "/objetos/cofre_Azul.png";
                    break;
                case VELOCIDAD:
                    path = "/objetos/cofre_Amarillo.png";
                    break;
                case ATAQUE:
                    path = "/objetos/cofre_Rojo.png";
                    break;
                case CURACION:
                    path = "/objetos/cofre_Verde.png";
                    break;
            }
            imagen = ImageIO.read(getClass().getResourceAsStream(path));
            imagen = miTool.escalarImagen(imagen, tamanioTile, tamanioTile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        colision = true;
    }

}
