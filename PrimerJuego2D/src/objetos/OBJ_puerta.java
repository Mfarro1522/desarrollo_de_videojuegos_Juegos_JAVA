package objetos;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class OBJ_puerta extends superObjeto {

    public OBJ_puerta(int tamanioTile) {
        nombre = "puerta";
        try {
            BufferedImage imagenOriginal = ImageIO.read(getClass().getResource("/objetos/puerta.png"));
            
            imagen = miTool.escalarImagen(imagenOriginal, tamanioTile, tamanioTile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        colision = true;
    }

}
