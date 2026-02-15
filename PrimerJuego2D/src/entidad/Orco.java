package entidad;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import main.PanelJuego;
import main.UtilityTool;

/**
 * Enemigo tipo Orco.
 * Se mueve en las 4 direcciones pero solo tiene animaciones de izquierda y
 * derecha.
 * Los sprites izquierdos se generan mediante espejo horizontal de los derechos.
 * Incluye animaciones de caminar (4 frames), ataque (3 frames) y muerte (3
 * frames).
 */
public class Orco extends NPC {

    // ===== CACHE ESTÁTICO DE SPRITES (compartidos entre todas las instancias) =====
    private static BufferedImage s_izq1, s_izq2, s_izq3, s_izq4;
    private static BufferedImage s_der1, s_der2, s_der3, s_der4;
    private static BufferedImage s_ataqueIzq1, s_ataqueIzq2, s_ataqueIzq3;
    private static BufferedImage s_ataqueDer1, s_ataqueDer2, s_ataqueDer3;
    private static BufferedImage s_muerte1, s_muerte2, s_muerte3;
    private static boolean spritesLoaded = false;

    // Referencias de instancia (apuntan al caché estático)
    private BufferedImage izq1, izq2, izq3, izq4;
    private BufferedImage der1, der2, der3, der4;
    private BufferedImage ataqueIzq1, ataqueIzq2, ataqueIzq3;
    private BufferedImage ataqueDer1, ataqueDer2, ataqueDer3;
    private BufferedImage muerteOrco1, muerteOrco2, muerteOrco3;

    // Dirección horizontal para reutilizar sprites al ir arriba/abajo
    private String ultimaDireccionHorizontal = "derecha";

    // Sistema de animación
    private int frameActual = 1;
    private int contadorAnim = 0;
    private int velocidadAnim = 10;

    // Sistema de ataque
    private boolean estaAtacando = false;
    private int contadorAtaque = 0;
    private int duracionAtaque = 30;
    private int cooldownAtaqueContador = 0;
    private int cooldownAtaque = 60;

    public Orco(PanelJuego pj) {
        super(pj);
        tipoNPC = TipoNPC.ORCO;
        inicializarEstadisticas();
        cargarSpritesEstaticos(pj);
        asignarSprites();
    }

    private void inicializarEstadisticas() {
        vidaMaxima = 30;
        vidaActual = vidaMaxima;
        ataque = 8;
        defensa = 2;
        vel = 1;
        direccion = "derecha";
        radioDeteccion = 6 * pj.tamanioTile;
        radioAtaque = pj.tamanioTile + 10;
        experienciaAOtorgar = 25;
    }

