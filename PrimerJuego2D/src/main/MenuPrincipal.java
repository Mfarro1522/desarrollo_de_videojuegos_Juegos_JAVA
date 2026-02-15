package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * Clase que maneja el dibujo y la lógica del menú principal.
 * Centraliza las coordenadas de los botones para que el mouse y el teclado
 * funcionen correctamente.
 */
public class MenuPrincipal {

	PanelJuego pj;

	// ===== Colores =====
	private final Color COLOR_FONDO = new Color(15, 15, 25);
	private final Color COLOR_PANEL = new Color(30, 30, 60, 220);
	private final Color COLOR_BORDE = new Color(120, 80, 200);
	private final Color COLOR_BOTON = new Color(40, 40, 80);
	private final Color COLOR_BOTON_SELECCIONADO = new Color(80, 50, 160);
	private final Color COLOR_TEXTO = new Color(220, 220, 240);
	private final Color COLOR_TITULO = new Color(255, 80, 80);
	private final Color COLOR_SUBTITULO = new Color(180, 140, 255);
	private final Color COLOR_HIGHLIGHT = new Color(255, 215, 0);

	// ===== Fuentes =====
	private Font fuenteTitulo = new Font("Arial", Font.BOLD, 64);
	private Font fuenteBoton = new Font("Arial", Font.BOLD, 24);
	private Font fuentePequena = new Font("Arial", Font.PLAIN, 16);

	// ===== Geometría de botones (CONSTANTES CENTRALIZADAS) =====
	public static final int BOTON_ANCHO = 260;
	public static final int BOTON_ALTO = 50;
	public static final int Y_INICIO = 220;
	public static final int ESPACIADO = 70;
	public static final int NUM_OPCIONES = 4;

	private final String[] OPCIONES = { "COMENZAR", "AYUDA", "LOGROS", "CRÉDITOS" };

	public MenuPrincipal(PanelJuego pj) {
		this.pj = pj;
	}

	// ===== Métodos de geometría para acceso externo (Mouse) =====

	/**
	 * Retorna la coordenada X del inicio de los botones (centrado).
	 */
	public int getBotonX() {
		return pj.anchoPantalla / 2 - BOTON_ANCHO / 2;
	}

	/**
	 * Retorna la coordenada Y del botón en la posición dada.
	 */
	public int getBotonY(int indice) {
		return Y_INICIO + indice * ESPACIADO;
	}

	/**
	 * Verifica si un punto (x, y) está dentro de algún botón del menú.
	 * Retorna el índice del botón (0-3) o -1 si no está en ninguno.
	 */
	public int getBotonEnPosicion(int x, int y) {
		int btnX = getBotonX();
		for (int i = 0; i < NUM_OPCIONES; i++) {
			int btnY = getBotonY(i);
			if (x >= btnX && x <= btnX + BOTON_ANCHO &&
					y >= btnY && y <= btnY + BOTON_ALTO) {
				return i;
			}
		}
		return -1;
	}

	// ===== Dibujado =====

	public void dibujar(Graphics2D g2) {
		int ancho = pj.anchoPantalla;
		int alto = pj.altoPantalla;

		// Fondo
		if (pj.imagenFondoMenu != null) {
			g2.drawImage(pj.imagenFondoMenu, 0, 0, null);
		} else {
			g2.setColor(COLOR_FONDO);
			g2.fillRect(0, 0, ancho, alto);

			// Líneas decorativas
			g2.setColor(new Color(40, 20, 60));
			for (int i = 0; i < ancho; i += 40) {
				g2.drawLine(i, 0, i, alto);
			}
			for (int i = 0; i < alto; i += 40) {
				g2.drawLine(0, i, ancho, i);
			}
		}

		// Título
		g2.setFont(fuenteTitulo);
		String titulo = "ARENA SURVIVORS";
		int xTitulo = obtenerXCentrado(g2, titulo);
		int yTitulo = 80; // Antes 130
		// Sombra
		g2.setColor(new Color(80, 0, 0));
		g2.drawString(titulo, xTitulo + 3, yTitulo + 3);
		// Texto
		g2.setColor(COLOR_TITULO);
		g2.drawString(titulo, xTitulo, yTitulo);

		// Subtítulo
		g2.setFont(fuentePequena);
		g2.setColor(COLOR_SUBTITULO);
		String sub = "Sobrevive. Evoluciona. Conquista.";
		g2.drawString(sub, obtenerXCentrado(g2, sub), yTitulo + 35);

		// Botones
		int menuOpcion = pj.kh.menuOpcion;
		int btnX = getBotonX();

		for (int i = 0; i < OPCIONES.length; i++) {
			int yBoton = getBotonY(i);
			boolean seleccionado = (i == menuOpcion);

			// Fondo del botón
			g2.setColor(seleccionado ? COLOR_BOTON_SELECCIONADO : COLOR_BOTON);
			g2.fillRoundRect(btnX, yBoton, BOTON_ANCHO, BOTON_ALTO, 15, 15);

			// Borde
			if (seleccionado) {
				g2.setColor(COLOR_HIGHLIGHT);
				g2.drawRoundRect(btnX - 1, yBoton - 1, BOTON_ANCHO + 2, BOTON_ALTO + 2, 15, 15);
			} else {
				g2.setColor(COLOR_BORDE);
				g2.drawRoundRect(btnX, yBoton, BOTON_ANCHO, BOTON_ALTO, 15, 15);
			}

			// Texto del botón
			g2.setFont(fuenteBoton);
			g2.setColor(seleccionado ? COLOR_HIGHLIGHT : COLOR_TEXTO);
			FontMetrics fm = g2.getFontMetrics();
			int xTexto = btnX + (BOTON_ANCHO - fm.stringWidth(OPCIONES[i])) / 2;
			int yTexto = yBoton + (BOTON_ALTO + fm.getAscent() - fm.getDescent()) / 2;
			g2.drawString(OPCIONES[i], xTexto, yTexto);

			// Indicador de selección
			if (seleccionado) {
				g2.setColor(COLOR_HIGHLIGHT);
				g2.drawString("▶", btnX - 30, yTexto);
			}
		}

		// Instrucciones
		g2.setFont(fuentePequena);
		g2.setColor(new Color(150, 150, 170));
		String instrucciones = "W/S o ↑/↓ para navegar  •  ENTER o Click para seleccionar";
		g2.drawString(instrucciones, obtenerXCentrado(g2, instrucciones), alto - 30);
	}

	private int obtenerXCentrado(Graphics2D g2, String texto) {
		int longitudTexto = (int) g2.getFontMetrics().getStringBounds(texto, g2).getWidth();
		return (pj.anchoPantalla / 2) - (longitudTexto / 2);
	}
}
