package mundo;

import configuracion.Configuracion;
import entidad.*;
import entidad.NPC.TipoNPC;
import items.*;

/**
 * Configura y gestiona la colocación de objetos y NPCs en el mundo.
 *
 * OPTIMIZACIONES:
 * - Object Pool: pre-instancia POOL_TOTAL NPCs al inicio (cero 'new' durante
 * gameplay)
 * - Activación/Desactivación: reutiliza instancias sin crear nuevas
 * - Spawn por tipo según nivel del jugador
 */
public class GestorRecursos {

    MundoJuego mundo;

    public static final int POOL_TOTAL = 1000;
    private static final int POOL_BAT = 250;
    private static final int POOL_SLIME = 250;
    private static final int POOL_ORCO = 250;
    private static final int POOL_GHOUL = 250;

    private static final int INICIO_BAT = 0;
    private static final int INICIO_SLIME = POOL_BAT;
    private static final int INICIO_ORCO = POOL_BAT + POOL_SLIME;
    private static final int INICIO_GHOUL = POOL_BAT + POOL_SLIME + POOL_ORCO;

    private int maxNPCsActivos = 80;

    public GestorRecursos(MundoJuego mundo) {
        this.mundo = mundo;
    }

    // ===== OBJECT POOL =====

    public void inicializarPool() {
        for (int i = INICIO_BAT; i < INICIO_BAT + POOL_BAT; i++)
            mundo.npcs[i] = new Bat(mundo);
        for (int i = INICIO_SLIME; i < INICIO_SLIME + POOL_SLIME; i++)
            mundo.npcs[i] = new Slime(mundo);
        for (int i = INICIO_ORCO; i < INICIO_ORCO + POOL_ORCO; i++)
            mundo.npcs[i] = new Orco(mundo);
        for (int i = INICIO_GHOUL; i < INICIO_GHOUL + POOL_GHOUL; i++)
            mundo.npcs[i] = new Ghoul(mundo);
        System.out.println("[Pool] Inicializado: " + POOL_TOTAL + " NPCs pre-instanciados.");
    }

    public void desactivarTodos() {
        for (int i = 0; i < POOL_TOTAL; i++) {
            if (mundo.npcs[i] != null)
                mundo.npcs[i].activo = false;
        }
        mundo.contadorNPCs = 0;
    }

    // ===== BÚSQUEDA DE SLOTS =====

    private int buscarSlotInactivo(TipoNPC tipo) {
        int inicio, fin;
        switch (tipo) {
            case BAT:
                inicio = INICIO_BAT;
                fin = INICIO_BAT + POOL_BAT;
                break;
            case SLIME:
                inicio = INICIO_SLIME;
                fin = INICIO_SLIME + POOL_SLIME;
                break;
            case ORCO:
                inicio = INICIO_ORCO;
                fin = INICIO_ORCO + POOL_ORCO;
                break;
            case GHOUL:
                inicio = INICIO_GHOUL;
                fin = INICIO_GHOUL + POOL_GHOUL;
                break;
            default:
                return -1;
        }
        for (int i = inicio; i < fin; i++) {
            if (mundo.npcs[i] != null && !mundo.npcs[i].activo)
                return i;
        }
        return -1;
    }

    // ===== SELECCIÓN SEGÚN NIVEL =====

    /**
     * Progresión de enemigos:
     *  Nivel 1-2 → solo Bats
     *  Nivel 3-4 → Bats + Slimes
     *  Nivel 5-6 → Slimes + Orcos
     *  Nivel 7-8 → Orcos + Ghouls
     *  Nivel 9+  → los 4 tipos
     */
    private TipoNPC elegirTipoEnemigo() {
        int nivel = mundo.estadisticas.nivel;
        int roll = (int) (Math.random() * 100);

        if (nivel <= 2) {
            // Solo murciélagos
            return TipoNPC.BAT;
        } else if (nivel <= 4) {
            // 65% Bats, 35% Slimes
            return (roll < 65) ? TipoNPC.BAT : TipoNPC.SLIME;
        } else if (nivel <= 6) {
            // 30% Slimes, 70% Orcos
            return (roll < 30) ? TipoNPC.SLIME : TipoNPC.ORCO;
        } else if (nivel <= 8) {
            // 30% Orcos, 70% Ghouls
            return (roll < 30) ? TipoNPC.ORCO : TipoNPC.GHOUL;
        } else {
            // Nivel 9+: todos (25% cada uno)
            if (roll < 25) return TipoNPC.BAT;
            else if (roll < 50) return TipoNPC.SLIME;
            else if (roll < 75) return TipoNPC.ORCO;
            else return TipoNPC.GHOUL;
        }
    }

