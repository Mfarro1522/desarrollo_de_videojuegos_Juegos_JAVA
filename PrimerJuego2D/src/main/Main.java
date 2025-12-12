package main;

import javax.swing.JFrame;

public class Main {
	
	public static void main(String[] args) {
		
		JFrame ventana = new JFrame();
		ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ventana.setResizable(false);
		
		PanelJuego paneljuego = new PanelJuego();
		ventana.add(paneljuego);
		
		ventana.pack(); //empaquetamos para verlo

		ventana.setTitle("Primer Juego 2D");
		ventana.setLocationRelativeTo(null);
		ventana.setVisible(true);
		
		paneljuego.setupJuego();
		paneljuego.iniciarHiloJuego();
	}

}