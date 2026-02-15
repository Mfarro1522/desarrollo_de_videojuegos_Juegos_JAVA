package tiles;

import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.imageio.ImageIO;

import configuracion.Configuracion;
import mundo.MundoJuego;
import utilidades.Herramientas;

/**
 * Gestiona la carga, generación y renderizado del mapa de tiles.
 *
 * OPTIMIZACIONES:
 * - Renderizado por rango visible: solo dibuja las ~16x12 tiles en pantalla.
 * - Sprites pre-escalados: las imágenes se escalan una sola vez al cargar.
 */
public class TileManager {

    MundoJuego mundo;

    public Tile[] tiles;
    public int[][] mapaPorNumeroTile;

    private static final int MAX_TIPOS_TILE = 50;

    public TileManager(MundoJuego mundo) {
        this.mundo = mundo;
        tiles = new Tile[MAX_TIPOS_TILE];
        mapaPorNumeroTile = new int[Configuracion.MUNDO_COLUMNAS][Configuracion.MUNDO_FILAS];
        getImagenTile("/tiles/rutaTiles.txt");
        generarMapaProcedural();
    }

    public void getImagenTile(String rutaFile) {
        Herramientas tool = new Herramientas();
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
                if (linea.isEmpty())
                    continue;

                String[] partes = linea.split(";");
                if (partes.length < 2)
                    continue;

                String rutaImagen = partes[0].trim();
                int colision = Integer.parseInt(partes[1].trim());

                int indice = extraerIndiceTile(rutaImagen);
                if (indice < 0 || indice >= MAX_TIPOS_TILE)
                    continue;

                Tile tile = new Tile();
                InputStream imgStream = getClass().getResourceAsStream(rutaImagen);
                if (imgStream == null) {
                    System.err.println("[TileManager] Imagen no encontrada, saltando: " + rutaImagen);
                    continue;
                }
                tile.imagen = tool.escalarImagen(
                        ImageIO.read(imgStream),
                        Configuracion.TAMANO_TILE, Configuracion.TAMANO_TILE);
                tile.colision = (colision == 1);
                tiles[indice] = tile;
            }
            br.close();
        } catch (Exception e) {
            System.err.println("[TileManager] Error al cargar tiles: " + e.getMessage());
            e.printStackTrace();
        }

        cargarTilesManuales(tool);
        cargarTilesAdicionales(tool);
    }

    private void cargarTilesManuales(Herramientas tool) {
        try {
            // TILE 0: Pasto Principal (Base)
            cargarTileIndividual(0, "/tiles/pastoPrincipal.png", false, tool);

            // TILE 2: Sólido / Obstáculo
            cargarTileIndividual(2, "/tiles/Solido_0001.png", true, tool);
            cargarTileIndividual(3, "/tiles/Solido_0002.png", true, tool);

            // TILES 20-24: Variantes de Pasto
            cargarTileIndividual(30, "/tiles/pastoVariantes_0001.png", false, tool);
            cargarTileIndividual(31, "/tiles/pastoVariantes_0002.png", false, tool);
            cargarTileIndividual(32, "/tiles/pastoVariantes_0003.png", false, tool);
            cargarTileIndividual(33, "/tiles/pastoVariantes_0004.png", false, tool);

        } catch (Exception e) {
            System.err.println("[TileManager] Error cargando tiles manuales: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cargarTileIndividual(int id, String ruta, boolean colision, Herramientas tool) throws Exception {
        if (id < 0 || id >= MAX_TIPOS_TILE)
            return;

        InputStream is = getClass().getResourceAsStream(ruta);
        if (is != null) {
            Tile tile = new Tile();
            tile.imagen = tool.escalarImagen(
                    ImageIO.read(is), Configuracion.TAMANO_TILE, Configuracion.TAMANO_TILE);
            tile.colision = colision;
            tiles[id] = tile;
            is.close();
        } else {
            System.err.println("[TileManager] No se encontró imagen: " + ruta);
        }
    }

    private int extraerIndiceTile(String ruta) {
        try {
            String nombre = ruta.substring(ruta.lastIndexOf('/') + 1);
            nombre = nombre.replace("tile_", "").replace(".png", "");
            return Integer.parseInt(nombre);
        } catch (Exception e) {
            return -1;
        }
    }

    private void cargarTilesAdicionales(Herramientas tool) {
        for (int i = 0; i < MAX_TIPOS_TILE; i++) {
            if (tiles[i] != null)
                continue;

            String ruta = "/tiles/tile_" + String.format("%02d", i) + ".png";
            try {
                InputStream is = getClass().getResourceAsStream(ruta);
                if (is != null) {
                    Tile tile = new Tile();
                    tile.imagen = tool.escalarImagen(
                            ImageIO.read(is), Configuracion.TAMANO_TILE, Configuracion.TAMANO_TILE);
                    tile.colision = true;
                    tiles[i] = tile;
                    is.close();
                }
            } catch (Exception e) {
                // Tile no existe
            }
        }
    }

    private void generarMapaProcedural() {

        int[] obstacleVariantes = { 2, 3 }; // Sólidos

        // Variantes de pasto: Se repite el 0 para que sea el predominante
        int[] pastoVariantes = {
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
                30,
                31, 31, 31,
                32,
                33, 33, 33,
        };

        for (int tv : pastoVariantes) {
            if (tiles[tv] != null)
                tiles[tv].colision = false;
        }
        for (int ov : obstacleVariantes) {
            if (tiles[ov] != null)
                tiles[ov].colision = true;
        }

        int centroCol = Configuracion.MUNDO_COLUMNAS / 2;
        int centroFila = Configuracion.MUNDO_FILAS / 2;
        int radioSeguro = 5;

        for (int col = 0; col < Configuracion.MUNDO_COLUMNAS; col++) {
            for (int fila = 0; fila < Configuracion.MUNDO_FILAS; fila++) {
                int distCol = Math.abs(col - centroCol);
                int distFila = Math.abs(fila - centroFila);
                if (distCol <= radioSeguro && distFila <= radioSeguro) {
                    mapaPorNumeroTile[col][fila] = elegirPastoAleatorio(pastoVariantes);
                    continue;
                }

                if (col == 0 || col == Configuracion.MUNDO_COLUMNAS - 1
                        || fila == 0 || fila == Configuracion.MUNDO_FILAS - 1) {
                    mapaPorNumeroTile[col][fila] = obstacleVariantes[(int) (Math.random() * obstacleVariantes.length)];
                    continue;
                }

                double random = Math.random();
                if (random < 0.03) {
                    mapaPorNumeroTile[col][fila] = obstacleVariantes[(int) (Math.random() * obstacleVariantes.length)];
                } else {
                    mapaPorNumeroTile[col][fila] = elegirPastoAleatorio(pastoVariantes);
                }
            }
        }
    }

    private int elegirPastoAleatorio(int[] variantes) {
        int idx = (int) (Math.random() * variantes.length);
        return variantes[idx];
    }

    /**
     * Dibuja SOLO los tiles visibles en pantalla.
     * OPTIMIZACIÓN: Calcula el rango de columnas/filas visibles según la posición
     * del jugador y solo itera ese subconjunto.
     */
    public void draw(Graphics2D g2) {
        int tile = Configuracion.TAMANO_TILE;

        int jugadorWorldX = mundo.jugador.worldx;
        int jugadorWorldY = mundo.jugador.worldy;
        int jugadorScreenX = mundo.jugador.screenX;
        int jugadorScreenY = mundo.jugador.screeny;

        int colInicio = Math.max(0, (jugadorWorldX - jugadorScreenX) / tile - 1);
        int colFin = Math.min(Configuracion.MUNDO_COLUMNAS - 1,
                (jugadorWorldX + jugadorScreenX) / tile + 2);
        int filaInicio = Math.max(0, (jugadorWorldY - jugadorScreenY) / tile - 1);
        int filaFin = Math.min(Configuracion.MUNDO_FILAS - 1,
                (jugadorWorldY + jugadorScreenY) / tile + 2);

        for (int col = colInicio; col <= colFin; col++) {
            for (int fila = filaInicio; fila <= filaFin; fila++) {
                int tileNum = mapaPorNumeroTile[col][fila];

                int worldX = col * tile;
                int worldY = fila * tile;

                int screenX = worldX - jugadorWorldX + jugadorScreenX;
                int screenY = worldY - jugadorWorldY + jugadorScreenY;

                if (tiles[tileNum] != null) {
                    g2.drawImage(tiles[tileNum].imagen, screenX, screenY, null);
                }
            }
        }
    }
}
