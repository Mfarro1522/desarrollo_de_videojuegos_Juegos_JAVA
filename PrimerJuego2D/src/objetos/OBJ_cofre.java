package objetos;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class OBJ_cofre extends superObjeto {

    public OBJ_cofre(int tamanioTile) {
        nombre = "cofre";
        try {
            BufferedImage imagenOriginal = ImageIO.read(getClass().getResource("/objetos/cofre.png"));
            
            imagen = miTool.escalarImagen(imagenOriginal, tamanioTile, tamanioTile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
