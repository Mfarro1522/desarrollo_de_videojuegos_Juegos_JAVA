package items;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;
import javax.imageio.ImageIO;

import configuracion.Configuracion;
import mundo.MundoJuego;

/**
 * Cofre que otorga power-ups al jugador.
 */
public class CofrePowerUp extends SuperObjeto {

    public enum TipoPowerUp {
        INVENCIBILIDAD, VELOCIDAD, ATAQUE, CURACION
    }

    public TipoPowerUp tipoPowerUp;

    public CofrePowerUp(int tamanioTile, TipoPowerUp tipo) {
        this.tipoPowerUp = tipo;
        nombre = "Cofre Power-Up";

        try {
            imagen = ImageIO.read(getClass().getResourceAsStream("/objetos/cofre.png"));
            imagen = miTool.escalarImagen(imagen, tamanioTile, tamanioTile);
        } catch (IOException e) {
            imagen = miTool.crearImagenColor(tamanioTile, tamanioTile, obtenerColorPorTipo());
        }

        colision = true;
    }

    private Color obtenerColorPorTipo() {
        switch (tipoPowerUp) {
            case INVENCIBILIDAD: return Color.CYAN;
            case VELOCIDAD: return Color.YELLOW;
            case ATAQUE: return Color.RED;
            case CURACION: return Color.GREEN;
            default: return Color.MAGENTA;
        }
    }

    @Override
    public void draw(Graphics2D g2, MundoJuego mundo) {
        int screenX = worldX - mundo.jugador.worldx + mundo.jugador.screenX;
        int screenY = worldY - mundo.jugador.worldy + mundo.jugador.screeny;

        int tile = Configuracion.TAMANO_TILE;
        if (worldX + tile > mundo.jugador.worldx - mundo.jugador.screenX
                && worldX - tile < mundo.jugador.worldx + mundo.jugador.screenX
                && worldY + tile > mundo.jugador.worldy - mundo.jugador.screeny
                && worldY - tile < mundo.jugador.worldy + mundo.jugador.screeny) {

            g2.drawImage(imagen, screenX, screenY, null);

            g2.setColor(new Color(obtenerColorPorTipo().getRed(),
                    obtenerColorPorTipo().getGreen(),
                    obtenerColorPorTipo().getBlue(), 100));
            g2.fillRect(screenX, screenY, tile, tile);
        }
    }
}
