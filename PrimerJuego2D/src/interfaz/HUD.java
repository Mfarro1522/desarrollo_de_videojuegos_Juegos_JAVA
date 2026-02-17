package interfaz;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import configuracion.Configuracion;
import entidad.DemonBat;
import entidad.KingSlime;
import mundo.MundoJuego;
import utilidades.Notificacion;

/**
 * HUD: Barra de vida, estadísticas en juego, power-ups activos, notificaciones.
 */
public class HUD {

    MundoJuego mundo;

    public HUD(MundoJuego mundo) {
        this.mundo = mundo;
    }

    public void dibujar(Graphics2D g2) {
        // ===== Barra de vida =====
        int xBarra = 20, yBarra = 20;
        int anchoBarraMax = 200, altoBarra = 20;

        g2.setColor(Color.RED);
        g2.fillRect(xBarra, yBarra, anchoBarraMax, altoBarra);

        int anchoVida = (int)((double) mundo.jugador.vidaActual / mundo.jugador.vidaMaxima * anchoBarraMax);
        g2.setColor(Color.GREEN);
        g2.fillRect(xBarra, yBarra, anchoVida, altoBarra);

        g2.setColor(Color.WHITE);
        g2.drawRect(xBarra, yBarra, anchoBarraMax, altoBarra);

        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString(mundo.jugador.vidaActual + " / " + mundo.jugador.vidaMaxima,
                xBarra + 5, yBarra + 15);

        // ===== Panel de estadísticas =====
        int panelX = 20, panelY = 50;
        int panelAncho = 250, panelAlto = 120;

        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(panelX, panelY, panelAncho, panelAlto, 10, 10);
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(panelX, panelY, panelAncho, panelAlto, 10, 10);

        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.setColor(Color.WHITE);
        g2.drawString("⚔ Enemigos: " + mundo.contadorNPCs, panelX + 10, panelY + 25);
        g2.drawString("Nivel: " + mundo.estadisticas.nivel, panelX + 10, panelY + 50);

        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        g2.drawString("XP: " + mundo.estadisticas.experiencia + "/"
                + mundo.estadisticas.experienciaSiguienteNivel, panelX + 10, panelY + 70);

        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString("⏱ " + mundo.estadisticas.formatearTiempo(
                mundo.estadisticas.tiempoSobrevivido), panelX + 10, panelY + 92);

        // Nombre del personaje
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(new Color(180, 140, 255));
        g2.drawString("🎮 " + mundo.jugador.tipoPersonaje, panelX + 10, panelY + 112);

        // ===== Power-ups activos =====
        int yPowerUp = Configuracion.ALTO_PANTALLA - 40;
        g2.setFont(new Font("Arial", Font.BOLD, 16));

        if (mundo.jugador.powerUps.invencibilidadActiva) {
            g2.setColor(Color.CYAN);
            g2.drawString("🛡 Invencible (" + mundo.jugador.powerUps.getTiempoInvencibilidad() + "s)",
                    20, yPowerUp);
            yPowerUp -= 25;
        }
        if (mundo.jugador.powerUps.velocidadAumentada) {
            g2.setColor(Color.YELLOW);
            g2.drawString("⚡ Velocidad (" + mundo.jugador.powerUps.getTiempoVelocidad() + "s)",
                    20, yPowerUp);
            yPowerUp -= 25;
        }
        if (mundo.jugador.powerUps.ataqueAumentado) {
            g2.setColor(Color.RED);
            g2.drawString("💪 Ataque (" + mundo.jugador.powerUps.getTiempoAtaque() + "s)",
                    20, yPowerUp);
        }

        // ===== Notificaciones =====
        dibujarNotificaciones(g2);

        // ===== Barra de vida del Boss DemonBat =====
        if (mundo.bossActivo != null && mundo.bossActivo.activo && mundo.bossActivo.estaVivo) {
            dibujarBarraVidaBoss(g2);
        }

        // ===== Barras de vida de KingSlimes =====
        if (mundo.kingSlimesVivos > 0) {
            dibujarBarrasKingSlime(g2);
        }
    }

    private void dibujarNotificaciones(Graphics2D g2) {
        int notifX = Configuracion.ANCHO_PANTALLA - 320;
        int notifY = 20;
        int espaciado = 35;

        int maxNotif = Math.min(5, mundo.notificaciones.size());
        int inicio = Math.max(0, mundo.notificaciones.size() - maxNotif);

        for (int i = inicio; i < mundo.notificaciones.size(); i++) {
            Notificacion notif = mundo.notificaciones.get(i);
            float opacidad = notif.getOpacidad();

            Color colorAlpha = new Color(
                    notif.color.getRed(), notif.color.getGreen(), notif.color.getBlue(),
                    (int)(255 * opacidad));

            g2.setColor(new Color(0, 0, 0, (int)(180 * opacidad)));
            g2.fillRoundRect(notifX - 5, notifY - 20, 310, 30, 8, 8);

            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.setColor(colorAlpha);
            g2.drawString(notif.mensaje, notifX, notifY);

            notifY += espaciado;
        }
    }

