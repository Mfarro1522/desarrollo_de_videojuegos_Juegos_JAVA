package main;

import entidad.Entidad;

public class detectorColisiones {

	PanelJuego pj;

	public detectorColisiones(PanelJuego pj) {
		this.pj = pj;
	}

	public void chektile(Entidad e) {

		// Coordenadas del mundo del área sólida
		int areaIzquierdaX = e.worldx + e.AreaSolida.x; // borde izquierdo del área sólida
		int areaDerechaX = e.worldx + e.AreaSolida.x + e.AreaSolida.width; // borde derecho del área sólida
		int areaArribaY = e.worldy + e.AreaSolida.y; // borde superior del área sólida
		int areaAbajoY = e.worldy + e.AreaSolida.y + e.AreaSolida.height; // borde inferior del área sólida

		// Convertir a índices de tiles (columnas y filas)
		int columnaIzquierda = areaIzquierdaX / pj.tamanioTile;
		int columnaDerechaX = areaDerechaX / pj.tamanioTile;
		int filaArriba = areaArribaY / pj.tamanioTile;
		int filaAbajo = areaAbajoY / pj.tamanioTile;

		int tileIzquierdo, tileDerechoOCentral;

		switch (e.direccion) {
		case "arriba":
			int filaArribaProxima = (areaArribaY - e.vel) / pj.tamanioTile;
            tileIzquierdo = pj.tileManager.mapaPorNumeroTile[columnaIzquierda][filaArribaProxima];
            tileDerechoOCentral = pj.tileManager.mapaPorNumeroTile[columnaDerechaX][filaArribaProxima];
            if (pj.tileManager.tiles[tileIzquierdo].colision || pj.tileManager.tiles[tileDerechoOCentral].colision)
                e.hayColision = true;


			break;
		case "abajo":
			int filaAbajoProxima = (areaAbajoY + e.vel) / pj.tamanioTile;
            tileIzquierdo = pj.tileManager.mapaPorNumeroTile[columnaIzquierda][filaAbajoProxima];
            tileDerechoOCentral = pj.tileManager.mapaPorNumeroTile[columnaDerechaX][filaAbajoProxima];
            if (pj.tileManager.tiles[tileIzquierdo].colision || pj.tileManager.tiles[tileDerechoOCentral].colision)
                e.hayColision = true;

			break;

		case "izquierda":
			int columnaIzquierdaProxima = (areaIzquierdaX - e.vel) / pj.tamanioTile;
            tileIzquierdo = pj.tileManager.mapaPorNumeroTile[columnaIzquierdaProxima][filaArriba];
            tileDerechoOCentral = pj.tileManager.mapaPorNumeroTile[columnaIzquierdaProxima][filaAbajo];
            if (pj.tileManager.tiles[tileIzquierdo].colision || pj.tileManager.tiles[tileDerechoOCentral].colision)
                e.hayColision = true;
			break;

		case "derecha":
			int columnaDerechaProxima = (areaDerechaX + e.vel) / pj.tamanioTile;
            tileIzquierdo = pj.tileManager.mapaPorNumeroTile[columnaDerechaProxima][filaArriba];
            tileDerechoOCentral = pj.tileManager.mapaPorNumeroTile[columnaDerechaProxima][filaAbajo];
            if (pj.tileManager.tiles[tileIzquierdo].colision || pj.tileManager.tiles[tileDerechoOCentral].colision)
                e.hayColision = true;
			break;

		default:
			break;
		}

	}
}
