package objetos;

import java.awt.Rectangle;
import java.io.IOException;

import javax.imageio.ImageIO;

public class OBJ_botas extends superObjeto {

	public OBJ_botas() {
		nombre = "botas";
		AreaSolida = new Rectangle(0,0,40,40);
		try {
			imagen = ImageIO.read(getClass().getResource("/objetos/zapato.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
