package interfaz;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

import configuracion.Configuracion;
import entrada.GestorEntrada;
import mundo.MundoJuego;
import utilidades.Herramientas;

/**
 * Pantalla de selección de personaje.
 */
public class PantallaSeleccion {

    MundoJuego mundo;
    GestorEntrada entrada;

    private BufferedImage[] spritesPreview = new BufferedImage[3];

    private final String[] NOMBRES = { "Sideral", "Mago", "Doom" };
    private final String[] RANGOS = { "Rango Sideral", "Mago Arcano", "Guerrero Infernal" };
    private final String[][] DESCRIPCIONES = {
            { "Dragón con ataques", "a distancia" },
            { "Mago con ataques", "mágicos a distancia" },
            { "Guerrero cuerpo a", "cuerpo con espada" }
    };
    private final int[][] STATS = {
            { 20, 15, 3, 5 },
            { 15, 20, 2, 4 },
            { 30, 12, 8, 3 }
    };
    private final Color[] COLORES = {
            new Color(100, 200, 255),
            new Color(180, 100, 255),
            new Color(255, 80, 50)
    };

    public PantallaSeleccion(MundoJuego mundo, GestorEntrada entrada) {
        this.mundo = mundo;
        this.entrada = entrada;
        cargarSpritesPreview();
    }

    private void cargarSpritesPreview() {
        String[] carpetas = { "/jugador/Sideral/", "/jugador/Mago/", "/jugador/Doom/" };
        Herramientas tool = new Herramientas();
        for (int i = 0; i < 3; i++) {
            try {
                BufferedImage original = ImageIO.read(getClass().getResourceAsStream(carpetas[i] + "abajo_0001.png"));
                int previewSize = Configuracion.TAMANO_TILE * 2;
                spritesPreview[i] = tool.escalarImagen(original, previewSize, previewSize);
            } catch (IOException e) {
                spritesPreview[i] = null;
            }
        }
    }

