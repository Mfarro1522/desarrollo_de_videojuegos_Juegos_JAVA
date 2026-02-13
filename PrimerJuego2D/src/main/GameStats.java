package main;

import java.awt.Color;

/**
 * Gestiona las estadísticas del juego (tiempo, enemigos eliminados, daño recibido, etc.)
 */
public class GameStats {
    
    // Estadísticas de combate
    public int enemigosDerrotados = 0;
    public int ataquesRecibidos = 0;
    public int danioTotalRecibido = 0;
    
    // Tiempo de juego
    public long tiempoInicio = 0;
    public long tiempoSobrevivido = 0; // En segundos
    
    // Power-ups
    public int cofresRecogidos = 0;
    public int powerUpsActivos = 0;
    
    // Nivel
    public int nivel = 1;
    public int experiencia = 0;
    public int experienciaSiguienteNivel = 100;
    
	// Referencia al panel de juego para las notificaciones
	private PanelJuego pj;
	
	// Récords
	public static long recordTiempoSobrevivido = 0; // Estático para persistir entre juegos
	public boolean nuevoRecord = false;
	
	public void setPanelJuego(PanelJuego pj) {
		this.pj = pj;
	}
	
	public void iniciar() {
		tiempoInicio = System.currentTimeMillis();
	}
    
    public void actualizar() {
        if (tiempoInicio > 0) {
            tiempoSobrevivido = (System.currentTimeMillis() - tiempoInicio) / 1000;
        }
    }
    
    public void registrarAtaqueRecibido(int dano) {
        ataquesRecibidos++;
        danioTotalRecibido += dano;
    }
    
    public void registrarEnemigoEliminado() {
        enemigosDerrotados++;
    }
    
    public void registrarCofreRecogido() {
        cofresRecogidos++;
    }
    
    public void ganarExperiencia(int exp) {
        experiencia += exp;
        while (experiencia >= experienciaSiguienteNivel) {
            subirNivel();
        }
    }
    
    private void subirNivel() {
        experiencia -= experienciaSiguienteNivel;
        nivel++;
        experienciaSiguienteNivel = (int)(experienciaSiguienteNivel * 1.5);
		
		// Mostrar notificación de subida de nivel
		if (pj != null) {
			pj.agregarNotificacion("⭐ ¡NIVEL " + nivel + "!", Color.YELLOW, 3);
		}
    }
    
    public void finalizarJuego() {
    	actualizar();
        if (tiempoSobrevivido > recordTiempoSobrevivido) {
            recordTiempoSobrevivido = tiempoSobrevivido;
            nuevoRecord = true;
        }
    }
    
    public String formatearTiempo(long segundos) {
        long minutos = segundos / 60;
        long segs = segundos % 60;
        return String.format("%02d:%02d", minutos, segs);
    }
}
