package nucleo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import javax.swing.JPanel;

import configuracion.Configuracion;
import entrada.GestorEntrada;
import interfaz.InterfazUsuario;
import mundo.MundoJuego;

/**
 * Panel principal del juego (JPanel + Game Loop).
 *
 * Responsabilidades MÍNIMAS:
 * - Configurar el JPanel (tamaño, listeners)
 * - Ejecutar el game loop a 60 FPS
 * - Delegar update → MundoJuego
 * - Delegar render → TileManager + entidades + InterfazUsuario
 */
@SuppressWarnings("serial")
public class PanelJuego extends JPanel implements Runnable {

    public MundoJuego mundo;
    public GestorEntrada entrada;
    public InterfazUsuario interfaz;

    Thread threadJuego;

    public PanelJuego() {
        this.setPreferredSize(new Dimension(
                Configuracion.ANCHO_PANTALLA, Configuracion.ALTO_PANTALLA));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        setFocusable(true);

        // Crear mundo
        mundo = new MundoJuego();

        // Crear entrada unificada
        entrada = new GestorEntrada(mundo);
        this.addKeyListener(entrada);
        this.addMouseListener(entrada);
        this.addMouseMotionListener(entrada);

        // Crear interfaz
        interfaz = new InterfazUsuario(mundo, entrada);

        // Conectar menú principal al gestor de entrada (para detección de hover/click)
        entrada.setMenuPrincipal(interfaz.menuPrincipal);
    }

    /**
     * Configuración inicial del juego.
     */
    public void setupJuego() {
        mundo.setupJuego();
    }

    public void iniciarHiloJuego() {
        threadJuego = new Thread(this);
        threadJuego.start();
    }

    /**
     * Game Loop a 60 FPS con delta time.
     */
    @Override
    public void run() {
        double intervaloDibujo = 1000000000.0 / Configuracion.FPS;
        double delta = 0;
        double tiempoFinal = System.nanoTime();
        double tiempoActual;

        while (threadJuego != null) {
            tiempoActual = System.nanoTime();
            delta += (tiempoActual - tiempoFinal) / intervaloDibujo;
            tiempoFinal = tiempoActual;

            while (delta >= 1) {
                mundo.update();
                repaint();
                Toolkit.getDefaultToolkit().sync();
                delta--;
            }
        }
    }

    /**
     * Renderizado: tiles → objetos → NPCs → proyectiles → jugador → UI.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Solo dibujar mundo en estados de juego activo
        if (mundo.gameState == Configuracion.ESTADO_JUGANDO
                || mundo.gameState == Configuracion.ESTADO_PAUSA
                || mundo.gameState == Configuracion.ESTADO_GAME_OVER
                || mundo.gameState == Configuracion.ESTADO_BOSS_FIGHT) {

            // Tiles
            mundo.tileManager.draw(g2);

            // Objetos
            for (int i = 0; i < mundo.objs.length; i++) {
                if (mundo.objs[i] != null) {
                    mundo.objs[i].draw(g2, mundo);
                }
            }

            // NPCs con FRUSTUM CULLING
            int margenCullX = mundo.jugador.screenX + Configuracion.TAMANO_TILE;
            int margenCullY = mundo.jugador.screeny + Configuracion.TAMANO_TILE;
            for (int i = 0; i < mundo.npcs.length; i++) {
                if (mundo.npcs[i] != null && mundo.npcs[i].activo) {
                    int dx = mundo.npcs[i].worldx - mundo.jugador.worldx;
                    int dy = mundo.npcs[i].worldy - mundo.jugador.worldy;
                    if (dx > -margenCullX && dx < margenCullX
                            && dy > -margenCullY && dy < margenCullY) {
                        mundo.npcs[i].draw(g2);
                    }
                }
            }

            // Proyectiles con Frustum Culling
            for (int i = 0; i < mundo.proyectiles.length; i++) {
                if (mundo.proyectiles[i] != null && mundo.proyectiles[i].activo) {
                    int dx = mundo.proyectiles[i].worldX - mundo.jugador.worldx;
                    int dy = mundo.proyectiles[i].worldY - mundo.jugador.worldy;
                    if (dx > -margenCullX && dx < margenCullX
                            && dy > -margenCullY && dy < margenCullY) {
                        mundo.proyectiles[i].draw(g2);
                    }
                }
            }

            // Jugador (siempre visible)
            mundo.jugador.draw(g2);

            // Boss DemonBat (si existe y está activo)
            if (mundo.bossActivo != null && mundo.bossActivo.activo) {
                int bx = mundo.bossActivo.worldx - mundo.jugador.worldx;
                int by = mundo.bossActivo.worldy - mundo.jugador.worldy;
                if (bx > -margenCullX * 2 && bx < margenCullX * 2
                        && by > -margenCullY * 2 && by < margenCullY * 2) {
                    mundo.bossActivo.draw(g2);
                }
            }

            // Boss KingSlimes (3 instancias)
            for (int i = 0; i < mundo.kingSlimes.length; i++) {
                if (mundo.kingSlimes[i] != null && mundo.kingSlimes[i].activo) {
                    int kx = mundo.kingSlimes[i].worldx - mundo.jugador.worldx;
                    int ky = mundo.kingSlimes[i].worldy - mundo.jugador.worldy;
                    if (kx > -margenCullX && kx < margenCullX
                            && ky > -margenCullY && ky < margenCullY) {
                        mundo.kingSlimes[i].draw(g2);
                    }
                }
            }

            // Proyectiles especiales del boss
            for (int i = 0; i < mundo.proyectilesEspeciales.length; i++) {
                if (mundo.proyectilesEspeciales[i] != null && mundo.proyectilesEspeciales[i].activo) {
                    int dx = mundo.proyectilesEspeciales[i].worldX - mundo.jugador.worldx;
                    int dy = mundo.proyectilesEspeciales[i].worldY - mundo.jugador.worldy;
                    if (dx > -margenCullX && dx < margenCullX
                            && dy > -margenCullY && dy < margenCullY) {
                        mundo.proyectilesEspeciales[i].draw(g2);
                    }
                }
            }
        }

        // UI siempre al final
        interfaz.draw(g2);

        g2.dispose();
    }
}
