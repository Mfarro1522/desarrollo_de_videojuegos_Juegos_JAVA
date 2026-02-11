package objetos;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class OBJ_botas extends superObjeto {

	public OBJ_botas(int tamanioTile) {
		nombre = "botas";
		AreaSolida = new Rectangle(0,0,40,40);
		try {
			BufferedImage imagenOriginal = ImageIO.read(getClass().getResource("/objetos/zapato.png"));
			
			imagen = miTool.escalarImagen(imagenOriginal, tamanioTile, tamanioTile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
