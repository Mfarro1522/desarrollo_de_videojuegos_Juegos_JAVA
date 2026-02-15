package entidad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import main.PanelJuego;

/**
 * Clase base para todos los NPCs (enemigos, aliados, etc).
 * Implementa comportamiento básico de IA, combate y Object Pooling.
 *
 * OPTIMIZACIONES:
 * - Object Pooling: los NPCs se pre-instancian y se reutilizan con activar()/desactivar()
 * - Culling Lógico: NPCs lejanos reducen frecuencia de actualización
 * - Rectángulos pre-allocados: cero creación de objetos en colisiones
 */
public abstract class NPC extends Entidad {

    protected PanelJuego pj;

    // ===== Tipo de NPC para Object Pooling =====
    public enum TipoNPC { BAT, SLIME, ORCO, GHOUL }
    public TipoNPC tipoNPC;

    // ===== Sistema de IA =====
    public int radioDeteccion = 5 * 64; // 5 tiles de distancia
    public int radioAtaque = 64; // 1 tile de distancia

    // ===== Control de movimiento =====
    protected int contadorMovimiento = 0;
    protected int duracionMovimiento = 60; // Cambia dirección cada 60 frames

    // ===== Sistema de daño individual =====
    private int contadorDanio = 0;
    private int cooldownDanio = 30; // 0.5 segundos entre ataques del mismo NPC

    // ===== Sistema de drops =====
    public int experienciaAOtorgar = 10;

    // ===== Culling Lógico: reduce updates para NPCs lejanos =====
    private int contadorCulling = 0;
    private static final int DISTANCIA_CULLING_TILES = 20;
    private static final int FRAMES_SKIP_LEJANO = 10;

    // ===== Rectángulos pre-allocados para colisiones (cero GC) =====
    private final Rectangle tempAreaJugador = new Rectangle();
    private final Rectangle tempAreaNPC = new Rectangle();

    public NPC(PanelJuego pj) {
        this.pj = pj;
        AreaSolida = new Rectangle(8, 16, 48, 48);
        AreaSolidaDefaultX = AreaSolida.x;
        AreaSolidaDefaultY = AreaSolida.y;
    }

    /**
     * Actualiza la IA del NPC (debe ser implementado por subclases).
     */
    public abstract void actualizarIA();

    // ===== OBJECT POOLING: Métodos de ciclo de vida =====

