package items;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import configuracion.Configuracion;
import utilidades.Herramientas;
import mundo.MundoJuego;

/**
 * Clase base para todos los objetos interactivos del mundo.
 */
public class SuperObjeto {

    public BufferedImage imagen;
    public String nombre;
    public boolean colision;
    public int worldX, worldY;
    protected Herramientas miTool = new Herramientas();

    public Rectangle AreaSolida = new Rectangle(0, 0, 48, 48);
    public int AreaSolidaDefaultX = 0;
    public int AreaSolidaDefaultY = 0;

    public void draw(Graphics2D g2, MundoJuego mundo) {
        int screenX = worldX - mundo.jugador.worldx + mundo.jugador.screenX;
        int screenY = worldY - mundo.jugador.worldy + mundo.jugador.screeny;

        int tile = Configuracion.TAMANO_TILE;
        if (worldX + tile > mundo.jugador.worldx - mundo.jugador.screenX
                && worldX - tile < mundo.jugador.worldx + mundo.jugador.screenX
                && worldY + tile > mundo.jugador.worldy - mundo.jugador.screeny
                && worldY - tile < mundo.jugador.worldy + mundo.jugador.screeny) {
            g2.drawImage(imagen, screenX, screenY, null);
        }
    }
}
