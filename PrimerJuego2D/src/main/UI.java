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

	// Las fuentes se declaran AQUÍ, no dentro del draw()
	Font arial_40;
	Font arial_80B; // B = Bold

	BufferedImage imagenLlave;

	public boolean mensajeActivo = false;
	public String mensaje = "";
	int contadorMensaje = 0;

	// Estado del juego
	public boolean juegoTerminado = false;

	double tiempoJuego;
	DecimalFormat formatoDecimal = new DecimalFormat("#0.00");

	/**
	 * Constructor: Inicializa fuentes y carga recursos.
	 */
	public UI(PanelJuego pj) {
		this.pj = pj;

		// Crear fuentes UNA sola vez (optimización)
		arial_40 = new Font("Arial", Font.PLAIN, 40);
		arial_80B = new Font("Arial", Font.BOLD, 80);
		// Cargar imagen de llave para mostrar en el HUD
		OBJ_llave llave = new OBJ_llave();
		imagenLlave = llave.imagen;
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
		if (juegoTerminado == true) {
			// Pantalla de victoria
			dibujarPantallaFin(g2);
		} else {
			// HUD normal durante el juego
			dibujarHUD(g2);
		}
	}

	/**
	 * Dibuja el HUD normal: llaves, tiempo, mensajes.
	 */
	private void dibujarHUD(Graphics2D g2) {
		g2.setFont(arial_40);
		g2.setColor(Color.WHITE);

		// Dibujar icono de llave
		g2.drawImage(imagenLlave, pj.tamanioTile / 2, pj.tamanioTile / 2, pj.tamanioTile, pj.tamanioTile, null);

		// Dibujar número de llaves (x2, x3, etc.)
		g2.drawString("x " + pj.jugador.numeroLlaves, 74, 65);

		// === TIEMPO DE JUEGO ===
		tiempoJuego += (double) 1 / 60; // Incrementar cada frame
		g2.drawString("Tiempo: " + formatoDecimal.format(tiempoJuego), pj.tamanioTile * 11, 65);

		// === MENSAJES TEMPORALES ===
		if (mensajeActivo == true) {
			g2.setFont(g2.getFont().deriveFont(30F));
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
		g2.setColor(Color.WHITE);
		texto = "Tu tiempo fue: " + formatoDecimal.format(tiempoJuego) + " segundos";
		longitudTexto = (int) g2.getFontMetrics().getStringBounds(texto, g2).getWidth();
		x = (pj.anchoPantalla / 2) - (longitudTexto / 2);
		y = pj.altoPantalla / 2 + (pj.tamanioTile * 2);
		g2.drawString(texto, x, y);
		
		// === DETENER EL JUEGO ===
		pj.threadJuego = null;
	}

	/**
	 * Método utilitario para obtener la posición X centrada de un texto.
	 */
	public int obtenerXCentrado(String texto, Graphics2D g2) {
		int longitudTexto = (int) g2.getFontMetrics().getStringBounds(texto, g2).getWidth();
		return (pj.anchoPantalla / 2) - (longitudTexto / 2);
	}
}
