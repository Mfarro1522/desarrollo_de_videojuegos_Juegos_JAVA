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

    // ===== Reciclaje Agresivo (Despawn) =====
    // 1.5 veces el ancho de pantalla (aprox 24 tiles si ancho es 16)
    // Usamos el cuadrado para evitar Math.sqrt()
    private static final int DISTANCIA_DESAPARICION_TILES = (int) (Configuracion.MAX_COLUMNAS_PANTALLA * 1.5);
    private static final int DISTANCIA_DESAPARICION_SQ = (DISTANCIA_DESAPARICION_TILES * Configuracion.TAMANO_TILE)
            * (DISTANCIA_DESAPARICION_TILES * Configuracion.TAMANO_TILE);

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

        // ===== CULLING LÓGICO Y DESAPARICIÓN =====
        int dxJ = mundo.jugador.worldx - worldx;
        int dyJ = mundo.jugador.worldy - worldy;
        int distSqJugador = dxJ * dxJ + dyJ * dyJ;

        // 1. Reciclaje Agresivo: Si está muy lejos, desactivar inmediatamente (return
        // to pool)
        if (distSqJugador > DISTANCIA_DESAPARICION_SQ) {
            desactivar();
            return;
        }

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
        // mover(); -> Reemplazado por Soft Collisions
        aplicarSeparacionSuave();

        verificarColisionConJugador();

        if (contadorDanio > 0) {
            contadorDanio--;
        }

        contadorSpites++;
        if (contadorSpites > 10) {
            numeroSpites = (numeroSpites == 1) ? 2 : 1;
            contadorSpites = 0;
        }

        actualizarAnimacion();
    }

    protected void actualizarAnimacion() {
        // Por defecto no hace nada, las subclases pueden sobrescribirlo
    }

    // ===== MOVIMIENTO FLUIDO (SOFT COLLISIONS) =====
    private void aplicarSeparacionSuave() {
        // 1. Vector de Seguimiento (Hacia el objetivo/jugador)
        // Usamos la dirección establecida por la IA (perseguirJugador o similar)
        double trackX = 0;
        double trackY = 0;

        // Convertimos la dirección string a vector
        switch (direccion) {
            case "arriba":
                trackY = -1;
                break;
            case "abajo":
                trackY = 1;
                break;
            case "izquierda":
                trackX = -1;
                break;
            case "derecha":
                trackX = 1;
                break;
        }

        // 2. Vector de Repulsión (Alejarse de vecinos)
        double repulseX = 0;
        double repulseY = 0;
        int vecinosCercanos = 0;

        // Radio de colisión suave (un poco menos que el tile para permitir agrupación
        // ligera)
        int radioRepulsion = Configuracion.TAMANO_TILE;

        mundo.grillaEspacial.consultar(worldx, worldy);
        int[] vecinos = mundo.grillaEspacial.getResultado();
        int count = mundo.grillaEspacial.getResultadoCount();

        for (int i = 0; i < count; i++) {
            int idx = vecinos[i];
            NPC vecino = mundo.npcs[idx];

            if (vecino == this || !vecino.activo)
                continue;

            double dx = worldx - vecino.worldx;
            double dy = worldy - vecino.worldy;
            double distSq = dx * dx + dy * dy;

            if (distSq < radioRepulsion * radioRepulsion && distSq > 0) {
                double dist = Math.sqrt(distSq);
                // Fuerza inversamente proporcional la distancia
                double fuerza = (radioRepulsion - dist) / radioRepulsion;
                repulseX += (dx / dist) * fuerza;
                repulseY += (dy / dist) * fuerza;
                vecinosCercanos++;
            }
        }

        // 3. Combinar Vectores
        // Pesos: Seguimiento vs Repulsión
        double pesoTrack = 1.0;
        double pesoRepulse = 1.5; // La repulsión tiene prioridad para evitar superposición

        if (vecinosCercanos > 0) {
            repulseX /= vecinosCercanos;
            repulseY /= vecinosCercanos;
        }

        double finalX = (trackX * pesoTrack) + (repulseX * pesoRepulse);
        double finalY = (trackY * pesoTrack) + (repulseY * pesoRepulse);

        // Normalizar resultado si es necesario (para mantener velocidad constante)
        double len = Math.sqrt(finalX * finalX + finalY * finalY);
        if (len > 0) {
            finalX = (finalX / len) * vel;
            finalY = (finalY / len) * vel;
        }

        // 4. Aplicar Movimiento (con colisión de tiles)
        // Intentamos mover en X
        if (finalX != 0) {
            int nextX = worldx + (int) Math.round(finalX);
            boolean colisionX = verificarColisionTile(nextX, worldy);
            if (!colisionX)
                worldx = nextX;
        }

        // Intentamos mover en Y
        if (finalY != 0) {
            int nextY = worldy + (int) Math.round(finalY);
            boolean colisionY = verificarColisionTile(worldx, nextY);
            if (!colisionY)
                worldy = nextY;
        }
    }

    private boolean verificarColisionTile(int nextX, int nextY) {
        // Versión simplificada de chektile para un punto o caja reducida
        // Aquí aprovechamos que ya tenemos lógica en DetectorColisiones,
        // pero necesitamos verificar una posición hipotética.
        // Por simplicidad y rendimiento, verificamos las esquinas del área sólida.

        int left = nextX + AreaSolida.x;
        int right = nextX + AreaSolida.x + AreaSolida.width;
        int top = nextY + AreaSolida.y;
        int bottom = nextY + AreaSolida.y + AreaSolida.height;

        int col1 = left / Configuracion.TAMANO_TILE;
        int col2 = right / Configuracion.TAMANO_TILE;
        int row1 = top / Configuracion.TAMANO_TILE;
        int row2 = bottom / Configuracion.TAMANO_TILE;

        // Verificar límites del mundo
        if (col1 < 0 || col2 >= Configuracion.MUNDO_COLUMNAS ||
                row1 < 0 || row2 >= Configuracion.MUNDO_FILAS)
            return true;

        // Verificar tiles sólidos
        if (esTileSolido(col1, row1) || esTileSolido(col2, row1) ||
                esTileSolido(col1, row2) || esTileSolido(col2, row2)) {
            return true;
        }

        return false;
    }

    private boolean esTileSolido(int col, int row) {
        int tileNum = mundo.tileManager.mapaPorNumeroTile[col][row];
        return mundo.tileManager.tiles[tileNum].colision;
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