    // ===== POSICIONES =====

    private int[] generarPosicionAleatoria() {
        int margen = 3 * Configuracion.TAMANO_TILE;
        int x = margen + (int) (Math.random() * (Configuracion.MUNDO_ANCHO - 2 * margen));
        int y = margen + (int) (Math.random() * (Configuracion.MUNDO_ALTO - 2 * margen));
        return new int[] { x, y };
    }

    private boolean esUbicacionValida(int x, int y) {
        int col = x / Configuracion.TAMANO_TILE;
        int fila = y / Configuracion.TAMANO_TILE;

        if (col < 0 || col >= Configuracion.MUNDO_COLUMNAS ||
                fila < 0 || fila >= Configuracion.MUNDO_FILAS) {
            return false;
        }

        int tileNum = mundo.tileManager.mapaPorNumeroTile[col][fila];
        if (mundo.tileManager.tiles[tileNum] != null && mundo.tileManager.tiles[tileNum].colision) {
            return false;
        }
        return true;
    }

    // ===== SPAWN =====

    private boolean spawnearNPC(TipoNPC tipo, int x, int y) {
        int slot = buscarSlotInactivo(tipo);
        if (slot == -1)
            return false;
        mundo.npcs[slot].activar(x, y);
        return true;
    }

    // ===== PÚBLICOS =====

    public void setObjetct() {
        int t = Configuracion.TAMANO_TILE;
        int cantidadCofres = 10;
        int intentosMaximos = 100;

        for (int i = 0; i < cantidadCofres; i++) {
            if (i >= mundo.objs.length)
                break;

            boolean posicionado = false;
            int intentos = 0;

            while (!posicionado && intentos < intentosMaximos) {
                // Generar posición aleatoria respetando márgenes
                int margen = 2; // Margen de seguridad desde los bordes
                int col = margen + (int) (Math.random() * (Configuracion.MUNDO_COLUMNAS - 2 * margen));
                int fila = margen + (int) (Math.random() * (Configuracion.MUNDO_FILAS - 2 * margen));

                if (esPosicionValidaParaObjeto(col, fila)) {
                    int worldX = col * t;
                    int worldY = fila * t;

                    // Decidir tipo de cofre (20% Normal, 80% PowerUp)
                    if (Math.random() < 0.2) {
                        mundo.objs[i] = new CofreNormal(t);
                    } else {
                        // Seleccionar PowerUp aleatorio
                        CofrePowerUp.TipoPowerUp[] tipos = CofrePowerUp.TipoPowerUp.values();
                        CofrePowerUp.TipoPowerUp tipoSeleccionado = tipos[(int) (Math.random() * tipos.length)];
                        mundo.objs[i] = new CofrePowerUp(t, tipoSeleccionado);
                    }

                    mundo.objs[i].worldX = worldX;
                    mundo.objs[i].worldY = worldY;
                    posicionado = true;
                    // System.out.println("Cofre " + i + " generado en: " + col + ", " + fila);
                }
                intentos++;
            }
            if (!posicionado) {
                System.out
                        .println("No se pudo colocar el cofre " + i + " después de " + intentosMaximos + " intentos.");
            }
        }
    }

    private boolean esPosicionValidaParaObjeto(int col, int fila) {
        // 1. Verificar límites del mapa (aunque ya se filtró en la generación, doble
        // check)
        if (col < 0 || col >= Configuracion.MUNDO_COLUMNAS ||
                fila < 0 || fila >= Configuracion.MUNDO_FILAS) {
            return false;
        }

        // 2. Verificar si la posición actual es sólida
        int tileNum = mundo.tileManager.mapaPorNumeroTile[col][fila];
        if (mundo.tileManager.tiles[tileNum] != null && mundo.tileManager.tiles[tileNum].colision) {
            return false;
        }

        // 3. Verificar si está rodeado de sólidos (inaccesible)
        boolean norteSolido = esSolido(col, fila - 1);
        boolean surSolido = esSolido(col, fila + 1);
        boolean oesteSolido = esSolido(col - 1, fila);
        boolean esteSolido = esSolido(col + 1, fila);

        if (norteSolido && surSolido && oesteSolido && esteSolido) {
            return false;
        }

        // 4. (Opcional) Verificar si ya hay otro objeto en esa posición para evitar
        // superposición
        // Esto requeriría iterar sobre mundo.objs existente si se llama
        // incrementalmente,
        // pero como es al inicio, podemos asumir que si i > 0, chequeamos los
        // anteriores.
        // Por simplicidad y dado el tamaño del mapa, la probabilidad es baja, pero
        // idealmente se chequearía.

        return true;
    }

