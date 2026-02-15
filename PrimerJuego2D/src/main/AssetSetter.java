package main;

import entidad.*;
import entidad.NPC.TipoNPC;
import objetos.*;

/**
 * Configura y gestiona la colocación de objetos y NPCs en el mundo.
 *
 * OPTIMIZACIONES:
 * - Object Pool: pre-instancia POOL_TOTAL NPCs al inicio (cero 'new' durante gameplay)
 * - Activación/Desactivación: reutiliza instancias muertas sin crear nuevas
 * - Spawn por tipo según nivel del jugador
 */
public class AssetSetter {

    PanelJuego pj;

    // ===== TAMAÑO DEL POOL =====
    public static final int POOL_TOTAL = 1000;
    private static final int POOL_BAT = 250;
    private static final int POOL_SLIME = 250;
    private static final int POOL_ORCO = 250;
    private static final int POOL_GHOUL = 250;

    // Rangos de índices por tipo (para búsqueda rápida de slots libres)
    private static final int INICIO_BAT = 0;
    private static final int INICIO_SLIME = POOL_BAT;
    private static final int INICIO_ORCO = POOL_BAT + POOL_SLIME;
    private static final int INICIO_GHOUL = POOL_BAT + POOL_SLIME + POOL_ORCO;

    // Límites de NPCs activos simultáneamente
    private int maxNPCsActivos = 80;

    // Configuración de spawn
    private static final int DISTANCIA_MIN_SPAWN = 5;  // tiles
    private static final int DISTANCIA_MAX_SPAWN = 20; // tiles

    public AssetSetter(PanelJuego pj) {
        this.pj = pj;
    }

    // ===== OBJECT POOL: Pre-instanciar todos los NPCs =====

    /**
     * Pre-instancia POOL_TOTAL NPCs (desactivados).
     * Se llama UNA VEZ en setupJuego(). Elimina allocations durante gameplay.
     */
    public void inicializarPool() {
        for (int i = INICIO_BAT; i < INICIO_BAT + POOL_BAT; i++) {
            pj.npcs[i] = new Bat(pj);
        }
        for (int i = INICIO_SLIME; i < INICIO_SLIME + POOL_SLIME; i++) {
            pj.npcs[i] = new Slime(pj);
        }
        for (int i = INICIO_ORCO; i < INICIO_ORCO + POOL_ORCO; i++) {
            pj.npcs[i] = new Orco(pj);
        }
        for (int i = INICIO_GHOUL; i < INICIO_GHOUL + POOL_GHOUL; i++) {
            pj.npcs[i] = new Ghoul(pj);
        }
        System.out.println("[Pool] Inicializado: " + POOL_TOTAL + " NPCs pre-instanciados.");
    }

    /**
     * Desactiva todos los NPCs del pool (para reinicio de partida).
     */
    public void desactivarTodos() {
        for (int i = 0; i < POOL_TOTAL; i++) {
            if (pj.npcs[i] != null) {
                pj.npcs[i].activo = false;
            }
        }
        pj.contadorNPCs = 0;
    }

    // ===== BÚSQUEDA DE SLOTS LIBRES =====

