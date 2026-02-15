package colision;

import configuracion.Configuracion;
import entidad.Entidad;
import mundo.MundoJuego;

/**
 * Sistema de detección de colisiones.
 * Verifica colisiones entre entidades, tiles y objetos del mundo.
 */
public class DetectorColisiones {

    MundoJuego mundo;

    public DetectorColisiones(MundoJuego mundo) {
        this.mundo = mundo;
    }

    /**
     * Detecta colisión entre una entidad y los tiles sólidos del mapa.
     */
    public void chektile(Entidad e) {
        int tile = Configuracion.TAMANO_TILE;

        int areaIzquierdaX = e.worldx + e.AreaSolida.x;
        int areaDerechaX = e.worldx + e.AreaSolida.x + e.AreaSolida.width;
        int areaArribaY = e.worldy + e.AreaSolida.y;
        int areaAbajoY = e.worldy + e.AreaSolida.y + e.AreaSolida.height;

        int columnaIzquierda = areaIzquierdaX / tile;
        int columnaDerechaX = areaDerechaX / tile;
        int filaArriba = areaArribaY / tile;
        int filaAbajo = areaAbajoY / tile;

        if (columnaIzquierda < 0 || columnaDerechaX >= Configuracion.MUNDO_COLUMNAS
                || filaArriba < 0 || filaAbajo >= Configuracion.MUNDO_FILAS) {
            e.hayColision = true;
            return;
        }

        int tileIzquierdo, tileDerechoOCentral;

        switch (e.direccion) {
            case "arriba":
                int filaArribaProxima = (areaArribaY - e.vel) / tile;
                if (filaArribaProxima < 0) { e.hayColision = true; break; }
                tileIzquierdo = mundo.tileManager.mapaPorNumeroTile[columnaIzquierda][filaArribaProxima];
                tileDerechoOCentral = mundo.tileManager.mapaPorNumeroTile[columnaDerechaX][filaArribaProxima];
                if (mundo.tileManager.tiles[tileIzquierdo].colision || mundo.tileManager.tiles[tileDerechoOCentral].colision)
                    e.hayColision = true;
                break;
            case "abajo":
                int filaAbajoProxima = (areaAbajoY + e.vel) / tile;
                if (filaAbajoProxima >= Configuracion.MUNDO_FILAS) { e.hayColision = true; break; }
                tileIzquierdo = mundo.tileManager.mapaPorNumeroTile[columnaIzquierda][filaAbajoProxima];
                tileDerechoOCentral = mundo.tileManager.mapaPorNumeroTile[columnaDerechaX][filaAbajoProxima];
                if (mundo.tileManager.tiles[tileIzquierdo].colision || mundo.tileManager.tiles[tileDerechoOCentral].colision)
                    e.hayColision = true;
                break;
            case "izquierda":
                int columnaIzquierdaProxima = (areaIzquierdaX - e.vel) / tile;
                if (columnaIzquierdaProxima < 0) { e.hayColision = true; break; }
                tileIzquierdo = mundo.tileManager.mapaPorNumeroTile[columnaIzquierdaProxima][filaArriba];
                tileDerechoOCentral = mundo.tileManager.mapaPorNumeroTile[columnaIzquierdaProxima][filaAbajo];
                if (mundo.tileManager.tiles[tileIzquierdo].colision || mundo.tileManager.tiles[tileDerechoOCentral].colision)
                    e.hayColision = true;
                break;
            case "derecha":
                int columnaDerechaProxima = (areaDerechaX + e.vel) / tile;
                if (columnaDerechaProxima >= Configuracion.MUNDO_COLUMNAS) { e.hayColision = true; break; }
                tileIzquierdo = mundo.tileManager.mapaPorNumeroTile[columnaDerechaProxima][filaArriba];
                tileDerechoOCentral = mundo.tileManager.mapaPorNumeroTile[columnaDerechaProxima][filaAbajo];
                if (mundo.tileManager.tiles[tileIzquierdo].colision || mundo.tileManager.tiles[tileDerechoOCentral].colision)
                    e.hayColision = true;
                break;
            default:
                break;
        }
    }

    /**
     * Detecta colisión entre una entidad y los objetos del mundo.
     * @return índice del objeto colisionado, o 999 si no hay colisión
     */
    public int checkObjeto(Entidad entidad, boolean esJugador) {
        int indice = 999;

        for (int i = 0; i < mundo.objs.length; i++) {
            if (mundo.objs[i] != null) {
                entidad.AreaSolida.x = entidad.worldx + entidad.AreaSolida.x;
                entidad.AreaSolida.y = entidad.worldy + entidad.AreaSolida.y;

                mundo.objs[i].AreaSolida.x = mundo.objs[i].worldX + mundo.objs[i].AreaSolida.x;
                mundo.objs[i].AreaSolida.y = mundo.objs[i].worldY + mundo.objs[i].AreaSolida.y;

                switch (entidad.direccion) {
                    case "arriba":   entidad.AreaSolida.y -= entidad.vel; break;
                    case "abajo":    entidad.AreaSolida.y += entidad.vel; break;
                    case "izquierda": entidad.AreaSolida.x -= entidad.vel; break;
                    case "derecha":  entidad.AreaSolida.x += entidad.vel; break;
                }

                if (entidad.AreaSolida.intersects(mundo.objs[i].AreaSolida)) {
                    if (mundo.objs[i].colision) {
                        entidad.hayColision = true;
                    }
                    if (esJugador) {
                        indice = i;
                    }
                }

                entidad.AreaSolida.x = entidad.AreaSolidaDefaultX;
                entidad.AreaSolida.y = entidad.AreaSolidaDefaultY;
                mundo.objs[i].AreaSolida.x = mundo.objs[i].AreaSolidaDefaultX;
                mundo.objs[i].AreaSolida.y = mundo.objs[i].AreaSolidaDefaultY;
            }
        }
        return indice;
    }
}
