package main;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Manejador de eventos de mouse para los menús del juego.
 * Usa las coordenadas centralizadas de MenuPrincipal para detección correcta.
 */
public class MouseHandler implements MouseListener, MouseMotionListener {

	PanelJuego pj;
	keyHandler kh;

	// Posición actual del mouse
	public int mouseX = 0;
	public int mouseY = 0;

	public MouseHandler(PanelJuego pj, keyHandler kh) {
		this.pj = pj;
		this.kh = kh;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();

		// MENÚ PRINCIPAL
		if (pj.gameState == pj.menuState) {
			manejarClickMenu(x, y);
		}
		// SELECCIÓN DE PERSONAJE
		else if (pj.gameState == pj.seleccionState) {
			manejarClickSeleccion(x, y);
		}
		// AYUDA
		else if (pj.gameState == pj.ayudaState) {
			pj.gameState = pj.menuState;
		}
		// LOGROS
		else if (pj.gameState == pj.logrosState) {
			pj.gameState = pj.menuState;
		}
		// CRÉDITOS
		else if (pj.gameState == pj.creditosState) {
			pj.gameState = pj.menuState;
		}
		// GAME OVER
		else if (pj.gameState == pj.gameOverState) {
			pj.reiniciarJuego();
		}
	}

	/**
	 * Maneja clicks en el menú principal usando las coordenadas de MenuPrincipal.
	 */
	private void manejarClickMenu(int x, int y) {
		MenuPrincipal menu = pj.ui.menuPrincipal;
		int boton = menu.getBotonEnPosicion(x, y);

		if (boton >= 0) {
			switch (boton) {
				case 0: // Comenzar
					pj.gameState = pj.seleccionState;
					kh.seleccionPersonaje = 0;
					break;
				case 1: // Ayuda
					pj.gameState = pj.ayudaState;
					break;
				case 2: // Logros
					pj.gameState = pj.logrosState;
					break;
				case 3: // Créditos
					pj.gameState = pj.creditosState;
					break;
			}
		}
	}

	/**
	 * Maneja clicks en la selección de personaje.
	 */
	private void manejarClickSeleccion(int x, int y) {
		int ancho = pj.anchoPantalla;
		int alto = pj.altoPantalla;

		// Panel de personajes (mismas coordenadas que UI.java)
		int panelX = 80;
		int panelY = 90;
		int panelAncho = ancho - 160;
		int panelAlto = alto - 180;

		// Tarjetas
		int tarjetaAncho = (panelAncho - 80) / 3;
		int tarjetaAlto = panelAlto - 120;
		int tarjetaY = panelY + 20;

		// Verificar click en tarjetas de personaje
		for (int i = 0; i < 3; i++) {
			int tarjetaX = panelX + 20 + i * (tarjetaAncho + 20);
			if (x >= tarjetaX && x <= tarjetaX + tarjetaAncho &&
					y >= tarjetaY && y <= tarjetaY + tarjetaAlto) {
				kh.seleccionPersonaje = i;
				break;
			}
		}

		// Botón confirmar
		int btnAncho = 200;
		int btnAlto = 45;
		int btnX = ancho / 2 - btnAncho / 2;
		int btnY = panelY + panelAlto + 15;

		if (x >= btnX && x <= btnX + btnAncho &&
				y >= btnY && y <= btnY + btnAlto) {
			pj.iniciarJuego(keyHandler.PERSONAJES[kh.seleccionPersonaje]);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();

		// Actualizar hover en menú principal
		if (pj.gameState == pj.menuState) {
			actualizarHoverMenu();
		}
	}

	/**
	 * Actualiza la opción seleccionada del menú según posición del mouse.
	 * Usa las coordenadas centralizadas de MenuPrincipal.
	 */
	private void actualizarHoverMenu() {
		MenuPrincipal menu = pj.ui.menuPrincipal;
		int boton = menu.getBotonEnPosicion(mouseX, mouseY);
		if (boton >= 0) {
			kh.menuOpcion = boton;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}
}
