package entidad;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Entidad {
	public int worldx, worldy;
	public int vel;

	public BufferedImage arriba1, arriba2, abajo1, abajo2, izquierda1, izquierda2, izquierda3, derecha1, derecha2,
			derecha3;
	public String direccion;

	public int contadorSpites = 0;
	public int numeroSpites = 1;
	
	public Rectangle AreaSolida;
	//public Rectangle verHitbox ;
	public boolean hayColision = false;
}
