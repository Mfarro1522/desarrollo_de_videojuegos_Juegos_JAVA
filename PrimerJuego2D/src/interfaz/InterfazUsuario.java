package interfaz;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

import configuracion.Configuracion;
import entrada.GestorEntrada;
import mundo.MundoJuego;
import utilidades.Herramientas;

/**
 * Orquestador de la interfaz de usuario.
 *
 * Delega el dibujo a pantallas especializadas según el gameState actual.
 */
public class InterfazUsuario {

    MundoJuego mundo;
    GestorEntrada entrada;

    // Sub-pantallas
    public MenuPrincipal menuPrincipal;
    public HUD hud;
    public PantallaSeleccion pantallaSeleccion;
    public PantallaAyuda pantallaAyuda;
    public PantallaLogros pantallaLogros;
    public PantallaCreditos pantallaCreditos;
    public PantallaGameOver pantallaGameOver;
    public PantallaPausa pantallaPausa;

    // Colores compartidos
    static final Color COLOR_FONDO = new Color(15, 15, 25);
    static final Color COLOR_PANEL = new Color(30, 30, 60, 220);
    static final Color COLOR_BORDE = new Color(120, 80, 200);
    static final Color COLOR_TEXTO = new Color(220, 220, 240);
    static final Color COLOR_TITULO = new Color(255, 80, 80);
    static final Color COLOR_SUBTITULO = new Color(180, 140, 255);
    static final Color COLOR_HIGHLIGHT = new Color(255, 215, 0);

    // Fuentes compartidas
    static final Font FUENTE_TITULO = new Font("Arial", Font.BOLD, 64);
    static final Font FUENTE_BOTON = new Font("Arial", Font.BOLD, 24);
    static final Font FUENTE_STATS = new Font("Arial", Font.BOLD, 18);
    static final Font FUENTE_CREDITOS = new Font("Arial", Font.PLAIN, 22);
    static final Font FUENTE_PEQUENA = new Font("Arial", Font.PLAIN, 16);
    static final Font FUENTE_40 = new Font("Arial", Font.PLAIN, 40);
    static final Font FUENTE_80B = new Font("Arial", Font.BOLD, 80);

    public InterfazUsuario(MundoJuego mundo, GestorEntrada entrada) {
        this.mundo = mundo;
        this.entrada = entrada;

        menuPrincipal = new MenuPrincipal(mundo, entrada);
        hud = new HUD(mundo);
        pantallaSeleccion = new PantallaSeleccion(mundo, entrada);
        pantallaAyuda = new PantallaAyuda(mundo);
        pantallaLogros = new PantallaLogros(mundo);
        pantallaCreditos = new PantallaCreditos(mundo);
        pantallaGameOver = new PantallaGameOver(mundo);
        pantallaPausa = new PantallaPausa(mundo);
    }

    /**
     * Dibuja la interfaz correspondiente al estado actual del juego.
     */
    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int state = mundo.gameState;

        if (state == Configuracion.ESTADO_MENU) {
            menuPrincipal.dibujar(g2);
        } else if (state == Configuracion.ESTADO_SELECCION) {
            dibujarFondoMenu(g2, 0.4f);
            pantallaSeleccion.dibujar(g2);
        } else if (state == Configuracion.ESTADO_AYUDA) {
            dibujarFondoMenu(g2, 0.4f);
            pantallaAyuda.dibujar(g2);
        } else if (state == Configuracion.ESTADO_LOGROS) {
            dibujarFondoMenu(g2, 0.4f);
            pantallaLogros.dibujar(g2);
        } else if (state == Configuracion.ESTADO_CREDITOS) {
            dibujarFondoMenu(g2, 0.4f);
            pantallaCreditos.dibujar(g2);
        } else if (state == Configuracion.ESTADO_JUGANDO) {
            hud.dibujar(g2);
        } else if (state == Configuracion.ESTADO_PAUSA) {
            hud.dibujar(g2);
            pantallaPausa.dibujar(g2);
        } else if (state == Configuracion.ESTADO_GAME_OVER) {
            pantallaGameOver.dibujar(g2);
        }
    }

    // ===== Utilidades compartidas =====

    static int obtenerXCentrado(Graphics2D g2, String texto) {
        int ancho = (int) g2.getFontMetrics().getStringBounds(texto, g2).getWidth();
        return (Configuracion.ANCHO_PANTALLA / 2) - (ancho / 2);
    }

    private void dibujarFondoMenu(Graphics2D g2, float opacidad) {
        if (mundo.imagenFondoMenu != null) {
            java.awt.Composite original = g2.getComposite();
            g2.setComposite(java.awt.AlphaComposite.getInstance(
                    java.awt.AlphaComposite.SRC_OVER, opacidad));
            g2.drawImage(mundo.imagenFondoMenu, 0, 0, null);
            g2.setComposite(original);
        } else {
            g2.setColor(COLOR_FONDO);
            g2.fillRect(0, 0, Configuracion.ANCHO_PANTALLA, Configuracion.ALTO_PANTALLA);
        }
    }
}
