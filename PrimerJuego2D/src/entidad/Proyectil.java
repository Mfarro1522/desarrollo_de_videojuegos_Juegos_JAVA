package entidad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import main.PanelJuego;

/**
 * Proyectil disparado por el jugador automáticamente
 */
public class Proyectil {
    
    public int worldX, worldY;
    public int velocidad = 6;
    public String direccion;
    public boolean activo = true;
    
    private PanelJuego pj;
    private int dano;
    private int tamano = 16;
    private int distanciaRecorrida = 0;
    private int alcanceMaximo = 400; // Píxeles
    
    // Tipos de proyectil
    public enum TipoProyectil {
        BASICO,    // Proyectil básico
        LATIGO,    // Látigo de área
        VARITA     // Disparo mágico
    }
    
    public TipoProyectil tipo = TipoProyectil.BASICO;
    
    public Proyectil(PanelJuego pj, int x, int y, String direccion, int dano) {
        this.pj = pj;
        this.worldX = x;
        this.worldY = y;
        this.direccion = direccion;
        this.dano = dano;
    }
    
    public void update() {
        if (!activo) return;
        
        // Mover proyectil
        switch (direccion) {
            case "arriba": worldY -= velocidad; break;
            case "abajo": worldY += velocidad; break;
            case "izquierda": worldX -= velocidad; break;
            case "derecha": worldX += velocidad; break;
        }
        
        distanciaRecorrida += velocidad;
        
        // Desactivar si alcanzó el límite
        if (distanciaRecorrida >= alcanceMaximo) {
            activo = false;
            return;
        }
        
        // Verificar colisión con NPCs
        Rectangle proyectilArea = new Rectangle(worldX, worldY, tamano, tamano);
        
        for (int i = 0; i < pj.npcs.length; i++) {
            if (pj.npcs[i] != null && pj.npcs[i].estaVivo) {
                Rectangle npcArea = new Rectangle(
                    pj.npcs[i].worldx + pj.npcs[i].AreaSolida.x,
                    pj.npcs[i].worldy + pj.npcs[i].AreaSolida.y,
                    pj.npcs[i].AreaSolida.width,
                    pj.npcs[i].AreaSolida.height
                );
                
                if (proyectilArea.intersects(npcArea)) {
                    pj.npcs[i].recibirDanio(dano);
                    activo = false;
                    
                    // Si el NPC murió, dar experiencia
                    if (!pj.npcs[i].estaVivo) {
                        pj.stats.registrarEnemigoEliminado();
                        pj.stats.ganarExperiencia(pj.npcs[i].experienciaAOtorgar);
                    }
                    break;
                }
            }
        }
    }
    
    public void draw(Graphics2D g2) {
        if (!activo) return;
        
        int screenX = worldX - pj.jugador.worldx + pj.jugador.screenX;
        int screenY = worldY - pj.jugador.worldy + pj.jugador.screeny;
        
        // Dibujar proyectil según tipo
        switch (tipo) {
            case BASICO:
                g2.setColor(Color.YELLOW);
                g2.fillOval(screenX, screenY, tamano, tamano);
                g2.setColor(Color.ORANGE);
                g2.drawOval(screenX, screenY, tamano, tamano);
                break;
            case LATIGO:
                g2.setColor(new Color(139, 69, 19)); // Marrón
                g2.fillRect(screenX, screenY, tamano * 2, tamano / 2);
                break;
            case VARITA:
                g2.setColor(Color.CYAN);
                g2.fillOval(screenX, screenY, tamano, tamano);
                g2.setColor(Color.WHITE);
                g2.fillOval(screenX + 4, screenY + 4, tamano / 2, tamano / 2);
                break;
        }
    }
}
