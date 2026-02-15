package entidad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import configuracion.Configuracion;
import mundo.MundoJuego;

/**
 * Proyectil disparado por el jugador automáticamente.
 */
public class Proyectil {

    public int worldX, worldY;
    public int velocidad = 6;
    public String direccion;
    public boolean activo = true;

    private MundoJuego mundo;
    private int dano;
    private int tamano = 16;
    private int distanciaRecorrida = 0;
    private int alcanceMaximo = 400;

    private final Rectangle npcArea = new Rectangle();

    public enum TipoProyectil { BASICO, LATIGO, VARITA }
    public TipoProyectil tipo = TipoProyectil.BASICO;

    public Proyectil(MundoJuego mundo, int x, int y, String direccion, int dano) {
        this.mundo = mundo;
        this.worldX = x;
        this.worldY = y;
        this.direccion = direccion;
        this.dano = dano;
    }

    public void update() {
        if (!activo) return;

        switch (direccion) {
            case "arriba":    worldY -= velocidad; break;
            case "abajo":     worldY += velocidad; break;
            case "izquierda": worldX -= velocidad; break;
            case "derecha":   worldX += velocidad; break;
        }

        distanciaRecorrida += velocidad;

        if (distanciaRecorrida >= alcanceMaximo) {
            activo = false;
            return;
        }

        // Colisión con NPCs usando Spatial Hash Grid
        Rectangle proyectilArea = new Rectangle(worldX, worldY, tamano, tamano);

        mundo.grillaEspacial.consultar(worldX, worldY);
        int[] cercanos = mundo.grillaEspacial.getResultado();
        int count = mundo.grillaEspacial.getResultadoCount();

        for (int j = 0; j < count; j++) {
            int i = cercanos[j];
            if (mundo.npcs[i] != null && mundo.npcs[i].activo && mundo.npcs[i].estaVivo) {
                npcArea.setBounds(
                        mundo.npcs[i].worldx + mundo.npcs[i].AreaSolida.x,
                        mundo.npcs[i].worldy + mundo.npcs[i].AreaSolida.y,
                        mundo.npcs[i].AreaSolida.width,
                        mundo.npcs[i].AreaSolida.height);

                if (proyectilArea.intersects(npcArea)) {
                    mundo.npcs[i].recibirDanio(dano);
                    activo = false;

                    if (!mundo.npcs[i].estaVivo) {
                        mundo.estadisticas.registrarEnemigoEliminado();
                        mundo.estadisticas.ganarExperiencia(mundo.npcs[i].experienciaAOtorgar);
                    }
                    break;
                }
            }
        }
    }

    public void draw(Graphics2D g2) {
        if (!activo) return;

        int screenX = worldX - mundo.jugador.worldx + mundo.jugador.screenX;
        int screenY = worldY - mundo.jugador.worldy + mundo.jugador.screeny;

        switch (tipo) {
            case BASICO:
                g2.setColor(Color.YELLOW);
                g2.fillOval(screenX, screenY, tamano, tamano);
                g2.setColor(Color.ORANGE);
                g2.drawOval(screenX, screenY, tamano, tamano);
                break;
            case LATIGO:
                g2.setColor(new Color(139, 69, 19));
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
