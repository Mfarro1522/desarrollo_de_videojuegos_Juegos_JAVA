package main;

/**
 * Grilla Espacial Hash (Spatial Hash Grid) para partición de entidades.
 * Reduce verificaciones de colisión de O(N²) a ~O(N) dividiendo
 * el mundo en celdas y verificando solo entidades en celdas adyacentes.
 *
 * Diseño zero-allocation: usa arrays pre-allocados para
 * evitar presión sobre el Garbage Collector durante el gameplay.
 */
public class SpatialHashGrid {

    private final int tamanioCelda;
    private final int columnas;
    private final int filas;
    private final int totalCeldas;

    // Cada celda almacena índices al array de NPCs
    private final int[][] celdaIndices;
    private final int[] celdaContador;
    private static final int MAX_POR_CELDA = 32;

    // Buffer reutilizable para resultados de consulta (cero allocations)
    private final int[] resultado;
    private int resultadoCount;

    /**
     * Crea una grilla espacial para el mundo dado.
     *
     * @param anchoMundo   ancho total del mundo en píxeles
     * @param altoMundo    alto total del mundo en píxeles
     * @param tamanioCelda tamaño de cada celda en píxeles (recomendado: 2-4 tiles)
     */
    public SpatialHashGrid(int anchoMundo, int altoMundo, int tamanioCelda) {
        this.tamanioCelda = tamanioCelda;
        this.columnas = (anchoMundo / tamanioCelda) + 1;
        this.filas = (altoMundo / tamanioCelda) + 1;
        this.totalCeldas = columnas * filas;

        celdaIndices = new int[totalCeldas][MAX_POR_CELDA];
        celdaContador = new int[totalCeldas];
        resultado = new int[MAX_POR_CELDA * 9]; // 9 celdas adyacentes máximo
    }

    /**
     * Limpia todas las celdas. Llamar al inicio de cada frame antes de insertar.
     */
    public void limpiar() {
        for (int i = 0; i < totalCeldas; i++) {
            celdaContador[i] = 0;
        }
        resultadoCount = 0;
    }

    /**
     * Inserta un índice de NPC en la celda correspondiente a su posición.
     *
     * @param indice índice del NPC en el array principal
     * @param worldX posición X en el mundo
     * @param worldY posición Y en el mundo
     */
    public void insertar(int indice, int worldX, int worldY) {
        int celdaIdx = obtenerIndiceCelda(worldX, worldY);
        if (celdaIdx < 0 || celdaIdx >= totalCeldas) return;

        int count = celdaContador[celdaIdx];
        if (count < MAX_POR_CELDA) {
            celdaIndices[celdaIdx][count] = indice;
            celdaContador[celdaIdx]++;
        }
    }

    /**
     * Consulta NPCs en la celda y celdas adyacentes (3x3) a la posición dada.
     * Resultado almacenado internamente: usar getResultado() y getResultadoCount().
     *
     * @param worldX posición X de consulta
     * @param worldY posición Y de consulta
     */
    public void consultar(int worldX, int worldY) {
        resultadoCount = 0;
        int col = clamp(worldX / tamanioCelda, 0, columnas - 1);
        int fila = clamp(worldY / tamanioCelda, 0, filas - 1);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int c = col + dx;
                int f = fila + dy;
                if (c >= 0 && c < columnas && f >= 0 && f < filas) {
                    int celdaIdx = c * filas + f;
                    int count = celdaContador[celdaIdx];
                    for (int i = 0; i < count && resultadoCount < resultado.length; i++) {
                        resultado[resultadoCount++] = celdaIndices[celdaIdx][i];
                    }
                }
            }
        }
    }

    /** Retorna el buffer de resultados (no crear copia). */
    public int[] getResultado() {
        return resultado;
    }

    /** Retorna cuántos índices válidos hay en el resultado. */
    public int getResultadoCount() {
        return resultadoCount;
    }

    private int obtenerIndiceCelda(int worldX, int worldY) {
        int col = clamp(worldX / tamanioCelda, 0, columnas - 1);
        int fila = clamp(worldY / tamanioCelda, 0, filas - 1);
        return col * filas + fila;
    }

    private int clamp(int valor, int min, int max) {
        return Math.max(min, Math.min(valor, max));
    }
}
