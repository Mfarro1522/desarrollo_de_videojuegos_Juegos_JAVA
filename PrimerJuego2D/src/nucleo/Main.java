package nucleo;

import javax.swing.JFrame;

/**
 * Punto de entrada de la aplicación.
 */
public class Main {

    public static void main(String[] args) {
        JFrame ventana = new JFrame();
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setResizable(false);
        ventana.setTitle("Arena Survivors");

        PanelJuego panelJuego = new PanelJuego();
        ventana.add(panelJuego);
        ventana.pack();

        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);

        panelJuego.setupJuego();
        panelJuego.iniciarHiloJuego();
    }
}
