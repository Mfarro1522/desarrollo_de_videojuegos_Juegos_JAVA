package entidad;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import main.PanelJuego;
import main.UtilityTool;

/**
 * Enemigo tipo Ghoul (Necrófago).
 * Comportamiento: Enemigo melee con animaciones de caminar y ataque.
 * Usa espejo horizontal para generar sprites de izquierda.
 */
public class Ghoul extends NPC {

    private UtilityTool tool = new UtilityTool();

    // Sprites de movimiento (4 frames por lado)
    private BufferedImage izq1, izq2, izq3, izq4;
    private BufferedImage der1, der2, der3, der4;

    // Sprites de ataque (3 frames por lado)
    private BufferedImage ataqueIzq1, ataqueIzq2, ataqueIzq3;
    private BufferedImage ataqueDer1, ataqueDer2, ataqueDer3;

    // Control de animación
    private String ultimaDireccionHorizontal = "derecha";
    private int frameActual = 1;
    private int contadorAnim = 0;
    private int velocidadAnim = 10;

    // Control de ataque
    private boolean estaAtacando = false;
    private int frameAtaque = 0;
    private int contadorFrameAtaque = 0;
    private int duracionFrameAtaque = 10;
    private int radioAtaque = pj.tamanioTile; // 1 tile de rango
    private int cooldownAtaque = 60; // 1 segundo entre ataques
    private int cooldownAtaqueContador = 0;
    private boolean danioAplicado = false;

    public Ghoul(PanelJuego pj) {
        super(pj);

        // Estadísticas del Ghoul (similar al Orco pero más rápido y menos vida)
        vidaMaxima = 25;
        vidaActual = vidaMaxima;
        ataque = 6;
        defensa = 1;
        vel = 2; // Más rápido que el Orco
        direccion = "abajo";

        // IA
        radioDeteccion = 7 * pj.tamanioTile; // Detecta a mayor distancia
        experienciaAOtorgar = 15;

        cargarSprites();
    }

