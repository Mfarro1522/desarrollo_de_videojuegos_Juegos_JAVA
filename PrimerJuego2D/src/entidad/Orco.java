package entidad;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import configuracion.Configuracion;
import mundo.MundoJuego;
import utilidades.Herramientas;

/**
 * Enemigo tipo Orco. 4 frames caminar + 3 frames ataque.
 * CACHE ESTÁTICO de sprites compartido. Object Pooling.
 */
public class Orco extends NPC {

    private static BufferedImage s_izq1, s_izq2, s_izq3, s_izq4;
    private static BufferedImage s_der1, s_der2, s_der3, s_der4;
    private static BufferedImage s_ataqueIzq1, s_ataqueIzq2, s_ataqueIzq3;
    private static BufferedImage s_ataqueDer1, s_ataqueDer2, s_ataqueDer3;
    private static BufferedImage s_muerte1, s_muerte2, s_muerte3;
    private static boolean spritesLoaded = false;

    private BufferedImage izq1, izq2, izq3, izq4;
    private BufferedImage der1, der2, der3, der4;
    private BufferedImage ataqueIzq1, ataqueIzq2, ataqueIzq3;
    private BufferedImage ataqueDer1, ataqueDer2, ataqueDer3;

    private String ultimaDireccionHorizontal = "derecha";
    private int frameActual = 1;
    private int contadorAnim = 0;
    private int velocidadAnim = 10;

    private boolean estaAtacando = false;
    private int contadorAtaque = 0;
    private int duracionAtaque = 30;
    private int cooldownAtaqueContador = 0;
    private int cooldownAtaque = 60;

    public Orco(MundoJuego mundo) {
        super(mundo);
        tipoNPC = TipoNPC.ORCO;
        inicializarEstadisticas();
        cargarSpritesEstaticos();
        asignarSprites();
    }

    private void inicializarEstadisticas() {
        vidaMaxima = 50; // Tanque medio
        vidaActual = vidaMaxima;
        ataque = 5; // Daño medio
        defensa = 1;
        vel = 1;
        direccion = "derecha";
        radioDeteccion = 6 * Configuracion.TAMANO_TILE;
        radioAtaque = Configuracion.TAMANO_TILE + 10;
        experienciaAOtorgar = 20;
    }

