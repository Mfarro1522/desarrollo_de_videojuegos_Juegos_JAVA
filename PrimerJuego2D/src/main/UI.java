package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class UI {

	PanelJuego pj;
	Graphics2D g2;

	// Fuentes
	Font arial_40;
	Font arial_80B;

	// Colores del menú
	private final Color COLOR_FONDO = new Color(15, 15, 25);
	private final Color COLOR_PANEL = new Color(30, 30, 60, 220);
	private final Color COLOR_BORDE = new Color(120, 80, 200);
	private final Color COLOR_BOTON = new Color(40, 40, 80);
	private final Color COLOR_BOTON_SELECCIONADO = new Color(80, 50, 160);
	private final Color COLOR_TEXTO = new Color(220, 220, 240);
	private final Color COLOR_TITULO = new Color(255, 80, 80);
	private final Color COLOR_SUBTITULO = new Color(180, 140, 255);
	private final Color COLOR_HIGHLIGHT = new Color(255, 215, 0);

	// Fuentes del menú
	private Font fuenteTitulo;
	private Font fuenteBoton;
	private Font fuenteStats;
	private Font fuenteCreditos;
	private Font fuentePequena;

	// Sprites de preview para selección de personaje
	private BufferedImage[] spritesPreview = new BufferedImage[3];
	
	// Imagen de fondo del menú principal
	// TODO: Agregar imagen de fondo del menú
	// 1. Colocar la imagen en: res/menu/fondo_menu.png (dimensiones recomendadas: 1024x768)
	// 2. Descomentar la línea en el método cargarImagenFondoMenu()
	// 3. La imagen se dibujará automáticamente ajustada a la pantalla
	private BufferedImage imagenFondoMenu = null;
	
	// Imagen para la pantalla de Ayuda
	// TODO: Agregar imagen de ayuda
	// 1. Colocar la imagen en: res/menu/ayuda.png (dimensiones recomendadas: 1024x768)
	// 2. Descomentar la línea en el método cargarImagenAyuda()
	// 3. La imagen mostrará controles, mecánicas y tips del juego
	private BufferedImage imagenAyuda = null;

	// Datos de personajes
	private final String[] NOMBRES_PERSONAJES = { "Sideral", "Mago", "Doom" };
	private final String[] RANGOS = { "Rango Sideral", "Mago Arcano", "Guerrero Infernal" };
	private final String[] DESCRIPCIONES = {
			"Dragón con ataques a distancia",
			"Mago con ataques mágicos a distancia",
			"Guerrero cuerpo a cuerpo con espada"
	};
	private final int[][] STATS = {
			// { vida, ataque, defensa, velocidad }
			{ 20, 15, 3, 5 }, // Sideral
			{ 15, 20, 2, 4 }, // Mago
			{ 30, 12, 8, 3 } // Doom
	};
	private final Color[] COLORES_PERSONAJE = {
			new Color(100, 200, 255), // Sideral - azul celeste
			new Color(180, 100, 255), // Mago - púrpura
			new Color(255, 80, 50) // Doom - rojo
	};

	public UI(PanelJuego pj) {
		this.pj = pj;

		arial_40 = new Font("Arial", Font.PLAIN, 40);
		arial_80B = new Font("Arial", Font.BOLD, 80);

		fuenteTitulo = new Font("Arial", Font.BOLD, 64);
		fuenteBoton = new Font("Arial", Font.BOLD, 24);
		fuenteStats = new Font("Arial", Font.BOLD, 18);
		fuenteCreditos = new Font("Arial", Font.PLAIN, 22);
		fuentePequena = new Font("Arial", Font.PLAIN, 16);

		// Cargar sprites de preview
		cargarSpritesPreview();
		
		// Cargar imagen de fondo del menú
		cargarImagenFondoMenu();
		
		// Cargar imagen de ayuda
		cargarImagenAyuda();
	}
	
	/**
	 * Carga la imagen de fondo del menú principal.
	 */
	private void cargarImagenFondoMenu() {
		try {
			// TODO: Descomentar cuando agregues la imagen en res/menu/fondo_menu.png
			// imagenFondoMenu = ImageIO.read(getClass().getResourceAsStream("/menu/fondo_menu.png"));
			// UtilityTool tool = new UtilityTool();
			// imagenFondoMenu = tool.escalarImagen(imagenFondoMenu, pj.anchoPantalla, pj.altoPantalla);
		} catch (Exception e) {
			imagenFondoMenu = null;
			// Si no se encuentra la imagen, se usará el fondo por defecto
		}
	}
	
	/**
	 * Carga la imagen de la pantalla de Ayuda.
	 */
	private void cargarImagenAyuda() {
		try {
			// TODO: Descomentar cuando agregues la imagen en res/menu/ayuda.png
			// imagenAyuda = ImageIO.read(getClass().getResourceAsStream("/menu/ayuda.png"));
			// UtilityTool tool = new UtilityTool();
			// imagenAyuda = tool.escalarImagen(imagenAyuda, pj.anchoPantalla, pj.altoPantalla);
		} catch (Exception e) {
			imagenAyuda = null;
		}
	}

	/**
	 * Carga los sprites de preview (abajo_0001) de cada personaje.
	 */
	private void cargarSpritesPreview() {
		String[] carpetas = { "/jugador/Sideral/", "/jugador/Mago/", "/jugador/Doom/" };
		UtilityTool tool = new UtilityTool();
		for (int i = 0; i < 3; i++) {
			try {
				BufferedImage original = ImageIO.read(getClass().getResourceAsStream(carpetas[i] + "abajo_0001.png"));
				// Escalar a 3x para que se vea bien como preview
				int previewSize = pj.tamanioTile * 2;
				spritesPreview[i] = tool.escalarImagen(original, previewSize, previewSize);
			} catch (IOException e) {
				spritesPreview[i] = null;
			}
		}
	}

	/**
	 * Método principal de dibujado del HUD/UI.
	 */
	public void draw(Graphics2D g2) {
		this.g2 = g2;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (pj.gameState == pj.menuState) {
			dibujarMenu();
		} else if (pj.gameState == pj.seleccionState) {
			dibujarSeleccionPersonaje();
		} else if (pj.gameState == pj.ayudaState) {
			dibujarAyuda();
		} else if (pj.gameState == pj.logrosState) {
			dibujarLogros();
		} else if (pj.gameState == pj.creditosState) {
			dibujarCreditos();
		} else if (pj.gameState == pj.playState) {
			dibujarHUD();
		} else if (pj.gameState == pj.pauseState) {
			dibujarHUD();
			dibujarPantallaPausa();
		} else if (pj.gameState == pj.gameOverState) {
			dibujarGameOver();
		}
	}

	// ===================================================================
	// MENÚ PRINCIPAL
	// ===================================================================

	private void dibujarMenu() {
		int ancho = pj.anchoPantalla;
		int alto = pj.altoPantalla;

		// Fondo - Imagen o color sólido
		if (imagenFondoMenu != null) {
			// Dibujar imagen de fondo
			g2.drawImage(imagenFondoMenu, 0, 0, null);
		} else {
			// Fondo por defecto
			g2.setColor(COLOR_FONDO);
			g2.fillRect(0, 0, ancho, alto);

			// Líneas decorativas de fondo
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
		int xTitulo = obtenerXCentrado(titulo);
		// Sombra
		g2.setColor(new Color(80, 0, 0));
		g2.drawString(titulo, xTitulo + 3, 133);
		// Texto
		g2.setColor(COLOR_TITULO);
		g2.drawString(titulo, xTitulo, 130);

		// Subtítulo
		g2.setFont(fuentePequena);
		g2.setColor(COLOR_SUBTITULO);
		String sub = "Sobrevive. Evoluciona. Conquista.";
		g2.drawString(sub, obtenerXCentrado(sub), 165);

		// Botones del menú
		int botonAncho = 260;
		int botonAlto = 50;
		int xCentro = ancho / 2 - botonAncho / 2;
		int yInicio = 220;
		int espaciado = 70;

		String[] opciones = { "COMENZAR", "AYUDA", "LOGROS", "CRÉDITOS" };
		int menuOpcion = pj.kh.menuOpcion;

		for (int i = 0; i < opciones.length; i++) {
			int yBoton = yInicio + i * espaciado;
			boolean seleccionado = (i == menuOpcion);

			// Fondo del botón
			if (seleccionado) {
				g2.setColor(COLOR_BOTON_SELECCIONADO);
			} else {
				g2.setColor(COLOR_BOTON);
			}
			g2.fillRoundRect(xCentro, yBoton, botonAncho, botonAlto, 15, 15);

			// Borde
			if (seleccionado) {
				g2.setColor(COLOR_HIGHLIGHT);
				g2.drawRoundRect(xCentro - 1, yBoton - 1, botonAncho + 2, botonAlto + 2, 15, 15);
			} else {
				g2.setColor(COLOR_BORDE);
				g2.drawRoundRect(xCentro, yBoton, botonAncho, botonAlto, 15, 15);
			}

			// Texto del botón
			g2.setFont(fuenteBoton);
			g2.setColor(seleccionado ? COLOR_HIGHLIGHT : COLOR_TEXTO);
			FontMetrics fm = g2.getFontMetrics();
			int xTexto = xCentro + (botonAncho - fm.stringWidth(opciones[i])) / 2;
			int yTexto = yBoton + (botonAlto + fm.getAscent() - fm.getDescent()) / 2;
			g2.drawString(opciones[i], xTexto, yTexto);

			// Indicador de selección
			if (seleccionado) {
				g2.setColor(COLOR_HIGHLIGHT);
				g2.drawString("▶", xCentro - 30, yTexto);
			}
		}

		// Placeholder solo para Ayuda (Logros ya está implementado)
		if (menuOpcion == 1) {
			g2.setFont(fuentePequena);
			g2.setColor(new Color(255, 255, 100, 180));
			g2.drawString("(Próximamente)", obtenerXCentrado("(Próximamente)"),
					yInicio + menuOpcion * espaciado + botonAlto + 18);
		}

		// Instrucciones
		g2.setFont(fuentePequena);
		g2.setColor(new Color(150, 150, 170));
		String instrucciones = "W/S o ↑/↓ para navegar  •  ENTER para seleccionar";
		g2.drawString(instrucciones, obtenerXCentrado(instrucciones), alto - 30);
	}

	// ===================================================================
	// SELECCIÓN DE PERSONAJE
	// ===================================================================

	private void dibujarSeleccionPersonaje() {
		int ancho = pj.anchoPantalla;
		int alto = pj.altoPantalla;
		int seleccion = pj.kh.seleccionPersonaje;

		// Fondo
		g2.setColor(COLOR_FONDO);
		g2.fillRect(0, 0, ancho, alto);

		// Título
		g2.setFont(new Font("Arial", Font.BOLD, 42));
		g2.setColor(COLOR_TEXTO);
		String titulo = "Elegir personaje";
		g2.drawString(titulo, obtenerXCentrado(titulo), 60);

		// Panel principal
		int panelX = 80;
		int panelY = 90;
		int panelAncho = ancho - 160;
		int panelAlto = alto - 180;
		g2.setColor(COLOR_PANEL);
		g2.fillRoundRect(panelX, panelY, panelAncho, panelAlto, 20, 20);
		g2.setColor(COLOR_BORDE);
		g2.drawRoundRect(panelX, panelY, panelAncho, panelAlto, 20, 20);

		// Tarjetas de personajes
		int tarjetaAncho = (panelAncho - 80) / 3;
		int tarjetaAlto = panelAlto - 120;
		int tarjetaY = panelY + 20;

		for (int i = 0; i < 3; i++) {
			int tarjetaX = panelX + 20 + i * (tarjetaAncho + 20);
			boolean seleccionado = (i == seleccion);

			// Fondo de tarjeta
			if (seleccionado) {
				g2.setColor(new Color(COLORES_PERSONAJE[i].getRed(),
						COLORES_PERSONAJE[i].getGreen(),
						COLORES_PERSONAJE[i].getBlue(), 40));
			} else {
				g2.setColor(new Color(20, 20, 40, 200));
			}
			g2.fillRoundRect(tarjetaX, tarjetaY, tarjetaAncho, tarjetaAlto, 15, 15);

			// Borde
			if (seleccionado) {
				g2.setColor(COLORES_PERSONAJE[i]);
				g2.drawRoundRect(tarjetaX - 1, tarjetaY - 1, tarjetaAncho + 2, tarjetaAlto + 2, 15, 15);
				g2.drawRoundRect(tarjetaX - 2, tarjetaY - 2, tarjetaAncho + 4, tarjetaAlto + 4, 15, 15);
			} else {
				g2.setColor(new Color(80, 80, 100));
				g2.drawRoundRect(tarjetaX, tarjetaY, tarjetaAncho, tarjetaAlto, 15, 15);
			}

			int contenidoX = tarjetaX + 15;
			int contenidoY = tarjetaY + 30;

			// Nombre del personaje
			g2.setFont(new Font("Arial", Font.BOLD, 22));
			g2.setColor(seleccionado ? COLORES_PERSONAJE[i] : COLOR_TEXTO);
			g2.drawString(NOMBRES_PERSONAJES[i], contenidoX, contenidoY);
			contenidoY += 25;

			// Rango
			g2.setFont(fuentePequena);
			g2.setColor(new Color(180, 180, 200));
			g2.drawString(RANGOS[i], contenidoX, contenidoY);
			contenidoY += 30;

			// Preview del personaje con sprite real
			int previewSize = pj.tamanioTile * 2;
			int previewX = tarjetaX + (tarjetaAncho - previewSize) / 2;
			g2.setColor(new Color(15, 15, 30));
			g2.fillRect(previewX, contenidoY, previewSize, previewSize);
			g2.setColor(COLORES_PERSONAJE[i]);
			g2.drawRect(previewX, contenidoY, previewSize, previewSize);
			if (spritesPreview[i] != null) {
				g2.drawImage(spritesPreview[i], previewX, contenidoY, null);
			}
			contenidoY += previewSize + 20;

			// Descripción
			g2.setFont(fuentePequena);
			g2.setColor(new Color(180, 180, 200));
			g2.drawString(DESCRIPCIONES[i], contenidoX, contenidoY);
			contenidoY += 30;

			// Stats
			g2.setFont(fuenteStats);
			dibujarStat(contenidoX, contenidoY, "❤ Vida:", STATS[i][0], 30, Color.GREEN);
			contenidoY += 25;
			dibujarStat(contenidoX, contenidoY, "⚔ Ataque:", STATS[i][1], 20, Color.ORANGE);
			contenidoY += 25;
			dibujarStat(contenidoX, contenidoY, "🛡 Defensa:", STATS[i][2], 10, Color.CYAN);
			contenidoY += 25;
			dibujarStat(contenidoX, contenidoY, "⚡ Velocidad:", STATS[i][3], 5, Color.YELLOW);

			// Etiqueta "SELECCIONADO"
			if (seleccionado) {
				g2.setFont(fuentePequena);
				g2.setColor(COLOR_HIGHLIGHT);
				String sel = "▼ SELECCIONADO ▼";
				int xSel = tarjetaX + (tarjetaAncho - g2.getFontMetrics().stringWidth(sel)) / 2;
				g2.drawString(sel, xSel, tarjetaY + tarjetaAlto - 10);
			}
		}

		// Botón confirmar
		int btnAncho = 200;
		int btnAlto = 45;
		int btnX = ancho / 2 - btnAncho / 2;
		int btnY = panelY + panelAlto + 15;
		g2.setColor(new Color(50, 150, 50));
		g2.fillRoundRect(btnX, btnY, btnAncho, btnAlto, 12, 12);
		g2.setColor(new Color(100, 255, 100));
		g2.drawRoundRect(btnX, btnY, btnAncho, btnAlto, 12, 12);
		g2.setFont(fuenteBoton);
		g2.setColor(Color.WHITE);
		String confirmar = "CONFIRMAR";
		FontMetrics fm = g2.getFontMetrics();
		g2.drawString(confirmar, btnX + (btnAncho - fm.stringWidth(confirmar)) / 2,
				btnY + (btnAlto + fm.getAscent() - fm.getDescent()) / 2);

		// Instrucciones
		g2.setFont(fuentePequena);
		g2.setColor(new Color(150, 150, 170));
		String instr = "A/D o ←/→ para elegir  •  ENTER para confirmar  •  ESC para volver";
		g2.drawString(instr, obtenerXCentrado(instr), alto - 10);
	}

	/**
	 * Dibuja una barra de stat con etiqueta y valor.
	 */
	private void dibujarStat(int x, int y, String etiqueta, int valor, int max, Color color) {
		g2.setColor(COLOR_TEXTO);
		g2.drawString(etiqueta, x, y);

		int barraX = x + 115;
		int barraAncho = 60;
		int barraAlto = 12;

		// Fondo de barra
		g2.setColor(new Color(40, 40, 60));
		g2.fillRect(barraX, y - 11, barraAncho, barraAlto);

		// Barra de valor
		int anchoProporcional = (int) ((double) valor / max * barraAncho);
		g2.setColor(color);
		g2.fillRect(barraX, y - 11, anchoProporcional, barraAlto);

		// Borde
		g2.setColor(new Color(80, 80, 100));
		g2.drawRect(barraX, y - 11, barraAncho, barraAlto);
	}

	// ===================================================================
	// AYUDA
	// ===================================================================

	private void dibujarAyuda() {
		int ancho = pj.anchoPantalla;
		int alto = pj.altoPantalla;

		// Fondo - Imagen o color sólido
		if (imagenAyuda != null) {
			// Dibujar imagen de ayuda
			g2.drawImage(imagenAyuda, 0, 0, null);
		} else {
			// Fondo por defecto
			g2.setColor(COLOR_FONDO);
			g2.fillRect(0, 0, ancho, alto);

			// Título
			g2.setFont(new Font("Arial", Font.BOLD, 48));
			g2.setColor(COLOR_TITULO);
			String titulo = "AYUDA";
			g2.drawString(titulo, obtenerXCentrado(titulo), 80);

			// Panel
			int panelAncho = 800;
			int panelAlto = 500;
			int panelX = ancho / 2 - panelAncho / 2;
			int panelY = 130;
			g2.setColor(COLOR_PANEL);
			g2.fillRoundRect(panelX, panelY, panelAncho, panelAlto, 20, 20);
			g2.setColor(COLOR_BORDE);
			g2.drawRoundRect(panelX, panelY, panelAncho, panelAlto, 20, 20);

			// Contenido
			int x = panelX + 40;
			int y = panelY + 50;
			int espaciado = 35;

			g2.setFont(new Font("Arial", Font.BOLD, 24));
			g2.setColor(COLOR_SUBTITULO);
			g2.drawString("CONTROLES", x, y);
			y += espaciado + 10;

			g2.setFont(fuenteStats);
			g2.setColor(COLOR_TEXTO);
			g2.drawString("• WASD o Flechas: Mover personaje", x + 20, y);
			y += espaciado;
			g2.drawString("• P: Pausar juego", x + 20, y);
			y += espaciado;
			g2.drawString("• ESC: Volver al menú / Cerrar pantalla", x + 20, y);
			y += espaciado + 15;

			g2.setFont(new Font("Arial", Font.BOLD, 24));
			g2.setColor(COLOR_SUBTITULO);
			g2.drawString("MECÁNICAS", x, y);
			y += espaciado + 10;

			g2.setFont(fuenteStats);
			g2.setColor(COLOR_TEXTO);
			g2.drawString("• Sobrevive el mayor tiempo posible", x + 20, y);
			y += espaciado;
			g2.drawString("• Elimina enemigos para ganar experiencia", x + 20, y);
			y += espaciado;
			g2.drawString("• Recoge cofres para obtener power-ups", x + 20, y);
			y += espaciado;
			g2.drawString("• Los enemigos aparecen continuamente", x + 20, y);
			y += espaciado + 15;

			g2.setFont(new Font("Arial", Font.BOLD, 24));
			g2.setColor(COLOR_SUBTITULO);
			g2.drawString("POWER-UPS", x, y);
			y += espaciado + 10;

			g2.setFont(fuenteStats);
			g2.setColor(new Color(100, 255, 255));
			g2.drawString("🛡 Invencibilidad: Inmune al daño temporalmente", x + 20, y);
			y += espaciado;
			g2.setColor(Color.YELLOW);
			g2.drawString("⚡ Velocidad: Movimiento más rápido", x + 20, y);
			y += espaciado;
			g2.setColor(new Color(255, 100, 100));
			g2.drawString("💪 Ataque: Mayor daño a enemigos", x + 20, y);
		}

		// Instrucciones
		g2.setFont(fuentePequena);
		g2.setColor(new Color(150, 150, 170));
		String instrucciones = "Presiona ESC o ENTER para volver";
		g2.drawString(instrucciones, obtenerXCentrado(instrucciones), alto - 30);
	}

	// ===================================================================
	// LOGROS
	// ===================================================================

	private void dibujarLogros() {
		int ancho = pj.anchoPantalla;
		int alto = pj.altoPantalla;

		// Fondo
		g2.setColor(COLOR_FONDO);
		g2.fillRect(0, 0, ancho, alto);

		// Título
		g2.setFont(new Font("Arial", Font.BOLD, 48));
		g2.setColor(COLOR_TITULO);
		String titulo = "LOGROS Y ESTADÍSTICAS";
		g2.drawString(titulo, obtenerXCentrado(titulo), 80);

		// Panel principal
		int panelAncho = 850;
		int panelAlto = 520;
		int panelX = ancho / 2 - panelAncho / 2;
		int panelY = 120;
		g2.setColor(COLOR_PANEL);
		g2.fillRoundRect(panelX, panelY, panelAncho, panelAlto, 20, 20);
		g2.setColor(COLOR_BORDE);
		g2.drawRoundRect(panelX, panelY, panelAncho, panelAlto, 20, 20);

		// Contenido
		int x = panelX + 60;
		int y = panelY + 60;
		int espaciado = 50;

		// Récord de tiempo
		g2.setFont(new Font("Arial", Font.BOLD, 28));
		g2.setColor(COLOR_HIGHLIGHT);
		g2.drawString("🏆 RÉCORD DE TIEMPO", x, y);
		y += 35;

		g2.setFont(new Font("Arial", Font.BOLD, 48));
		g2.setColor(new Color(255, 215, 0));
		String tiempoRecord = pj.stats.formatearTiempo(GameStats.recordTiempoSobrevivido);
		g2.drawString(tiempoRecord, obtenerXCentrado(tiempoRecord), y);
		y += espaciado + 20;

		// Línea divisoria
		g2.setColor(COLOR_BORDE);
		g2.drawLine(panelX + 40, y - 10, panelX + panelAncho - 40, y - 10);
		y += 10;

		// Estadísticas adicionales
		g2.setFont(new Font("Arial", Font.BOLD, 24));
		g2.setColor(COLOR_SUBTITULO);
		g2.drawString("ESTADÍSTICAS ACUMULADAS", x, y);
		y += 40;

		// Crear estadísticas acumuladas (podrían ser estáticas en GameStats)
		g2.setFont(new Font("Arial", Font.BOLD, 20));
		g2.setColor(COLOR_TEXTO);
		
		// Enemigos totales eliminados
		g2.setColor(new Color(255, 100, 100));
		g2.drawString("⚔ Enemigos Eliminados Totales:", x, y);
		g2.setColor(Color.WHITE);
		g2.drawString(String.valueOf(GameStats.enemigosTotalesEliminados), x + 450, y);
		y += espaciado - 5;

		// Cofres totales recogidos
		g2.setColor(new Color(255, 215, 0));
		g2.drawString("📦 Cofres Recogidos Totales:", x, y);
		g2.setColor(Color.WHITE);
		g2.drawString(String.valueOf(GameStats.cofresTotalesRecogidos), x + 450, y);
		y += espaciado - 5;

		// Partidas jugadas
		g2.setColor(new Color(100, 200, 255));
		g2.drawString("🎮 Partidas Jugadas:", x, y);
		g2.setColor(Color.WHITE);
		g2.drawString(String.valueOf(GameStats.partidasJugadas), x + 450, y);
		y += espaciado - 5;

		// Nivel máximo alcanzado
		g2.setColor(new Color(180, 100, 255));
		g2.drawString("⭐ Nivel Máximo Alcanzado:", x, y);
		g2.setColor(Color.WHITE);
		g2.drawString(String.valueOf(GameStats.nivelMaximoAlcanzado), x + 450, y);
		y += espaciado - 5;

		// Daño total recibido
		g2.setColor(new Color(255, 150, 50));
		g2.drawString("💔 Daño Total Recibido:", x, y);
		g2.setColor(Color.WHITE);
		g2.drawString(String.valueOf(GameStats.danioTotalRecibidoAcumulado), x + 450, y);

		// Instrucciones
		g2.setFont(fuentePequena);
		g2.setColor(new Color(150, 150, 170));
		String instrucciones = "Presiona ESC o ENTER para volver";
		g2.drawString(instrucciones, obtenerXCentrado(instrucciones), alto - 30);
	}

	// ===================================================================
	// CRÉDITOS
	// ===================================================================

	private void dibujarCreditos() {
		int ancho = pj.anchoPantalla;
		int alto = pj.altoPantalla;

		// Fondo
		g2.setColor(COLOR_FONDO);
		g2.fillRect(0, 0, ancho, alto);

		// Título
		g2.setFont(new Font("Arial", Font.BOLD, 48));
		g2.setColor(COLOR_TITULO);
		String titulo = "CRÉDITOS";
		g2.drawString(titulo, obtenerXCentrado(titulo), 120);

		// Panel
		int panelAncho = 400;
		int panelAlto = 300;
		int panelX = ancho / 2 - panelAncho / 2;
		int panelY = 160;
		g2.setColor(COLOR_PANEL);
		g2.fillRoundRect(panelX, panelY, panelAncho, panelAlto, 20, 20);
		g2.setColor(COLOR_BORDE);
		g2.drawRoundRect(panelX, panelY, panelAncho, panelAlto, 20, 20);

		// Subtítulo
		g2.setFont(new Font("Arial", Font.BOLD, 22));
		g2.setColor(COLOR_SUBTITULO);
		String equipo = "Equipo de Desarrollo";
		g2.drawString(equipo, obtenerXCentrado(equipo), panelY + 45);

		// Línea decorativa
		g2.setColor(COLOR_BORDE);
		g2.drawLine(panelX + 40, panelY + 60, panelX + panelAncho - 40, panelY + 60);

		// Nombres
		String[] nombres = { "Mauricio", "Jack", "Angela", "Melissa" };
		Color[] coloresNombre = {
				new Color(255, 120, 120),
				new Color(120, 200, 255),
				new Color(255, 200, 100),
				new Color(180, 255, 150)
		};

		g2.setFont(fuenteCreditos);
		int yNombre = panelY + 100;
		int espaciado = 45;

		for (int i = 0; i < nombres.length; i++) {
			g2.setColor(coloresNombre[i]);
			String nombre = "★  " + nombres[i];
			g2.drawString(nombre, obtenerXCentrado(nombre), yNombre + i * espaciado);
		}

		// Instrucciones
		g2.setFont(fuentePequena);
		g2.setColor(new Color(150, 150, 170));
		String instr = "Presiona ENTER o ESC para volver";
		g2.drawString(instr, obtenerXCentrado(instr), alto - 40);
	}

	// ===================================================================
	// HUD (En juego)
	// ===================================================================

	public void dibujarHUD() {
		// ===== Barra de vida del jugador =====
		int xBarra = 20;
		int yBarra = 20;
		int anchoBarraMax = 200;
		int altoBarra = 20;

		g2.setColor(Color.RED);
		g2.fillRect(xBarra, yBarra, anchoBarraMax, altoBarra);

		int anchoVida = (int) ((double) pj.jugador.vidaActual / pj.jugador.vidaMaxima * anchoBarraMax);
		g2.setColor(Color.GREEN);
		g2.fillRect(xBarra, yBarra, anchoVida, altoBarra);

		g2.setColor(Color.WHITE);
		g2.drawRect(xBarra, yBarra, anchoBarraMax, altoBarra);

		g2.setFont(new Font("Arial", Font.BOLD, 16));
		g2.drawString(pj.jugador.vidaActual + " / " + pj.jugador.vidaMaxima, xBarra + 5, yBarra + 15);

		// ===== Panel de estadísticas =====
		int panelX = 20;
		int panelY = 50;
		int panelAncho = 250;
		int panelAlto = 120;

		g2.setColor(new Color(0, 0, 0, 150));
		g2.fillRoundRect(panelX, panelY, panelAncho, panelAlto, 10, 10);
		g2.setColor(Color.WHITE);
		g2.drawRoundRect(panelX, panelY, panelAncho, panelAlto, 10, 10);

		g2.setFont(new Font("Arial", Font.BOLD, 18));
		g2.setColor(Color.WHITE);
		g2.drawString("⚔ Enemigos: " + pj.contadorNPCs, panelX + 10, panelY + 25);

		g2.drawString("Nivel: " + pj.stats.nivel, panelX + 10, panelY + 50);
		g2.setFont(new Font("Arial", Font.PLAIN, 14));
		g2.drawString("XP: " + pj.stats.experiencia + "/" + pj.stats.experienciaSiguienteNivel, panelX + 10,
				panelY + 70);

		g2.setFont(new Font("Arial", Font.BOLD, 16));
		g2.drawString("⏱ " + pj.stats.formatearTiempo(pj.stats.tiempoSobrevivido), panelX + 10, panelY + 92);

		// Nombre del personaje
		g2.setFont(new Font("Arial", Font.BOLD, 14));
		g2.setColor(new Color(180, 140, 255));
		g2.drawString("🎮 " + pj.jugador.tipoPersonaje, panelX + 10, panelY + 112);

		// ===== Power-ups activos =====
		int yPowerUp = pj.altoPantalla - 40;
		g2.setFont(new Font("Arial", Font.BOLD, 16));

		if (pj.jugador.powerUps.invencibilidadActiva) {
			g2.setColor(Color.CYAN);
			g2.drawString("🛡 Invencible (" + pj.jugador.powerUps.getTiempoInvencibilidad() + "s)", 20, yPowerUp);
			yPowerUp -= 25;
		}
		if (pj.jugador.powerUps.velocidadAumentada) {
			g2.setColor(Color.YELLOW);
			g2.drawString("⚡ Velocidad (" + pj.jugador.powerUps.getTiempoVelocidad() + "s)", 20, yPowerUp);
			yPowerUp -= 25;
		}
		if (pj.jugador.powerUps.ataqueAumentado) {
			g2.setColor(Color.RED);
			g2.drawString("💪 Ataque (" + pj.jugador.powerUps.getTiempoAtaque() + "s)", 20, yPowerUp);
		}

		// ===== Notificaciones =====
		dibujarNotificaciones();
	}

	// ===================================================================
	// PAUSA
	// ===================================================================

	public void dibujarPantallaPausa() {
		g2.setColor(new Color(0, 0, 0, 150));
		g2.fillRect(0, 0, pj.anchoPantalla, pj.altoPantalla);

		g2.setFont(arial_40);
		g2.setColor(Color.white);

		String texto = "PAUSADO";
		int x = obtenerXCentrado(texto);
		int y = pj.altoPantalla / 2;
		g2.drawString(texto, x, y);
	}

	// ===================================================================
	// GAME OVER
	// ===================================================================

	public void dibujarGameOver() {
		g2.setColor(new Color(0, 0, 0, 200));
		g2.fillRect(0, 0, pj.anchoPantalla, pj.altoPantalla);

		g2.setFont(arial_80B);
		String textoMuerte = "GAME OVER";
		int x = obtenerXCentrado(textoMuerte);
		int y = 150;

		g2.setColor(Color.BLACK);
		g2.drawString(textoMuerte, x + 5, y + 5);
		g2.setColor(Color.RED);
		g2.drawString(textoMuerte, x, y);

		g2.setFont(new Font("Arial", Font.BOLD, 30));
		g2.setColor(Color.WHITE);

		int yEstadistica = 250;
		int espaciado = 40;

		String tiempoTexto = "Tiempo sobrevivido: " + pj.stats.formatearTiempo(pj.stats.tiempoSobrevivido);
		g2.drawString(tiempoTexto, obtenerXCentrado(tiempoTexto), yEstadistica);
		yEstadistica += espaciado;

		if (pj.stats.nuevoRecord) {
			g2.setColor(Color.YELLOW);
			String recordTexto = "¡NUEVO RÉCORD!";
			g2.drawString(recordTexto, obtenerXCentrado(recordTexto), yEstadistica);
			g2.setColor(Color.WHITE);
		} else {
			String recordTexto = "Récord: " + pj.stats.formatearTiempo(GameStats.recordTiempoSobrevivido);
			g2.drawString(recordTexto, obtenerXCentrado(recordTexto), yEstadistica);
		}
		yEstadistica += espaciado;

		String enemigoTexto = "Enemigos eliminados: " + pj.stats.enemigosDerrotados;
		g2.drawString(enemigoTexto, obtenerXCentrado(enemigoTexto), yEstadistica);
		yEstadistica += espaciado;

		String ataquesTexto = "Ataques recibidos: " + pj.stats.ataquesRecibidos;
		g2.drawString(ataquesTexto, obtenerXCentrado(ataquesTexto), yEstadistica);
		yEstadistica += espaciado;

		String nivelTexto = "Nivel alcanzado: " + pj.stats.nivel;
		g2.drawString(nivelTexto, obtenerXCentrado(nivelTexto), yEstadistica);
		yEstadistica += espaciado;

		String cofresTexto = "Cofres recogidos: " + pj.stats.cofresRecogidos;
		g2.drawString(cofresTexto, obtenerXCentrado(cofresTexto), yEstadistica);
		yEstadistica += espaciado + 30;

		g2.setFont(new Font("Arial", Font.PLAIN, 20));
		g2.setColor(Color.LIGHT_GRAY);
		String instruccion = "Presiona R para volver al menú";
		g2.drawString(instruccion, obtenerXCentrado(instruccion), yEstadistica);
	}

	// ===================================================================
	// NOTIFICACIONES
	// ===================================================================

	private void dibujarNotificaciones() {
		int notifX = pj.anchoPantalla - 320;
		int notifY = 20;
		int espaciado = 35;

		int maxNotif = Math.min(5, pj.notificaciones.size());
		int inicio = Math.max(0, pj.notificaciones.size() - maxNotif);

		for (int i = inicio; i < pj.notificaciones.size(); i++) {
			Notificacion notif = pj.notificaciones.get(i);

			float opacidad = notif.getOpacidad();
			Color colorConAlpha = new Color(
					notif.color.getRed(),
					notif.color.getGreen(),
					notif.color.getBlue(),
					(int) (255 * opacidad));

			g2.setColor(new Color(0, 0, 0, (int) (180 * opacidad)));
			g2.fillRoundRect(notifX - 5, notifY - 20, 310, 30, 8, 8);

			g2.setFont(new Font("Arial", Font.BOLD, 16));
			g2.setColor(colorConAlpha);
			g2.drawString(notif.mensaje, notifX, notifY);

			notifY += espaciado;
		}
	}

	// ===================================================================
	// UTILIDADES
	// ===================================================================

	public int obtenerXCentrado(String texto) {
		int longitudTexto = (int) g2.getFontMetrics().getStringBounds(texto, g2).getWidth();
		return (pj.anchoPantalla / 2) - (longitudTexto / 2);
	}
}
