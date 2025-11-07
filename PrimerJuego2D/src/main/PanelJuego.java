package main;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;

/*
 * el estilo de juego es retro 2d basado en mosaicos tiles
 * todos los elementos npcs personaje objetos etc se construyen con sprites
 * de tamaño fijo tipicamente 16x16 o 32x32 pixeles
 * como los sistemas retro por ejemplo snes usaban resoluciones bajas como 256x224
 * los graficos se veian bien a escala 11 pero en pantallas modernas eso es muy pequeño
 * por eso aplicaremos escalado entero para mantener el estilo pixel art sin distorsion
 */

public class PanelJuego extends JPanel{
	
	final int OriginalTile = 16; //juego 16x16
	final int scale = 3;
	
	final int tamanioTile = OriginalTile*scale; //48 *48
	//relacion 4*3 clasica 
	final int maxPantallaColumnas = 16;
	final int maxPantallaFilas = 12;
	//tamaño de la panatalla 
	final int anchoPantalla = tamanioTile*maxPantallaColumnas; //768 px
	final int altoPantalla = tamanioTile*maxPantallaFilas; //576 px
	
	public PanelJuego() {
		this.setPreferredSize(new Dimension(anchoPantalla,altoPantalla));
		this.setBackground(Color.BLACK);
		//MEJORA EL RENDIMIENTO 
		this.setDoubleBuffered(true);
		
	}
	
}
