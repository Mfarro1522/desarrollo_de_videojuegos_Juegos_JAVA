package objetos;

import java.io.IOException;

import javax.imageio.ImageIO;

public class OBJ_cofre extends superObjeto {

    public OBJ_cofre() {
        nombre = "cofre";
        try {
            imagen = ImageIO.read(getClass().getResource("/objetos/cofre.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
