package utilidades;

import java.awt.Color;

/**
 * Representa una notificación temporal en pantalla.
 */
public class Notificacion {

    public String mensaje;
    public Color color;
    public int duracion;
    public int tiempoRestante;

    public Notificacion(String mensaje, Color color, int duracionSegundos) {
        this.mensaje = mensaje;
        this.color = color;
        this.duracion = duracionSegundos * 60;
        this.tiempoRestante = this.duracion;
    }

    public void actualizar() {
        if (tiempoRestante > 0) {
            tiempoRestante--;
        }
    }

    public boolean estaActiva() {
        return tiempoRestante > 0;
    }

    public float getOpacidad() {
        if (tiempoRestante < 30) {
            return tiempoRestante / 30f;
        }
        return 1f;
    }
}
