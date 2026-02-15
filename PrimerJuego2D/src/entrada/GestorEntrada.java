package entrada;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import configuracion.Configuracion;
import mundo.MundoJuego;

/**
 * Gestor unificado de entrada: teclado + ratón.
 *
 * Centraliza TODO el manejo de input en una sola clase.
 * Referencia a MundoJuego para cambiar estados y acceder a datos del juego.
 */
public class GestorEntrada implements KeyListener, MouseListener, MouseMotionListener {

    MundoJuego mundo;

    // Movimiento
    public boolean arribaPres, abajoPres, izqPres, drchPres;

    // Menú
    public int menuOpcion = 0;
    public int seleccionPersonaje = 0;

    // Personajes disponibles
    public static final String[] PERSONAJES = { "Sideral", "Mago", "Doom" };

    // Mouse
    public int mouseX = 0;
    public int mouseY = 0;

    // Referencia lazy al menú principal (se asigna desde PanelJuego)
    private interfaz.MenuPrincipal menuPrincipal;

    public GestorEntrada(MundoJuego mundo) {
        this.mundo = mundo;
    }

    public void setMenuPrincipal(interfaz.MenuPrincipal menu) {
        this.menuPrincipal = menu;
    }

    // =====================================================================
    // TECLADO
    // =====================================================================

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int keycode = e.getKeyCode();

        if (mundo.gameState == Configuracion.ESTADO_MENU) {
            manejarInputMenu(keycode);
        } else if (mundo.gameState == Configuracion.ESTADO_SELECCION) {
            manejarInputSeleccion(keycode);
        } else if (mundo.gameState == Configuracion.ESTADO_AYUDA) {
            manejarInputVolver(keycode);
        } else if (mundo.gameState == Configuracion.ESTADO_LOGROS) {
            manejarInputVolver(keycode);
        } else if (mundo.gameState == Configuracion.ESTADO_CREDITOS) {
            manejarInputVolver(keycode);
        } else if (mundo.gameState == Configuracion.ESTADO_JUGANDO) {
            manejarInputJuego(keycode);
        } else if (mundo.gameState == Configuracion.ESTADO_PAUSA) {
            if (keycode == KeyEvent.VK_P) mundo.gameState = Configuracion.ESTADO_JUGANDO;
        } else if (mundo.gameState == Configuracion.ESTADO_GAME_OVER) {
            if (keycode == KeyEvent.VK_R) mundo.reiniciarJuego();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keycode = e.getKeyCode();
        if (keycode == KeyEvent.VK_W || keycode == KeyEvent.VK_UP)    arribaPres = false;
        if (keycode == KeyEvent.VK_S || keycode == KeyEvent.VK_DOWN)  abajoPres = false;
        if (keycode == KeyEvent.VK_A || keycode == KeyEvent.VK_LEFT)  izqPres = false;
        if (keycode == KeyEvent.VK_D || keycode == KeyEvent.VK_RIGHT) drchPres = false;
    }

    // ===== Sub-handlers teclado =====

    private void manejarInputMenu(int keycode) {
        if (keycode == KeyEvent.VK_W || keycode == KeyEvent.VK_UP) {
            menuOpcion--;
            if (menuOpcion < 0) menuOpcion = 3;
        }
        if (keycode == KeyEvent.VK_S || keycode == KeyEvent.VK_DOWN) {
            menuOpcion++;
            if (menuOpcion > 3) menuOpcion = 0;
        }
        if (keycode == KeyEvent.VK_ENTER) {
            switch (menuOpcion) {
                case 0: mundo.gameState = Configuracion.ESTADO_SELECCION; seleccionPersonaje = 0; break;
                case 1: mundo.gameState = Configuracion.ESTADO_AYUDA; break;
                case 2: mundo.gameState = Configuracion.ESTADO_LOGROS; break;
                case 3: mundo.gameState = Configuracion.ESTADO_CREDITOS; break;
            }
        }
    }

