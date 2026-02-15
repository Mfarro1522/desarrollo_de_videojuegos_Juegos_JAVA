package interfaz;

import java.awt.Color;
import java.awt.Graphics2D;

import configuracion.Configuracion;
import mundo.MundoJuego;

/**
 * Pantalla de pausa (overlay semitransparente).
 */
public class PantallaPausa {

    MundoJuego mundo;

    public PantallaPausa(MundoJuego mundo) {
        this.mundo = mundo;
    }

    public void dibujar(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, Configuracion.ANCHO_PANTALLA, Configuracion.ALTO_PANTALLA);

        g2.setFont(InterfazUsuario.FUENTE_40);
        g2.setColor(Color.WHITE);
        String texto = "PAUSADO";
        int x = InterfazUsuario.obtenerXCentrado(g2, texto);
        int y = Configuracion.ALTO_PANTALLA / 2;
        g2.drawString(texto, x, y);
    }
}
