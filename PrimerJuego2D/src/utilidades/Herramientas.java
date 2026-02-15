package utilidades;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

/**
 * Clase de utilidad para el escalado optimizado de imágenes.
 * Usa interpolación NEAREST_NEIGHBOR para mantener la nitidez del pixel art.
 */
public class Herramientas {

    /**
     * Escala una imagen a un tamaño específico manteniendo la nitidez del pixel art.
     */
    public BufferedImage escalarImagen(BufferedImage original, int ancho, int alto) {
        BufferedImage imagenScalada = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = imagenScalada.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(original, 0, 0, ancho, alto, null);
        g2.dispose();
        return imagenScalada;
    }

    /**
     * Crea una imagen de color sólido (útil para placeholders).
     */
    public BufferedImage crearImagenColor(int ancho, int alto, Color color) {
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = imagen.createGraphics();
        g2.setColor(color);
        g2.fillRect(0, 0, ancho, alto);
        g2.dispose();
        return imagen;
    }

    /**
     * Voltea una imagen horizontalmente (efecto espejo).
     * Útil para crear sprites de dirección opuesta.
     */
    public BufferedImage voltearImagenHorizontal(BufferedImage original) {
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-original.getWidth(), 0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(original, null);
    }
}
