package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Manejador de eventos de teclado. Controla el estado de las teclas (W, A, S,
 * D) para el movimiento.
 */
public class keyHandler implements KeyListener {

	PanelJuego pj;

	public boolean arribaPres, abajoPres, izqPres, drchPres;

	public keyHandler(PanelJuego pj) {
		this.pj = pj;
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	/**
	 * Se ejecuta cuando una tecla es presionada. Marca la bandera correspondiente a
	 * true.
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		int keycode = e.getKeyCode();

		if (pj.gameState == pj.playState) {
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
			
			// Flechas (agregado)
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

			// tecla de pausa
			if (keycode == KeyEvent.VK_P) {
				pj.gameState = pj.pauseState;
			}
		} else if (pj.gameState == pj.pauseState) {
			// tecla de pausa en este caso como esta detenido quita la pausa
			if (keycode == KeyEvent.VK_P) {
				pj.gameState = pj.playState;
			}
			
		} else if (pj.gameState == pj.gameOverState) {
			// tecla para reiniciar el juego
			if (keycode == KeyEvent.VK_R) {
				pj.reiniciarJuego();
			}
		}

	}

	/**
	 * Se ejecuta cuando una tecla es liberada. Marca la bandera correspondiente a
	 * false.
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
		
		// Flechas (agregado)
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
