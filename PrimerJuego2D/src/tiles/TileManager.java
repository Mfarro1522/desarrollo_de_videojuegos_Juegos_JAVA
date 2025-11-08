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
		mapaPorNumeroTile = new int[pj.maxPantallaColumnas][pj.maxPantallaFilas];
		getImagenTile();
		cargarMapa("/mapas/mapa01.txt");
	}

	public void getImagenTile() {

		try {
			tiles[0] = new Tile();
			tiles[0].imagen = ImageIO.read(getClass().getResource("/tiles/piso_tierra.png"));

			tiles[1] = new Tile();
			tiles[1].imagen = ImageIO.read(getClass().getResource("/tiles/pared_adobe.png"));

			tiles[2] = new Tile();
			tiles[2].imagen = ImageIO.read(getClass().getResource("/tiles/agua_verde.png"));
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

			while (col < pj.maxPantallaColumnas && fila < pj.maxPantallaFilas) {
				// con esto leemos una linea de nuestro txt
				String linea = br.readLine();

				while (col < pj.maxPantallaColumnas) {
					String numeros[] = linea.split(" ");
					// ahora almacenamos estos indices en nustro array que tenemos determinado para
					// eso
					int num = Integer.parseInt(numeros[col]);

					mapaPorNumeroTile[col][fila] = num;
					col++;
				}
				if (col == pj.maxPantallaColumnas) {
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
		int columna = 0;
		int fila = 0;
		int x = 0;
		int y = 0;

		while (columna < pj.maxPantallaColumnas && fila < pj.maxPantallaFilas) {

			int numeroTile = mapaPorNumeroTile[columna][fila];

			g2.drawImage(tiles[numeroTile].imagen, x, y, pj.tamanioTile, pj.tamanioTile, null);
			columna++;
			x += pj.tamanioTile;
			if (columna == pj.maxPantallaColumnas) {
				columna = 0;
				x = 0;
				fila++;
				y += pj.tamanioTile;
			}
		}
	}

}
