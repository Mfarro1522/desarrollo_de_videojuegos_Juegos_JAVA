package colision;

import configuracion.Configuracion;
import entidad.Entidad;
import mundo.MundoJuego;

/**
 * Sistema de detección de colisiones del juego.
 * <p>
 * Esta clase se encarga de verificar si una entidad (como el jugador o un NPC)
 * está intentando moverse hacia una posición ocupada por un tile sólido o un
 * objeto.
 * <p>
 * Funciona proyectando la posición futura de la entidad basándose en su
 * velocidad
 * y dirección, y verificando si el área sólida de la entidad intersecta con
 * algo.
 */
public class DetectorColisiones {

    MundoJuego mundo;

    public DetectorColisiones(MundoJuego mundo) {
        this.mundo = mundo;
    }

    /**
     * Verifica las colisiones con los tiles del mapa.
     * <p>
     * Calcula la posición futura de la entidad y comprueba si los tiles
     * en esa posición tienen la propiedad de colisión activada.
     *
     * @param e La entidad que se va a verificar.
     */
    public void chektile(Entidad e) {
        if (e.vuela) {
            return;
        }
        int tile = Configuracion.TAMANO_TILE;

        // Calcular las coordenadas del área sólida de la entidad en el mundo
        int areaIzquierdaX = e.worldx + e.AreaSolida.x;
        int areaDerechaX = e.worldx + e.AreaSolida.x + e.AreaSolida.width;
        int areaArribaY = e.worldy + e.AreaSolida.y;
        int areaAbajoY = e.worldy + e.AreaSolida.y + e.AreaSolida.height;

        // Determinar en qué columna y fila del mapa se encuentran los bordes del área
        // sólida
        int columnaIzquierda = areaIzquierdaX / tile;
        int columnaDerechaX = areaDerechaX / tile;
        int filaArriba = areaArribaY / tile;
        int filaAbajo = areaAbajoY / tile;

        // Verificar límites del mapa para evitar errores de índice fuera de rango
        if (columnaIzquierda < 0 || columnaDerechaX >= Configuracion.MUNDO_COLUMNAS
                || filaArriba < 0 || filaAbajo >= Configuracion.MUNDO_FILAS) {
            e.hayColision = true;
            return;
        }

        int tileNum1, tileNum2;

        // Verificar colisión según la dirección de movimiento
        switch (e.direccion) {
            case "arriba":
                // Predecir la fila superior en el siguiente paso
                filaArriba = (areaArribaY - e.vel) / tile;
                if (filaArriba < 0) {
                    e.hayColision = true;
                    break;
                } // Límite superior

                // Obtener los tiles de las dos esquinas superiores
                tileNum1 = mundo.tileManager.mapaPorNumeroTile[columnaIzquierda][filaArriba];
                tileNum2 = mundo.tileManager.mapaPorNumeroTile[columnaDerechaX][filaArriba];

                // Si alguno de los tiles es sólido, hay colisión
                if (mundo.tileManager.tiles[tileNum1].colision || mundo.tileManager.tiles[tileNum2].colision) {
                    e.hayColision = true;
                }
                break;

            case "abajo":
                // Predecir la fila inferior en el siguiente paso
                filaAbajo = (areaAbajoY + e.vel) / tile;
                if (filaAbajo >= Configuracion.MUNDO_FILAS) {
                    e.hayColision = true;
                    break;
                } // Límite inferior

                // Obtener los tiles de las dos esquinas inferiores
                tileNum1 = mundo.tileManager.mapaPorNumeroTile[columnaIzquierda][filaAbajo];
                tileNum2 = mundo.tileManager.mapaPorNumeroTile[columnaDerechaX][filaAbajo];

                if (mundo.tileManager.tiles[tileNum1].colision || mundo.tileManager.tiles[tileNum2].colision) {
                    e.hayColision = true;
                }
                break;

            case "izquierda":
                // Predecir la columna izquierda en el siguiente paso
                columnaIzquierda = (areaIzquierdaX - e.vel) / tile;
                if (columnaIzquierda < 0) {
                    e.hayColision = true;
                    break;
                } // Límite izquierdo

                // Obtener los tiles de las dos esquinas izquierdas
                tileNum1 = mundo.tileManager.mapaPorNumeroTile[columnaIzquierda][filaArriba];
                tileNum2 = mundo.tileManager.mapaPorNumeroTile[columnaIzquierda][filaAbajo];

                if (mundo.tileManager.tiles[tileNum1].colision || mundo.tileManager.tiles[tileNum2].colision) {
                    e.hayColision = true;
                }
                break;

            case "derecha":
                // Predecir la columna derecha en el siguiente paso
                columnaDerechaX = (areaDerechaX + e.vel) / tile;
                if (columnaDerechaX >= Configuracion.MUNDO_COLUMNAS) {
                    e.hayColision = true;
                    break;
                } // Límite derecho

                // Obtener los tiles de las dos esquinas derechas
                tileNum1 = mundo.tileManager.mapaPorNumeroTile[columnaDerechaX][filaArriba];
                tileNum2 = mundo.tileManager.mapaPorNumeroTile[columnaDerechaX][filaAbajo];

                if (mundo.tileManager.tiles[tileNum1].colision || mundo.tileManager.tiles[tileNum2].colision) {
                    e.hayColision = true;
                }
                break;
        }
    }

    /**
     * Verifica si una entidad está colisionando con un objeto del juego.
     * 
     * @param entidad   La entidad que se mueve.
     * @param esJugador Indica si la entidad es el jugador (para lógica específica
     *                  de recolección/interacción).
     * @return El índice del objeto con el que se colisiona, o 999 si no hay
     *         colisión.
     */
    public int checkObjeto(Entidad entidad, boolean esJugador) {
        int indice = 999;

        // Iterar sobre todos los objetos del mundo
        for (int i = 0; i < mundo.objs.length; i++) {
            if (mundo.objs[i] != null) {
                // Obtener posición absoluta del área sólida de la entidad
                entidad.AreaSolida.x = entidad.worldx + entidad.AreaSolida.x;
                entidad.AreaSolida.y = entidad.worldy + entidad.AreaSolida.y;

                // Obtener posición absoluta del área sólida del objeto
                mundo.objs[i].AreaSolida.x = mundo.objs[i].worldX + mundo.objs[i].AreaSolida.x;
                mundo.objs[i].AreaSolida.y = mundo.objs[i].worldY + mundo.objs[i].AreaSolida.y;

                // Simular el movimiento de la entidad para ver si intersecta
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

                // Verificar intersección de rectángulos
                if (entidad.AreaSolida.intersects(mundo.objs[i].AreaSolida)) {
                    // Si el objeto es sólido, marcar colisión en la entidad
                    if (mundo.objs[i].colision) {
                        entidad.hayColision = true;
                    }
                    // Si es el jugador, devolver el índice del objeto para interacción
                    if (esJugador) {
                        indice = i;
                    }
                }

                // Restaurar las posiciones de las áreas sólidas a sus valores relativos por
                // defecto
                // Esto es CRUCIAL porque AreaSolida se reutiliza en el siguiente frame
                entidad.AreaSolida.x = entidad.AreaSolidaDefaultX;
                entidad.AreaSolida.y = entidad.AreaSolidaDefaultY;
                mundo.objs[i].AreaSolida.x = mundo.objs[i].AreaSolidaDefaultX;
                mundo.objs[i].AreaSolida.y = mundo.objs[i].AreaSolidaDefaultY;
            }
        }
        return indice;
    }
}
