package main;

import objetos.OBJ_llave;
import objetos.OBJ_puerta;
import objetos.OBJ_botas;
import objetos.OBJ_cofre;

/**
 * Encargada de colocar los objetos (como llaves, puertas, cofres) en el mapa
 * del juego.
 */
public class AssetSetter {
	PanelJuego pj;

	public AssetSetter(PanelJuego pj) {
		this.pj = pj;
	}

	/**
	 * Instancia y posiciona los objetos en el mapa.
	 */
	public void setObjetct() {
		pj.objs[0] = new OBJ_llave(pj.tamanioTile);
		pj.objs[0].worldX = 24 * pj.tamanioTile;
		pj.objs[0].worldY = 12 * pj.tamanioTile;

		pj.objs[1] = new OBJ_llave(pj.tamanioTile);
		pj.objs[1].worldX = 23 * pj.tamanioTile;
		pj.objs[1].worldY = 40 * pj.tamanioTile;

		pj.objs[2] = new OBJ_llave(pj.tamanioTile);
		pj.objs[2].worldX = 38 * pj.tamanioTile;
		pj.objs[2].worldY = 8 * pj.tamanioTile;

		pj.objs[3] = new OBJ_puerta(pj.tamanioTile);
		pj.objs[3].worldX = 11 * pj.tamanioTile;
		pj.objs[3].worldY = 12 * pj.tamanioTile;

		pj.objs[4] = new OBJ_puerta(pj.tamanioTile);
		pj.objs[4].worldX = 9 * pj.tamanioTile;
		pj.objs[4].worldY = 26 * pj.tamanioTile;

		pj.objs[5] = new OBJ_puerta(pj.tamanioTile);
		pj.objs[5].worldX = 13 * pj.tamanioTile;
		pj.objs[5].worldY = 22 * pj.tamanioTile;

		pj.objs[6] = new OBJ_cofre(pj.tamanioTile);
		pj.objs[6].worldX = 11 * pj.tamanioTile;
		pj.objs[6].worldY = 9 * pj.tamanioTile;
		
		pj.objs[7] = new OBJ_botas(pj.tamanioTile);
		pj.objs[7].worldX = 37 * pj.tamanioTile;
		pj.objs[7].worldY = 42 * pj.tamanioTile;


	}

}
