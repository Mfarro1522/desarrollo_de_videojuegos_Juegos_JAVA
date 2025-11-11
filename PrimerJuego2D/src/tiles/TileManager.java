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
	Tile[] tiles;
	int mapaPorNumeroTile[][];

	public TileManager(PanelJuego pj) {
		this.pj = pj;
		tiles = new Tile[10];
		mapaPorNumeroTile = new int[pj.maxWorldcol][pj.maxWorldfilas];
		getImagenTile();
		cargarMapa("/mapas/world01.txt");
	}

	public void getImagenTile() {

		try {
			tiles[0] = new Tile();
			tiles[0].imagen = ImageIO.read(getClass().getResource("/tiles/pasto_cosme.png"));

			tiles[1] = new Tile();
			tiles[1].imagen = ImageIO.read(getClass().getResource("/tiles/pared_adobe.png"));

			tiles[2] = new Tile();
			tiles[2].imagen = ImageIO.read(getClass().getResource("/tiles/agua_normal.png"));

			tiles[3] = new Tile();
			tiles[3].imagen = ImageIO.read(getClass().getResource("/tiles/piso_tierra.png"));

			tiles[4] = new Tile();
			tiles[4].imagen = ImageIO.read(getClass().getResource("/tiles/arbol.png"));

			tiles[5] = new Tile();
			tiles[5].imagen = ImageIO.read(getClass().getResource("/tiles/arena.png"));

			tiles[6] = new Tile();
			tiles[6].imagen = ImageIO.read(getClass().getResource("/tiles/pasto_cosme1.png"));

		} catch (IOException e) {
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
