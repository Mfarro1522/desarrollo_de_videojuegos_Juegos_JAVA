package objetos;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class OBJ_llave extends superObjeto{
	
	public OBJ_llave(int tamanioTile) {
		 nombre = "llave";
		 try {
			BufferedImage imagenOriginal =  ImageIO.read(getClass().getResource("/objetos/llave.png"));
			
			imagen = miTool.escalarImagen(imagenOriginal, tamanioTile, tamanioTile);
		 
		 } catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
