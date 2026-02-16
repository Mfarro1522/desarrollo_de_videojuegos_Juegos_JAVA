package estadisticas;

import java.awt.Color;
import java.io.*;

/**
 * Gestiona las estadísticas del juego (tiempo, enemigos eliminados, daño, etc.)
 * Guarda y carga récords desde res/stats/stats.txt
 */
public class Estadisticas {

    // Estadísticas de combate
    public int enemigosDerrotados = 0;
    public int ataquesRecibidos = 0;
    public int danioTotalRecibido = 0;

    // Tiempo de juego
    public long tiempoInicio = 0;
    public long tiempoSobrevivido = 0;

    // Power-ups
    public int cofresRecogidos = 0;
    public int powerUpsActivos = 0;

    // Nivel
    public int nivel = 1;
    public int experiencia = 0;
    public int experienciaSiguienteNivel = 100;

    // Callback para notificaciones (inyectado por MundoJuego)
    private NotificacionCallback callbackNotificacion;

    // Callback para lógica de nivel (inyectado por MundoJuego)
    private LevelUpCallback callbackLevelUp;

    // Récords y estadísticas acumuladas (estáticas para persistir entre juegos)
    public static long recordTiempoSobrevivido = 0;
    public static int enemigosTotalesEliminados = 0;
    public static int cofresTotalesRecogidos = 0;
    public static int partidasJugadas = 0;
    public static int nivelMaximoAlcanzado = 1;
    public static int danioTotalRecibidoAcumulado = 0;
    public boolean nuevoRecord = false;

    /** Interfaz funcional para desacoplar notificaciones. */
    public interface NotificacionCallback {
        void notificar(String mensaje, Color color, int duracionSegundos);
    }

    /** Interfaz funcional para lógica de subida de nivel. */
    public interface LevelUpCallback {
        void onLevelUp(int nuevoNivel);
    }

    public void setCallbackNotificacion(NotificacionCallback cb) {
        this.callbackNotificacion = cb;
    }

    public void setCallbackLevelUp(LevelUpCallback cb) {
        this.callbackLevelUp = cb;
    }

    public void iniciar() {
        tiempoInicio = System.currentTimeMillis();
        partidasJugadas++;
    }

    public void actualizar() {
        if (tiempoInicio > 0) {
            tiempoSobrevivido = (System.currentTimeMillis() - tiempoInicio) / 1000;
        }
    }

    public void registrarAtaqueRecibido(int dano) {
        ataquesRecibidos++;
        danioTotalRecibido += dano;
        danioTotalRecibidoAcumulado += dano;
    }

    public void registrarEnemigoEliminado() {
        enemigosDerrotados++;
        enemigosTotalesEliminados++;
    }

    public void registrarCofreRecogido() {
        cofresRecogidos++;
        cofresTotalesRecogidos++;
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
        experienciaSiguienteNivel = (int) (experienciaSiguienteNivel * 1.5);

        if (callbackNotificacion != null) {
            callbackNotificacion.notificar("⭐ ¡NIVEL " + nivel + "!", Color.YELLOW, 3);
        }

        if (callbackLevelUp != null) {
            callbackLevelUp.onLevelUp(nivel);
        }
    }

    public void finalizarJuego() {
        actualizar();
        if (tiempoSobrevivido > recordTiempoSobrevivido) {
            recordTiempoSobrevivido = tiempoSobrevivido;
            nuevoRecord = true;
        }
        if (nivel > nivelMaximoAlcanzado) {
            nivelMaximoAlcanzado = nivel;
        }
        guardarStats();
    }

    public String formatearTiempo(long segundos) {
        long minutos = segundos / 60;
        long segs = segundos % 60;
        return String.format("%02d:%02d", minutos, segs);
    }

    // ===== PERSISTENCIA =====

    private static final String STATS_FILE = "res/stats/stats.txt";

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
                if (linea.isEmpty() || linea.startsWith("#"))
                    continue;

                String[] partes = linea.split("=");
                if (partes.length != 2)
                    continue;

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

    public static void guardarStats() {
        try {
            File dir = new File("res/stats");
            if (!dir.exists())
                dir.mkdirs();

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