    /**
     * Busca un slot inactivo para el tipo dado.
     * @return índice del slot libre, o -1 si no hay disponible.
     */
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
            if (pj.npcs[i] != null && !pj.npcs[i].activo) {
                return i;
            }
        }
        return -1; // Pool agotado para este tipo
    }

    // ===== SELECCIÓN DE TIPO SEGÚN NIVEL =====

    /**
     * Elige el tipo de enemigo a spawnear basándose en el nivel del jugador.
     * - Lvl 1-2: solo Bats
     * - Lvl 3-4: Bats + Slimes
     * - Lvl 5-9: Slimes + Orcos (desaparecen Bats)
     * - Lvl 10-14: Orcos + Ghouls (desaparecen Slimes)
     * - Lvl 15+: todos los tipos
     */
    private TipoNPC elegirTipoEnemigo() {
        int nivel = pj.stats.nivel;
        double rand = Math.random();

        if (nivel <= 2) {
            // Nivel 1-2: solo Bats
            return TipoNPC.BAT;
        } else if (nivel <= 4) {
            // Nivel 3-4: 50% Bat, 50% Slime
            if (rand < 0.50) return TipoNPC.BAT;
            return TipoNPC.SLIME;
        } else if (nivel <= 9) {
            // Nivel 5-9: 60% Slime, 40% Orco (Bats desaparecen)
            if (rand < 0.60) return TipoNPC.SLIME;
            return TipoNPC.ORCO;
        } else if (nivel <= 14) {
            // Nivel 10-14: 50% Orco, 50% Ghoul (Slimes desaparecen)
            if (rand < 0.50) return TipoNPC.ORCO;
            return TipoNPC.GHOUL;
        } else {
            // Nivel 15+: todos los tipos
            if (rand < 0.25) return TipoNPC.BAT;
            if (rand < 0.50) return TipoNPC.SLIME;
            if (rand < 0.75) return TipoNPC.ORCO;
            return TipoNPC.GHOUL;
        }
    }

    // ===== GENERACIÓN DE POSICIONES =====

    /**
     * Genera una posición aleatoria válida en el mapa (dentro de los bordes).
     * @return array [x, y] en coordenadas del mundo
     */
    private int[] generarPosicionAleatoria() {
        int margen = 3 * pj.tamanioTile; // No spawnear en los bordes
        int x = margen + (int) (Math.random() * (pj.maxWorldAncho - 2 * margen));
        int y = margen + (int) (Math.random() * (pj.maxWorldAlto - 2 * margen));
        return new int[] { x, y };
    }

    /**
     * Genera una posición aleatoria a una distancia razonable del jugador.
     * Evita spawns demasiado cerca o demasiado lejos.
     * @return array [x, y] en coordenadas del mundo
     */
    private int[] generarPosicionCercaJugador() {
        int minDist = DISTANCIA_MIN_SPAWN * pj.tamanioTile;
        int maxDist = DISTANCIA_MAX_SPAWN * pj.tamanioTile;

        for (int intento = 0; intento < 10; intento++) {
            double angulo = Math.random() * 2 * Math.PI;
            int distancia = minDist + (int) (Math.random() * (maxDist - minDist));
            int x = pj.jugador.worldx + (int) (Math.cos(angulo) * distancia);
            int y = pj.jugador.worldy + (int) (Math.sin(angulo) * distancia);

            // Asegurar que esté dentro del mapa
            int margen = 2 * pj.tamanioTile;
            x = Math.max(margen, Math.min(x, pj.maxWorldAncho - margen));
            y = Math.max(margen, Math.min(y, pj.maxWorldAlto - margen));

            return new int[] { x, y };
        }

        // Fallback: posición aleatoria
        return generarPosicionAleatoria();
    }

    // ===== SPAWN DE NPCS (usando Object Pool) =====

    /**
     * Activa un NPC del pool en una posición dada.
     * @return true si se activó exitosamente
     */
    private boolean spawnearNPC(TipoNPC tipo, int x, int y) {
        int slot = buscarSlotInactivo(tipo);
        if (slot == -1) return false;

        pj.npcs[slot].activar(x, y);
        return true;
    }

    // ===== MÉTODOS PÚBLICOS (referenciados por PanelJuego) =====

    /**
     * Coloca los objetos iniciales del juego (solo cofres).
     */
    public void setObjetct() {
        int t = pj.tamanioTile;

        // Cofre normal
        pj.objs[0] = new OBJ_cofre(t);
        pj.objs[0].worldX = 30 * t;
        pj.objs[0].worldY = 29 * t;

        // Cofres de PowerUp distribuidos por el mapa
        pj.objs[1] = new OBJ_CofrePowerUp(t, OBJ_CofrePowerUp.TipoPowerUp.INVENCIBILIDAD);
        pj.objs[1].worldX = 15 * t;
        pj.objs[1].worldY = 15 * t;

        pj.objs[2] = new OBJ_CofrePowerUp(t, OBJ_CofrePowerUp.TipoPowerUp.VELOCIDAD);
        pj.objs[2].worldX = 45 * t;
        pj.objs[2].worldY = 45 * t;

        pj.objs[3] = new OBJ_CofrePowerUp(t, OBJ_CofrePowerUp.TipoPowerUp.ATAQUE);
        pj.objs[3].worldX = 60 * t;
        pj.objs[3].worldY = 30 * t;

        pj.objs[4] = new OBJ_CofrePowerUp(t, OBJ_CofrePowerUp.TipoPowerUp.CURACION);
        pj.objs[4].worldX = 25 * t;
        pj.objs[4].worldY = 50 * t;

        // Cofres adicionales en otras zonas
        pj.objs[5] = new OBJ_cofre(t);
        pj.objs[5].worldX = 70 * t;
        pj.objs[5].worldY = 20 * t;

        pj.objs[6] = new OBJ_CofrePowerUp(t, OBJ_CofrePowerUp.TipoPowerUp.CURACION);
        pj.objs[6].worldX = 80 * t;
        pj.objs[6].worldY = 70 * t;

        pj.objs[7] = new OBJ_CofrePowerUp(t, OBJ_CofrePowerUp.TipoPowerUp.VELOCIDAD);
        pj.objs[7].worldX = 10 * t;
        pj.objs[7].worldY = 80 * t;

        pj.objs[8] = new OBJ_cofre(t);
        pj.objs[8].worldX = 55 * t;
        pj.objs[8].worldY = 65 * t;

        pj.objs[9] = new OBJ_CofrePowerUp(t, OBJ_CofrePowerUp.TipoPowerUp.INVENCIBILIDAD);
        pj.objs[9].worldX = 85 * t;
        pj.objs[9].worldY = 40 * t;
    }

    /**
     * Spawn inicial de NPCs al comenzar partida.
     * Activa 60 enemigos distribuidos aleatoriamente usando el pool.
     */
    public void setNPCs() {
        int cantidadInicial = 60;
        for (int i = 0; i < cantidadInicial; i++) {
            TipoNPC tipo = elegirTipoEnemigo();
            int[] pos = generarPosicionAleatoria();
            spawnearNPC(tipo, pos[0], pos[1]);
        }
        System.out.println("[Pool] Spawn inicial: " + pj.contadorNPCs + " NPCs activos.");
    }

    /**
     * Respawnea enemigos periódicamente (llamado cada segundo por PanelJuego).
     * Mantiene una presión constante de enemigos según el nivel.
     */
    public void respawnearEnemigos() {
        // Ajustar cantidad máxima según nivel
        maxNPCsActivos = 60 + (pj.stats.nivel * 10);
        if (maxNPCsActivos > 300) {
            maxNPCsActivos = 300; // Tope absoluto
        }

        // Solo spawnear si hay menos NPCs que el máximo
        if (pj.contadorNPCs >= maxNPCsActivos) {
            return;
        }

        // Spawnear 1-3 enemigos por ciclo
        int cantidad = 1 + (int) (Math.random() * 3);
        for (int i = 0; i < cantidad && pj.contadorNPCs < maxNPCsActivos; i++) {
            TipoNPC tipo = elegirTipoEnemigo();
            int[] pos = generarPosicionCercaJugador();
            spawnearNPC(tipo, pos[0], pos[1]);
        }
    }

    /**
     * Verifica si hay enemigos cerca del jugador y spawna si no hay suficientes.
     * Evita que el jugador se quede sin acción en zonas vacías.
     */
    public void verificarYSpawnearCercanos() {
        int radioCercano = 10 * pj.tamanioTile;
        int radioSq = radioCercano * radioCercano;
        int enemigoCercanos = 0;

        for (int i = 0; i < POOL_TOTAL; i++) {
            if (pj.npcs[i] != null && pj.npcs[i].activo && pj.npcs[i].estaVivo) {
                int dx = pj.npcs[i].worldx - pj.jugador.worldx;
                int dy = pj.npcs[i].worldy - pj.jugador.worldy;
                if (dx * dx + dy * dy < radioSq) {
                    enemigoCercanos++;
                }
            }
        }

        // Si hay menos de 5 enemigos cerca, forzar spawn
        if (enemigoCercanos < 5) {
            int necesarios = 5 - enemigoCercanos;
            for (int i = 0; i < necesarios; i++) {
                TipoNPC tipo = elegirTipoEnemigo();
                int[] pos = generarPosicionCercaJugador();
                spawnearNPC(tipo, pos[0], pos[1]);
            }
        }
    }
}
