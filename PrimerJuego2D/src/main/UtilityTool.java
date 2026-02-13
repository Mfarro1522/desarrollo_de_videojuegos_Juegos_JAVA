package main;
/**
*clase de utilidad para el escalado optimizado de imágenes.
* Usa
interpolación NEAREST_NEIGHBOR para mantener la nitidez del los pixel art.
*/

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class UtilityTool {
	
	/**
	* Escala una imagen a un tamaño específico manteniendo la nitidez del pixel art.
	*
	* @param original - Imagen original a escalar
	* @param width - Ancho deseado
	* @param height - Alto deseado
	* @return BufferedImage escalada
	*/
	
	public BufferedImage escalarImagen (BufferedImage original , int ancho , int alto ) {
		// 1. Crear nueva imagen en blanco del tamaño deseado
		BufferedImage imagenScalada = new BufferedImage(ancho, alto, original.getType());
		// 2. Obtener el contexto gráfico
		Graphics2D g2 = imagenScalada.createGraphics();
		
		//Importante en linux crear interpolacion para el pixel art
		// Esto evita el efecto borroso en imágenes pequeñas escaladas
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		// 4. Dibujar la imagen original en el nuevo tamaño
		g2.drawImage(original,0,0,ancho,alto,null);
		// 5. Liberar recursos
		g2.dispose();
		// 6. Retornar imagen escalada
		return imagenScalada;
	}
	
	/**
	 * Crea una imagen de color sólido (útil para placeholders)
	 * @param ancho - Ancho de la imagen
	 * @param alto - Alto de la imagen
	 * @param color - Color de relleno
	 * @return BufferedImage con el color especificado
	 */
	public BufferedImage crearImagenColor(int ancho, int alto, Color color) {
		BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = imagen.createGraphics();
		g2.setColor(color);
		g2.fillRect(0, 0, ancho, alto);
		g2.dispose();
		return imagen;
	}

}
