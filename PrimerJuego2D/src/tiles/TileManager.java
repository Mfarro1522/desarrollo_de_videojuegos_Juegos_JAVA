package tiles;

import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;
import main.PanelJuego;

public class TileManager {

	PanelJuego pj;
	public Tile[] tiles;
	public int mapaPorNumeroTile[][];

	public TileManager(PanelJuego pj) {
		this.pj = pj;
		mapaPorNumeroTile = new int[pj.maxWorldcol][pj.maxWorldfilas];
		getImagenTile("/tiles/rutaTiles.txt");
		cargarMapa("/mapas/world01_1.txt");
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
						
						if (parametros[1].trim().equals("1")) {
							tiles[indice].colision = true;
						}
						
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

	public void cargarMapa(String rutaFile) {
		try {
			InputStream is = getClass().getResourceAsStream(rutaFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			int col = 0;
			int fila = 0;

			while (col < pj.maxWorldcol && fila < pj.maxWorldfilas) {
				// con esto leemos una linea de nuestro txt
				String linea = br.readLine();

				while (col < pj.maxWorldcol) {
					String numeros[] = linea.split(" ");
					// ahora almacenamos estos indices en nustro array que tenemos determinado para
					// eso
					int num = Integer.parseInt(numeros[col]);

					mapaPorNumeroTile[col][fila] = num;
					col++;
				}
				if (col == pj.maxWorldcol) {
					col = 0;
					fila++;
				}

			}
			br.close();

		} catch (IOException e) {
			e.printStackTrace();
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

			if (worldX +pj.tamanioTile> pj.jugador.worldx - pj.jugador.screenX && worldX - pj.tamanioTile< pj.jugador.worldx + pj.jugador.screenX
					&& worldY +pj.tamanioTile > pj.jugador.worldy - pj.jugador.screeny
					&& worldY -pj.tamanioTile< pj.jugador.worldy + pj.jugador.screeny) {
				g2.drawImage(tiles[numeroTile].imagen, screenX, screenY, pj.tamanioTile, pj.tamanioTile, null);
				
			}
			worldCol++;
			if (worldCol == pj.maxWorldcol) {
				worldCol = 0;
				worldFila++;

			}
		}
	}

}
