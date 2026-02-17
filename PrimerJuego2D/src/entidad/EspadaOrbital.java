package entidad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import configuracion.Configuracion;
import mundo.MundoJuego;
import utilidades.Herramientas;

/**
 * Espada orbital que gira alrededor del jugador.
 *
 * Niveles:
 *   1 (Madera): 1 espada, radio 1.2 tiles, 1 rev/1.5s
 *   2 (Hierro): 2 espadas, expansión al moverse, 1 rev/1.0s
 *   3 (Oro):    3 espadas, expansión + pulso de daño cada 3s
 */
public class EspadaOrbital {

    private MundoJuego mundo;
    private boolean activa = false;
    private int nivel = 0;

    // Ángulo de rotación (radianes)
    private double angulo = 0;

    // Configuración por nivel
    private int numEspadas = 1;
    private double radioBase;        // radio cuando está quieto
    private double radioExpandido;   // radio cuando se mueve (nivel 2+)
    private double radioActual;      // radio interpolado actual
    private double velocidadGiro;    // radianes por frame

    // Daño
    private double multiplicadorDano;

    // Pulso de daño (nivel 3)
    private int contadorPulso = 0;
    private static final int INTERVALO_PULSO = 180; // 3 segundos
    private boolean pulsando = false;
    private int contadorAnimPulso = 0;
    private static final int DURACION_ANIM_PULSO = 10;

    // Sprites
    private BufferedImage[] sprites = new BufferedImage[3];
    private int spriteSize;

    // Colisión - pre-allocados
    private final Rectangle areaEspada = new Rectangle();
    private final Rectangle areaNPC = new Rectangle();

    // Para suavizar la transición del radio
    private static final double LERP_SPEED = 0.08;

    public EspadaOrbital(MundoJuego mundo) {
        this.mundo = mundo;
        this.spriteSize = Configuracion.TAMANO_TILE / 2; // 32px
        cargarSprites();
    }

    private void cargarSprites() {
        Herramientas tool = new Herramientas();
        String[] paths = {
            "/objetos/Amuletos/EspadaMagicaMadera.png",
            "/objetos/Amuletos/EspadaMagicaHierro.png",
            "/objetos/Amuletos/EspadaMagicaOro.png"
        };
        for (int i = 0; i < 3; i++) {
            try {
                BufferedImage original = javax.imageio.ImageIO.read(
                    getClass().getResourceAsStream(paths[i]));
                sprites[i] = tool.escalarImagen(original, spriteSize, spriteSize);
            } catch (Exception e) {
                // Fallback: tintear sprite anterior con dorado
                if (i > 0 && sprites[i - 1] != null) {
                    sprites[i] = tool.tintImage(sprites[i - 1], new Color(255, 215, 0, 100));
                }
                System.out.println("[EspadaOrbital] Sprite nivel " + (i + 1) + " no encontrado, usando fallback.");
            }
        }
    }

    /**
     * Activa/configura la espada orbital al nivel dado.
     */
    public void setNivel(int nivel) {
        this.nivel = nivel;
        this.activa = true;
        int tile = Configuracion.TAMANO_TILE;

        switch (nivel) {
            case 1:
                numEspadas = 1;
                radioBase = tile * 1.2;
                radioExpandido = tile * 1.2; // No se expande en nivel 1
                velocidadGiro = (2 * Math.PI) / 90; // 1 rev / 1.5s (90 frames)
                multiplicadorDano = 0.35;
                break;
            case 2:
                numEspadas = 2;
                radioBase = tile * 1.2;
                radioExpandido = tile * 1.8;
                velocidadGiro = (2 * Math.PI) / 60; // 1 rev / 1.0s
                multiplicadorDano = 0.35;
                break;
            case 3:
                numEspadas = 3;
                radioBase = tile * 1.5;
                radioExpandido = tile * 2.3;
                velocidadGiro = (2 * Math.PI) / 50; // 1 rev / 0.83s
                multiplicadorDano = 0.40;
                contadorPulso = 0;
                break;
        }

        radioActual = radioBase;
    }

