package main;

import javax.swing.JFrame;

/**
 * Clase principal que inicializa la ventana del juego y el hilo de ejecución.
 */
public class Main {

	/**
	 * Punto de entrada de la aplicación.
	 * Configura la ventana (JFrame) y añade el PanelJuego.
	 * 
	 * @param args color
	 */
	public static void main(String[] args) {

		JFrame ventana = new JFrame();
		ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ventana.setResizable(false);

		PanelJuego paneljuego = new PanelJuego();
		ventana.add(paneljuego);

		ventana.pack(); // empaquetamos para verlo

		ventana.setTitle("Arena Survivors");
		ventana.setLocationRelativeTo(null);
		ventana.setVisible(true);

		paneljuego.setupJuego();
		paneljuego.iniciarHiloJuego();
	}

}