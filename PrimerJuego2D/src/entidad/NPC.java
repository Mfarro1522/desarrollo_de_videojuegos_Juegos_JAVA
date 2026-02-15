package entidad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import configuracion.Configuracion;
import mundo.MundoJuego;
import utilidades.Herramientas;

/**
 * Clase base para todos los NPCs (enemigos).
 * Implementa comportamiento básico de IA, combate y Object Pooling.
 *
 * OPTIMIZACIONES:
 * - Object Pooling: los NPCs se pre-instancian y se reutilizan con
 * activar()/desactivar()
 * - Culling Lógico: NPCs lejanos reducen frecuencia de actualización
 * - Rectángulos pre-allocados: cero creación de objetos en colisiones
 */
public abstract class NPC extends Entidad {

    protected MundoJuego mundo;

    public enum TipoNPC {
        BAT, SLIME, ORCO, GHOUL
    }

    public TipoNPC tipoNPC;

    public int radioDeteccion = 5 * 64;
    public int radioAtaque = 64;

    protected int contadorMovimiento = 0;
    protected int duracionMovimiento = 60;

    private int contadorDanio = 0;
    private int cooldownDanio = 30;

    public int experienciaAOtorgar = 10;

    // ===== Culling Lógico =====
    private int contadorCulling = 0;
    private static final int DISTANCIA_CULLING_TILES = 20;
    private static final int FRAMES_SKIP_LEJANO = 10;

    // Rectángulos pre-allocados para colisiones (cero GC)
    private final Rectangle tempAreaJugador = new Rectangle();
    private final Rectangle tempAreaNPC = new Rectangle();

    public NPC(MundoJuego mundo) {
        this.mundo = mundo;
        AreaSolida = new Rectangle(8, 16, 48, 48);
        AreaSolidaDefaultX = AreaSolida.x;
        AreaSolidaDefaultY = AreaSolida.y;
    }

    public abstract void actualizarIA();

    // ===== OBJECT POOLING =====

    public void activar(int x, int y) {
        worldx = x;
        worldy = y;
        vidaActual = vidaMaxima;
        estaVivo = true;
        activo = true;
        estado = EstadoEntidad.IDLE;
        direccion = "abajo";
        frameMuerte = 0;
        contadorMuerte = 0;
        contadorInvulnerabilidad = 0;
        contadorDanio = 0;
        contadorMovimiento = 0;
        contadorCulling = 0;
        hayColision = false;
        resetearEstado();
        mundo.contadorNPCs++;
    }

    public void desactivar() {
        activo = false;
        mundo.contadorNPCs--;
    }

    public abstract void resetearEstado();

    public void update() {
        if (!estaVivo) {
            contadorMuerte++;
            if (contadorMuerte >= duracionFrameMuerte) {
                frameMuerte++;
                contadorMuerte = 0;
                if (frameMuerte >= 3) {
                    desactivar();
                }
            }
            return;
        }

        // ===== CULLING LÓGICO =====
        int dxJ = mundo.jugador.worldx - worldx;
        int dyJ = mundo.jugador.worldy - worldy;
        int distSqJugador = dxJ * dxJ + dyJ * dyJ;
        int distCulling = DISTANCIA_CULLING_TILES * Configuracion.TAMANO_TILE;
        int distSqCulling = distCulling * distCulling;

        if (distSqJugador > distSqCulling) {
            contadorCulling++;
            if (contadorCulling % FRAMES_SKIP_LEJANO != 0) {
                return;
            }
        }

        actualizarInvulnerabilidad();
        actualizarIA();
        mover();
        verificarColisionConJugador();

        if (contadorDanio > 0) {
            contadorDanio--;
        }

        contadorSpites++;
        if (contadorSpites > 10) {
            numeroSpites = (numeroSpites == 1) ? 2 : 1;
            contadorSpites = 0;
        }
    }

    protected void mover() {
        hayColision = false;
        mundo.dColisiones.chektile(this);

        if (!hayColision) {
            switch (direccion) {
                case "arriba":
                    worldy -= vel;
                    break;
                case "abajo":
                    worldy += vel;
                    break;
                case "izquierda":
                    worldx -= vel;
                    break;
                case "derecha":
                    worldx += vel;
                    break;
            }
        }
    }