    public void desactivar() {
        activa = false;
        nivel = 0;
    }

    public boolean estaActiva() {
        return activa;
    }

    // ===== UPDATE =====

    public void update() {
        if (!activa || mundo.jugador == null) return;

        Jugador jugador = mundo.jugador;
        boolean enMovimiento = jugador.hayMovimiento;

        // Interpolar radio según movimiento (nivel 2+)
        if (nivel >= 2) {
            double radioObjetivo = enMovimiento ? radioExpandido : radioBase;
            // Aplicar bonus de sinergia Espada + Bebida
            radioObjetivo *= jugador.gestorAmuletos.getBonusRadioOrbital();
            radioActual = lerp(radioActual, radioObjetivo, LERP_SPEED);

            // Velocidad de giro aumentada al moverse
            double velocidadActual = enMovimiento ? velocidadGiro * 1.3 : velocidadGiro;
            angulo += velocidadActual;
        } else {
            angulo += velocidadGiro;
        }

        // Normalizar ángulo
        if (angulo > Math.PI * 2) angulo -= Math.PI * 2;

        // Verificar colisiones de cada espada
        int centroX = jugador.worldx + Configuracion.TAMANO_TILE / 2;
        int centroY = jugador.worldy + Configuracion.TAMANO_TILE / 2;
        int dano = (int)(jugador.ataque * multiplicadorDano);

        for (int i = 0; i < numEspadas; i++) {
            double swordAngle = angulo + (2 * Math.PI * i / numEspadas);
            int swordX = centroX + (int)(Math.cos(swordAngle) * radioActual) - spriteSize / 2;
            int swordY = centroY + (int)(Math.sin(swordAngle) * radioActual) - spriteSize / 2;
            verificarColisiones(swordX, swordY, dano);
        }

        // Pulso de daño (nivel 3)
        if (nivel >= 3) {
            contadorPulso++;
            if (contadorPulso >= INTERVALO_PULSO) {
                emitirPulso(centroX, centroY, dano);
                contadorPulso = 0;
                pulsando = true;
                contadorAnimPulso = DURACION_ANIM_PULSO;
            }
            if (pulsando) {
                contadorAnimPulso--;
                if (contadorAnimPulso <= 0) pulsando = false;
            }
        }
    }

    private void verificarColisiones(int swordX, int swordY, int dano) {
        areaEspada.setBounds(swordX, swordY, spriteSize, spriteSize);

        // NPCs normales via spatial grid
        mundo.grillaEspacial.consultar(swordX, swordY);
        int[] cercanos = mundo.grillaEspacial.getResultado();
        int count = mundo.grillaEspacial.getResultadoCount();

        for (int j = 0; j < count; j++) {
            int i = cercanos[j];
            if (mundo.npcs[i] != null && mundo.npcs[i].activo && mundo.npcs[i].estaVivo) {
                areaNPC.setBounds(
                    mundo.npcs[i].worldx + mundo.npcs[i].AreaSolida.x,
                    mundo.npcs[i].worldy + mundo.npcs[i].AreaSolida.y,
                    mundo.npcs[i].AreaSolida.width,
                    mundo.npcs[i].AreaSolida.height);

                if (areaEspada.intersects(areaNPC)) {
                    mundo.npcs[i].recibirDanio(dano);
                    if (!mundo.npcs[i].estaVivo) {
                        mundo.notificarEnemigoEliminado(mundo.npcs[i].experienciaAOtorgar);
                    }
                }
            }
        }

        // Boss DemonBat
        if (mundo.bossActivo != null && mundo.bossActivo.activo && mundo.bossActivo.estaVivo) {
            areaNPC.setBounds(
                mundo.bossActivo.worldx + mundo.bossActivo.AreaSolida.x,
                mundo.bossActivo.worldy + mundo.bossActivo.AreaSolida.y,
                mundo.bossActivo.AreaSolida.width,
                mundo.bossActivo.AreaSolida.height);
            if (areaEspada.intersects(areaNPC)) {
                mundo.bossActivo.recibirDanio(dano);
            }
        }

        // KingSlimes
        for (int k = 0; k < mundo.kingSlimes.length; k++) {
            if (mundo.kingSlimes[k] != null && mundo.kingSlimes[k].activo && mundo.kingSlimes[k].estaVivo) {
                areaNPC.setBounds(
                    mundo.kingSlimes[k].worldx + mundo.kingSlimes[k].AreaSolida.x,
                    mundo.kingSlimes[k].worldy + mundo.kingSlimes[k].AreaSolida.y,
                    mundo.kingSlimes[k].AreaSolida.width,
                    mundo.kingSlimes[k].AreaSolida.height);
                if (areaEspada.intersects(areaNPC)) {
                    mundo.kingSlimes[k].recibirDanio(dano);
                }
            }
        }
    }