    private void manejarInputSeleccion(int keycode) {
        if (keycode == KeyEvent.VK_A || keycode == KeyEvent.VK_LEFT) {
            seleccionPersonaje--;
            if (seleccionPersonaje < 0) seleccionPersonaje = 2;
        }
        if (keycode == KeyEvent.VK_D || keycode == KeyEvent.VK_RIGHT) {
            seleccionPersonaje++;
            if (seleccionPersonaje > 2) seleccionPersonaje = 0;
        }
        if (keycode == KeyEvent.VK_ENTER) {
            mundo.iniciarJuego(PERSONAJES[seleccionPersonaje], this);
        }
        if (keycode == KeyEvent.VK_ESCAPE) {
            mundo.gameState = Configuracion.ESTADO_MENU;
        }
    }

    private void manejarInputVolver(int keycode) {
        if (keycode == KeyEvent.VK_ESCAPE || keycode == KeyEvent.VK_ENTER) {
            mundo.gameState = Configuracion.ESTADO_MENU;
        }
    }

    private void manejarInputJuego(int keycode) {
        if (keycode == KeyEvent.VK_W || keycode == KeyEvent.VK_UP)    arribaPres = true;
        if (keycode == KeyEvent.VK_S || keycode == KeyEvent.VK_DOWN)  abajoPres = true;
        if (keycode == KeyEvent.VK_A || keycode == KeyEvent.VK_LEFT)  izqPres = true;
        if (keycode == KeyEvent.VK_D || keycode == KeyEvent.VK_RIGHT) drchPres = true;
        if (keycode == KeyEvent.VK_P) mundo.gameState = Configuracion.ESTADO_PAUSA;
    }

    // =====================================================================
    // MOUSE
    // =====================================================================

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX(), y = e.getY();

        if (mundo.gameState == Configuracion.ESTADO_MENU) {
            manejarClickMenu(x, y);
        } else if (mundo.gameState == Configuracion.ESTADO_SELECCION) {
            manejarClickSeleccion(x, y);
        } else if (mundo.gameState == Configuracion.ESTADO_AYUDA
                || mundo.gameState == Configuracion.ESTADO_LOGROS
                || mundo.gameState == Configuracion.ESTADO_CREDITOS) {
            mundo.gameState = Configuracion.ESTADO_MENU;
        } else if (mundo.gameState == Configuracion.ESTADO_GAME_OVER) {
            mundo.reiniciarJuego();
        }
    }

    private void manejarClickMenu(int x, int y) {
        if (menuPrincipal == null) return;
        int boton = menuPrincipal.getBotonEnPosicion(x, y);
        if (boton >= 0) {
            switch (boton) {
                case 0: mundo.gameState = Configuracion.ESTADO_SELECCION; seleccionPersonaje = 0; break;
                case 1: mundo.gameState = Configuracion.ESTADO_AYUDA; break;
                case 2: mundo.gameState = Configuracion.ESTADO_LOGROS; break;
                case 3: mundo.gameState = Configuracion.ESTADO_CREDITOS; break;
            }
        }
    }

    private void manejarClickSeleccion(int x, int y) {
        int ancho = Configuracion.ANCHO_PANTALLA;
        int alto = Configuracion.ALTO_PANTALLA;

        int panelX = 80, panelY = 90;
        int panelAncho = ancho - 160;
        int panelAlto = alto - 180;
        int tarjetaAncho = (panelAncho - 80) / 3;
        int tarjetaAlto = panelAlto - 120;
        int tarjetaY = panelY + 20;

        for (int i = 0; i < 3; i++) {
            int tarjetaX = panelX + 20 + i * (tarjetaAncho + 20);
            if (x >= tarjetaX && x <= tarjetaX + tarjetaAncho
                    && y >= tarjetaY && y <= tarjetaY + tarjetaAlto) {
                seleccionPersonaje = i;
                break;
            }
        }

        // Botón confirmar
        int btnAncho = 200, btnAlto = 45;
        int btnX = ancho / 2 - btnAncho / 2;
        int btnY = panelY + panelAlto + 15;
        if (x >= btnX && x <= btnX + btnAncho && y >= btnY && y <= btnY + btnAlto) {
            mundo.iniciarJuego(PERSONAJES[seleccionPersonaje], this);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        if (mundo.gameState == Configuracion.ESTADO_MENU && menuPrincipal != null) {
            int boton = menuPrincipal.getBotonEnPosicion(mouseX, mouseY);
            if (boton >= 0) menuOpcion = boton;
        }
    }

    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseDragged(MouseEvent e) {}
}
