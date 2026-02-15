package entidad;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Clase base para todas las entidades del juego (Jugador, NPCs, Objetos).
 * Contiene atributos comunes como posición, velocidad, sprites y área de
 * colisión.
 */
public class Entidad {

    public int worldx, worldy;
    public int vel;

    // Combate
    public int vidaMaxima;
    public int vidaActual;
    public int ataque;
    public int defensa;
    public boolean estaVivo = true;

    // ===== OBJECT POOLING: flag de activación =====
    public boolean activo = false;

    public enum EstadoEntidad {
        IDLE, MOVIENDO, ATACANDO, HERIDO, MURIENDO
    }

    public EstadoEntidad estado = EstadoEntidad.IDLE;

    // ===== Sistema de combate =====
    public int contadorInvulnerabilidad = 0;
    public int duracionInvulnerabilidad = 60;

    public void recibirDanio(int cantidad) {
        if (contadorInvulnerabilidad > 0)
            return;
        int danioReal = Math.max(1, cantidad - defensa);
        vidaActual -= danioReal;
        contadorInvulnerabilidad = duracionInvulnerabilidad;

        if (vidaActual <= 0) {
            vidaActual = 0;
            estaVivo = false;
            estado = EstadoEntidad.MURIENDO;
        } else {
            estado = EstadoEntidad.HERIDO;
        }
    }

    public void actualizarInvulnerabilidad() {
        if (contadorInvulnerabilidad > 0) {
            contadorInvulnerabilidad--;
        }
    }

    // Ruta de carpeta para imágenes
    protected String rutaCarpeta = "";

    // Sprites de movimiento
    public BufferedImage arriba1, arriba2, abajo1, abajo2, izquierda1, izquierda2, derecha1, derecha2;

    // Sprites de muerte
    public BufferedImage muerte1, muerte2, muerte3;
    public int contadorMuerte = 0;
    public int frameMuerte = 0;
    public int duracionFrameMuerte = 15;

    // Sprites de ataque por dirección
    public BufferedImage ataqueArriba, ataqueAbajo, ataqueDer, ataqueIzq;
    public int contadorAnimAtaque = 0;
    public int duracionAnimAtaque = 15;

    public String direccion;

    public int contadorSpites = 0;
    public int numeroSpites = 1;

    public Rectangle AreaSolida;
    public int AreaSolidaDefaultX;
    public int AreaSolidaDefaultY;

    public boolean hayColision = false;
    public boolean vuela = false;
}
