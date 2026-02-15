package interfaz;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import configuracion.Configuracion;
import mundo.MundoJuego;

/**
 * Pantalla de ayuda con controles, mecánicas y power-ups.
 */
public class PantallaAyuda {

    MundoJuego mundo;

    public PantallaAyuda(MundoJuego mundo) {
        this.mundo = mundo;
    }

    public void dibujar(Graphics2D g2) {
        int ancho = Configuracion.ANCHO_PANTALLA;
        int alto = Configuracion.ALTO_PANTALLA;

        // Título
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        g2.setColor(InterfazUsuario.COLOR_TITULO);
        String titulo = "AYUDA";
        g2.drawString(titulo, InterfazUsuario.obtenerXCentrado(g2, titulo), 80);

        // Panel
        int panelAncho = 800, panelAlto = 600;
        int panelX = ancho / 2 - panelAncho / 2, panelY = 100;
        g2.setColor(InterfazUsuario.COLOR_PANEL);
        g2.fillRoundRect(panelX, panelY, panelAncho, panelAlto, 20, 20);
        g2.setColor(InterfazUsuario.COLOR_BORDE);
        g2.drawRoundRect(panelX, panelY, panelAncho, panelAlto, 20, 20);

        int x = panelX + 40, y = panelY + 50;
        int espaciado = 35;

        g2.setFont(new Font("Arial", Font.BOLD, 24));
        g2.setColor(InterfazUsuario.COLOR_SUBTITULO);
        g2.drawString("CONTROLES", x, y);
        y += espaciado + 10;

        g2.setFont(InterfazUsuario.FUENTE_STATS);
        g2.setColor(InterfazUsuario.COLOR_TEXTO);
        g2.drawString("• WASD o Flechas: Mover personaje", x + 20, y); y += espaciado;
        g2.drawString("• P: Pausar juego", x + 20, y); y += espaciado;
        g2.drawString("• ESC: Volver al menú / Cerrar pantalla", x + 20, y); y += espaciado + 15;

        g2.setFont(new Font("Arial", Font.BOLD, 24));
        g2.setColor(InterfazUsuario.COLOR_SUBTITULO);
        g2.drawString("MECÁNICAS", x, y);
        y += espaciado + 10;

        g2.setFont(InterfazUsuario.FUENTE_STATS);
        g2.setColor(InterfazUsuario.COLOR_TEXTO);
        g2.drawString("• Sobrevive el mayor tiempo posible", x + 20, y); y += espaciado;
        g2.drawString("• Elimina enemigos para ganar experiencia", x + 20, y); y += espaciado;
        g2.drawString("• Recoge cofres para obtener power-ups", x + 20, y); y += espaciado;
        g2.drawString("• Los enemigos aparecen continuamente", x + 20, y); y += espaciado + 15;

        g2.setFont(new Font("Arial", Font.BOLD, 24));
        g2.setColor(InterfazUsuario.COLOR_SUBTITULO);
        g2.drawString("POWER-UPS", x, y);
        y += espaciado + 10;

        g2.setFont(InterfazUsuario.FUENTE_STATS);
        g2.setColor(new Color(100, 255, 255));
        g2.drawString("🛡 Invencibilidad: Inmune al daño temporalmente", x + 20, y); y += espaciado;
        g2.setColor(Color.YELLOW);
        g2.drawString("⚡ Velocidad: Movimiento más rápido", x + 20, y); y += espaciado;
        g2.setColor(new Color(255, 100, 100));
        g2.drawString("💪 Ataque: Mayor daño a enemigos", x + 20, y);

        // Instrucciones
        g2.setFont(InterfazUsuario.FUENTE_PEQUENA);
        g2.setColor(new Color(150, 150, 170));
        String instr = "Presiona ESC o ENTER para volver";
        g2.drawString(instr, InterfazUsuario.obtenerXCentrado(g2, instr), alto - 30);
    }
}