    private void cargarSprites() {
        try {
            rutaCarpeta = "/Npc/Ghoul/";

            // Cargar solo sprites de derecha para caminar (4 frames)
            BufferedImage tempDer1 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "ghoulDer01.png"));
            BufferedImage tempDer2 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "ghoulDer02.png"));
            BufferedImage tempDer3 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "ghoulDer03.png"));
            BufferedImage tempDer4 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "ghoulDer04.png"));

            // Escalar sprites de derecha
            der1 = tool.escalarImagen(tempDer1, pj.tamanioTile, pj.tamanioTile);
            der2 = tool.escalarImagen(tempDer2, pj.tamanioTile, pj.tamanioTile);
            der3 = tool.escalarImagen(tempDer3, pj.tamanioTile, pj.tamanioTile);
            der4 = tool.escalarImagen(tempDer4, pj.tamanioTile, pj.tamanioTile);

            // Generar sprites de izquierda mediante espejo horizontal
            izq1 = tool.voltearImagenHorizontal(der1);
            izq2 = tool.voltearImagenHorizontal(der2);
            izq3 = tool.voltearImagenHorizontal(der3);
            izq4 = tool.voltearImagenHorizontal(der4);

            // Cargar sprites de ataque de derecha (3 frames)
            BufferedImage tempAtaqueDer1 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "ataqueDer1.png"));
            BufferedImage tempAtaqueDer2 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "ataqueDer2.png"));
            BufferedImage tempAtaqueDer3 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "ataqueDer3.png"));

            // Escalar sprites de ataque derecha
            ataqueDer1 = tool.escalarImagen(tempAtaqueDer1, pj.tamanioTile, pj.tamanioTile);
            ataqueDer2 = tool.escalarImagen(tempAtaqueDer2, pj.tamanioTile, pj.tamanioTile);
            ataqueDer3 = tool.escalarImagen(tempAtaqueDer3, pj.tamanioTile, pj.tamanioTile);

            // Generar sprites de ataque izquierda
            ataqueIzq1 = tool.voltearImagenHorizontal(ataqueDer1);
            ataqueIzq2 = tool.voltearImagenHorizontal(ataqueDer2);
            ataqueIzq3 = tool.voltearImagenHorizontal(ataqueDer3);

            // Asignar sprites base para compatibilidad con NPC.draw()
            // Usar primer frame de caminar para direcciones arriba/abajo
            arriba1 = der1;
            arriba2 = der2;
            abajo1 = der1;
            abajo2 = der2;
            izquierda1 = izq1;
            izquierda2 = izq2;
            derecha1 = der1;
            derecha2 = der2;

            // Usar sprites de ataque como muerte (placeholder)
            muerte1 = ataqueDer3;
            muerte2 = ataqueDer2;
            muerte3 = ataqueDer1;

        } catch (Exception e) {
            System.err.println("[Ghoul] Error al cargar sprites: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void actualizarIA() {
        if (estaAtacando) {
            return; // No actualizar IA mientras ataca
        }

        // Calcular distancia al jugador
        int distanciaX = pj.jugador.worldx - worldx;
        int distanciaY = pj.jugador.worldy - worldy;
        double distancia = Math.sqrt(distanciaX * distanciaX + distanciaY * distanciaY);

        if (distancia < radioDeteccion) {
            // Jugador detectado
            if (distancia <= radioAtaque && cooldownAtaqueContador == 0) {
                // En rango de ataque y cooldown listo
                iniciarAtaque();
            } else {
                // Perseguir al jugador
                if (Math.abs(distanciaX) > Math.abs(distanciaY)) {
                    direccion = (distanciaX > 0) ? "derecha" : "izquierda";
                } else {
                    direccion = (distanciaY > 0) ? "abajo" : "arriba";
                }
                estado = EstadoEntidad.MOVIENDO;
            }
        } else {
            // Fuera de rango: movimiento aleatorio
            perseguirJugador();
        }

        // Actualizar última dirección horizontal
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
        frameAtaque = 0;
        contadorFrameAtaque = 0;
        danioAplicado = false;
        estado = EstadoEntidad.ATACANDO;
    }

    @Override
    protected void mover() {
        if (estaAtacando) {
            // Procesar animación de ataque
            contadorFrameAtaque++;
            if (contadorFrameAtaque >= duracionFrameAtaque) {
                frameAtaque++;
                contadorFrameAtaque = 0;

                // Aplicar daño en el frame 1 (medio de la animación)
                if (frameAtaque == 1 && !danioAplicado) {
                    aplicarDanioAtaque();
                    danioAplicado = true;
                }

                if (frameAtaque >= 3) {
                    // Fin del ataque
                    estaAtacando = false;
                    frameAtaque = 0;
                    cooldownAtaqueContador = cooldownAtaque;
                    estado = EstadoEntidad.IDLE;
                }
            }
            return; // No moverse durante el ataque
        }

        // Movimiento normal
        super.mover();

        // Actualizar animación de caminar
        contadorAnim++;
        if (contadorAnim >= velocidadAnim) {
            frameActual++;
            if (frameActual > 4) {
                frameActual = 1;
            }
            contadorAnim = 0;
        }
    }

    private void aplicarDanioAtaque() {
        // Verificar si el jugador está en rango
        int distanciaX = Math.abs(pj.jugador.worldx - worldx);
        int distanciaY = Math.abs(pj.jugador.worldy - worldy);
        double distancia = Math.sqrt(distanciaX * distanciaX + distanciaY * distanciaY);

        if (distancia <= radioAtaque) {
            pj.jugador.recibirDanio(ataque);
        }
    }

    @Override
    protected BufferedImage obtenerSprite() {
        if (estaAtacando) {
            // Sprites de ataque
            boolean usarIzquierda = ultimaDireccionHorizontal.equals("izquierda");

            if (usarIzquierda) {
                switch (frameAtaque) {
                    case 0:
                        return ataqueIzq1;
                    case 1:
                        return ataqueIzq2;
                    case 2:
                        return ataqueIzq3;
                    default:
                        return ataqueIzq1;
                }
            } else {
                switch (frameAtaque) {
                    case 0:
                        return ataqueDer1;
                    case 1:
                        return ataqueDer2;
                    case 2:
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
