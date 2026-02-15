package main;

import java.awt.Color;
import entidad.Bat;
import entidad.Ghoul;
import entidad.Orco;
import entidad.Slime;
import objetos.OBJ_llave;
import objetos.OBJ_puerta;
import objetos.OBJ_botas;
import objetos.OBJ_cofre;
import objetos.OBJ_CofrePowerUp;

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
	 * Instancia y posiciona los objetos en el mapa (cofres power-up).
	 */
	public void setObjetct() {
		// Colocar cofres power-up aleatoriamente por TODO el mapa
		colocarCofresPowerUp(10); // 10 cofres iniciales
	}

	/**
	 * Coloca cofres power-up aleatoriamente por TODO el mapa
	 */
	private void colocarCofresPowerUp(int cantidad) {
		OBJ_CofrePowerUp.TipoPowerUp[] tipos = OBJ_CofrePowerUp.TipoPowerUp.values();

		for (int i = 0; i < cantidad && i < pj.objs.length; i++) {
			// Tipo aleatorio
			OBJ_CofrePowerUp.TipoPowerUp tipoAleatorio = tipos[(int) (Math.random() * tipos.length)];

			pj.objs[i] = new OBJ_CofrePowerUp(pj.tamanioTile, tipoAleatorio);

			// Posición COMPLETAMENTE ALEATORIA en TODO el mapa
			int worldX = (int) (Math.random() * (pj.maxWorldcol - 4) + 2) * pj.tamanioTile;
			int worldY = (int) (Math.random() * (pj.maxWorldfilas - 4) + 2) * pj.tamanioTile;

			pj.objs[i].worldX = worldX;
			pj.objs[i].worldY = worldY;
		}

		pj.agregarNotificacion("✓ " + cantidad + " cofres aparecieron por el mapa", Color.GREEN, 3);
	}

	/**
	 * Genera NPCs enemigos por TODO el mapa.
	 */
	public void setNPCs() {
		// Spawn inicial: 60 slimes distribuidos por TODO el mapa
		int cantidadEnemigos = 60;

		for (int i = 0; i < cantidadEnemigos; i++) {
			spawnearEnemigoAleatorio();
		}

		pj.agregarNotificacion("⚔ " + cantidadEnemigos + " enemigos aparecieron", Color.RED, 3);
	}

	/**
	 * Genera oleadas continuas de enemigos.
	 * Llamar desde update() cuando hay pocos enemigos.
	 */
	public void respawnearEnemigos() {
		// Mantener entre 50 y 80 enemigos activos para acción constante
		int minimoEnemigos = 50;
		int maximoEnemigos = 80;

		if (pj.contadorNPCs < minimoEnemigos) {
			// Spawnear oleadas grandes (8 a 15 enemigos)
			int cantidad = (int) (Math.random() * 8) + 8;

			for (int i = 0; i < cantidad; i++) {
				spawnearEnemigo();
			}
		} else if (pj.contadorNPCs < maximoEnemigos) {
			// Spawnear pequeños grupos (2 a 4 enemigos)
			int cantidad = (int) (Math.random() * 3) + 2;

			for (int i = 0; i < cantidad; i++) {
				spawnearEnemigo();
			}
		}
	}

	/**
	 * Spawnea un enemigo en una posición cercana al jugador.
	 */
	private void spawnearEnemigo() {
		// Buscar slot vacío
		int slot = -1;
		for (int i = 0; i < pj.npcs.length; i++) {
			if (pj.npcs[i] == null) {
				slot = i;
				break;
			}
		}

		if (slot == -1)
			return; // Array lleno

		pj.npcs[slot] = crearEnemigoAleatorio();

		// Generar posición aleatoria cerca del jugador
		int distanciaMinima = 6 * pj.tamanioTile;
		int distanciaMaxima = 12 * pj.tamanioTile;

		double angulo = Math.random() * 2 * Math.PI;
		int distancia = (int) (Math.random() * (distanciaMaxima - distanciaMinima) + distanciaMinima);

		int offsetX = (int) (Math.cos(angulo) * distancia);
		int offsetY = (int) (Math.sin(angulo) * distancia);

		int worldX = pj.jugador.worldx + offsetX;
		int worldY = pj.jugador.worldy + offsetY;

		// Asegurar límites
		worldX = Math.max(pj.tamanioTile, Math.min(worldX, pj.maxWorldAncho - pj.tamanioTile));
		worldY = Math.max(pj.tamanioTile, Math.min(worldY, pj.maxWorldAlto - pj.tamanioTile));

		pj.npcs[slot].worldx = worldX;
		pj.npcs[slot].worldy = worldY;
		pj.npcs[slot].direccion = "abajo";

		pj.contadorNPCs++;
	}

	/**
	 * Spawnea un enemigo en una posición COMPLETAMENTE ALEATORIA del mapa.
	 */
	private void spawnearEnemigoAleatorio() {
		// Buscar slot vacío
		int slot = -1;
		for (int i = 0; i < pj.npcs.length; i++) {
			if (pj.npcs[i] == null) {
				slot = i;
				break;
			}
		}

		if (slot == -1)
			return; // Array lleno

		pj.npcs[slot] = crearEnemigoAleatorio();

		// Posición COMPLETAMENTE ALEATORIA en TODO el mapa
		int worldX = (int) (Math.random() * (pj.maxWorldcol - 4) + 2) * pj.tamanioTile;
		int worldY = (int) (Math.random() * (pj.maxWorldfilas - 4) + 2) * pj.tamanioTile;

		pj.npcs[slot].worldx = worldX;
		pj.npcs[slot].worldy = worldY;
		pj.npcs[slot].direccion = "abajo";

		pj.contadorNPCs++;
	}

	/**
	 * Verifica si hay enemigos cerca del jugador.
	 * Si no hay enemigos en un radio cercano, spawnea una oleada inmediata.
	 */
	public void verificarYSpawnearCercanos() {
		int radioCercano = 10 * pj.tamanioTile; // Radio de 10 tiles
		int enemigosCercanos = 0;

		// Contar enemigos cercanos
		for (int i = 0; i < pj.npcs.length; i++) {
			if (pj.npcs[i] != null && pj.npcs[i].estaVivo) {
				int distanciaX = Math.abs(pj.npcs[i].worldx - pj.jugador.worldx);
				int distanciaY = Math.abs(pj.npcs[i].worldy - pj.jugador.worldy);
				double distancia = Math.sqrt(distanciaX * distanciaX + distanciaY * distanciaY);

				if (distancia < radioCercano) {
					enemigosCercanos++;
				}
			}
		}

		// Si hay menos de 10 enemigos cerca, spawnear oleada inmediata
		if (enemigosCercanos < 10) {
			int cantidad = 15 - enemigosCercanos; // Llenar hasta 15 cercanos

			for (int i = 0; i < cantidad; i++) {
				spawnearEnemigoCercano();
			}

			pj.agregarNotificacion("⚡ Oleada de refuerzo: +" + cantidad + " enemigos", Color.ORANGE, 2);
		}
	}

	/**
	 * Spawnea un enemigo específicamente CERCA del jugador (para oleadas de
	 * refuerzo).
	 */
	private void spawnearEnemigoCercano() {
		// Buscar slot vacío
		int slot = -1;
		for (int i = 0; i < pj.npcs.length; i++) {
			if (pj.npcs[i] == null) {
				slot = i;
				break;
			}
		}

		if (slot == -1)
			return; // Array lleno

		pj.npcs[slot] = crearEnemigoAleatorio();

		// Spawn MUY cerca para acción inmediata
		int distanciaMinima = 4 * pj.tamanioTile; // Muy cerca
		int distanciaMaxima = 8 * pj.tamanioTile; // Cercano

		double angulo = Math.random() * 2 * Math.PI;
		int distancia = (int) (Math.random() * (distanciaMaxima - distanciaMinima) + distanciaMinima);

		int offsetX = (int) (Math.cos(angulo) * distancia);
		int offsetY = (int) (Math.sin(angulo) * distancia);

		int worldX = pj.jugador.worldx + offsetX;
		int worldY = pj.jugador.worldy + offsetY;

		// Asegurar límites
		worldX = Math.max(pj.tamanioTile, Math.min(worldX, pj.maxWorldAncho - pj.tamanioTile));
		worldY = Math.max(pj.tamanioTile, Math.min(worldY, pj.maxWorldAlto - pj.tamanioTile));

		pj.npcs[slot].worldx = worldX;
		pj.npcs[slot].worldy = worldY;
		pj.npcs[slot].direccion = "abajo";

		pj.contadorNPCs++;
	}

	/**
	 * Crea un enemigo aleatorio basado en el nivel del jugador.
	 * Progresión:
	 * - Nivel 1: Solo Bats
	 * - Nivel 2-4: Bats + Slimes
	 * - Nivel 5-9: Slimes + Orcos (Bats desaparecen)
	 * - Nivel 10-14: Orcos + Ghouls (Slimes desaparecen)
	 * - Nivel 15+: Todos los enemigos
	 */
	private entidad.NPC crearEnemigoAleatorio() {
		int nivelJugador = pj.stats.nivel;

		// Nivel 1: Solo Bats (100%)
		if (nivelJugador < 2) {
			return new Bat(pj);
		}

		// Nivel 2-4: Bats (50%) + Slimes (50%)
		if (nivelJugador < 5) {
			return (Math.random() < 0.5) ? new Bat(pj) : new Slime(pj);
		}

		// Nivel 5-9: Slimes (60%) + Orcos (40%)
		if (nivelJugador < 10) {
			return (Math.random() < 0.6) ? new Slime(pj) : new Orco(pj);
		}

		// Nivel 10-14: Orcos (50%) + Ghouls (50%)
		if (nivelJugador < 15) {
			return (Math.random() < 0.5) ? new Orco(pj) : new Ghoul(pj);
		}

		// Nivel 15+: Todos los enemigos (25% cada uno)
		double random = Math.random();
		if (random < 0.25) {
			return new Bat(pj);
		} else if (random < 0.50) {
			return new Slime(pj);
		} else if (random < 0.75) {
			return new Orco(pj);
		} else {
			return new Ghoul(pj);
		}
	}

}
