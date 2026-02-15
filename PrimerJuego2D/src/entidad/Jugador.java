package entidad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

import configuracion.Configuracion;
import entrada.GestorEntrada;
import items.CofrePowerUp;
import items.CofreNormal;
import mundo.MundoJuego;
import utilidades.Herramientas;

/**
 * Clase del jugador principal.
 */
public class Jugador extends Entidad {

    MundoJuego mundo;
    GestorEntrada entrada;

    public final int screenX;
    public final int screeny;

    boolean hayMovimiento = false;
    int contadorPixeles = 0;
    int contadorReposo = 0;

    boolean debug = false;
    private Herramientas miTool = new Herramientas();

    public PowerUpManager powerUps = new PowerUpManager();

    private int contadorAtaque = 0;
    private int intervaloAtaque = 30;
    private int velocidadBase = 4;

    private final Rectangle tempAreaJugador = new Rectangle();
    private final Rectangle tempAreaNPC = new Rectangle();

    // ===== Sistema de personaje =====
    public String tipoPersonaje = "Doom";
    private boolean esMelee = false;
    private boolean tieneSpritesAtaque = true;
    private boolean tieneSpritesmuerte = true;

    public Jugador(MundoJuego mundo, GestorEntrada entrada) {
        this.mundo = mundo;
        this.entrada = entrada;

        screenX = Configuracion.ANCHO_PANTALLA / 2 - (Configuracion.TAMANO_TILE / 2);
        screeny = Configuracion.ALTO_PANTALLA / 2 - (Configuracion.TAMANO_TILE / 2);

        AreaSolida = new Rectangle();
        AreaSolida.x = 1;
        AreaSolida.y = 1;
        AreaSolida.height = Configuracion.TAMANO_TILE - 2;
        AreaSolida.width = Configuracion.TAMANO_TILE - 2;

        AreaSolidaDefaultX = AreaSolida.x;
        AreaSolidaDefaultY = AreaSolida.y;

        setValorePorDefecto();
    }

    public void configurarPersonaje(String tipo) {
        this.tipoPersonaje = tipo;

        switch (tipo) {
            case "Sideral":
                rutaCarpeta = "/jugador/Sideral/";
                vidaMaxima = 150; ataque = 15; defensa = 15;
                velocidadBase = 3; esMelee = false;
                tieneSpritesAtaque = true; tieneSpritesmuerte = true;
                break;
            case "Mago":
                rutaCarpeta = "/jugador/Mago/";
                vidaMaxima = 50; ataque = 20; defensa = 2;
                velocidadBase = 7; esMelee = false;
                tieneSpritesAtaque = true; tieneSpritesmuerte = true;
                break;
            case "Doom": default:
                rutaCarpeta = "/jugador/Doom/";
                vidaMaxima = 75; ataque = 35; defensa = 10;
                velocidadBase = 5; esMelee = true;
                tieneSpritesAtaque = true; tieneSpritesmuerte = true;
                break;
        }

        vidaActual = vidaMaxima;
        vel = velocidadBase;
        estaVivo = true;
        estado = EstadoEntidad.IDLE;
        direccion = "abajo";
        frameMuerte = 0;
        contadorMuerte = 0;
        contadorAnimAtaque = 0;

        worldx = Configuracion.TAMANO_TILE * (Configuracion.MUNDO_COLUMNAS / 2);
        worldy = Configuracion.TAMANO_TILE * (Configuracion.MUNDO_FILAS / 2);

        getImagenDelJugador();
    }

    public void setValorePorDefecto() {
        worldx = Configuracion.TAMANO_TILE * (Configuracion.MUNDO_COLUMNAS / 2);
        worldy = Configuracion.TAMANO_TILE * (Configuracion.MUNDO_FILAS / 2);
        vel = velocidadBase;
        direccion = "abajo";

        vidaMaxima = 25;
        vidaActual = vidaMaxima;
        ataque = 10;
        defensa = 5;
        estaVivo = true;
        estado = EstadoEntidad.IDLE;

        getImagenDelJugador();
    }

