package configuracion;

/**
 * Constantes globales del juego centralizadas.
 * Todos los valores de configuración (tiles, pantalla, mundo, rendimiento)
 * se definen aquí para evitar dispersión y duplicación.
 */
public final class Configuracion {

    private Configuracion() {} // No instanciable

    // ===== TILE =====
    public static final int TILE_ORIGINAL = 32;
    public static final int ESCALA = 2;
    public static final int TAMANO_TILE = TILE_ORIGINAL * ESCALA; // 64x64

    // ===== PANTALLA (relación 4:3 clásica) =====
    public static final int MAX_COLUMNAS_PANTALLA = 16;
    public static final int MAX_FILAS_PANTALLA = 12;
    public static final int ANCHO_PANTALLA = TAMANO_TILE * MAX_COLUMNAS_PANTALLA; // 1024
    public static final int ALTO_PANTALLA = TAMANO_TILE * MAX_FILAS_PANTALLA;     // 768

    // ===== MUNDO =====
    public static final int MUNDO_COLUMNAS = 100;
    public static final int MUNDO_FILAS = 100;
    public static final int MUNDO_ANCHO = MUNDO_COLUMNAS * TAMANO_TILE;
    public static final int MUNDO_ALTO = MUNDO_FILAS * TAMANO_TILE;

    // ===== RENDIMIENTO =====
    public static final int FPS = 60;

    // ===== CAPACIDADES =====
    public static final int MAX_OBJETOS = 15;
    public static final int MAX_PROYECTILES = 100;

    // ===== ESTADOS DEL JUEGO =====
    public static final int ESTADO_TITULO = 0;
    public static final int ESTADO_JUGANDO = 1;
    public static final int ESTADO_PAUSA = 2;
    public static final int ESTADO_DIALOGO = 3;
    public static final int ESTADO_GAME_OVER = 4;
    public static final int ESTADO_MENU = 5;
    public static final int ESTADO_SELECCION = 6;
    public static final int ESTADO_CREDITOS = 7;
    public static final int ESTADO_AYUDA = 8;
    public static final int ESTADO_LOGROS = 9;
}
