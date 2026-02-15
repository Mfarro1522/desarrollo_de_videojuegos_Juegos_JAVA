package tiles;

import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.imageio.ImageIO;
import main.PanelJuego;
import main.UtilityTool;

/**
 * Gestiona la carga, generación y renderizado del mapa de tiles.
 *
 * OPTIMIZACIONES:
 * - Renderizado por rango visible: solo dibuja las ~16x12 tiles en pantalla
 *   en lugar de iterar las 100x100 = 10,000 tiles del mundo.
 * - Sprites pre-escalados: las imágenes se escalan una sola vez al cargar.
 */
public class TileManager {

    PanelJuego pj;

    /** Array de tipos de tile disponibles (índice = número de tile). */
    public Tile[] tiles;

    /** Mapa del mundo: mapaPorNumeroTile[columna][fila] = índice de tile. */
    public int[][] mapaPorNumeroTile;

    /** Cantidad máxima de tipos de tile soportados. */
    private static final int MAX_TIPOS_TILE = 50;

    public TileManager(PanelJuego pj) {
        this.pj = pj;
        tiles = new Tile[MAX_TIPOS_TILE];
        mapaPorNumeroTile = new int[pj.maxWorldcol][pj.maxWorldfilas];
        getImagenTile("/tiles/rutaTiles.txt");
        generarMapaProcedural();
    }

    /**
     * Carga las definiciones de tiles desde el archivo de configuración.
     * Formato: /tiles/tile_XX.png;colision (0 = libre, 1 = sólido)
     * El número XX del nombre de archivo se usa como índice en tiles[].
     */
    public void getImagenTile(String rutaFile) {
        UtilityTool tool = new UtilityTool();
        try {
            InputStream is = getClass().getResourceAsStream(rutaFile);
            if (is == null) {
                System.err.println("[TileManager] No se encontró: " + rutaFile);
                return;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String linea;

            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                String[] partes = linea.split(";");
                if (partes.length < 2) continue;

                String rutaImagen = partes[0].trim();
                int colision = Integer.parseInt(partes[1].trim());

                // Extraer número de tile del nombre de archivo (tile_XX.png)
                int indice = extraerIndiceTile(rutaImagen);
                if (indice < 0 || indice >= MAX_TIPOS_TILE) continue;

                Tile tile = new Tile();
                tile.imagen = tool.escalarImagen(
                        ImageIO.read(getClass().getResourceAsStream(rutaImagen)),
                        pj.tamanioTile, pj.tamanioTile);
                tile.colision = (colision == 1);
                tiles[indice] = tile;
            }
            br.close();
        } catch (Exception e) {
            System.err.println("[TileManager] Error al cargar tiles: " + e.getMessage());
            e.printStackTrace();
        }

        // Cargar tiles adicionales que no están en rutaTiles.txt
        cargarTilesAdicionales(tool);
    }

