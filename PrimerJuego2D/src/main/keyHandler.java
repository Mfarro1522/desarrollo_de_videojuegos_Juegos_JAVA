package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class keyHandler implements KeyListener {
	
	public boolean arribaPres , abajoPres , izqPres , drchPres;
	
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

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
