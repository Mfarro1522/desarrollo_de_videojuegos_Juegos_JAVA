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

	// NUEVO CODIGO
	public int vidaMaxima;
	public int vidaActual;
	public int ataque;
	public int defensa;
	public boolean estaVivo = true;

	public enum EstadoEntidad {
		IDLE, // En reposo
		MOVIENDO, // Caminando
		ATACANDO, // Atacando
		HERIDO, // Recibiendo daño
		MURIENDO // Animación de muerte
	}

	public EstadoEntidad estado = EstadoEntidad.IDLE;

// ===== NUEVO: Sistema de combate =====
	public int contadorInvulnerabilidad = 0; // Frames de invulnerabilidad tras daño
	public int duracionInvulnerabilidad = 60; // 1 segundo a 60 FPS

	public void recibirDanio(int cantidad) {
		if (contadorInvulnerabilidad > 0)
			return; // Invulnerable

		int danioReal = Math.max(1, cantidad - defensa); // Mínimo 1 de daño
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

	/**
	 * Actualiza el contador de invulnerabilidad.
	 */
	public void actualizarInvulnerabilidad() {
		if (contadorInvulnerabilidad > 0) {
			contadorInvulnerabilidad--;
		}
	}

	public BufferedImage arriba1, arriba2, abajo1, abajo2, izquierda1, izquierda2, izquierda3, derecha1, derecha2,
			derecha3;
	public String direccion;

	public int contadorSpites = 0;
	public int numeroSpites = 1;

	public Rectangle AreaSolida;
	public int AreaSolidaDefaultX;
	public int AreaSolidaDefaultY;

	public boolean hayColision = false;
}
