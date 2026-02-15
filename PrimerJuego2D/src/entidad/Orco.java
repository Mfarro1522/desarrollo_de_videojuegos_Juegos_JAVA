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

    private UtilityTool tool = new UtilityTool();

    // 4 frames de caminar por dirección
    private BufferedImage izq1, izq2, izq3, izq4;
    private BufferedImage der1, der2, der3, der4;

    // 3 frames de ataque por dirección
    private BufferedImage ataqueIzq1, ataqueIzq2, ataqueIzq3;
    private BufferedImage ataqueDer1, ataqueDer2, ataqueDer3;

    // 3 frames de muerte (sin dirección específica)
    private BufferedImage muerteOrco1, muerteOrco2, muerteOrco3;

    // Dirección horizontal para reutilizar sprites al ir arriba/abajo
    private String ultimaDireccionHorizontal = "derecha";

    // Sistema de animación
    private int frameActual = 1; // 1-4 para caminar, 1-3 para ataque
    private int contadorAnim = 0;
    private int velocidadAnim = 10; // Frames entre cambios de sprite

    // Sistema de ataque
    private boolean estaAtacando = false;
    private int contadorAtaque = 0;
    private int duracionAtaque = 30; // Duración total del ataque en frames
    private int cooldownAtaqueContador = 0;
    private int cooldownAtaque = 60; // Tiempo entre ataques

    public Orco(PanelJuego pj) {
        super(pj);

        // Estadísticas (más fuerte y lento que Bat)
        vidaMaxima = 30;
        vidaActual = vidaMaxima;
        ataque = 8;
        defensa = 2;
        vel = 1; // Más lento que Bat

        direccion = "derecha";

        // IA
        radioDeteccion = 6 * pj.tamanioTile;
        radioAtaque = pj.tamanioTile + 10; // Rango de ataque melee
        experienciaAOtorgar = 25;

        cargarSprites();
    }

    private void cargarSprites() {
        try {
            rutaCarpeta = "/Npc/Orco/";

            // ===== CARGAR SPRITES DE CAMINAR (SOLO DERECHA) =====
            BufferedImage tempDer1 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "der01.png"));
            BufferedImage tempDer2 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "der02.png"));
            BufferedImage tempDer3 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "der03.png"));
            BufferedImage tempDer4 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "der05.png"));

            // Escalar sprites de derecha
            der1 = tool.escalarImagen(tempDer1, pj.tamanioTile, pj.tamanioTile);
            der2 = tool.escalarImagen(tempDer2, pj.tamanioTile, pj.tamanioTile);
            der3 = tool.escalarImagen(tempDer3, pj.tamanioTile, pj.tamanioTile);
            der4 = tool.escalarImagen(tempDer4, pj.tamanioTile, pj.tamanioTile);

            // Generar sprites de izquierda mediante espejo
            izq1 = tool.voltearImagenHorizontal(der1);
            izq2 = tool.voltearImagenHorizontal(der2);
            izq3 = tool.voltearImagenHorizontal(der3);
            izq4 = tool.voltearImagenHorizontal(der4);

            // ===== CARGAR SPRITES DE ATAQUE (SOLO DERECHA) =====
            BufferedImage tempAtaqueDer1 = ImageIO
                    .read(getClass().getResourceAsStream(rutaCarpeta + "ataqueder01.png"));
            BufferedImage tempAtaqueDer2 = ImageIO
                    .read(getClass().getResourceAsStream(rutaCarpeta + "ataqueder02.png"));
            BufferedImage tempAtaqueDer3 = ImageIO
                    .read(getClass().getResourceAsStream(rutaCarpeta + "ataqueder03.png"));

            // Escalar sprites de ataque derecha
            ataqueDer1 = tool.escalarImagen(tempAtaqueDer1, pj.tamanioTile, pj.tamanioTile);
            ataqueDer2 = tool.escalarImagen(tempAtaqueDer2, pj.tamanioTile, pj.tamanioTile);
            ataqueDer3 = tool.escalarImagen(tempAtaqueDer3, pj.tamanioTile, pj.tamanioTile);

            // Generar sprites de ataque izquierda mediante espejo
            ataqueIzq1 = tool.voltearImagenHorizontal(ataqueDer1);
            ataqueIzq2 = tool.voltearImagenHorizontal(ataqueDer2);
            ataqueIzq3 = tool.voltearImagenHorizontal(ataqueDer3);

            // ===== CARGAR SPRITES DE MUERTE =====
            BufferedImage tempMuerte1 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "muerte01.png"));
            BufferedImage tempMuerte2 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "muerte02.png"));
            BufferedImage tempMuerte3 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "muerte03.png"));

            // Escalar sprites de muerte
            muerteOrco1 = tool.escalarImagen(tempMuerte1, pj.tamanioTile, pj.tamanioTile);
            muerteOrco2 = tool.escalarImagen(tempMuerte2, pj.tamanioTile, pj.tamanioTile);
            muerteOrco3 = tool.escalarImagen(tempMuerte3, pj.tamanioTile, pj.tamanioTile);

            // ===== ASIGNAR SPRITES BASE PARA COMPATIBILIDAD CON NPC =====
            izquierda1 = izq1;
            izquierda2 = izq2;
            derecha1 = der1;
            derecha2 = der2;
            arriba1 = der1;
            arriba2 = der2;
            abajo1 = der1;
            abajo2 = der2;

            // Sprites de muerte propios
            muerte1 = muerteOrco1;
            muerte2 = muerteOrco2;
            muerte3 = muerteOrco3;

        } catch (Exception e) {
            System.err.println("[Orco] Error al cargar sprites: " + e.getMessage());
            e.printStackTrace();
        }
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
}
