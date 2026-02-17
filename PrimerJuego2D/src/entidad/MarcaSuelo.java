package entidad;

import java.awt.Color;
import java.awt.Graphics2D;

import configuracion.Configuracion;
import mundo.MundoJuego;

/**
 * Sistema de marcas en el suelo para el personaje Sideral con Libro Mágico.
 *
 * Nivel 1: Marcas rojas de 1.0s, daño = ataque × 0.2 cada 0.3s
 * Nivel 2: Marcas rojas de 2.0s, daño = ataque × 0.3, ralentiza 30%
 *
 * Usa arrays planos (SoA) para máximo rendimiento con cero GC.
 */
public class MarcaSuelo {

    private static final int MAX_MARCAS = 40;

    private MundoJuego mundo;
    private int nivel = 1;
    private boolean activa = false;

    // Datos por marca (Structure of Arrays)
    private int[] worldX = new int[MAX_MARCAS];
    private int[] worldY = new int[MAX_MARCAS];
    private int[] duracion = new int[MAX_MARCAS]; // frames restantes
    private boolean[] slots = new boolean[MAX_MARCAS]; // true = activo

    // Configuración por nivel
    private int duracionMaxima;    // frames
    private double multiplicadorDano;
    private int tickInterval = 18; // 0.3 segundos a 60fps
    private int tickCounter = 0;
    private boolean ralentiza = false;
    private int radio;             // radio en píxeles

    public MarcaSuelo(MundoJuego mundo) {
        this.mundo = mundo;
        this.radio = Configuracion.TAMANO_TILE / 2;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
        this.activa = true;

        switch (nivel) {
            case 1:
                duracionMaxima = 60;  // 1.0 segundo
                multiplicadorDano = 0.2;
                ralentiza = false;
                radio = Configuracion.TAMANO_TILE / 2;
                break;
            case 2:
                duracionMaxima = 120; // 2.0 segundos
                multiplicadorDano = 0.3;
                ralentiza = true;
                radio = (int)(Configuracion.TAMANO_TILE * 0.75);
                break;
        }
    }

    public void desactivarTodas() {
        activa = false;
        for (int i = 0; i < MAX_MARCAS; i++) {
            slots[i] = false;
        }
    }

    /**
     * Agrega una nueva marca en la posición dada.
     * Llamado cuando Sideral se mueve un tile completo.
     */
    public void agregarMarca(int x, int y) {
        if (!activa) return;

        // Buscar slot libre
        for (int i = 0; i < MAX_MARCAS; i++) {
            if (!slots[i]) {
                worldX[i] = x;
                worldY[i] = y;
                duracion[i] = duracionMaxima;
                slots[i] = true;
                return;
            }
        }
        // Si no hay slot libre, reciclar el más viejo
        int masViejo = 0;
        int menorDuracion = Integer.MAX_VALUE;
        for (int i = 0; i < MAX_MARCAS; i++) {
            if (duracion[i] < menorDuracion) {
                menorDuracion = duracion[i];
                masViejo = i;
            }
        }
        worldX[masViejo] = x;
        worldY[masViejo] = y;
        duracion[masViejo] = duracionMaxima;
        slots[masViejo] = true;
    }

    /**
     * Actualiza marcas: decrementa duración y aplica daño en ticks.
     */
    public void update() {
        if (!activa) return;

        tickCounter++;
        boolean tickDano = tickCounter >= tickInterval;
        if (tickDano) tickCounter = 0;

        for (int i = 0; i < MAX_MARCAS; i++) {
            if (!slots[i]) continue;

            duracion[i]--;
            if (duracion[i] <= 0) {
                slots[i] = false;
                continue;
            }

            // Aplicar daño en tick
            if (tickDano) {
                int dano = (int)(mundo.jugador.ataque * multiplicadorDano);
                aplicarDanoEnArea(worldX[i], worldY[i], dano);
            }
        }
    }

    private void aplicarDanoEnArea(int cx, int cy, int dano) {
        int radioSq = radio * radio;

        // Usar spatial grid para encontrar NPCs cercanos
        mundo.grillaEspacial.consultar(cx, cy);
        int[] cercanos = mundo.grillaEspacial.getResultado();
        int count = mundo.grillaEspacial.getResultadoCount();

        for (int j = 0; j < count; j++) {
            int idx = cercanos[j];
            NPC npc = mundo.npcs[idx];
            if (npc != null && npc.activo && npc.estaVivo) {
                int dx = npc.worldx - cx;
                int dy = npc.worldy - cy;
                if (dx * dx + dy * dy <= radioSq) {
                    npc.recibirDanio(dano);
                    if (!npc.estaVivo) {
                        mundo.notificarEnemigoEliminado(npc.experienciaAOtorgar);
                    }
                }
            }
        }
    }

    /**
     * Dibuja las marcas activas como círculos rojos semi-transparentes.
     */
    public void draw(Graphics2D g2) {
        if (!activa) return;

        Jugador jugador = mundo.jugador;

        for (int i = 0; i < MAX_MARCAS; i++) {
            if (!slots[i]) continue;

            int screenX = worldX[i] - jugador.worldx + jugador.screenX;
            int screenY = worldY[i] - jugador.worldy + jugador.screeny;

            // Solo dibujar si está en pantalla
            if (screenX < -radio || screenX > Configuracion.ANCHO_PANTALLA + radio ||
                screenY < -radio || screenY > Configuracion.ALTO_PANTALLA + radio) {
                continue;
            }

            // Opacidad basada en duración restante
            float opacidad = Math.min(1.0f, (float) duracion[i] / (duracionMaxima * 0.3f));
            opacidad = Math.min(opacidad, 0.5f);

            java.awt.Composite originalComp = g2.getComposite();
            g2.setComposite(java.awt.AlphaComposite.getInstance(
                java.awt.AlphaComposite.SRC_OVER, opacidad));

            // Círculo rojo principal
            g2.setColor(new Color(200, 30, 30));
            g2.fillOval(screenX, screenY, radio * 2, radio * 2);

            // Borde más brillante
            g2.setColor(new Color(255, 50, 50));
            g2.drawOval(screenX, screenY, radio * 2, radio * 2);

            // Nivel 2: indicador de slow (borde azul interior)
            if (ralentiza) {
                g2.setColor(new Color(100, 100, 255));
                g2.drawOval(screenX + 3, screenY + 3, radio * 2 - 6, radio * 2 - 6);
            }

            g2.setComposite(originalComp);
        }
    }
}
