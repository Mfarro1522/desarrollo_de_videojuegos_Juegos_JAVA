package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Manejador de eventos de teclado.
 * Controla el estado de las teclas (W, A, S, D) para el movimiento.
 */
public class keyHandler implements KeyListener {

	public boolean arribaPres, abajoPres, izqPres, drchPres;

	@Override
	public void keyTyped(KeyEvent e) {
	}

	/**
	 * Se ejecuta cuando una tecla es presionada.
	 * Marca la bandera correspondiente a true.
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		int keycode = e.getKeyCode();

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

	}

	/**
	 * Se ejecuta cuando una tecla es liberada.
	 * Marca la bandera correspondiente a false.
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		int keycode = e.getKeyCode();

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

	}

}
