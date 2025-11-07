package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;

import javax.swing.JPanel;

/*
 * el estilo de juego es retro 2d basado en mosaicos tiles
 * todos los elementos npcs personaje objetos etc se construyen con sprites
 * de tamaño fijo tipicamente 16x16 o 32x32 pixeles
 * como los sistemas retro por ejemplo snes usaban resoluciones bajas como 256x224
 * los graficos se veian bien a escala 11 pero en pantallas modernas eso es muy pequeño
 * por eso aplicaremos escalado entero para mantener el estilo pixel art sin distorsion
 */

@SuppressWarnings("serial")
public class PanelJuego extends JPanel implements Runnable {

	final int OriginalTile = 16; // juego 16x16
	final int scale = 3;

	final int tamanioTile = OriginalTile * scale; // 48 *48
	// relacion 4*3 clasica
	final int maxPantallaColumnas = 16;
	final int maxPantallaFilas = 12;
	// tamaño de la panatalla
	final int anchoPantalla = tamanioTile * maxPantallaColumnas; // 768 px
	final int altoPantalla = tamanioTile * maxPantallaFilas; // 576 px

	// lsitener
	keyHandler kh = new keyHandler();
	Thread threadJuego;

	// set dafault player
	int jugadorX = 100;
	int jugadorY = 100;
	int velocidadJugador = 4;

	// FPS
	int FPS = 60;

	public PanelJuego() {
		this.setPreferredSize(new Dimension(anchoPantalla, altoPantalla));
		this.setBackground(Color.BLACK);
		// MEJORA EL RENDIMIENTO
		this.setDoubleBuffered(true);
		this.addKeyListener(kh);
		setFocusable(true);

	}

	public void iniciarHiloJuego() {
		threadJuego = new Thread(this);
		threadJuego.start();
	}

	//Game loop
	public void run() {
		double intervaloDibujo = 1000000000 / FPS;
		double delta = 0;
		double tiempoFinal = System.nanoTime();
		double tiempoActual;
		double temporizador = 0;
		double contador = 0;

		
		
		while (threadJuego != null) {

			tiempoActual = System.nanoTime();
			delta +=(tiempoActual - tiempoFinal)/intervaloDibujo;
			temporizador += (tiempoActual-tiempoFinal);
			tiempoFinal = tiempoActual;
			
			while(delta>=1) {
				update();
				repaint();
				//si estas en linux agrega esta linea
				Toolkit.getDefaultToolkit().sync();
				delta--;
				contador++;

			}
			
			if(temporizador >= 1000000000 ) {
				System.out.println("Fps : "+contador);
				temporizador = 0;
				contador = 0;
			}
			
		}
	}

	public void update() {
		if (kh.arribaPres == true) {
			jugadorY -= velocidadJugador;
		}
		if (kh.abajoPres == true) {
			jugadorY += velocidadJugador;
		}
		if (kh.izqPres == true) {
			jugadorX -= velocidadJugador;
		}
		if (kh.drchPres == true) {
			jugadorX += velocidadJugador;
		}
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.white);
		g2.fillRect(jugadorX, jugadorY, tamanioTile, tamanioTile);
		g2.dispose();
	}

}