    public void getImagenDelJugador() {
        int tile = Configuracion.TAMANO_TILE;
        try {
            if (rutaCarpeta == null || rutaCarpeta.isEmpty()) {
                rutaCarpeta = "/jugador/Doom/";
            }

            arriba1 = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "arriba_0001.png")), tile, tile);
            arriba2 = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "arriba_0002.png")), tile, tile);
            abajo1 = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "abajo_0001.png")), tile, tile);
            abajo2 = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "abajo_0002.png")), tile, tile);
            derecha1 = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "derecha_0001.png")), tile, tile);
            derecha2 = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "derecha_0002.png")), tile, tile);
            izquierda1 = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "izquierda_0001.png")), tile, tile);
            izquierda2 = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "izquierda_0002.png")), tile, tile);

            if (tieneSpritesAtaque) {
                if (tipoPersonaje.equals("Sideral")) {
                    ataqueArriba = arriba1;
                    ataqueAbajo = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "ataqueAbajo_0001.png")), tile, tile);
                    ataqueDer = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "ataqueDer_0001.png")), tile, tile);
                    ataqueIzq = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "ataqueIzq_0001.png")), tile, tile);
                } else {
                    ataqueArriba = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "ataqueArriba_0001.png")), tile, tile);
                    ataqueAbajo = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "ataqueAbajo_0002.png")), tile, tile);
                    ataqueDer = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "ataqueDer_0001.png")), tile, tile);
                    ataqueIzq = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "ataqueIzq_0001.png")), tile, tile);
                }
            } else {
                ataqueArriba = null; ataqueAbajo = null; ataqueDer = null; ataqueIzq = null;
            }

            if (tieneSpritesmuerte) {
                muerte1 = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "muerte_0001.png")), tile, tile);
                muerte2 = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "muerte_0002.png")), tile, tile);
                muerte3 = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "muerte_0003.png")), tile, tile);
            } else {
                muerte1 = null; muerte2 = null; muerte3 = null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ===== UPDATE =====

    public void update() {
        actualizarInvulnerabilidad();

        if (!estaVivo) {
            estado = EstadoEntidad.MURIENDO;
            contadorMuerte++;
            if (contadorMuerte >= duracionFrameMuerte) {
                frameMuerte++;
                contadorMuerte = 0;
                if (frameMuerte >= 3) frameMuerte = 2;
            }
            return;
        }

        if (contadorAnimAtaque > 0) {
            contadorAnimAtaque--;
            estado = EstadoEntidad.ATACANDO;
            if (esMelee) atacarMelee();
            if (contadorAnimAtaque == 0) estado = EstadoEntidad.IDLE;
        }

        // Movimiento
        if (!hayMovimiento) {
            if (entrada.arribaPres) { direccion = "arriba"; hayMovimiento = true; }
            else if (entrada.abajoPres) { direccion = "abajo"; hayMovimiento = true; }
            else if (entrada.izqPres) { direccion = "izquierda"; hayMovimiento = true; }
            else if (entrada.drchPres) { direccion = "derecha"; hayMovimiento = true; }
        }

        if (hayMovimiento) {
            hayColision = false;
            mundo.dColisiones.chektile(this);

            int objIndex = mundo.dColisiones.checkObjeto(this, true);
            recogerObjeto(objIndex);

            if (!hayColision) {
                switch (direccion) {
                    case "arriba":    worldy -= vel; break;
                    case "abajo":     worldy += vel; break;
                    case "izquierda": worldx -= vel; break;
                    case "derecha":   worldx += vel; break;
                }
            }

            contadorPixeles += vel;
            if (contadorPixeles >= Configuracion.TAMANO_TILE) {
                hayMovimiento = false;
                contadorPixeles = 0;
            }

            contadorSpites++;
            if (contadorSpites > 10) {
                if (numeroSpites == 1) numeroSpites = 2;
                else if (numeroSpites == 2) numeroSpites = 3;
                else if (numeroSpites == 3) numeroSpites = 1;
                contadorSpites = 0;
            }
        } else {
            contadorReposo++;
            if (contadorReposo == 20) {
                numeroSpites = 1;
                contadorReposo = 0;
            }
        }

        if (estado != EstadoEntidad.ATACANDO) {
            estado = hayMovimiento ? EstadoEntidad.MOVIENDO : EstadoEntidad.IDLE;
        }

        powerUps.actualizar();
        vel = (int) (velocidadBase * powerUps.multiplicadorVelocidad);

        contadorAtaque++;
        if (contadorAtaque >= intervaloAtaque) {
            ejecutarAtaque();
            contadorAtaque = 0;
        }
    }

    // ===== ATAQUE =====

    private void ejecutarAtaque() {
        contadorAnimAtaque = duracionAnimAtaque;
        estado = EstadoEntidad.ATACANDO;
        if (!esMelee) dispararProyectil();
    }

    private void dispararProyectil() {
        for (int i = 0; i < mundo.proyectiles.length; i++) {
            if (mundo.proyectiles[i] == null) {
                int dano = (int) (ataque * powerUps.multiplicadorAtaque);
                int proyectilX = worldx + Configuracion.TAMANO_TILE / 2 - 8;
                int proyectilY = worldy + Configuracion.TAMANO_TILE / 2 - 8;
                mundo.proyectiles[i] = new Proyectil(mundo, proyectilX, proyectilY, direccion, dano);
                break;
            }
        }
    }

    /**
     * Ataque melee (Doom): usa GrillaEspacial para encontrar NPCs cercanos.
     */
    private void atacarMelee() {
        tempAreaJugador.setBounds(worldx + AreaSolida.x, worldy + AreaSolida.y, AreaSolida.width, AreaSolida.height);
        int dano = (int) (ataque * powerUps.multiplicadorAtaque);

        mundo.grillaEspacial.consultar(worldx, worldy);
        int[] cercanos = mundo.grillaEspacial.getResultado();
        int count = mundo.grillaEspacial.getResultadoCount();

        for (int j = 0; j < count; j++) {
            int i = cercanos[j];
            if (mundo.npcs[i] != null && mundo.npcs[i].activo && mundo.npcs[i].estaVivo) {
                tempAreaNPC.setBounds(
                        mundo.npcs[i].worldx + mundo.npcs[i].AreaSolida.x,
                        mundo.npcs[i].worldy + mundo.npcs[i].AreaSolida.y,
                        mundo.npcs[i].AreaSolida.width,
                        mundo.npcs[i].AreaSolida.height);

                if (tempAreaJugador.intersects(tempAreaNPC)) {
                    mundo.npcs[i].recibirDanio(dano);
                    if (!mundo.npcs[i].estaVivo) {
                        mundo.estadisticas.registrarEnemigoEliminado();
                        mundo.estadisticas.ganarExperiencia(mundo.npcs[i].experienciaAOtorgar);
                    }
                }
            }
        }
    }

    // ===== DAÑO =====

    @Override
    public void recibirDanio(int cantidad) {
        if (powerUps.invencibilidadActiva) return;
        mundo.estadisticas.registrarAtaqueRecibido(cantidad);
        super.recibirDanio(cantidad);
    }

    // ===== DRAW =====

    public void draw(Graphics2D g2) {
        int tile = Configuracion.TAMANO_TILE;
        BufferedImage imagen = null;

        if (estado == EstadoEntidad.MURIENDO) {
            if (tieneSpritesmuerte && muerte1 != null) {
                if (frameMuerte == 0) imagen = muerte1;
                else if (frameMuerte == 1) imagen = muerte2;
                else imagen = muerte3;
                g2.drawImage(imagen, screenX, screeny, null);
            } else {
                g2.setColor(Color.RED);
                g2.fillRect(screenX, screeny, tile, tile);
                g2.setColor(Color.WHITE);
                g2.drawString("X", screenX + tile / 2 - 4, screeny + tile / 2 + 4);
            }
            return;
        }

        if (estado == EstadoEntidad.ATACANDO) {
            imagen = obtenerSpriteAtaque();
            if (imagen == null) {
                g2.setColor(new Color(255, 50, 50, 180));
                g2.fillRect(screenX, screeny, tile, tile);
                g2.setColor(Color.WHITE);
                g2.drawString("⚔", screenX + tile / 2 - 6, screeny + tile / 2 + 6);
                return;
            }
        } else {
            imagen = obtenerSpriteMovimiento();
        }

        g2.drawImage(imagen, screenX, screeny, null);

        if (contadorInvulnerabilidad > 0 && contadorInvulnerabilidad % 10 < 5) {
            g2.setColor(new Color(255, 0, 0, 100));
            g2.fillRect(screenX, screeny, tile, tile);
        }

        if (debug) {
            g2.setColor(Color.RED);
            g2.drawRect(screenX + AreaSolida.x, screeny + AreaSolida.y, AreaSolida.width, AreaSolida.height);
        }
    }

    private BufferedImage obtenerSpriteAtaque() {
        switch (direccion) {
            case "arriba": return ataqueArriba;
            case "abajo": return ataqueAbajo;
            case "izquierda": return ataqueIzq;
            case "derecha": return ataqueDer;
            default: return null;
        }
    }

    private BufferedImage obtenerSpriteMovimiento() {
        switch (direccion) {
            case "arriba":    return (numeroSpites == 1) ? arriba1 : arriba2;
            case "abajo":     return (numeroSpites == 1) ? abajo1 : abajo2;
            case "izquierda": return (numeroSpites == 1) ? izquierda1 : izquierda2;
            case "derecha":   return (numeroSpites == 1) ? derecha1 : derecha2;
            default: return abajo1;
        }
    }

    // ===== OBJETOS =====

    public void recogerObjeto(int index) {
        if (index != 999) {
            if (mundo.objs[index] instanceof CofrePowerUp) {
                CofrePowerUp cofre = (CofrePowerUp) mundo.objs[index];

                switch (cofre.tipoPowerUp) {
                    case INVENCIBILIDAD:
                        powerUps.activarInvencibilidad(10);
                        mundo.agregarNotificacion("🛡 Invencibilidad activada!", Color.CYAN, 3);
                        break;
                    case VELOCIDAD:
                        powerUps.aumentarVelocidad(50, 15);
                        mundo.agregarNotificacion("⚡ Velocidad aumentada!", Color.YELLOW, 3);
                        break;
                    case ATAQUE:
                        powerUps.aumentarAtaque(30, 20);
                        mundo.agregarNotificacion("💪 Ataque aumentado!", Color.RED, 3);
                        break;
                    case CURACION:
                        vidaActual = Math.min(vidaActual + 30, vidaMaxima);
                        mundo.agregarNotificacion("❤ +30 de vida!", Color.GREEN, 3);
                        break;
                }

                mundo.estadisticas.registrarCofreRecogido();
                mundo.objs[index] = null;
            } else if (mundo.objs[index] instanceof CofreNormal) {
                mundo.estadisticas.registrarCofreRecogido();
                mundo.agregarNotificacion("📦 Cofre encontrado! +50 EXP", Color.ORANGE, 3);
                mundo.estadisticas.ganarExperiencia(50);
                mundo.objs[index] = null;
            }
        }
    }
}
