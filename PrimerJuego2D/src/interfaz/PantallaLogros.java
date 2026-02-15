package interfaz;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import configuracion.Configuracion;
import estadisticas.Estadisticas;
import mundo.MundoJuego;

/**
 * Pantalla de logros y estadísticas acumuladas.
 */
public class PantallaLogros {

    MundoJuego mundo;

    public PantallaLogros(MundoJuego mundo) {
        this.mundo = mundo;
    }

    public void dibujar(Graphics2D g2) {
        int ancho = Configuracion.ANCHO_PANTALLA;
        int alto = Configuracion.ALTO_PANTALLA;

        // Título
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        g2.setColor(InterfazUsuario.COLOR_TITULO);
        String titulo = "LOGROS Y ESTADÍSTICAS";
        g2.drawString(titulo, InterfazUsuario.obtenerXCentrado(g2, titulo), 80);

        // Panel
        int panelAncho = 850, panelAlto = 520;
        int panelX = ancho / 2 - panelAncho / 2, panelY = 120;
        g2.setColor(InterfazUsuario.COLOR_PANEL);
        g2.fillRoundRect(panelX, panelY, panelAncho, panelAlto, 20, 20);
        g2.setColor(InterfazUsuario.COLOR_BORDE);
        g2.drawRoundRect(panelX, panelY, panelAncho, panelAlto, 20, 20);

        int x = panelX + 60, y = panelY + 60;
        int espaciado = 50;

        // Récord
        g2.setFont(new Font("Arial", Font.BOLD, 28));
        g2.setColor(InterfazUsuario.COLOR_HIGHLIGHT);
        g2.drawString("🏆 RÉCORD DE TIEMPO", x, y);
        y += 35;

        g2.setFont(new Font("Arial", Font.BOLD, 48));
        g2.setColor(new Color(255, 215, 0));
        String tiempoRecord = mundo.estadisticas.formatearTiempo(Estadisticas.recordTiempoSobrevivido);
        g2.drawString(tiempoRecord, InterfazUsuario.obtenerXCentrado(g2, tiempoRecord), y);
        y += espaciado + 20;

        // Línea divisoria
        g2.setColor(InterfazUsuario.COLOR_BORDE);
        g2.drawLine(panelX + 40, y - 10, panelX + panelAncho - 40, y - 10);
        y += 10;

        g2.setFont(new Font("Arial", Font.BOLD, 24));
        g2.setColor(InterfazUsuario.COLOR_SUBTITULO);
        g2.drawString("ESTADÍSTICAS ACUMULADAS", x, y);
        y += 40;

        g2.setFont(new Font("Arial", Font.BOLD, 20));

        g2.setColor(new Color(255, 100, 100));
        g2.drawString("⚔ Enemigos Eliminados Totales:", x, y);
        g2.setColor(Color.WHITE);
        g2.drawString(String.valueOf(Estadisticas.enemigosTotalesEliminados), x + 450, y);
        y += espaciado - 5;

        g2.setColor(new Color(255, 215, 0));
        g2.drawString("📦 Cofres Recogidos Totales:", x, y);
        g2.setColor(Color.WHITE);
        g2.drawString(String.valueOf(Estadisticas.cofresTotalesRecogidos), x + 450, y);
        y += espaciado - 5;

        g2.setColor(new Color(100, 200, 255));
        g2.drawString("🎮 Partidas Jugadas:", x, y);
        g2.setColor(Color.WHITE);
        g2.drawString(String.valueOf(Estadisticas.partidasJugadas), x + 450, y);
        y += espaciado - 5;

        g2.setColor(new Color(180, 100, 255));
        g2.drawString("⭐ Nivel Máximo Alcanzado:", x, y);
        g2.setColor(Color.WHITE);
        g2.drawString(String.valueOf(Estadisticas.nivelMaximoAlcanzado), x + 450, y);
        y += espaciado - 5;

        g2.setColor(new Color(255, 150, 50));
        g2.drawString("💔 Daño Total Recibido:", x, y);
        g2.setColor(Color.WHITE);
        g2.drawString(String.valueOf(Estadisticas.danioTotalRecibidoAcumulado), x + 450, y);

        // Persistencia
        g2.setFont(new Font("Arial", Font.ITALIC, 14));
        g2.setColor(new Color(100, 200, 100));
        g2.drawString("✔ Estadísticas guardadas en res/stats/stats.txt", panelX + 60, panelY + panelAlto - 15);

        // Instrucciones
        g2.setFont(InterfazUsuario.FUENTE_PEQUENA);
        g2.setColor(new Color(150, 150, 170));
        String instr = "Presiona ESC o ENTER para volver";
        g2.drawString(instr, InterfazUsuario.obtenerXCentrado(g2, instr), alto - 30);
    }
}
