package interfaz;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

import configuracion.Configuracion;
import mundo.MundoJuego;

/**
 * Pantalla de ayuda con controles, mecánicas y power-ups.
 */
public class PantallaAyuda {

    MundoJuego mundo;

    private BufferedImage imagenAyuda;
    private final int ANCHO_IMAGEN = 800;
    private final int ALTO_IMAGEN = 600;

    public PantallaAyuda(MundoJuego mundo) {
        this.mundo = mundo;
        cargarImagen();
    }

    private void cargarImagen() {
        try {
            imagenAyuda = ImageIO.read(getClass().getResourceAsStream("/bg/panelAyuda.png"));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error cargando imagen de ayuda: /bg/panelAyuda.png");
        }
    }

    public void dibujar(Graphics2D g2) {
        int ancho = Configuracion.ANCHO_PANTALLA;
        int alto = Configuracion.ALTO_PANTALLA;

        // Fondo oscuro semitransparente para resaltar el panel (opcional, como overlay)
        g2.setColor(new Color(0, 0, 0, 50));
        g2.fillRect(0, 0, ancho, alto);

        // Calcular posición centrada
        // Calcular posición (Y fijo en 100 como el original)
        int x = ancho / 2 - ANCHO_IMAGEN / 2;
        int y = 100;

        // Dibujar imagen
        if (imagenAyuda != null) {
            g2.drawImage(imagenAyuda, x, y, ANCHO_IMAGEN, ALTO_IMAGEN, null);
        } else {
            // Fallback
            g2.setColor(Color.RED);
            g2.drawString("Error cargando imagen", x + 20, y + 40);
        }

        // Borde morado (Estilo consistente)
        g2.setColor(InterfazUsuario.COLOR_BORDE);
        g2.drawRoundRect(x, y, ANCHO_IMAGEN, ALTO_IMAGEN, 20, 20);

        // Instrucciones de salida
        g2.setFont(InterfazUsuario.FUENTE_PEQUENA);
        g2.setColor(Color.WHITE);
        String instr = "Presiona ESC o ENTER para volver";
        g2.drawString(instr, InterfazUsuario.obtenerXCentrado(g2, instr), y + ALTO_IMAGEN + 40);
    }
}