    private synchronized void cargarSpritesEstaticos() {
        if (spritesLoaded)
            return;
        Herramientas tool = new Herramientas();
        int tile = Configuracion.TAMANO_TILE;
        try {
            String ruta = "/Npc/Orco/";

            s_der1 = tool.escalarImagen(ImageIO.read(Orco.class.getResourceAsStream(ruta + "der01.png")), tile, tile);
            s_der2 = tool.escalarImagen(ImageIO.read(Orco.class.getResourceAsStream(ruta + "der02.png")), tile, tile);
            s_der3 = tool.escalarImagen(ImageIO.read(Orco.class.getResourceAsStream(ruta + "der03.png")), tile, tile);
            s_der4 = tool.escalarImagen(ImageIO.read(Orco.class.getResourceAsStream(ruta + "der05.png")), tile, tile);

            s_izq1 = tool.voltearImagenHorizontal(s_der1);
            s_izq2 = tool.voltearImagenHorizontal(s_der2);
            s_izq3 = tool.voltearImagenHorizontal(s_der3);
            s_izq4 = tool.voltearImagenHorizontal(s_der4);

            s_ataqueDer1 = tool.escalarImagen(ImageIO.read(Orco.class.getResourceAsStream(ruta + "ataqueder01.png")),
                    tile, tile);
            s_ataqueDer2 = tool.escalarImagen(ImageIO.read(Orco.class.getResourceAsStream(ruta + "ataqueder02.png")),
                    tile, tile);
            s_ataqueDer3 = tool.escalarImagen(ImageIO.read(Orco.class.getResourceAsStream(ruta + "ataqueder03.png")),
                    tile, tile);

            s_ataqueIzq1 = tool.voltearImagenHorizontal(s_ataqueDer1);
            s_ataqueIzq2 = tool.voltearImagenHorizontal(s_ataqueDer2);
            s_ataqueIzq3 = tool.voltearImagenHorizontal(s_ataqueDer3);

            s_muerte1 = tool.escalarImagen(ImageIO.read(Orco.class.getResourceAsStream(ruta + "muerte01.png")), tile,
                    tile);
            s_muerte2 = tool.escalarImagen(ImageIO.read(Orco.class.getResourceAsStream(ruta + "muerte02.png")), tile,
                    tile);
            s_muerte3 = tool.escalarImagen(ImageIO.read(Orco.class.getResourceAsStream(ruta + "muerte03.png")), tile,
                    tile);

            spritesLoaded = true;
        } catch (Exception e) {
            System.err.println("[Orco] Error al cargar sprites: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void asignarSprites() {
        izq1 = s_izq1;
        izq2 = s_izq2;
        izq3 = s_izq3;
        izq4 = s_izq4;
        der1 = s_der1;
        der2 = s_der2;
        der3 = s_der3;
        der4 = s_der4;
        ataqueIzq1 = s_ataqueIzq1;
        ataqueIzq2 = s_ataqueIzq2;
        ataqueIzq3 = s_ataqueIzq3;
        ataqueDer1 = s_ataqueDer1;
        ataqueDer2 = s_ataqueDer2;
        ataqueDer3 = s_ataqueDer3;

        izquierda1 = s_izq1;
        izquierda2 = s_izq2;
        derecha1 = s_der1;
        derecha2 = s_der2;
        arriba1 = s_der1;
        arriba2 = s_der2;
        abajo1 = s_der1;
        abajo2 = s_der2;
        muerte1 = s_muerte1;
        muerte2 = s_muerte2;
        muerte3 = s_muerte3;
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
        if (estaAtacando)
            return;

        int distanciaX = mundo.jugador.worldx - worldx;
        int distanciaY = mundo.jugador.worldy - worldy;
        double distancia = Math.sqrt(distanciaX * distanciaX + distanciaY * distanciaY);

        if (distancia < radioDeteccion) {
            if (distancia <= radioAtaque && cooldownAtaqueContador == 0) {
                iniciarAtaque();
            } else {
                if (Math.abs(distanciaX) > Math.abs(distanciaY)) {
                    direccion = (distanciaX > 0) ? "derecha" : "izquierda";
                } else {
                    direccion = (distanciaY > 0) ? "abajo" : "arriba";
                }
                estado = EstadoEntidad.MOVIENDO;
            }
        } else {
            perseguirJugador();
        }

        if (direccion.equals("izquierda") || direccion.equals("derecha")) {
            ultimaDireccionHorizontal = direccion;
        }

        if (cooldownAtaqueContador > 0)
            cooldownAtaqueContador--;
    }

    private void iniciarAtaque() {
        estaAtacando = true;
        contadorAtaque = 0;
        frameActual = 1;
        estado = EstadoEntidad.ATACANDO;

        int distanciaX = mundo.jugador.worldx - worldx;
        if (Math.abs(distanciaX) > 10) {
            ultimaDireccionHorizontal = (distanciaX > 0) ? "derecha" : "izquierda";
        }
    }

    @Override
    protected void actualizarAnimacion() {
        if (estaAtacando) {
            contadorAtaque++;
            if (contadorAtaque % 10 == 0) {
                frameActual++;
                if (frameActual > 3)
                    frameActual = 3;
            }
            if (contadorAtaque == 15) {
                verificarColisionConJugador();
            }
            if (contadorAtaque >= duracionAtaque) {
                estaAtacando = false;
                frameActual = 1;
                cooldownAtaqueContador = cooldownAtaque;
                estado = EstadoEntidad.IDLE;
            }
            return;
        }

        contadorAnim++;
        if (contadorAnim > velocidadAnim) {
            frameActual++;
            if (frameActual > 4)
                frameActual = 1;
            contadorAnim = 0;
        }
    }

    @Override
    protected BufferedImage obtenerSprite() {
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

        boolean usarIzquierda;
        switch (direccion) {
            case "izquierda":
                usarIzquierda = true;
                break;
            case "derecha":
                usarIzquierda = false;
                break;
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