    public void dibujar(Graphics2D g2) {
        int ancho = Configuracion.ANCHO_PANTALLA;
        int alto = Configuracion.ALTO_PANTALLA;
        int seleccion = entrada.seleccionPersonaje;

        // Título
        g2.setFont(new Font("Arial", Font.BOLD, 42));
        g2.setColor(InterfazUsuario.COLOR_TEXTO);
        String titulo = "Elegir personaje";
        g2.drawString(titulo, InterfazUsuario.obtenerXCentrado(g2, titulo), 60);

        // Panel
        int panelX = 80, panelY = 90;
        int panelAncho = ancho - 160, panelAlto = alto - 180;
        g2.setColor(InterfazUsuario.COLOR_PANEL);
        g2.fillRoundRect(panelX, panelY, panelAncho, panelAlto, 20, 20);
        g2.setColor(InterfazUsuario.COLOR_BORDE);
        g2.drawRoundRect(panelX, panelY, panelAncho, panelAlto, 20, 20);

        // Tarjetas
        int tarjetaAncho = (panelAncho - 80) / 3;
        int tarjetaAlto = panelAlto - 120;
        int tarjetaY = panelY + 20;

        for (int i = 0; i < 3; i++) {
            int tarjetaX = panelX + 20 + i * (tarjetaAncho + 20);
            boolean sel = (i == seleccion);

            if (sel) {
                g2.setColor(new Color(COLORES[i].getRed(), COLORES[i].getGreen(), COLORES[i].getBlue(), 40));
            } else {
                g2.setColor(new Color(20, 20, 40, 200));
            }
            g2.fillRoundRect(tarjetaX, tarjetaY, tarjetaAncho, tarjetaAlto, 15, 15);

            if (sel) {
                g2.setColor(COLORES[i]);
                g2.drawRoundRect(tarjetaX - 1, tarjetaY - 1, tarjetaAncho + 2, tarjetaAlto + 2, 15, 15);
                g2.drawRoundRect(tarjetaX - 2, tarjetaY - 2, tarjetaAncho + 4, tarjetaAlto + 4, 15, 15);
            } else {
                g2.setColor(new Color(80, 80, 100));
                g2.drawRoundRect(tarjetaX, tarjetaY, tarjetaAncho, tarjetaAlto, 15, 15);
            }

            int cX = tarjetaX + 15, cY = tarjetaY + 30;

            // Nombre
            g2.setFont(new Font("Arial", Font.BOLD, 22));
            g2.setColor(sel ? COLORES[i] : InterfazUsuario.COLOR_TEXTO);
            g2.drawString(NOMBRES[i], cX, cY);
            cY += 25;

            // Rango
            g2.setFont(InterfazUsuario.FUENTE_PEQUENA);
            g2.setColor(new Color(180, 180, 200));
            g2.drawString(RANGOS[i], cX, cY);
            cY += 30;

            // Preview sprite
            int previewSize = Configuracion.TAMANO_TILE * 2;
            int previewX = tarjetaX + (tarjetaAncho - previewSize) / 2;
            g2.setColor(new Color(15, 15, 30));
            g2.fillRect(previewX, cY, previewSize, previewSize);
            g2.setColor(COLORES[i]);
            g2.drawRect(previewX, cY, previewSize, previewSize);
            if (spritesPreview[i] != null) g2.drawImage(spritesPreview[i], previewX, cY, null);
            cY += previewSize + 20;

            // Descripción
            g2.setFont(InterfazUsuario.FUENTE_PEQUENA);
            g2.setColor(new Color(180, 180, 200));
            for (String linea : DESCRIPCIONES[i]) { g2.drawString(linea, cX, cY); cY += 18; }
            cY += 10;

            // Stats
            g2.setFont(InterfazUsuario.FUENTE_STATS);
            dibujarStat(g2, cX, cY, "❤ Vida:", STATS[i][0], 30, Color.GREEN); cY += 25;
            dibujarStat(g2, cX, cY, "⚔ Ataque:", STATS[i][1], 20, Color.ORANGE); cY += 25;
            dibujarStat(g2, cX, cY, "🛡 Defensa:", STATS[i][2], 10, Color.CYAN); cY += 25;
            dibujarStat(g2, cX, cY, "⚡ Velocidad:", STATS[i][3], 5, Color.YELLOW);

            if (sel) {
                g2.setFont(InterfazUsuario.FUENTE_PEQUENA);
                g2.setColor(InterfazUsuario.COLOR_HIGHLIGHT);
                String s = "▼ SELECCIONADO ▼";
                int xS = tarjetaX + (tarjetaAncho - g2.getFontMetrics().stringWidth(s)) / 2;
                g2.drawString(s, xS, tarjetaY + tarjetaAlto - 10);
            }
        }

        // Botón confirmar
        int btnAncho = 200, btnAlto = 45;
        int btnX = ancho / 2 - btnAncho / 2;
        int btnY = panelY + panelAlto + 15;
        g2.setColor(new Color(50, 150, 50));
        g2.fillRoundRect(btnX, btnY, btnAncho, btnAlto, 12, 12);
        g2.setColor(new Color(100, 255, 100));
        g2.drawRoundRect(btnX, btnY, btnAncho, btnAlto, 12, 12);
        g2.setFont(InterfazUsuario.FUENTE_BOTON);
        g2.setColor(Color.WHITE);
        String confirmar = "CONFIRMAR";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(confirmar, btnX + (btnAncho - fm.stringWidth(confirmar)) / 2,
                btnY + (btnAlto + fm.getAscent() - fm.getDescent()) / 2);

        // Instrucciones
        g2.setFont(InterfazUsuario.FUENTE_PEQUENA);
        g2.setColor(new Color(150, 150, 170));
        String instr = "A/D o ←/→ para elegir  •  ENTER para confirmar  •  ESC para volver";
        g2.drawString(instr, InterfazUsuario.obtenerXCentrado(g2, instr), alto - 10);
    }

    private void dibujarStat(Graphics2D g2, int x, int y, String etiqueta, int valor, int max, Color color) {
        g2.setColor(InterfazUsuario.COLOR_TEXTO);
        g2.drawString(etiqueta, x, y);

        int barraX = x + 115, barraAncho = 60, barraAlto = 12;
        g2.setColor(new Color(40, 40, 60));
        g2.fillRect(barraX, y - 11, barraAncho, barraAlto);
        int anchoProp = (int)((double) valor / max * barraAncho);
        g2.setColor(color);
        g2.fillRect(barraX, y - 11, anchoProp, barraAlto);
        g2.setColor(new Color(80, 80, 100));
        g2.drawRect(barraX, y - 11, barraAncho, barraAlto);
    }
}
