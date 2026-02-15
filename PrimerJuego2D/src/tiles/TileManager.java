package tiles;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;
import main.PanelJuego;
import main.UtilityTool;

public class TileManager {

	PanelJuego pj;
	public Tile[] tiles;
	public int mapaPorNumeroTile[][];
	private UtilityTool miTool = new UtilityTool();

	private void setup(int indice, String ruta, boolean colision) {
		try {
			// cargar imagen
			BufferedImage imagen_original = ImageIO.read(getClass().getResource(ruta));
			// escalarla
			tiles[indice].imagen = miTool.escalarImagen(imagen_original, pj.tamanioTile, pj.tamanioTile);
			tiles[indice].colision = colision;
		} catch (IOException e) {
			System.err.println("Error al cargar imagen: " + ruta);
			e.printStackTrace();
		}
	}

	public TileManager(PanelJuego pj) {
		this.pj = pj;
		mapaPorNumeroTile = new int[pj.maxWorldcol][pj.maxWorldfilas];
		getImagenTile("/tiles/rutaTiles.txt");
		generarMapaProcedural();
	}

	public void getImagenTile(String rutaTiles) {

		try {
			InputStream is = getClass().getResourceAsStream(rutaTiles);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			// para no usar array list
			int numTiles = 0;
			String linea;
			while ((linea = br.readLine()) != null) {
				if (!linea.trim().isEmpty()) {
					numTiles++;
				}
			}
			br.close();
			tiles = new Tile[numTiles];

			// se tiene que iniciar de nuevo despues del close
			is = getClass().getResourceAsStream(rutaTiles);
			br = new BufferedReader(new InputStreamReader(is));

			int indice = 0;
			while ((linea = br.readLine()) != null) {
				if (!linea.trim().isEmpty()) {
					String[] parametros = linea.split(";");

					if (parametros.length >= 2) {
						tiles[indice] = new Tile();
						tiles[indice].imagen = ImageIO.read(getClass().getResource(parametros[0]));

						boolean tieneColision = parametros[1].trim().equals("1");
						setup(indice, parametros[0], tieneColision);
						indice++;
					}
				}
			}
			br.close();

		} catch (IOException e) {
			System.err.println("Error al leer rutaTiles: " + rutaTiles);
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("Error general en getImagenTile:");
			e.printStackTrace();
		}

	}

	// public void cargarMapa(String rutaFile) {
	// try {
	// InputStream is = getClass().getResourceAsStream(rutaFile);
	// BufferedReader br = new BufferedReader(new InputStreamReader(is));
	//
	// int col = 0;
	// int fila = 0;
	//
	// while (col < pj.maxWorldcol && fila < pj.maxWorldfilas) {
	// // con esto leemos una linea de nuestro txt
	// String linea = br.readLine();
	//
	// while (col < pj.maxWorldcol) {
	// String numeros[] = linea.split(" ");
	// // ahora almacenamos estos indices en nustro array que tenemos determinado
	// para
	// // eso
	// int num = Integer.parseInt(numeros[col]);
	//
	// mapaPorNumeroTile[col][fila] = num;
	// col++;
	// }
	// if (col == pj.maxWorldcol) {
	// col = 0;
	// fila++;
	// }
	//
	// }
	// br.close();
	//
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }

	/**
	 * Genera un mapa procedural en memoria con pasto base y obstáculos aleatorios.
	 * Reemplaza la carga desde archivo .txt.
	 */
	private void generarMapaProcedural() {
		// Índices según el nuevo archivo rutaTiles.txt simplificado:
		// 0: tile_00.png (pasto, sin colisión)
		// 1: tile_01.png (pasto, sin colisión)
		// 2: tile_03.png (árboles, con colisión)
		// 3: tile_17.png (suelo general, sin colisión)

		int baseTile = 3; // tile_17.png - suelo general (sin colisión)
		int baseTileAlt = 0; // tile_00.png - pasto (sin colisión)
		int baseTileAlt2 = 1; // tile_01.png - pasto alternativo (sin colisión)
		int obstacleTile = 2; // tile_03.png - árboles (con colisión)

		// Centro del mundo: zona segura de spawn
		int centroCol = pj.maxWorldcol / 2;
		int centroFila = pj.maxWorldfilas / 2;
		int radioSeguro = 5; // radio de tiles libres alrededor del spawn
		for (int col = 0; col < pj.maxWorldcol; col++) {
			for (int fila = 0; fila < pj.maxWorldfilas; fila++) {
				// Zona segura alrededor del spawn (sin obstáculos)
				int distCol = Math.abs(col - centroCol);
				int distFila = Math.abs(fila - centroFila);
				if (distCol <= radioSeguro && distFila <= radioSeguro) {
					mapaPorNumeroTile[col][fila] = baseTile;
					continue;
				}
				// Bordes del mundo = obstáculos sólidos (muralla natural)
				if (col == 0 || col == pj.maxWorldcol - 1 || fila == 0 || fila == pj.maxWorldfilas - 1) {
					mapaPorNumeroTile[col][fila] = obstacleTile;
					continue;
				}
				// Interior: distribución aleatoria
				double random = Math.random();
				if (random < 0.05) {
					// 5% obstáculos (árboles)
					mapaPorNumeroTile[col][fila] = obstacleTile;
				} else if (random < 0.35) {
					// 30% pasto tipo 1
					mapaPorNumeroTile[col][fila] = baseTileAlt;
				} else if (random < 0.50) {
					// 15% pasto tipo 2
					mapaPorNumeroTile[col][fila] = baseTileAlt2;
				} else {
					// 50% suelo general
					mapaPorNumeroTile[col][fila] = baseTile;
				}
			}
		}
	}

	public void draw(Graphics2D g2) {
		int worldCol = 0;
		int worldFila = 0;

		while (worldCol < pj.maxWorldcol && worldFila < pj.maxWorldfilas) {

			int numeroTile = mapaPorNumeroTile[worldCol][worldFila];
			// aqui implementaremos la camara

			int worldX = worldCol * pj.tamanioTile;
			int worldY = worldFila * pj.tamanioTile;
			int screenX = worldX - pj.jugador.worldx + pj.jugador.screenX;
			int screenY = worldY - pj.jugador.worldy + pj.jugador.screeny;

			if (worldX + pj.tamanioTile > pj.jugador.worldx - pj.jugador.screenX
					&& worldX - pj.tamanioTile < pj.jugador.worldx + pj.jugador.screenX
					&& worldY + pj.tamanioTile > pj.jugador.worldy - pj.jugador.screeny
					&& worldY - pj.tamanioTile < pj.jugador.worldy + pj.jugador.screeny) {
				g2.drawImage(tiles[numeroTile].imagen, screenX, screenY, null);

			}
			worldCol++;
			if (worldCol == pj.maxWorldcol) {
				worldCol = 0;
				worldFila++;

			}
		}
	}

}