    /**
     * Extrae el número de tile del nombre de archivo.
     * "/tiles/tile_17.png" -> 17
     */
    private int extraerIndiceTile(String ruta) {
        try {
            String nombre = ruta.substring(ruta.lastIndexOf('/') + 1);
            nombre = nombre.replace("tile_", "").replace(".png", "");
            return Integer.parseInt(nombre);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Carga tiles adicionales desde los archivos de imagen disponibles.
     * Completa cualquier tile faltante que no estuviera en rutaTiles.txt.
     */
    private void cargarTilesAdicionales(UtilityTool tool) {
        for (int i = 0; i < MAX_TIPOS_TILE; i++) {
            if (tiles[i] != null) continue; // Ya cargado

            String ruta = "/tiles/tile_" + String.format("%02d", i) + ".png";
            try {
                InputStream is = getClass().getResourceAsStream(ruta);
                if (is != null) {
                    Tile tile = new Tile();
                    tile.imagen = tool.escalarImagen(
                            ImageIO.read(is), pj.tamanioTile, pj.tamanioTile);
                    // Tiles no definidos en rutaTiles: marcar como sólidos por seguridad
                    tile.colision = true;
                    tiles[i] = tile;
                    is.close();
                }
            } catch (Exception e) {
                // Tile no existe, ignorar
            }
        }
    }

    /**
     * Genera un mapa procedural en memoria con pasto variado y obstáculos aleatorios.
     * Usa múltiples tiles de pasto para dar variedad visual al terreno.
     */
    private void generarMapaProcedural() {
        int baseTile = 17;       // Pasto base (colisión = 0)
        int obstacleTile = 2;    // Obstáculo sólido (colisión = 1)

        // Variantes de pasto para dar variedad visual (todos sin colisión)
        int[] pastoVariantes = { 17, 0, 1 ,17 ,17};

        // Asegurar que los tiles de pasto no tienen colisión
        for (int tv : pastoVariantes) {
            if (tiles[tv] != null) {
                tiles[tv].colision = false;
            }
        }
        if (tiles[obstacleTile] != null) {
            tiles[obstacleTile].colision = true;
        }

        // Centro del mundo: zona segura de spawn
        int centroCol = pj.maxWorldcol / 2;
        int centroFila = pj.maxWorldfilas / 2;
        int radioSeguro = 5;

        for (int col = 0; col < pj.maxWorldcol; col++) {
            for (int fila = 0; fila < pj.maxWorldfilas; fila++) {

                // Zona segura alrededor del spawn (sin obstáculos)
                int distCol = Math.abs(col - centroCol);
                int distFila = Math.abs(fila - centroFila);
                if (distCol <= radioSeguro && distFila <= radioSeguro) {
                    mapaPorNumeroTile[col][fila] = elegirPastoAleatorio(pastoVariantes);
                    continue;
                }

                // Bordes del mundo = obstáculos sólidos (muralla natural)
                if (col == 0 || col == pj.maxWorldcol - 1
                        || fila == 0 || fila == pj.maxWorldfilas - 1) {
                    mapaPorNumeroTile[col][fila] = obstacleTile;
                    continue;
                }

                // Interior: distribución aleatoria
                double random = Math.random();
                if (random < 0.03) {
                    mapaPorNumeroTile[col][fila] = obstacleTile; // 3% obstáculos (menos que antes)
                } else {
                    // 97% pasto con variedad visual
                    mapaPorNumeroTile[col][fila] = elegirPastoAleatorio(pastoVariantes);
                }
            }
        }
    }

    /**
     * Elige un tile de pasto aleatorio con peso: el tile base (17) es más frecuente.
     */
    private int elegirPastoAleatorio(int[] variantes) {
        double r = Math.random();
        if (r < 0.55) {
            return variantes[0]; // 55% tile base (17)
        } else {
            // 45% distribuido entre las demás variantes
            int idx = 1 + (int) (Math.random() * (variantes.length - 1));
            return variantes[idx];
        }
    }

    /**
     * Dibuja SOLO los tiles visibles en pantalla.
     *
     * OPTIMIZACIÓN: Calcula el rango de columnas/filas visibles según la posición
     * de la cámara, en lugar de iterar las 100x100 = 10,000 tiles del mundo.
     * Resultado: solo se dibujan ~16x12 = ~192 tiles por frame.
     */
    public void draw(Graphics2D g2) {
        // Calcular rango visible de tiles (con margen de 1 tile extra)
        int colInicio = Math.max(0, (pj.jugador.worldx - pj.jugador.screenX) / pj.tamanioTile - 1);
        int colFin = Math.min(pj.maxWorldcol - 1,
                (pj.jugador.worldx + pj.jugador.screenX) / pj.tamanioTile + 2);
        int filaInicio = Math.max(0, (pj.jugador.worldy - pj.jugador.screeny) / pj.tamanioTile - 1);
        int filaFin = Math.min(pj.maxWorldfilas - 1,
                (pj.jugador.worldy + pj.jugador.screeny) / pj.tamanioTile + 2);

        for (int col = colInicio; col <= colFin; col++) {
            for (int fila = filaInicio; fila <= filaFin; fila++) {
                int tileNum = mapaPorNumeroTile[col][fila];

                // Calcular posición en pantalla
                int worldX = col * pj.tamanioTile;
                int worldY = fila * pj.tamanioTile;
                int screenX = worldX - pj.jugador.worldx + pj.jugador.screenX;
                int screenY = worldY - pj.jugador.worldy + pj.jugador.screeny;

                // Dibujar tile si tiene imagen válida
                if (tileNum >= 0 && tileNum < MAX_TIPOS_TILE && tiles[tileNum] != null) {
                    g2.drawImage(tiles[tileNum].imagen, screenX, screenY, null);
                }
            }
        }
    }
}
