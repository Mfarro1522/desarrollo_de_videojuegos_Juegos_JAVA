package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

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
			// HUD del juego (vida, enemigos, etc.)
			dibujarHUD();
			
		} else if (pj.gameState == pj.pauseState) {
			dibujarHUD(); // Mantener HUD visible en pausa
			dibujarPantallaPausa();
		} else if (pj.gameState == pj.gameOverState) {
			// Pantalla de Game Over (corregido el bug)
			dibujarGameOver();
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
	
	/**
	 * Dibuja el HUD del jugador (vida, contador de NPCs).
	 */
	public void dibujarHUD() {
		// ===== Barra de vida del jugador =====
		int xBarra = 20;
		int yBarra = 20;
		int anchoBarraMax = 200;
		int altoBarra = 20;
		
		// Fondo (rojo)
		g2.setColor(Color.RED);
		g2.fillRect(xBarra, yBarra, anchoBarraMax, altoBarra);
		
		// Vida actual (verde)
		int anchoVida = (int)((double)pj.jugador.vidaActual / pj.jugador.vidaMaxima * anchoBarraMax);
		g2.setColor(Color.GREEN);
		g2.fillRect(xBarra, yBarra, anchoVida, altoBarra);
		
		// Borde
		g2.setColor(Color.WHITE);
		g2.drawRect(xBarra, yBarra, anchoBarraMax, altoBarra);
		
		// Texto de vida
		g2.setFont(new Font("Arial", Font.BOLD, 16));
		g2.drawString(pj.jugador.vidaActual + " / " + pj.jugador.vidaMaxima, xBarra + 5, yBarra + 15);
		
		// ===== Panel de estadísticas =====
		int panelX = 20;
		int panelY = 50;
		int panelAncho = 250;
		int panelAlto = 100;
		
		// Fondo del panel con transparencia
		g2.setColor(new Color(0, 0, 0, 150));
		g2.fillRoundRect(panelX, panelY, panelAncho, panelAlto, 10, 10);
		g2.setColor(Color.WHITE);
		g2.drawRoundRect(panelX, panelY, panelAncho, panelAlto, 10, 10);
		
		// Contenido del panel
		g2.setFont(new Font("Arial", Font.BOLD, 18));
		g2.setColor(Color.WHITE);
		g2.drawString("⚔ Enemigos: " + pj.contadorNPCs, panelX + 10, panelY + 25);
		
		g2.drawString("Nivel: " + pj.stats.nivel, panelX + 10, panelY + 50);
		g2.setFont(new Font("Arial", Font.PLAIN, 14));
		g2.drawString("XP: " + pj.stats.experiencia + "/" + pj.stats.experienciaSiguienteNivel, panelX + 10, panelY + 70);
		
		g2.setFont(new Font("Arial", Font.BOLD, 16));
		g2.drawString("⏱ " + pj.stats.formatearTiempo(pj.stats.tiempoSobrevivido), panelX + 10, panelY + 92);
		
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
		
		// ===== Panel de notificaciones =====
		dibujarNotificaciones();
	}
	
	/**
	 * Dibuja el panel de notificaciones en la parte superior derecha.
	 */
	private void dibujarNotificaciones() {
		int notifX = pj.anchoPantalla - 320; // Margen derecho
		int notifY = 20; // Margen superior
		int espaciado = 35;
		
		// Mostrar las últimas 5 notificaciones
		int maxNotif = Math.min(5, pj.notificaciones.size());
		int inicio = Math.max(0, pj.notificaciones.size() - maxNotif);
		
		for (int i = inicio; i < pj.notificaciones.size(); i++) {
			Notificacion notif = pj.notificaciones.get(i);
			
			float opacidad = notif.getOpacidad();
			Color colorConAlpha = new Color(
				notif.color.getRed(),
				notif.color.getGreen(),
				notif.color.getBlue(),
				(int)(255 * opacidad)
			);
			
			// Fondo semi-transparente
			g2.setColor(new Color(0, 0, 0, (int)(180 * opacidad)));
			g2.fillRoundRect(notifX - 5, notifY - 20, 310, 30, 8, 8);
			
			// Texto de la notificación
			g2.setFont(new Font("Arial", Font.BOLD, 16));
			g2.setColor(colorConAlpha);
			g2.drawString(notif.mensaje, notifX, notifY);
			
			notifY += espaciado;
		}
	}
	
	/**
	 * Dibuja la pantalla de Game Over con estadísticas
	 */
	public void dibujarGameOver() {
		// Fondo semitransparente
		g2.setColor(new Color(0, 0, 0, 200));
		g2.fillRect(0, 0, pj.anchoPantalla, pj.altoPantalla);
		
		// ===== GAME OVER =====
		g2.setFont(arial_80B);
		String textoMuerte = "GAME OVER";
		int x = obtenerXCentrado(textoMuerte);
		int y = 150;
		
		// Sombra
		g2.setColor(Color.BLACK);
		g2.drawString(textoMuerte, x + 5, y + 5);
		
		// Texto principal
		g2.setColor(Color.RED);
		g2.drawString(textoMuerte, x, y);
		
		// ===== ESTADÍSTICAS =====
		g2.setFont(new Font("Arial", Font.BOLD, 30));
		g2.setColor(Color.WHITE);
		
		int yEstadistica = 250;
		int espaciado = 40;
		
		// Tiempo sobrevivido
		String tiempoTexto = "Tiempo sobrevivido: " + pj.stats.formatearTiempo(pj.stats.tiempoSobrevivido);
		g2.drawString(tiempoTexto, obtenerXCentrado(tiempoTexto), yEstadistica);
		yEstadistica += espaciado;
		
		// Récord
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
		
		// Enemigos derrotados
		String enemigoTexto = "Enemigos eliminados: " + pj.stats.enemigosDerrotados;
		g2.drawString(enemigoTexto, obtenerXCentrado(enemigoTexto), yEstadistica);
		yEstadistica += espaciado;
		
		// Ataques recibidos
		String ataquesTexto = "Ataques recibidos: " + pj.stats.ataquesRecibidos;
		g2.drawString(ataquesTexto, obtenerXCentrado(ataquesTexto), yEstadistica);
		yEstadistica += espaciado;
		
		// Nivel alcanzado
		String nivelTexto = "Nivel alcanzado: " + pj.stats.nivel;
		g2.drawString(nivelTexto, obtenerXCentrado(nivelTexto), yEstadistica);
		yEstadistica += espaciado;
		
		// Cofres recogidos
		String cofresTexto = "Cofres recogidos: " + pj.stats.cofresRecogidos;
		g2.drawString(cofresTexto, obtenerXCentrado(cofresTexto), yEstadistica);
		yEstadistica += espaciado + 30;
		
		// Instrucciones
		g2.setFont(new Font("Arial", Font.PLAIN, 20));
		g2.setColor(Color.LIGHT_GRAY);
		String instruccion = "Presiona R para reiniciar";
		g2.drawString(instruccion, obtenerXCentrado(instruccion), yEstadistica);
	}
}