    protected void perseguirJugador() {
        int distanciaX = mundo.jugador.worldx - worldx;
        int distanciaY = mundo.jugador.worldy - worldy;
        int distanciaSq = distanciaX * distanciaX + distanciaY * distanciaY;
        int radioSq = radioDeteccion * radioDeteccion;

        if (distanciaSq < radioSq) {
            if (Math.abs(distanciaX) > Math.abs(distanciaY)) {
                direccion = (distanciaX > 0) ? "derecha" : "izquierda";
            } else {
                direccion = (distanciaY > 0) ? "abajo" : "arriba";
            }
            estado = EstadoEntidad.MOVIENDO;
        } else {
            contadorMovimiento++;
            if (contadorMovimiento > duracionMovimiento) {
                cambiarDireccionAleatoria();
                contadorMovimiento = 0;
            }
            estado = EstadoEntidad.IDLE;
        }
    }

    protected void cambiarDireccionAleatoria() {
        int random = (int) (Math.random() * 4);
        switch (random) {
            case 0:
                direccion = "arriba";
                break;
            case 1:
                direccion = "abajo";
                break;
            case 2:
                direccion = "izquierda";
                break;
            case 3:
                direccion = "derecha";
                break;
        }
    }

    protected void verificarColisionConJugador() {
        tempAreaJugador.setBounds(
                mundo.jugador.worldx + mundo.jugador.AreaSolida.x,
                mundo.jugador.worldy + mundo.jugador.AreaSolida.y,
                mundo.jugador.AreaSolida.width,
                mundo.jugador.AreaSolida.height);

        tempAreaNPC.setBounds(
                worldx + AreaSolida.x,
                worldy + AreaSolida.y,
                AreaSolida.width,
                AreaSolida.height);

        if (tempAreaJugador.intersects(tempAreaNPC) && contadorDanio == 0) {
            mundo.jugador.recibirDanio(ataque);
            contadorDanio = cooldownDanio;
        }
    }

    public void draw(Graphics2D g2) {
        int tile = Configuracion.TAMANO_TILE;
        int screenX = worldx - mundo.jugador.worldx + mundo.jugador.screenX;
        int screenY = worldy - mundo.jugador.worldy + mundo.jugador.screeny;

        if (worldx + tile > mundo.jugador.worldx - mundo.jugador.screenX
                && worldx - tile < mundo.jugador.worldx + mundo.jugador.screenX
                && worldy + tile > mundo.jugador.worldy - mundo.jugador.screeny
                && worldy - tile < mundo.jugador.worldy + mundo.jugador.screeny) {

            BufferedImage sprite;

            if (!estaVivo) {
                if (frameMuerte == 0)
                    sprite = muerte1;
                else if (frameMuerte == 1)
                    sprite = muerte2;
                else
                    sprite = muerte3;
            } else {
                sprite = obtenerSprite();
            }

            g2.drawImage(sprite, screenX, screenY, null);

            if (estaVivo) {
                dibujarBarraVida(g2, screenX, screenY);
            }

            if (contadorInvulnerabilidad > 0 && contadorInvulnerabilidad % 10 < 5 && estaVivo) {
                BufferedImage tintedSprite = new Herramientas().tintImage(sprite, new Color(255, 0, 0, 100));
                g2.drawImage(tintedSprite, screenX, screenY, null);
            }
        }
    }

    protected abstract BufferedImage obtenerSprite();

    protected void dibujarBarraVida(Graphics2D g2, int screenX, int screenY) {
        int anchoBarraMax = Configuracion.TAMANO_TILE;
        int altoBarra = 5;
        int yBarra = screenY - 10;

        g2.setColor(Color.RED);
        g2.fillRect(screenX, yBarra, anchoBarraMax, altoBarra);

        int anchoVida = (int) ((double) vidaActual / vidaMaxima * anchoBarraMax);
        g2.setColor(Color.GREEN);
        g2.fillRect(screenX, yBarra, anchoVida, altoBarra);

        g2.setColor(Color.BLACK);
        g2.drawRect(screenX, yBarra, anchoBarraMax, altoBarra);
    }
}
