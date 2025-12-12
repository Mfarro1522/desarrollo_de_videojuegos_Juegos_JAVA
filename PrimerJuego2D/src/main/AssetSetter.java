package main;

import objetos.OBJ_llave;
import objetos.OBJ_puerta;
import objetos.OBJ_cofre;

public class AssetSetter {
	PanelJuego pj;

	public AssetSetter(PanelJuego pj) {
		this.pj = pj;
	}

	public void setObjetct() {
		pj.objs[0] = new OBJ_llave();
		pj.objs[0].worldX = 23 * pj.tamanioTile;
		pj.objs[0].worldY = 7 * pj.tamanioTile;

		pj.objs[1] = new OBJ_llave();
		pj.objs[1].worldX = 23 * pj.tamanioTile;
		pj.objs[1].worldY = 40 * pj.tamanioTile;

		pj.objs[2] = new OBJ_llave();
		pj.objs[2].worldX = 38 * pj.tamanioTile;
		pj.objs[2].worldY = 8 * pj.tamanioTile;

		pj.objs[3] = new OBJ_puerta();
		pj.objs[3].worldX = 10 * pj.tamanioTile;
		pj.objs[3].worldY = 11 * pj.tamanioTile;

		pj.objs[4] = new OBJ_puerta();
		pj.objs[4].worldX = 8 * pj.tamanioTile;
		pj.objs[4].worldY = 28 * pj.tamanioTile;

		pj.objs[5] = new OBJ_puerta();
		pj.objs[5].worldX = 12 * pj.tamanioTile;
		pj.objs[5].worldY = 22 * pj.tamanioTile;

		pj.objs[6] = new OBJ_cofre();
		pj.objs[6].worldX = 10 * pj.tamanioTile;
		pj.objs[6].worldY = 7 * pj.tamanioTile;
	}

}
