package entidad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import main.PanelJuego;
import main.UtilityTool;

/**
 * Enemigo básico tipo Slime.
 * Comportamiento: Persigue al jugador lentamente.
 */
public class Slime extends NPC {
    
    private UtilityTool tool = new UtilityTool();
    private BufferedImage sprite1, sprite2;
    
    public Slime(PanelJuego pj) {
        super(pj);
        
        // Estadísticas
        vidaMaxima = 20;
        vidaActual = vidaMaxima;
        ataque = 3;
        defensa = 0;
        vel = 1;
        direccion = "abajo";
        
        // IA
        radioDeteccion = 6 * pj.tamanioTile;
        experienciaAOtorgar = 10;
        
        cargarSprites();
    }
    
    private void cargarSprites() {
        try {
            // =====================================================================
            // TODO: AGREGAR IMÁGENES DE SPRITES PARA NPCs
            // =====================================================================
            // Coloca las imágenes de los enemigos en la carpeta: 
            // res/enemigos/
            //
            // Ejemplo de estructura recomendada:
            // res/
            //   enemigos/
            //     slime/
            //       slime_1.png  (sprite de animación 1)
            //       slime_2.png  (sprite de animación 2)
            //     goblin/
            //       goblin_1.png
            //       goblin_2.png
            //     golem/
            //       golem_1.png
            //       golem_2.png
            //
            // Luego reemplaza las siguientes líneas con:
            // BufferedImage temp1 = ImageIO.read(getClass().getResourceAsStream("/enemigos/slime/slime_1.png"));
            // BufferedImage temp2 = ImageIO.read(getClass().getResourceAsStream("/enemigos/slime/slime_2.png"));
            // =====================================================================
            
            // Sprites temporales usando colores (placeholders)
            BufferedImage temp1 = crearSpriteTemporal(Color.GREEN);
            BufferedImage temp2 = crearSpriteTemporal(new Color(0, 200, 0));
            
            sprite1 = tool.escalarImagen(temp1, pj.tamanioTile, pj.tamanioTile);
            sprite2 = tool.escalarImagen(temp2, pj.tamanioTile, pj.tamanioTile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Crea un sprite temporal de color sólido.
     */
    private BufferedImage crearSpriteTemporal(Color color) {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        
        // Dibujar un círculo (slime)
        g2.setColor(color);
        g2.fillOval(4, 4, 24, 24);
        
        // Detalles del slime
        g2.setColor(new Color(255, 255, 255, 150));
        g2.fillOval(10, 8, 6, 6);
        
        g2.setColor(Color.BLACK);
        g2.fillOval(8, 10, 3, 3);
        g2.fillOval(16, 10, 3, 3);
        
        g2.dispose();
        return img;
    }
    
    @Override
    public void actualizarIA() {
        perseguirJugador();
    }
    
    @Override
    protected BufferedImage obtenerSprite() {
        return (numeroSpites == 1) ? sprite1 : sprite2;
    }
}
