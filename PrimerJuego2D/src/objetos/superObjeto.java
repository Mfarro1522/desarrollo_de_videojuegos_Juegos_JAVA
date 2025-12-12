package objetos;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import main.PanelJuego;

public class superObjeto {
	
	public BufferedImage imagen;
	public String nombre;
	public boolean colision;
	public int worldX , worldY;
	
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
