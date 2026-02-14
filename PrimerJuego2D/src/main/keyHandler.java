package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Manejador de eventos de teclado. Controla el estado de las teclas
 * para movimiento del jugador y navegación de menús.
 */
public class keyHandler implements KeyListener {

	PanelJuego pj;

	// Teclas de movimiento
	public boolean arribaPres, abajoPres, izqPres, drchPres;

	// Navegación de menú
	public int menuOpcion = 0; // 0=Comenzar, 1=Colección, 2=Logros, 3=Créditos
	public int seleccionPersonaje = 0; // 0=Sideral, 1=Mago, 2=Doom
	public boolean enterPresionado = false; // Flag de confirmación

	// Nombres de personajes (para uso en UI y Jugador)
	public static final String[] PERSONAJES = { "Sideral", "Mago", "Doom" };

	public keyHandler(PanelJuego pj) {
		this.pj = pj;
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	/**
	 * Se ejecuta cuando una tecla es presionada.
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		int keycode = e.getKeyCode();

		// ===== MENÚ PRINCIPAL =====
		if (pj.gameState == pj.menuState) {
			manejarInputMenu(keycode);

			// ===== SELECCIÓN DE PERSONAJE =====
		} else if (pj.gameState == pj.seleccionState) {
			manejarInputSeleccion(keycode);

			// ===== CRÉDITOS =====
		} else if (pj.gameState == pj.creditosState) {
			manejarInputCreditos(keycode);

			// ===== JUGANDO =====
		} else if (pj.gameState == pj.playState) {
			manejarInputJuego(keycode);

			// ===== PAUSA =====
		} else if (pj.gameState == pj.pauseState) {
			if (keycode == KeyEvent.VK_P) {
				pj.gameState = pj.playState;
			}

			// ===== GAME OVER =====
		} else if (pj.gameState == pj.gameOverState) {
			if (keycode == KeyEvent.VK_R) {
				pj.reiniciarJuego();
			}
		}
	}

	// ===== Input del menú principal =====
	private void manejarInputMenu(int keycode) {
		// Navegar arriba/abajo
		if (keycode == KeyEvent.VK_W || keycode == KeyEvent.VK_UP) {
			menuOpcion--;
			if (menuOpcion < 0) {
				menuOpcion = 3;
			}
		}
		if (keycode == KeyEvent.VK_S || keycode == KeyEvent.VK_DOWN) {
			menuOpcion++;
			if (menuOpcion > 3) {
				menuOpcion = 0;
			}
		}

		// Confirmar
		if (keycode == KeyEvent.VK_ENTER) {
			switch (menuOpcion) {
				case 0: // Comenzar
					pj.gameState = pj.seleccionState;
					seleccionPersonaje = 0;
					break;
				case 1: // Colección (placeholder)
					// TODO: Implementar pantalla de colección
					break;
				case 2: // Logros (placeholder)
					// TODO: Implementar pantalla de logros
					break;
				case 3: // Créditos
					pj.gameState = pj.creditosState;
					break;
			}
		}
	}

	// ===== Input de selección de personaje =====
	private void manejarInputSeleccion(int keycode) {
		// Navegar izquierda/derecha entre personajes
		if (keycode == KeyEvent.VK_A || keycode == KeyEvent.VK_LEFT) {
			seleccionPersonaje--;
			if (seleccionPersonaje < 0) {
				seleccionPersonaje = 2;
			}
		}
		if (keycode == KeyEvent.VK_D || keycode == KeyEvent.VK_RIGHT) {
			seleccionPersonaje++;
			if (seleccionPersonaje > 2) {
				seleccionPersonaje = 0;
			}
		}

		// Confirmar selección
		if (keycode == KeyEvent.VK_ENTER) {
			pj.iniciarJuego(PERSONAJES[seleccionPersonaje]);
		}

		// Volver al menú
		if (keycode == KeyEvent.VK_ESCAPE) {
			pj.gameState = pj.menuState;
		}
	}

	// ===== Input de créditos =====
	private void manejarInputCreditos(int keycode) {
		if (keycode == KeyEvent.VK_ESCAPE || keycode == KeyEvent.VK_ENTER) {
			pj.gameState = pj.menuState;
		}
	}

	// ===== Input de juego =====
	private void manejarInputJuego(int keycode) {
		// WASD
		if (keycode == KeyEvent.VK_W) {
			arribaPres = true;
		}
		if (keycode == KeyEvent.VK_S) {
			abajoPres = true;
		}
		if (keycode == KeyEvent.VK_A) {
			izqPres = true;
		}
		if (keycode == KeyEvent.VK_D) {
			drchPres = true;
		}

		// Flechas
		if (keycode == KeyEvent.VK_UP) {
			arribaPres = true;
		}
		if (keycode == KeyEvent.VK_DOWN) {
			abajoPres = true;
		}
		if (keycode == KeyEvent.VK_LEFT) {
			izqPres = true;
		}
		if (keycode == KeyEvent.VK_RIGHT) {
			drchPres = true;
		}

		// Pausa
		if (keycode == KeyEvent.VK_P) {
			pj.gameState = pj.pauseState;
		}
	}

	/**
	 * Se ejecuta cuando una tecla es liberada.
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		int keycode = e.getKeyCode();

		// WASD
		if (keycode == KeyEvent.VK_W) {
			arribaPres = false;
		}
		if (keycode == KeyEvent.VK_S) {
			abajoPres = false;
		}
		if (keycode == KeyEvent.VK_A) {
			izqPres = false;
		}
		if (keycode == KeyEvent.VK_D) {
			drchPres = false;
		}

		// Flechas
		if (keycode == KeyEvent.VK_UP) {
			arribaPres = false;
		}
		if (keycode == KeyEvent.VK_DOWN) {
			abajoPres = false;
		}
		if (keycode == KeyEvent.VK_LEFT) {
			izqPres = false;
		}
		if (keycode == KeyEvent.VK_RIGHT) {
			drchPres = false;
		}
	}
}
