package interfaz;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import configuracion.Configuracion;
import mundo.MundoJuego;

/**
 * Pantalla de créditos.
 */
public class PantallaCreditos {

    MundoJuego mundo;

    public PantallaCreditos(MundoJuego mundo) {
        this.mundo = mundo;
    }

    public void dibujar(Graphics2D g2) {
        int ancho = Configuracion.ANCHO_PANTALLA;
        int alto = Configuracion.ALTO_PANTALLA;

        // Título
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        g2.setColor(InterfazUsuario.COLOR_TITULO);
        String titulo = "CRÉDITOS";
        g2.drawString(titulo, InterfazUsuario.obtenerXCentrado(g2, titulo), 120);

        // Panel
        int panelAncho = 400, panelAlto = 300;
        int panelX = ancho / 2 - panelAncho / 2, panelY = 160;
        g2.setColor(InterfazUsuario.COLOR_PANEL);
        g2.fillRoundRect(panelX, panelY, panelAncho, panelAlto, 20, 20);
        g2.setColor(InterfazUsuario.COLOR_BORDE);
        g2.drawRoundRect(panelX, panelY, panelAncho, panelAlto, 20, 20);

        // Subtítulo
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.setColor(InterfazUsuario.COLOR_SUBTITULO);
        String equipo = "Equipo de Desarrollo";
        g2.drawString(equipo, InterfazUsuario.obtenerXCentrado(g2, equipo), panelY + 45);

        // Línea decorativa
        g2.setColor(InterfazUsuario.COLOR_BORDE);
        g2.drawLine(panelX + 40, panelY + 60, panelX + panelAncho - 40, panelY + 60);

        // Nombres
        String[] nombres = { "Mauricio", "Jack", "Angela", "Melissa" };
        Color[] colores = {
                new Color(255, 120, 120), new Color(120, 200, 255),
                new Color(255, 200, 100), new Color(180, 255, 150)
        };

        g2.setFont(InterfazUsuario.FUENTE_CREDITOS);
        int yNombre = panelY + 100;
        int espaciado = 45;
        for (int i = 0; i < nombres.length; i++) {
            g2.setColor(colores[i]);
            String nombre = "★  " + nombres[i];
            g2.drawString(nombre, InterfazUsuario.obtenerXCentrado(g2, nombre), yNombre + i * espaciado);
        }

        // Instrucciones
        g2.setFont(InterfazUsuario.FUENTE_PEQUENA);
        g2.setColor(new Color(150, 150, 170));
        String instr = "Presiona ENTER o ESC para volver";
        g2.drawString(instr, InterfazUsuario.obtenerXCentrado(g2, instr), alto - 40);
    }
}
