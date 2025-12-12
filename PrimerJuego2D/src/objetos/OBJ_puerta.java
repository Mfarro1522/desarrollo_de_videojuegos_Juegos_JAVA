package objetos;

import java.io.IOException;

import javax.imageio.ImageIO;

public class OBJ_puerta extends superObjeto {

    public OBJ_puerta() {
        nombre = "puerta";
        try {
            imagen = ImageIO.read(getClass().getResource("/objetos/puerta.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
