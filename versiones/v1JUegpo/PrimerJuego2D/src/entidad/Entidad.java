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
