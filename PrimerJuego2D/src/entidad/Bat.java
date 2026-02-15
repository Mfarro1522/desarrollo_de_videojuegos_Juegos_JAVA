package entidad;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import main.PanelJuego;
import main.UtilityTool;

/**
 * Enemigo tipo Murciélago (Bat).
 * Se mueve en las 4 direcciones pero solo tiene animaciones de izquierda y
 * derecha (4 frames por lado). Al moverse arriba/abajo usa el sprite de la
 * última dirección horizontal. Daño por colisión.
 */
public class Bat extends NPC {

    private UtilityTool tool = new UtilityTool();

    // 4 frames por dirección (solo izq y der)
    private BufferedImage izq1, izq2, izq3, izq4;
    private BufferedImage der1, der2, der3, der4;

    // Dirección horizontal para reutilizar sprites al ir arriba/abajo
    private String ultimaDireccionHorizontal = "derecha";

    // Animación de 4 frames
    private int frameActual = 1; // 1-4
    private int contadorAnim = 0;
    private int velocidadAnim = 8; // Frames entre cambios de sprite

    public Bat(PanelJuego pj) {
        super(pj);

        // Estadísticas (más rápido pero frágil)
        vidaMaxima = 12;
        vidaActual = vidaMaxima;
        ataque = 4;
        defensa = 0;
        vel = 2; // Más rápido que el Slime

        direccion = "izquierda";

        // IA
        radioDeteccion = 8 * pj.tamanioTile; // Ve más lejos que el Slime
        experienciaAOtorgar = 15;

        cargarSprites();
    }

    private void cargarSprites() {
        try {
            rutaCarpeta = "/Npc/Bats/";

            // Cargar solo sprites de derecha
            BufferedImage tempDer1 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "der01.png"));
            BufferedImage tempDer2 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "der02.png"));
            BufferedImage tempDer3 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "der03.png"));
            BufferedImage tempDer4 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "der04.png"));

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

            // Asignar sprites base para compatibilidad con NPC.draw() (muerte)
            // El Bat no tiene sprites de muerte propios, usar el primer frame como
            // placeholder
            izquierda1 = izq1;
            izquierda2 = izq2;
            derecha1 = der1;
            derecha2 = der2;
            arriba1 = der1;
            arriba2 = der2;
            abajo1 = der1;
            abajo2 = der2;

            // Cargar sprites de muerte
            BufferedImage tempMuerte1 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "muerte01.png"));
            BufferedImage tempMuerte2 = ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "muerte02.png"));

            muerte1 = tool.escalarImagen(tempMuerte1, pj.tamanioTile, pj.tamanioTile);
            muerte2 = tool.escalarImagen(tempMuerte2, pj.tamanioTile, pj.tamanioTile);
            muerte3 = tool.escalarImagen(tempMuerte2, pj.tamanioTile, pj.tamanioTile); // Reutilizar frame 2

        } catch (Exception e) {
            System.err.println("[Bat] Error al cargar sprites: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void actualizarIA() {
        perseguirJugador();

        // Rastrear dirección horizontal para sprites arriba/abajo
        if (direccion.equals("izquierda") || direccion.equals("derecha")) {
            ultimaDireccionHorizontal = direccion;
        }
    }

    /**
     * Override del movimiento: se mueve en las 4 direcciones normalmente.
     * El método heredado de NPC ya maneja las 4 direcciones.
     */
    @Override
    protected void mover() {
        // Usar movimiento estándar de NPC (4 direcciones)
        super.mover();

        // Actualizar animación de 4 frames
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
        // Determinar qué set usar: izquierda o derecha
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
                // Cuando va arriba/abajo, usar la última dirección horizontal
                usarIzquierda = ultimaDireccionHorizontal.equals("izquierda");
                break;
        }

        // Seleccionar frame según animación de 4 frames
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
