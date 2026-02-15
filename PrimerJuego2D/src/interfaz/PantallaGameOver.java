package interfaz;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import configuracion.Configuracion;
import estadisticas.Estadisticas;
import mundo.MundoJuego;

/**
 * Pantalla de Game Over con estadísticas de la partida.
 */
public class PantallaGameOver {

    MundoJuego mundo;

    public PantallaGameOver(MundoJuego mundo) {
        this.mundo = mundo;
    }

    public void dibujar(Graphics2D g2) {
        int ancho = Configuracion.ANCHO_PANTALLA;

        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, ancho, Configuracion.ALTO_PANTALLA);

        g2.setFont(InterfazUsuario.FUENTE_80B);
        String textoMuerte = "GAME OVER";
        int x = InterfazUsuario.obtenerXCentrado(g2, textoMuerte);
        int y = 150;

        g2.setColor(Color.BLACK);
        g2.drawString(textoMuerte, x + 5, y + 5);
        g2.setColor(Color.RED);
        g2.drawString(textoMuerte, x, y);

        g2.setFont(new Font("Arial", Font.BOLD, 30));
        g2.setColor(Color.WHITE);

        int yEst = 250;
        int esp = 40;

        String tiempo = "Tiempo sobrevivido: " + mundo.estadisticas.formatearTiempo(mundo.estadisticas.tiempoSobrevivido);
        g2.drawString(tiempo, InterfazUsuario.obtenerXCentrado(g2, tiempo), yEst);
        yEst += esp;

        if (mundo.estadisticas.nuevoRecord) {
            g2.setColor(Color.YELLOW);
            String rec = "¡NUEVO RÉCORD!";
            g2.drawString(rec, InterfazUsuario.obtenerXCentrado(g2, rec), yEst);
            g2.setColor(Color.WHITE);
        } else {
            String rec = "Récord: " + mundo.estadisticas.formatearTiempo(Estadisticas.recordTiempoSobrevivido);
            g2.drawString(rec, InterfazUsuario.obtenerXCentrado(g2, rec), yEst);
        }
        yEst += esp;

        String enemigos = "Enemigos eliminados: " + mundo.estadisticas.enemigosDerrotados;
        g2.drawString(enemigos, InterfazUsuario.obtenerXCentrado(g2, enemigos), yEst);
        yEst += esp;

        String ataques = "Ataques recibidos: " + mundo.estadisticas.ataquesRecibidos;
        g2.drawString(ataques, InterfazUsuario.obtenerXCentrado(g2, ataques), yEst);
        yEst += esp;

        String nivel = "Nivel alcanzado: " + mundo.estadisticas.nivel;
        g2.drawString(nivel, InterfazUsuario.obtenerXCentrado(g2, nivel), yEst);
        yEst += esp;

        String cofres = "Cofres recogidos: " + mundo.estadisticas.cofresRecogidos;
        g2.drawString(cofres, InterfazUsuario.obtenerXCentrado(g2, cofres), yEst);
        yEst += esp + 30;

        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        g2.setColor(Color.LIGHT_GRAY);
        String instr = "Presiona R para volver al menú";
        g2.drawString(instr, InterfazUsuario.obtenerXCentrado(g2, instr), yEst);
    }
}
