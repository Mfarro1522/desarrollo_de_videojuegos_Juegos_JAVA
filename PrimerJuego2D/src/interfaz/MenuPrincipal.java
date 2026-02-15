package interfaz;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import configuracion.Configuracion;
import entrada.GestorEntrada;
import mundo.MundoJuego;

/**
 * Menú principal del juego.
 */
public class MenuPrincipal {

    MundoJuego mundo;
    GestorEntrada entrada;

    // Colores
    private final Color COLOR_FONDO = new Color(15, 15, 25);
    private final Color COLOR_BOTON = new Color(40, 40, 80);
    private final Color COLOR_BOTON_SEL = new Color(80, 50, 160);
    private final Color COLOR_BORDE = new Color(120, 80, 200);
    private final Color COLOR_TEXTO = new Color(220, 220, 240);
    private final Color COLOR_TITULO = new Color(255, 80, 80);
    private final Color COLOR_SUBTITULO = new Color(180, 140, 255);
    private final Color COLOR_HIGHLIGHT = new Color(255, 215, 0);

    // Fuentes
    private Font fuenteTitulo = new Font("Arial", Font.BOLD, 64);
    private Font fuenteBoton = new Font("Arial", Font.BOLD, 24);
    private Font fuentePequena = new Font("Arial", Font.PLAIN, 16);

    // Geometría botones (CONSTANTES CENTRALIZADAS)
    public static final int BOTON_ANCHO = 260;
    public static final int BOTON_ALTO = 50;
    public static final int Y_INICIO = 220;
    public static final int ESPACIADO = 70;
    public static final int NUM_OPCIONES = 4;

    private final String[] OPCIONES = { "COMENZAR", "AYUDA", "LOGROS", "CRÉDITOS" };

    public MenuPrincipal(MundoJuego mundo, GestorEntrada entrada) {
        this.mundo = mundo;
        this.entrada = entrada;
    }

    public int getBotonX() {
        return Configuracion.ANCHO_PANTALLA / 2 - BOTON_ANCHO / 2;
    }

    public int getBotonY(int indice) {
        return Y_INICIO + indice * ESPACIADO;
    }

    public int getBotonEnPosicion(int x, int y) {
        int btnX = getBotonX();
        for (int i = 0; i < NUM_OPCIONES; i++) {
            int btnY = getBotonY(i);
            if (x >= btnX && x <= btnX + BOTON_ANCHO && y >= btnY && y <= btnY + BOTON_ALTO) {
                return i;
            }
        }
        return -1;
    }

    public void dibujar(Graphics2D g2) {
        int ancho = Configuracion.ANCHO_PANTALLA;
        int alto = Configuracion.ALTO_PANTALLA;

        // Fondo
        if (mundo.imagenFondoMenu != null) {
            g2.drawImage(mundo.imagenFondoMenu, 0, 0, null);
        } else {
            g2.setColor(COLOR_FONDO);
            g2.fillRect(0, 0, ancho, alto);
            g2.setColor(new Color(40, 20, 60));
            for (int i = 0; i < ancho; i += 40) g2.drawLine(i, 0, i, alto);
            for (int i = 0; i < alto; i += 40) g2.drawLine(0, i, ancho, i);
        }

        // Título
        g2.setFont(fuenteTitulo);
        String titulo = "ARENA SURVIVORS";
        int xTitulo = InterfazUsuario.obtenerXCentrado(g2, titulo);
        int yTitulo = 80;
        g2.setColor(new Color(80, 0, 0));
        g2.drawString(titulo, xTitulo + 3, yTitulo + 3);
        g2.setColor(COLOR_TITULO);
        g2.drawString(titulo, xTitulo, yTitulo);

        // Subtítulo
        g2.setFont(fuentePequena);
        g2.setColor(COLOR_SUBTITULO);
        String sub = "Sobrevive. Evoluciona. Conquista.";
        g2.drawString(sub, InterfazUsuario.obtenerXCentrado(g2, sub), yTitulo + 35);

        // Botones
        int menuOpcion = entrada.menuOpcion;
        int btnX = getBotonX();

        for (int i = 0; i < OPCIONES.length; i++) {
            int yBoton = getBotonY(i);
            boolean sel = (i == menuOpcion);

            g2.setColor(sel ? COLOR_BOTON_SEL : COLOR_BOTON);
            g2.fillRoundRect(btnX, yBoton, BOTON_ANCHO, BOTON_ALTO, 15, 15);

            if (sel) {
                g2.setColor(COLOR_HIGHLIGHT);
                g2.drawRoundRect(btnX - 1, yBoton - 1, BOTON_ANCHO + 2, BOTON_ALTO + 2, 15, 15);
            } else {
                g2.setColor(COLOR_BORDE);
                g2.drawRoundRect(btnX, yBoton, BOTON_ANCHO, BOTON_ALTO, 15, 15);
            }

            g2.setFont(fuenteBoton);
            g2.setColor(sel ? COLOR_HIGHLIGHT : COLOR_TEXTO);
            FontMetrics fm = g2.getFontMetrics();
            int xTexto = btnX + (BOTON_ANCHO - fm.stringWidth(OPCIONES[i])) / 2;
            int yTexto = yBoton + (BOTON_ALTO + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(OPCIONES[i], xTexto, yTexto);

            if (sel) {
                g2.setColor(COLOR_HIGHLIGHT);
                g2.drawString("▶", btnX - 30, yTexto);
            }
        }

        // Instrucciones
        g2.setFont(fuentePequena);
        g2.setColor(new Color(150, 150, 170));
        String instr = "W/S o ↑/↓ para navegar  •  ENTER o Click para seleccionar";
        g2.drawString(instr, InterfazUsuario.obtenerXCentrado(g2, instr), alto - 30);
    }
}
