package main;

import entidad.Entidad;

public class detectorColisiones {

	PanelJuego pj;

	public detectorColisiones(PanelJuego pj) {
		this.pj = pj;
	}

	/**
	 * Detecta colisión entre una entidad y los objetos del mundo (tiles). Verifica
	 * si la entidad va a chocar con un tile sólido en su próxima posición.
	 * 
	 * @param e - La entidad a verificar (normalmente el jugador o NPC)
	 */
	public void chektile(Entidad e) {

		int areaIzquierdaX = e.worldx + e.AreaSolida.x;
		int areaDerechaX = e.worldx + e.AreaSolida.x + e.AreaSolida.width;
		int areaArribaY = e.worldy + e.AreaSolida.y;
		int areaAbajoY = e.worldy + e.AreaSolida.y + e.AreaSolida.height;

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

	/**
	 * Detecta colisión entre una entidad y los objetos del mundo
	 * 
	 * @param entidad   - La entidad a verificar (normalmente el jugador)
	 * @param esJugador - true si la entidad es el jugador
	 * @return índice del objeto con el que colisionó, o 999 si no hay colisión
	 */
	public int checkObjeto(Entidad entidad, boolean esJugador) {
		int indice = 999; // valor por defecto si no hay colision

		// recorremos todos los objetos del mundo
		for (int i = 0; i < pj.objs.length; i++) {
			if (pj.objs[i] != null) {

				// obtenmos la posicion entidad con todo y area solida
				entidad.AreaSolida.x = entidad.worldx + entidad.AreaSolida.x;
				entidad.AreaSolida.y = entidad.worldy + entidad.AreaSolida.y;

				// y ahora obtenemos la posicion del objeto
				pj.objs[i].AreaSolida.x = pj.objs[i].worldX + pj.objs[i].AreaSolida.x;
				pj.objs[i].AreaSolida.y = pj.objs[i].worldY + pj.objs[i].AreaSolida.y;

				// Predecir la posición de la entidad según direccion
				switch (entidad.direccion) {
				case "arriba":
					entidad.AreaSolida.y -= entidad.vel;
					break;
				case "abajo":
					entidad.AreaSolida.y += entidad.vel;
					break;
				case "izquierda":
					entidad.AreaSolida.x -= entidad.vel;
					break;
				case "derecha":
					entidad.AreaSolida.x += entidad.vel;
					break;
				}

				// verificamos si hay interaccion con un metodo de java
				if (entidad.AreaSolida.intersects(pj.objs[i].AreaSolida)) {

					// Si el objeto tiene colisión sólida
					if (pj.objs[i].colision == true) {
						entidad.hayColision = true;
					}

					// Si es el jugador, guardar el índice del objeto
					if (esJugador == true) {
						indice = i;
					}
				}

				// CRÍTICO: Resetear las posiciones a los valores pordefecto
				entidad.AreaSolida.x = entidad.AreaSolidaDefaultX;
				entidad.AreaSolida.y = entidad.AreaSolidaDefaultY;
				pj.objs[i].AreaSolida.x = pj.objs[i].AreaSolidaDefaultX;
				pj.objs[i].AreaSolida.y = pj.objs[i].AreaSolidaDefaultY;
			}
		}
		return indice;
	}

}
