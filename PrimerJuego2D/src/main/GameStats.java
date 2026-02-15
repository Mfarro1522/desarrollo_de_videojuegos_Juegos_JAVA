package main;

import java.awt.Color;
import java.io.*;
import java.nio.file.*;

/**
 * Gestiona las estadísticas del juego (tiempo, enemigos eliminados, daño recibido, etc.)
 * Guarda y carga récords desde res/stats/stats.txt
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
	
	// Récords y estadísticas acumuladas (estáticas para persistir entre juegos)
	public static long recordTiempoSobrevivido = 0;
	public static int enemigosTotalesEliminados = 0;
	public static int cofresTotalesRecogidos = 0;
	public static int partidasJugadas = 0;
	public static int nivelMaximoAlcanzado = 1;
	public static int danioTotalRecibidoAcumulado = 0;
	public boolean nuevoRecord = false;
	
	public void setPanelJuego(PanelJuego pj) {
		this.pj = pj;
	}
	
	public void iniciar() {
		tiempoInicio = System.currentTimeMillis();
		partidasJugadas++; // Incrementar contador de partidas
	}
    
    public void actualizar() {
        if (tiempoInicio > 0) {
            tiempoSobrevivido = (System.currentTimeMillis() - tiempoInicio) / 1000;
        }
    }
    
    public void registrarAtaqueRecibido(int dano) {
        ataquesRecibidos++;
        danioTotalRecibido += dano;
        danioTotalRecibidoAcumulado += dano; // Acumular globalmente
    }
    
    public void registrarEnemigoEliminado() {
        enemigosDerrotados++;
        enemigosTotalesEliminados++; // Acumular globalmente
    }
    
    public void registrarCofreRecogido() {
        cofresRecogidos++;
        cofresTotalesRecogidos++; // Acumular globalmente
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
        // Actualizar nivel máximo alcanzado
        if (nivel > nivelMaximoAlcanzado) {
            nivelMaximoAlcanzado = nivel;
        }
        // Guardar stats al archivo
        guardarStats();
    }
    
    public String formatearTiempo(long segundos) {
        long minutos = segundos / 60;
        long segs = segundos % 60;
        return String.format("%02d:%02d", minutos, segs);
    }

    // ===== PERSISTENCIA EN ARCHIVO =====

    /** Ruta del archivo de stats */
    private static final String STATS_FILE = "res/stats/stats.txt";

    /**
     * Carga las estadísticas acumuladas desde el archivo al iniciar.
     */
    public static void cargarStats() {
        try {
            File archivo = new File(STATS_FILE);
            if (!archivo.exists()) {
                System.out.println("[Stats] No existe archivo de stats. Se usarán valores por defecto.");
                return;
            }

            BufferedReader br = new BufferedReader(new FileReader(archivo));
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) continue;

                String[] partes = linea.split("=");
                if (partes.length != 2) continue;

                String clave = partes[0].trim();
                String valor = partes[1].trim();

                switch (clave) {
                    case "recordTiempoSobrevivido":
                        recordTiempoSobrevivido = Long.parseLong(valor);
                        break;
                    case "enemigosTotalesEliminados":
                        enemigosTotalesEliminados = Integer.parseInt(valor);
                        break;
                    case "cofresTotalesRecogidos":
                        cofresTotalesRecogidos = Integer.parseInt(valor);
                        break;
                    case "partidasJugadas":
                        partidasJugadas = Integer.parseInt(valor);
                        break;
                    case "nivelMaximoAlcanzado":
                        nivelMaximoAlcanzado = Integer.parseInt(valor);
                        break;
                    case "danioTotalRecibidoAcumulado":
                        danioTotalRecibidoAcumulado = Integer.parseInt(valor);
                        break;
                }
            }
            br.close();
            System.out.println("[Stats] Estadísticas cargadas desde " + STATS_FILE);
        } catch (Exception e) {
            System.err.println("[Stats] Error al cargar stats: " + e.getMessage());
        }
    }

    /**
     * Guarda las estadísticas acumuladas en el archivo.
     */
    public static void guardarStats() {
        try {
            // Crear directorio si no existe
            File dir = new File("res/stats");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            PrintWriter pw = new PrintWriter(new FileWriter(STATS_FILE));
            pw.println("# Estadísticas acumuladas del juego");
            pw.println("# No editar manualmente");
            pw.println("recordTiempoSobrevivido=" + recordTiempoSobrevivido);
            pw.println("enemigosTotalesEliminados=" + enemigosTotalesEliminados);
            pw.println("cofresTotalesRecogidos=" + cofresTotalesRecogidos);
            pw.println("partidasJugadas=" + partidasJugadas);
            pw.println("nivelMaximoAlcanzado=" + nivelMaximoAlcanzado);
            pw.println("danioTotalRecibidoAcumulado=" + danioTotalRecibidoAcumulado);
            pw.close();
            System.out.println("[Stats] Estadísticas guardadas en " + STATS_FILE);
        } catch (Exception e) {
            System.err.println("[Stats] Error al guardar stats: " + e.getMessage());
        }
    }
}
