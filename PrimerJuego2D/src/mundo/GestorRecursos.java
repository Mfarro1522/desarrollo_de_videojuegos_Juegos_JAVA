package mundo;

import configuracion.Configuracion;
import entidad.*;
import entidad.NPC.TipoNPC;
import items.*;

/**
 * Configura y gestiona la colocación de objetos y NPCs en el mundo.
 *
 * OPTIMIZACIONES:
 * - Object Pool: pre-instancia POOL_TOTAL NPCs al inicio (cero 'new' durante gameplay)
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

    private static final int DISTANCIA_MIN_SPAWN = 5;
    private static final int DISTANCIA_MAX_SPAWN = 20;

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
            if (mundo.npcs[i] != null) mundo.npcs[i].activo = false;
        }
        mundo.contadorNPCs = 0;
    }

    // ===== BÚSQUEDA DE SLOTS =====

    private int buscarSlotInactivo(TipoNPC tipo) {
        int inicio, fin;
        switch (tipo) {
            case BAT:    inicio = INICIO_BAT;    fin = INICIO_BAT + POOL_BAT;    break;
            case SLIME:  inicio = INICIO_SLIME;  fin = INICIO_SLIME + POOL_SLIME;  break;
            case ORCO:   inicio = INICIO_ORCO;   fin = INICIO_ORCO + POOL_ORCO;   break;
            case GHOUL:  inicio = INICIO_GHOUL;  fin = INICIO_GHOUL + POOL_GHOUL;  break;
            default: return -1;
        }
        for (int i = inicio; i < fin; i++) {
            if (mundo.npcs[i] != null && !mundo.npcs[i].activo) return i;
        }
        return -1;
    }

    // ===== SELECCIÓN SEGÚN NIVEL =====

    private TipoNPC elegirTipoEnemigo() {
        int nivel = mundo.estadisticas.nivel;
        double rand = Math.random();

        if (nivel <= 2) return TipoNPC.BAT;
        else if (nivel <= 4) {
            return (rand < 0.50) ? TipoNPC.BAT : TipoNPC.SLIME;
        } else if (nivel <= 9) {
            return (rand < 0.60) ? TipoNPC.SLIME : TipoNPC.ORCO;
        } else if (nivel <= 14) {
            return (rand < 0.50) ? TipoNPC.ORCO : TipoNPC.GHOUL;
        } else {
            if (rand < 0.25) return TipoNPC.BAT;
            if (rand < 0.50) return TipoNPC.SLIME;
            if (rand < 0.75) return TipoNPC.ORCO;
            return TipoNPC.GHOUL;
        }
    }

    // ===== POSICIONES =====

    private int[] generarPosicionAleatoria() {
        int margen = 3 * Configuracion.TAMANO_TILE;
        int x = margen + (int) (Math.random() * (Configuracion.MUNDO_ANCHO - 2 * margen));
        int y = margen + (int) (Math.random() * (Configuracion.MUNDO_ALTO - 2 * margen));
        return new int[] { x, y };
    }

    private int[] generarPosicionCercaJugador() {
        int minDist = DISTANCIA_MIN_SPAWN * Configuracion.TAMANO_TILE;
        int maxDist = DISTANCIA_MAX_SPAWN * Configuracion.TAMANO_TILE;

        for (int intento = 0; intento < 10; intento++) {
            double angulo = Math.random() * 2 * Math.PI;
            int distancia = minDist + (int) (Math.random() * (maxDist - minDist));
            int x = mundo.jugador.worldx + (int) (Math.cos(angulo) * distancia);
            int y = mundo.jugador.worldy + (int) (Math.sin(angulo) * distancia);

            int margen = 2 * Configuracion.TAMANO_TILE;
            x = Math.max(margen, Math.min(x, Configuracion.MUNDO_ANCHO - margen));
            y = Math.max(margen, Math.min(y, Configuracion.MUNDO_ALTO - margen));

            return new int[] { x, y };
        }
        return generarPosicionAleatoria();
    }

    // ===== SPAWN =====

    private boolean spawnearNPC(TipoNPC tipo, int x, int y) {
        int slot = buscarSlotInactivo(tipo);
        if (slot == -1) return false;
        mundo.npcs[slot].activar(x, y);
        return true;
    }

    // ===== PÚBLICOS =====

    public void setObjetct() {
        int t = Configuracion.TAMANO_TILE;

        mundo.objs[0] = new CofreNormal(t);
        mundo.objs[0].worldX = 30 * t;
        mundo.objs[0].worldY = 29 * t;

        mundo.objs[1] = new CofrePowerUp(t, CofrePowerUp.TipoPowerUp.INVENCIBILIDAD);
        mundo.objs[1].worldX = 15 * t; mundo.objs[1].worldY = 15 * t;

        mundo.objs[2] = new CofrePowerUp(t, CofrePowerUp.TipoPowerUp.VELOCIDAD);
        mundo.objs[2].worldX = 45 * t; mundo.objs[2].worldY = 45 * t;

        mundo.objs[3] = new CofrePowerUp(t, CofrePowerUp.TipoPowerUp.ATAQUE);
        mundo.objs[3].worldX = 60 * t; mundo.objs[3].worldY = 30 * t;

        mundo.objs[4] = new CofrePowerUp(t, CofrePowerUp.TipoPowerUp.CURACION);
        mundo.objs[4].worldX = 25 * t; mundo.objs[4].worldY = 50 * t;

        mundo.objs[5] = new CofreNormal(t);
        mundo.objs[5].worldX = 70 * t; mundo.objs[5].worldY = 20 * t;

        mundo.objs[6] = new CofrePowerUp(t, CofrePowerUp.TipoPowerUp.CURACION);
        mundo.objs[6].worldX = 80 * t; mundo.objs[6].worldY = 70 * t;

        mundo.objs[7] = new CofrePowerUp(t, CofrePowerUp.TipoPowerUp.VELOCIDAD);
        mundo.objs[7].worldX = 10 * t; mundo.objs[7].worldY = 80 * t;

        mundo.objs[8] = new CofreNormal(t);
        mundo.objs[8].worldX = 55 * t; mundo.objs[8].worldY = 65 * t;

        mundo.objs[9] = new CofrePowerUp(t, CofrePowerUp.TipoPowerUp.INVENCIBILIDAD);
        mundo.objs[9].worldX = 85 * t; mundo.objs[9].worldY = 40 * t;
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

    public void respawnearEnemigos() {
        maxNPCsActivos = 60 + (mundo.estadisticas.nivel * 10);
        if (maxNPCsActivos > 300) maxNPCsActivos = 300;

        if (mundo.contadorNPCs >= maxNPCsActivos) return;

        int cantidad = 1 + (int) (Math.random() * 3);
        for (int i = 0; i < cantidad && mundo.contadorNPCs < maxNPCsActivos; i++) {
            TipoNPC tipo = elegirTipoEnemigo();
            int[] pos = generarPosicionCercaJugador();
            spawnearNPC(tipo, pos[0], pos[1]);
        }
    }

    public void verificarYSpawnearCercanos() {
        int radioCercano = 10 * Configuracion.TAMANO_TILE;
        int radioSq = radioCercano * radioCercano;
        int enemigoCercanos = 0;

        for (int i = 0; i < POOL_TOTAL; i++) {
            if (mundo.npcs[i] != null && mundo.npcs[i].activo && mundo.npcs[i].estaVivo) {
                int dx = mundo.npcs[i].worldx - mundo.jugador.worldx;
                int dy = mundo.npcs[i].worldy - mundo.jugador.worldy;
                if (dx * dx + dy * dy < radioSq) enemigoCercanos++;
            }
        }

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