    /**
     * Pulso de daño nivel 3: daño en área a todos los enemigos en el radio orbital.
     */
    private void emitirPulso(int centroX, int centroY, int dano) {
        int danoPulso = (int)(dano * 0.4); // 40% del daño de contacto
        double radioSq = radioActual * radioActual;

        // NPCs normales
        for (int i = 0; i < mundo.npcs.length; i++) {
            if (mundo.npcs[i] != null && mundo.npcs[i].activo && mundo.npcs[i].estaVivo) {
                double dx = mundo.npcs[i].worldx - centroX;
                double dy = mundo.npcs[i].worldy - centroY;
                if (dx * dx + dy * dy <= radioSq) {
                    mundo.npcs[i].recibirDanio(danoPulso);
                    if (!mundo.npcs[i].estaVivo) {
                        mundo.notificarEnemigoEliminado(mundo.npcs[i].experienciaAOtorgar);
                    }
                }
            }
        }
    }

    // ===== DRAW =====

    public void draw(Graphics2D g2) {
        if (!activa || mundo.jugador == null) return;

        Jugador jugador = mundo.jugador;
        int centroScreenX = jugador.screenX + Configuracion.TAMANO_TILE / 2;
        int centroScreenY = jugador.screeny + Configuracion.TAMANO_TILE / 2;

        BufferedImage sprite = sprites[Math.min(nivel - 1, 2)];
        if (sprite == null) return;

        for (int i = 0; i < numEspadas; i++) {
            double swordAngle = angulo + (2 * Math.PI * i / numEspadas);
            int sx = centroScreenX + (int)(Math.cos(swordAngle) * radioActual);
            int sy = centroScreenY + (int)(Math.sin(swordAngle) * radioActual);

            // Dibujar sprite rotado
            AffineTransform old = g2.getTransform();
            g2.rotate(swordAngle + Math.PI / 2, sx, sy);
            g2.drawImage(sprite, sx - spriteSize / 2, sy - spriteSize / 2, null);
            g2.setTransform(old);
        }

        // Efecto visual del pulso (nivel 3)
        if (pulsando && nivel >= 3) {
            float alpha = (float) contadorAnimPulso / DURACION_ANIM_PULSO * 0.3f;
            java.awt.Composite originalComp = g2.getComposite();
            g2.setComposite(java.awt.AlphaComposite.getInstance(
                java.awt.AlphaComposite.SRC_OVER, Math.max(0, alpha)));
            g2.setColor(new Color(255, 215, 0, 100)); // Dorado
            int r = (int) radioActual;
            g2.drawOval(centroScreenX - r, centroScreenY - r, r * 2, r * 2);
            g2.drawOval(centroScreenX - r + 2, centroScreenY - r + 2, r * 2 - 4, r * 2 - 4);
            g2.setComposite(originalComp);
        }

        // Indicador visual de radio (sutil, solo en nivel 2+)
        if (nivel >= 2) {
            java.awt.Composite originalComp = g2.getComposite();
            g2.setComposite(java.awt.AlphaComposite.getInstance(
                java.awt.AlphaComposite.SRC_OVER, 0.08f));
            g2.setColor(Color.WHITE);
            int r = (int) radioActual;
            g2.drawOval(centroScreenX - r, centroScreenY - r, r * 2, r * 2);
            g2.setComposite(originalComp);
        }
    }

    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}
