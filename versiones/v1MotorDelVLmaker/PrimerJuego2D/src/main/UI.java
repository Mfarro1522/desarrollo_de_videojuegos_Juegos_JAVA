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
	
	public String mensaje = "";
	int contadorMensaje = 0;
	public boolean juegoTerminado = false;
	double tiempoJuego;
	DecimalFormat formatoDecimal = new DecimalFormat("#0.00");
	BufferedImage imagenLlave;
	public boolean mensajeActivo = false;
	
	
	/**
	 * Constructor: Inicializa fuentes y carga recursos.
	 */
	public UI(PanelJuego pj) {
		this.pj = pj;

		arial_40 = new Font("Arial", Font.PLAIN, 40);
		arial_80B = new Font("Arial", Font.BOLD, 80);
	}

	/**
	 * Muestra un mensaje temporal en pantalla. El mensaje desaparece después de ~2
	 * segundos.
	 */
	public void mostrarMensaje(String texto) {
		mensaje = texto;
		mensajeActivo = true;
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
			
		} else if (pj.gameState == pj.playState) {
			dibujarPantallaPausa();
		}
	}

	/**
	 * Dibuja el HUD normal: llaves, tiempo, mensajes.
	 */
	private void dibujarHUD(Graphics2D g2) {
		g2.setFont(arial_40);
		g2.setColor(Color.WHITE);

		// Dibujar icono de llave
		g2.drawImage(imagenLlave, pj.tamanioTile / 2, pj.tamanioTile / 4, pj.tamanioTile, pj.tamanioTile, null);

		// Dibujar número de llaves con borde negro
		g2.setColor(Color.BLACK);
		g2.setColor(Color.WHITE);
		g2.drawString(" x " + pj.jugador.numeroLlaves, 77, 65);

		// === TIEMPO DE JUEGO ===
		tiempoJuego += (double) 1 / 60; // Incrementar cada frame
	
		g2.setColor(Color.WHITE);
		g2.drawString("Tiempo: " + formatoDecimal.format(tiempoJuego), pj.tamanioTile * 11, 65);

		// === MENSAJES TEMPORALES ===
		if (mensajeActivo == true) {
			g2.setFont(g2.getFont().deriveFont(30F));
			g2.setColor(Color.WHITE);
			g2.drawString(mensaje, pj.tamanioTile / 2, pj.tamanioTile * 5);
			contadorMensaje++;
			// Después de 120 frames (~2 segundos), ocultar mensaje
			if (contadorMensaje > 120) {
				contadorMensaje = 0;
				mensajeActivo = false;
			}
		}
		
	}

	/**
	 * Dibuja la pantalla de fin del juego.
	 */
	private void dibujarPantallaFin(Graphics2D g2) {
		
		g2.setFont(arial_40);
		g2.setColor(Color.WHITE);
		String texto;
		
		int x, y;
		int longitudTexto;
		
		// === MENSAJE DE FELICITACIONES ===
		texto = "¡ok eres kbro";
		g2.setFont(arial_80B);
		
		// Centrar texto horizontalmente
		longitudTexto = (int) g2.getFontMetrics().getStringBounds(texto, g2).getWidth();
		x = (pj.anchoPantalla / 2) - (longitudTexto / 2);
		y = pj.altoPantalla / 2 - (pj.tamanioTile * 2);
		
		// Sombra del texto (efecto visual)
		g2.setColor(Color.BLACK);
		g2.drawString(texto, x + 5, y + 5);
		
		// Texto principal
		g2.setColor(Color.YELLOW);
		g2.drawString(texto, x, y);
		
		// === TIEMPO FINAL ===
		g2.setFont(arial_40);
		texto = "Tu tiempo fue: " + formatoDecimal.format(tiempoJuego) + " segundos";
		longitudTexto = (int) g2.getFontMetrics().getStringBounds(texto, g2).getWidth();
		x = (pj.anchoPantalla / 2) - (longitudTexto / 2);
		y = pj.altoPantalla / 2 + (pj.tamanioTile * 2);
		g2.setColor(Color.WHITE);
		g2.drawString(texto, x, y);
		
		// === DETENER EL JUEGO ===
		pj.threadJuego = null;
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
