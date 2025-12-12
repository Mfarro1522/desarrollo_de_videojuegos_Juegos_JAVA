package objetos;

import java.io.IOException;

import javax.imageio.ImageIO;

public class OBJ_llave extends superObjeto{
	
	public OBJ_llave() {
		 nombre = "llave";
		 try {
			imagen =  ImageIO.read(getClass().getResource("/objetos/llave.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
