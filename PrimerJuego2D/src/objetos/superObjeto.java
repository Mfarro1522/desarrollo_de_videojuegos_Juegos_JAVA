package objetos;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import main.PanelJuego;

public class superObjeto {
	
	public BufferedImage imagen;
	public String nombre;
	public boolean colision;
	public int worldX , worldY;
	
	//hitbox por defecto del tamaño del tile
	public Rectangle AreaSolida = new Rectangle(0, 0, 48, 48); 
	public int AreaSolidaDefaultX = 0;
	public int AreaSolidaDefaultY = 0;
	
	public void draw(Graphics2D g2 , PanelJuego pj) {

			int screenX = worldX - pj.jugador.worldx + pj.jugador.screenX;
			int screenY = worldY - pj.jugador.worldy + pj.jugador.screeny;

			if (worldX +pj.tamanioTile> pj.jugador.worldx - pj.jugador.screenX && worldX - pj.tamanioTile< pj.jugador.worldx + pj.jugador.screenX
					&& worldY +pj.tamanioTile > pj.jugador.worldy - pj.jugador.screeny
					&& worldY -pj.tamanioTile< pj.jugador.worldy + pj.jugador.screeny) {
				g2.drawImage(imagen, screenX, screenY, pj.tamanioTile, pj.tamanioTile, null);
				
			}
			
	}


}
