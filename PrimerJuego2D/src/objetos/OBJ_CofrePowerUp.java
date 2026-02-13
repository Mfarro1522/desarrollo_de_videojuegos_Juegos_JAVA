package objetos;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;
import javax.imageio.ImageIO;
import main.UtilityTool;

/**
 * Cofre que otorga power-ups al jugador
 */
public class OBJ_CofrePowerUp extends superObjeto {
	
	private UtilityTool tool = new UtilityTool();
	
	public enum TipoPowerUp {
		INVENCIBILIDAD,    // Hace al jugador invencible por 10 segundos
		VELOCIDAD,         // Aumenta velocidad 50% por 15 segundos
		ATAQUE,            // Aumenta ataque 30% por 20 segundos
		CURACION           // Restaura 30 de vida
	}
	
	public TipoPowerUp tipoPowerUp;
	
	public OBJ_CofrePowerUp(int tamanioTile, TipoPowerUp tipo) {
		this.tipoPowerUp = tipo;
		nombre = "Cofre Power-Up";
		
		try {
			// Cargar imagen del cofre
			imagen = ImageIO.read(getClass().getResourceAsStream("/objetos/cofre.png"));
			imagen = tool.escalarImagen(imagen, tamanioTile, tamanioTile);
		} catch (IOException e) {
			// Si no existe la imagen, crear un cofre visual simple
			imagen = tool.crearImagenColor(tamanioTile, tamanioTile, obtenerColorPorTipo());
		}
		
		colision = true;
	}
	
	/**
	 * Retorna un color según el tipo de power-up
	 */
	private Color obtenerColorPorTipo() {
		switch (tipoPowerUp) {
			case INVENCIBILIDAD: return Color.CYAN;
			case VELOCIDAD: return Color.YELLOW;
			case ATAQUE: return Color.RED;
			case CURACION: return Color.GREEN;
			default: return Color.MAGENTA;
		}
	}
	
	/**
	 * Dibuja el cofre con un brillo según el tipo
	 */
	@Override
	public void draw(Graphics2D g2, main.PanelJuego pj) {
		int screenX = worldX - pj.jugador.worldx + pj.jugador.screenX;
		int screenY = worldY - pj.jugador.worldy + pj.jugador.screeny;
		
		if (worldX + pj.tamanioTile > pj.jugador.worldx - pj.jugador.screenX
				&& worldX - pj.tamanioTile < pj.jugador.worldx + pj.jugador.screenX
				&& worldY + pj.tamanioTile > pj.jugador.worldy - pj.jugador.screeny
				&& worldY - pj.tamanioTile < pj.jugador.worldy + pj.jugador.screeny) {
			
			// Dibujar imagen del cofre
			g2.drawImage(imagen, screenX, screenY, null);
			
			// Dibujar brillo del power-up
			g2.setColor(new Color(obtenerColorPorTipo().getRed(), 
			                       obtenerColorPorTipo().getGreen(), 
			                       obtenerColorPorTipo().getBlue(), 100));
			g2. fillRect(screenX, screenY, pj.tamanioTile, pj.tamanioTile);
		}
	}
}