    /**
     * Carga sprites UNA SOLA VEZ para todos los Orcos (Object Pooling).
     */
    private static synchronized void cargarSpritesEstaticos(PanelJuego pj) {
        if (spritesLoaded) return;
        UtilityTool tool = new UtilityTool();
        try {
            String ruta = "/Npc/Orco/";

            // Sprites de caminar (derecha)
            s_der1 = tool.escalarImagen(ImageIO.read(Orco.class.getResourceAsStream(ruta + "der01.png")), pj.tamanioTile, pj.tamanioTile);
            s_der2 = tool.escalarImagen(ImageIO.read(Orco.class.getResourceAsStream(ruta + "der02.png")), pj.tamanioTile, pj.tamanioTile);
            s_der3 = tool.escalarImagen(ImageIO.read(Orco.class.getResourceAsStream(ruta + "der03.png")), pj.tamanioTile, pj.tamanioTile);
            s_der4 = tool.escalarImagen(ImageIO.read(Orco.class.getResourceAsStream(ruta + "der05.png")), pj.tamanioTile, pj.tamanioTile);

            s_izq1 = tool.voltearImagenHorizontal(s_der1);
            s_izq2 = tool.voltearImagenHorizontal(s_der2);
            s_izq3 = tool.voltearImagenHorizontal(s_der3);
            s_izq4 = tool.voltearImagenHorizontal(s_der4);

            // Sprites de ataque (derecha)
            s_ataqueDer1 = tool.escalarImagen(ImageIO.read(Orco.class.getResourceAsStream(ruta + "ataqueder01.png")), pj.tamanioTile, pj.tamanioTile);
            s_ataqueDer2 = tool.escalarImagen(ImageIO.read(Orco.class.getResourceAsStream(ruta + "ataqueder02.png")), pj.tamanioTile, pj.tamanioTile);
            s_ataqueDer3 = tool.escalarImagen(ImageIO.read(Orco.class.getResourceAsStream(ruta + "ataqueder03.png")), pj.tamanioTile, pj.tamanioTile);

            s_ataqueIzq1 = tool.voltearImagenHorizontal(s_ataqueDer1);
            s_ataqueIzq2 = tool.voltearImagenHorizontal(s_ataqueDer2);
            s_ataqueIzq3 = tool.voltearImagenHorizontal(s_ataqueDer3);

            // Sprites de muerte
            s_muerte1 = tool.escalarImagen(ImageIO.read(Orco.class.getResourceAsStream(ruta + "muerte01.png")), pj.tamanioTile, pj.tamanioTile);
            s_muerte2 = tool.escalarImagen(ImageIO.read(Orco.class.getResourceAsStream(ruta + "muerte02.png")), pj.tamanioTile, pj.tamanioTile);
            s_muerte3 = tool.escalarImagen(ImageIO.read(Orco.class.getResourceAsStream(ruta + "muerte03.png")), pj.tamanioTile, pj.tamanioTile);

            spritesLoaded = true;
        } catch (Exception e) {
            System.err.println("[Orco] Error al cargar sprites: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void asignarSprites() {
        izq1 = s_izq1; izq2 = s_izq2; izq3 = s_izq3; izq4 = s_izq4;
        der1 = s_der1; der2 = s_der2; der3 = s_der3; der4 = s_der4;
        ataqueIzq1 = s_ataqueIzq1; ataqueIzq2 = s_ataqueIzq2; ataqueIzq3 = s_ataqueIzq3;
        ataqueDer1 = s_ataqueDer1; ataqueDer2 = s_ataqueDer2; ataqueDer3 = s_ataqueDer3;
        muerteOrco1 = s_muerte1; muerteOrco2 = s_muerte2; muerteOrco3 = s_muerte3;

        izquierda1 = s_izq1; izquierda2 = s_izq2;
        derecha1 = s_der1; derecha2 = s_der2;
        arriba1 = s_der1; arriba2 = s_der2;
        abajo1 = s_der1; abajo2 = s_der2;
        muerte1 = s_muerte1; muerte2 = s_muerte2; muerte3 = s_muerte3;
    }

    @Override
    public void resetearEstado() {
        ultimaDireccionHorizontal = "derecha";
        frameActual = 1;
        contadorAnim = 0;
        estaAtacando = false;
        contadorAtaque = 0;
        cooldownAtaqueContador = 0;
    }

    @Override
    public void actualizarIA() {
        if (estaAtacando) {
            // No actualizar IA mientras está atacando
            return;
        }

        int distanciaX = pj.jugador.worldx - worldx;
        int distanciaY = pj.jugador.worldy - worldy;
        double distancia = Math.sqrt(distanciaX * distanciaX + distanciaY * distanciaY);

        if (distancia < radioDeteccion) {
            // Perseguir al jugador
            if (distancia <= radioAtaque && cooldownAtaqueContador == 0) {
                // Iniciar ataque si está en rango
                iniciarAtaque();
            } else {
                // Moverse hacia el jugador
                if (Math.abs(distanciaX) > Math.abs(distanciaY)) {
                    direccion = (distanciaX > 0) ? "derecha" : "izquierda";
                } else {
                    direccion = (distanciaY > 0) ? "abajo" : "arriba";
                }
                estado = EstadoEntidad.MOVIENDO;
            }
        } else {
            // Movimiento aleatorio cuando no detecta al jugador
            perseguirJugador();
        }

        // Rastrear dirección horizontal para sprites arriba/abajo
        if (direccion.equals("izquierda") || direccion.equals("derecha")) {
            ultimaDireccionHorizontal = direccion;
        }

        // Actualizar cooldown de ataque
        if (cooldownAtaqueContador > 0) {
            cooldownAtaqueContador--;
        }
    }

    private void iniciarAtaque() {
        estaAtacando = true;
        contadorAtaque = 0;
        frameActual = 1;
        estado = EstadoEntidad.ATACANDO;

        // Orientarse hacia el jugador antes de atacar
        int distanciaX = pj.jugador.worldx - worldx;
        if (Math.abs(distanciaX) > 10) {
            ultimaDireccionHorizontal = (distanciaX > 0) ? "derecha" : "izquierda";
        }
    }

    @Override
    protected void mover() {
        if (estaAtacando) {
            // Actualizar animación de ataque
            contadorAtaque++;

            // Cambiar frame de ataque
            if (contadorAtaque % 10 == 0) {
                frameActual++;
                if (frameActual > 3) {
                    frameActual = 3;
                }
            }

            // Aplicar daño en el frame 2 del ataque
            if (contadorAtaque == 15) {
                verificarColisionConJugador();
            }

            // Terminar ataque
            if (contadorAtaque >= duracionAtaque) {
                estaAtacando = false;
                frameActual = 1;
                cooldownAtaqueContador = cooldownAtaque;
                estado = EstadoEntidad.IDLE;
            }
            return;
        }

        // Movimiento normal
        super.mover();

        // Actualizar animación de caminar (4 frames)
        contadorAnim++;
        if (contadorAnim > velocidadAnim) {
            frameActual++;
            if (frameActual > 4) {
                frameActual = 1;
            }
            contadorAnim = 0;
        }
    }

    @Override
    protected BufferedImage obtenerSprite() {
        // Si está atacando, usar sprites de ataque
        if (estaAtacando) {
            boolean usarIzquierda = ultimaDireccionHorizontal.equals("izquierda");

            if (usarIzquierda) {
                switch (frameActual) {
                    case 1:
                        return ataqueIzq1;
                    case 2:
                        return ataqueIzq2;
                    case 3:
                        return ataqueIzq3;
                    default:
                        return ataqueIzq1;
                }
            } else {
                switch (frameActual) {
                    case 1:
                        return ataqueDer1;
                    case 2:
                        return ataqueDer2;
                    case 3:
                        return ataqueDer3;
                    default:
                        return ataqueDer1;
                }
            }
        }

        // Sprites de caminar
        boolean usarIzquierda;
        switch (direccion) {
            case "izquierda":
                usarIzquierda = true;
                break;
            case "derecha":
                usarIzquierda = false;
                break;
            case "arriba":
            case "abajo":
            default:
                usarIzquierda = ultimaDireccionHorizontal.equals("izquierda");
                break;
        }

        if (usarIzquierda) {
            switch (frameActual) {
                case 1:
                    return izq1;
                case 2:
                    return izq2;
                case 3:
                    return izq3;
                case 4:
                    return izq4;
                default:
                    return izq1;
            }
        } else {
            switch (frameActual) {
                case 1:
                    return der1;
                case 2:
                    return der2;
                case 3:
                    return der3;
                case 4:
                    return der4;
                default:
                    return der1;
            }
        }
    }

    public static void resetearCache() {
        spritesLoaded = false;
    }
}
