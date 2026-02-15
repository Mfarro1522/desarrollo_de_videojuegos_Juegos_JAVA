package items;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Cofre normal que otorga puntos y experiencia.
 */
public class CofreNormal extends SuperObjeto {

    public CofreNormal(int tamanioTile) {
        nombre = "cofre";
        try {
            BufferedImage imagenOriginal = ImageIO.read(getClass().getResource("/objetos/cofre_Normal.png"));
            imagen = miTool.escalarImagen(imagenOriginal, tamanioTile, tamanioTile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
