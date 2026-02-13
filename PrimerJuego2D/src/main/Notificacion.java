package main;

import java.awt.Color;

/**
 * Representa una notificación temporal en pantalla.
 */
public class Notificacion {
    public String mensaje;
    public Color color;
    public int duracion; // En frames
    public int tiempoRestante; // En frames
    
    public Notificacion(String mensaje, Color color, int duracionSegundos) {
        this.mensaje = mensaje;
        this.color = color;
        this.duracion = duracionSegundos * 60; // Convertir a frames (60 FPS)
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
        // Fade out en los últimos 30 frames
        if (tiempoRestante < 30) {
            return tiempoRestante / 30f;
        }
        return 1f;
    }
}