    /**
     * Activa un NPC del pool en la posición dada. Resetea todo su estado.
     * Incrementa automáticamente el contador de NPCs activos.
     */
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
        pj.contadorNPCs++;
    }

    /**
     * Desactiva el NPC y lo devuelve al pool para reutilización.
     * Decrementa automáticamente el contador de NPCs activos.
     */
    public void desactivar() {
        activo = false;
        pj.contadorNPCs--;
    }

    /**
     * Resetea el estado específico de la subclase (cooldowns, flags de ataque, etc).
     * Cada subclase debe implementar este método.
     */
    public abstract void resetearEstado();

    /**
     * Método principal de actualización con culling lógico.
     * NPCs lejanos al jugador reducen su frecuencia de actualización.
     */
    public void update() {
        if (!estaVivo) {
            // Animación de muerte (3 frames)
            contadorMuerte++;
            if (contadorMuerte >= duracionFrameMuerte) {
                frameMuerte++;
                contadorMuerte = 0;

                // Después del tercer frame, desactivar y devolver al pool
                if (frameMuerte >= 3) {
                    desactivar();
                }
            }
            return;
        }

        // ===== CULLING LÓGICO: reducir updates para NPCs lejanos =====
        int dxJ = pj.jugador.worldx - worldx;
        int dyJ = pj.jugador.worldy - worldy;
        int distSqJugador = dxJ * dxJ + dyJ * dyJ;
        int distCulling = DISTANCIA_CULLING_TILES * pj.tamanioTile;
        int distSqCulling = distCulling * distCulling;

        if (distSqJugador > distSqCulling) {
            contadorCulling++;
            if (contadorCulling % FRAMES_SKIP_LEJANO != 0) {
                return; // Saltar este frame para NPCs lejanos
            }
        }

        actualizarInvulnerabilidad();
        actualizarIA();
        mover();
        verificarColisionConJugador();

        // Actualizar cooldown de daño
        if (contadorDanio > 0) {
            contadorDanio--;
        }

        // Animación de sprites
        contadorSpites++;
        if (contadorSpites > 10) {
            numeroSpites = (numeroSpites == 1) ? 2 : 1;
            contadorSpites = 0;
        }
    }

    /**
     * Mueve al NPC según su dirección.
     */
    protected void mover() {
        hayColision = false;
        pj.dColisiones.chektile(this);

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

    /**
     * Detecta si el jugador está cerca y lo persigue.
     * Optimizado: usa distancia al cuadrado para evitar Math.sqrt().
     */
    protected void perseguirJugador() {
        int distanciaX = pj.jugador.worldx - worldx;
        int distanciaY = pj.jugador.worldy - worldy;
        int distanciaSq = distanciaX * distanciaX + distanciaY * distanciaY;
        int radioSq = radioDeteccion * radioDeteccion;

        if (distanciaSq < radioSq) {
            // Perseguir al jugador
            if (Math.abs(distanciaX) > Math.abs(distanciaY)) {
                direccion = (distanciaX > 0) ? "derecha" : "izquierda";
            } else {
                direccion = (distanciaY > 0) ? "abajo" : "arriba";
            }
            estado = EstadoEntidad.MOVIENDO;
        } else {
            // Movimiento aleatorio
            contadorMovimiento++;
            if (contadorMovimiento > duracionMovimiento) {
                cambiarDireccionAleatoria();
                contadorMovimiento = 0;
            }
            estado = EstadoEntidad.IDLE;
        }
    }

    /**
     * Cambia a una dirección aleatoria.
     */
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

    /**
     * Verifica si está tocando al jugador y causa daño.
     * Optimizado: usa rectángulos pre-allocados para cero GC.
     */
    protected void verificarColisionConJugador() {
        tempAreaJugador.setBounds(
                pj.jugador.worldx + pj.jugador.AreaSolida.x,
                pj.jugador.worldy + pj.jugador.AreaSolida.y,
                pj.jugador.AreaSolida.width,
                pj.jugador.AreaSolida.height);

        tempAreaNPC.setBounds(
                worldx + AreaSolida.x,
                worldy + AreaSolida.y,
                AreaSolida.width,
                AreaSolida.height);

        // Solo causar daño si hay colisión y el cooldown expiró
        if (tempAreaJugador.intersects(tempAreaNPC) && contadorDanio == 0) {
            pj.jugador.recibirDanio(ataque);
            contadorDanio = cooldownDanio; // Reiniciar cooldown
        }
    }

    /**
     * Dibuja al NPC en pantalla.
     */
    public void draw(Graphics2D g2) {
        int screenX = worldx - pj.jugador.worldx + pj.jugador.screenX;
        int screenY = worldy - pj.jugador.worldy + pj.jugador.screeny;

        // Solo dibujar si está en pantalla
        if (worldx + pj.tamanioTile > pj.jugador.worldx - pj.jugador.screenX
                && worldx - pj.tamanioTile < pj.jugador.worldx + pj.jugador.screenX
                && worldy + pj.tamanioTile > pj.jugador.worldy - pj.jugador.screeny
                && worldy - pj.tamanioTile < pj.jugador.worldy + pj.jugador.screeny) {

            BufferedImage sprite;

            // Si está muriendo, mostrar animación de muerte
            if (!estaVivo) {
                if (frameMuerte == 0)
                    sprite = muerte1;
                else if (frameMuerte == 1)
                    sprite = muerte2;
                else
                    sprite = muerte3;
            } else {
                // Seleccionar sprite según dirección
                sprite = obtenerSprite();
            }

            g2.drawImage(sprite, screenX, screenY, null);

            // Solo dibujar barra de vida si está vivo
            if (estaVivo) {
                dibujarBarraVida(g2, screenX, screenY);
            }

            // Efecto de daño (parpadeo)
            if (contadorInvulnerabilidad > 0 && contadorInvulnerabilidad % 10 < 5 && estaVivo) {
                g2.setColor(new Color(255, 0, 0, 100));
                g2.fillRect(screenX, screenY, pj.tamanioTile, pj.tamanioTile);
            }
        }
    }

    /**
     * Obtiene el sprite actual según dirección y animación.
     */
    protected abstract BufferedImage obtenerSprite();

    /**
     * Dibuja la barra de vida sobre el NPC.
     */
    protected void dibujarBarraVida(Graphics2D g2, int screenX, int screenY) {
        int anchoBarraMax = pj.tamanioTile;
        int altoBarra = 5;
        int yBarra = screenY - 10;

        // Fondo (rojo)
        g2.setColor(Color.RED);
        g2.fillRect(screenX, yBarra, anchoBarraMax, altoBarra);

        // Vida actual (verde)
        int anchoVida = (int) ((double) vidaActual / vidaMaxima * anchoBarraMax);
        g2.setColor(Color.GREEN);
        g2.fillRect(screenX, yBarra, anchoVida, altoBarra);

        // Borde
        g2.setColor(Color.BLACK);
        g2.drawRect(screenX, yBarra, anchoBarraMax, altoBarra);
    }
}
