package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.GraphicAttribute;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

import objetos.OBJ_llave;

public class UI {

	PanelJuego pj;
	Graphics2D g2;
	
	// Fuentes
	Font arial_40;
	Font arial_80B; // B = Bold
	
	
	/**
	 * Constructor: Inicializa fuentes y carga recursos.
	 */
	public UI(PanelJuego pj) {
		this.pj = pj;

		arial_40 = new Font("Arial", Font.PLAIN, 40);
		arial_80B = new Font("Arial", Font.BOLD, 80);
	}


	/**
	 * Método principal de dibujado del HUD. Se llama desde paintComponent() de
	 * PanelJuego.
	 */
	public void draw(Graphics2D g2) {
		this.g2 = g2;
		g2.setFont(arial_40);
		g2.setColor(Color.white);
		
		//Dibujar segun estado Actual
		if (pj.gameState == pj.playState) {
			//estado jugando llenar aqui npc vide etc
			
		} else if (pj.gameState == pj.pauseState) {
			dibujarPantallaPausa();
		}
	}
	/**
	* Dibuja la pantalla de pausa con el texto "PAUSED" centrado.
	*/
	public void dibujarPantallaPausa() {
		g2.setFont(arial_40);
		g2.setColor(Color.white);
		
		String texto = "PAUSADO";
		
		int x = obtenerXCentrado(texto);
		int y = pj.altoPantalla/2;
		
		g2.drawString(texto, x, y);
	}

	/**
	* Calcula la coordenada X para centrar un texto en pantalla.
	* @param texto - El texto a centrar
	* @return La coordenada X donde debe dibujarse
	*/
	
	public int obtenerXCentrado(String texto) {
		int longitudTexto = (int) g2.getFontMetrics().getStringBounds(texto, g2).getWidth();
		return (pj.anchoPantalla / 2) - (longitudTexto / 2);
	}
}