    private boolean esSolido(int col, int fila) {
        if (col < 0 || col >= Configuracion.MUNDO_COLUMNAS ||
                fila < 0 || fila >= Configuracion.MUNDO_FILAS) {
            return true; // Los bordes del mundo cuentan como sólido
        }
        int tileNum = mundo.tileManager.mapaPorNumeroTile[col][fila];
        return (mundo.tileManager.tiles[tileNum] != null && mundo.tileManager.tiles[tileNum].colision);
    }

    public void setNPCs() {
        int cantidadInicial = 60;
        for (int i = 0; i < cantidadInicial; i++) {
            TipoNPC tipo = elegirTipoEnemigo();
            int[] pos = generarPosicionAleatoria();
            spawnearNPC(tipo, pos[0], pos[1]);
        }
        System.out.println("[Pool] Spawn inicial: " + mundo.contadorNPCs + " NPCs activos.");
    }

    // ===== SPAWNING EN ANILLO (EL CERCO) =====

    public void spawnearEnAnillo() {
        // No spawnear durante boss fight
        if (mundo.gameState == Configuracion.ESTADO_BOSS_FIGHT) {
            return;
        }

        // 1. Verificar límite de población
        actualizarLimitesDeAparicion();

        if (mundo.contadorNPCs >= maxNPCsActivos)
            return;

        // 2. Definir Frustum (Cámara) + Margen
        int cameraX = mundo.jugador.worldx - mundo.jugador.screenX;
        int cameraY = mundo.jugador.worldy - mundo.jugador.screeny;

        // Margen de 2 tiles fuera de la cámara
        int margin = 2 * Configuracion.TAMANO_TILE;

        int minX = cameraX - margin;
        int maxX = cameraX + Configuracion.ANCHO_PANTALLA + margin;
        int minY = cameraY - margin;
        int maxY = cameraY + Configuracion.ALTO_PANTALLA + margin;

        // 3. Seleccionar Lado del Anillo (0: Arriba, 1: Abajo, 2: Izquierda, 3:
        // Derecha)
        int side = (int) (Math.random() * 4);
        int spawnX = 0;
        int spawnY = 0;

        switch (side) {
            case 0: // Arriba
                spawnX = minX + (int) (Math.random() * (maxX - minX));
                spawnY = minY;
                break;
            case 1: // Abajo
                spawnX = minX + (int) (Math.random() * (maxX - minX));
                spawnY = maxY;
                break;
            case 2: // Izquierda
                spawnX = minX;
                spawnY = minY + (int) (Math.random() * (maxY - minY));
                break;
            case 3: // Derecha
                spawnX = maxX;
                spawnY = minY + (int) (Math.random() * (maxY - minY));
                break;
        }

        // 4. Validar y Spawnear
        if (esUbicacionValida(spawnX, spawnY)) {
            TipoNPC tipo = elegirTipoEnemigo();
            spawnearNPC(tipo, spawnX, spawnY);
        }
    }

    private void actualizarLimitesDeAparicion() {
        int nivel = mundo.estadisticas.nivel;

        if (nivel == 1) {
            maxNPCsActivos = 12; // Fase de introducción: pocos murciélagos.
        } else if (nivel <= 3) {
            maxNPCsActivos = 35; // Empiezan a agruparse.
        } else if (nivel <= 6) {
            maxNPCsActivos = 90; // Primera sensación de horda.
        } else if (nivel <= 9) {
            maxNPCsActivos = 180; // Presión alta, forzando al jugador a moverse.
        } else {
            // Nivel 10 en adelante: Se suelta el límite y entran cientos.
            // Usamos el Math.min para proteger la memoria estática de tus arrays.
            maxNPCsActivos = Math.min(POOL_TOTAL - 10, 250 + ((nivel - 10) * 20));
        }
    }

    // ===== BOSS SPAWN =====

    /**
     * Verifica si se debe spawnear el boss DemonBat.
     * Se activa al alcanzar nivel 4 durante el estado JUGANDO.
     */
    public void verificarSpawnBoss() {
        if (mundo.estadisticas.nivel >= 4
                && mundo.gameState == Configuracion.ESTADO_JUGANDO
                && mundo.bossActivo == null) {
            mundo.iniciarBossFight();
        }
    }

    /**
     * Verifica si se debe spawnear los 3 KingSlimes.
     * Se activa al alcanzar nivel 7 durante el estado JUGANDO.
     */
    public void verificarSpawnKingSlime() {
        if (mundo.estadisticas.nivel >= 7
                && mundo.gameState == Configuracion.ESTADO_JUGANDO
                && mundo.kingSlimesVivos == 0
                && mundo.kingSlimes[0] == null) {
            mundo.iniciarKingSlimeFight();
        }
    }
}