    private void dibujarBarraVidaBoss(Graphics2D g2) {
        DemonBat boss = mundo.bossActivo;
        int anchoBarraBoss = 400;
        int altoBarraBoss = 16;
        int xBoss = (Configuracion.ANCHO_PANTALLA - anchoBarraBoss) / 2;
        int yBoss = Configuracion.ALTO_PANTALLA - 50;

        // Fondo
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(xBoss - 5, yBoss - 22, anchoBarraBoss + 10, altoBarraBoss + 28, 8, 8);

        // Nombre del boss
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(Color.RED);
        String nombre = "DemonBat";
        if (boss.estadoBoss == DemonBat.EstadoBoss.QUIETO_DISPARANDO) {
            nombre += " [ATAQUE ESPECIAL]";
        }
        int textoAncho = g2.getFontMetrics().stringWidth(nombre);
        g2.drawString(nombre, xBoss + (anchoBarraBoss - textoAncho) / 2, yBoss - 5);

        // Barra roja de fondo
        g2.setColor(new Color(80, 0, 0));
        g2.fillRect(xBoss, yBoss, anchoBarraBoss, altoBarraBoss);

        // Barra de vida
        int anchoVida = (int) ((double) boss.vidaActual / boss.vidaMaxima * anchoBarraBoss);
        g2.setColor(new Color(200, 0, 0));
        g2.fillRect(xBoss, yBoss, anchoVida, altoBarraBoss);

        // Borde
        g2.setColor(Color.WHITE);
        g2.drawRect(xBoss, yBoss, anchoBarraBoss, altoBarraBoss);

        // Texto HP
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.setColor(Color.WHITE);
        String hpTexto = boss.vidaActual + " / " + boss.vidaMaxima;
        int hpAncho = g2.getFontMetrics().stringWidth(hpTexto);
        g2.drawString(hpTexto, xBoss + (anchoBarraBoss - hpAncho) / 2, yBoss + 13);
    }

    private void dibujarBarrasKingSlime(Graphics2D g2) {
        int anchoBarraKS = 180;
        int altoBarraKS = 12;
        int yBase = Configuracion.ALTO_PANTALLA - 85;
        int espaciado = 30;

        // Panel de fondo
        int panelAncho = anchoBarraKS + 30;
        int panelAlto = espaciado * 3 + 20;
        int panelX = (Configuracion.ANCHO_PANTALLA - panelAncho) / 2;
        int panelY = yBase - 18;
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(panelX, panelY, panelAncho, panelAlto, 8, 8);

        for (int i = 0; i < mundo.kingSlimes.length; i++) {
            KingSlime ks = mundo.kingSlimes[i];
            if (ks == null) continue;

            int xBar = (Configuracion.ANCHO_PANTALLA - anchoBarraKS) / 2;
            int yBar = yBase + i * espaciado;

            // Nombre
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            boolean enFuria = ks.estadoKing == KingSlime.EstadoKing.FURIA;
            String nombre = "KingSlime #" + (i + 1);
            if (!ks.estaVivo || !ks.activo) {
                nombre += " [MUERTO]";
                g2.setColor(Color.GRAY);
            } else if (enFuria) {
                nombre += " [FURIA]";
                g2.setColor(new Color(255, 80, 80));
            } else {
                g2.setColor(new Color(100, 255, 100));
            }
            g2.drawString(nombre, xBar, yBar - 2);

            // Barra de fondo
            g2.setColor(new Color(60, 0, 0));
            g2.fillRect(xBar, yBar, anchoBarraKS, altoBarraKS);

            // Barra de vida
            if (ks.estaVivo && ks.activo) {
                int anchoVida = (int) ((double) ks.vidaActual / ks.vidaMaxima * anchoBarraKS);
                g2.setColor(enFuria ? new Color(255, 50, 50) : new Color(50, 200, 50));
                g2.fillRect(xBar, yBar, anchoVida, altoBarraKS);
            }

            // Borde
            g2.setColor(Color.WHITE);
            g2.drawRect(xBar, yBar, anchoBarraKS, altoBarraKS);
        }
    }
}
