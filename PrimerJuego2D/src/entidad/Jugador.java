package entidad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import main.PanelJuego;
import main.UtilityTool;
import main.keyHandler;

public class Jugador extends Entidad {

	PanelJuego pj;
	keyHandler kh;

	public final int screenX;
	public final int screeny;

	boolean hayMovimiento = false;
	int contadorPixeles = 0;
	int contadorReposo = 0;

	boolean debug = false;// Cambiar a false para producción
	private UtilityTool miTool = new UtilityTool();

	// Sistema de power-ups
	public PowerUpManager powerUps = new PowerUpManager();

	// Sistema de ataque automático
	private int contadorAtaque = 0;
	private int intervaloAtaque = 30; // Dispara cada 30 frames (0.5 seg a 60 FPS)
	private int velocidadBase = 4;

	// Rectángulos pre-allocados para colisiones melee (cero GC)
	private final Rectangle tempAreaJugador = new Rectangle();
	private final Rectangle tempAreaNPC = new Rectangle();

	// ===== Sistema de personaje =====
	public String tipoPersonaje = "Doom"; // "Sideral", "Mago", "Doom"
	private boolean esMelee = false; // true = Doom (sin proyectiles, daño al contacto)
	private boolean tieneSpritesAtaque = true;
	private boolean tieneSpritesmuerte = true;

	/**
	 * Constructor de la clase Jugador.
	 */
	public Jugador(PanelJuego pj, keyHandler kh) {
		this.pj = pj;
		this.kh = kh;

		screenX = pj.anchoPantalla / 2 - (pj.tamanioTile / 2);
		screeny = pj.altoPantalla / 2 - (pj.tamanioTile / 2);

		AreaSolida = new Rectangle();
		AreaSolida.x = 1;
		AreaSolida.y = 1;
		AreaSolida.height = pj.tamanioTile - 2;
		AreaSolida.width = pj.tamanioTile - 2;

		AreaSolidaDefaultX = AreaSolida.x;
		AreaSolidaDefaultY = AreaSolida.y;

		setValorePorDefecto();
	}

	/**
	 * Configura el personaje seleccionado y carga sus recursos.
	 *
	 * @param tipo - "Sideral", "Mago" o "Doom"
	 */
	public void configurarPersonaje(String tipo) {
		this.tipoPersonaje = tipo;

		switch (tipo) {
			case "Sideral":
				rutaCarpeta = "/jugador/Sideral/";
				vidaMaxima = 10;
				ataque = 15;
				defensa = 3;
				velocidadBase = 5;
				esMelee = false;
				tieneSpritesAtaque = true;
				tieneSpritesmuerte = true;
				break;
			case "Mago":
				rutaCarpeta = "/jugador/Mago/";
				vidaMaxima = 15;
				ataque = 20;
				defensa = 2;
				velocidadBase = 4;
				esMelee = false;
				tieneSpritesAtaque = true;
				tieneSpritesmuerte = true;
				break;
			case "Doom":
			default:
				rutaCarpeta = "/jugador/Doom/";
				vidaMaxima = 10;
				ataque = 12;
				defensa = 8;
				velocidadBase = 3;
				esMelee = true; // Daño cuerpo a cuerpo
				tieneSpritesAtaque = true;
				tieneSpritesmuerte = true;
				break;
		}

		// Resetear estado
		vidaActual = vidaMaxima;
		vel = velocidadBase;
		estaVivo = true;
		estado = EstadoEntidad.IDLE;
		direccion = "abajo";
		frameMuerte = 0;
		contadorMuerte = 0;
		contadorAnimAtaque = 0;

		// Posicionar en el centro del mapa
		worldx = pj.tamanioTile * (pj.maxWorldcol / 2);
		worldy = pj.tamanioTile * (pj.maxWorldfilas / 2);

		// Cargar sprites
		getImagenDelJugador();
	}

	/**
	 * Establece valores por defecto (se llama desde constructor).
	 */
	public void setValorePorDefecto() {
		worldx = pj.tamanioTile * (pj.maxWorldcol / 2);
		worldy = pj.tamanioTile * (pj.maxWorldfilas / 2);
		vel = velocidadBase;
		direccion = "abajo";

		vidaMaxima = 25;
		vidaActual = vidaMaxima;
		ataque = 10;
		defensa = 5;
		estaVivo = true;
		estado = EstadoEntidad.IDLE;

		getImagenDelJugador();
	}

	/**
	 * Carga las imágenes de los sprites del jugador desde los recursos.
	 */
	public void getImagenDelJugador() {
		try {
			// Si no se ha configurado personaje aún, usar Doom por defecto
			if (rutaCarpeta == null || rutaCarpeta.isEmpty()) {
				rutaCarpeta = "/jugador/Doom/";
			}

			// ===== Sprites de movimiento (siempre disponibles) =====
			arriba1 = miTool.escalarImagen(
					ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "arriba_0001.png")), pj.tamanioTile,
					pj.tamanioTile);
			arriba2 = miTool.escalarImagen(
					ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "arriba_0002.png")), pj.tamanioTile,
					pj.tamanioTile);
			abajo1 = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "abajo_0001.png")),
					pj.tamanioTile, pj.tamanioTile);
			abajo2 = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "abajo_0002.png")),
					pj.tamanioTile, pj.tamanioTile);
			derecha1 = miTool.escalarImagen(
					ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "derecha_0001.png")), pj.tamanioTile,
					pj.tamanioTile);
			derecha2 = miTool.escalarImagen(
					ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "derecha_0002.png")), pj.tamanioTile,
					pj.tamanioTile);
			izquierda1 = miTool.escalarImagen(
					ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "izquierda_0001.png")), pj.tamanioTile,
					pj.tamanioTile);
			izquierda2 = miTool.escalarImagen(
					ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "izquierda_0002.png")), pj.tamanioTile,
					pj.tamanioTile);

			// ===== Sprites de ataque (si existen) =====
			if (tieneSpritesAtaque) {
				if (tipoPersonaje.equals("Sideral")) {
					// Sideral tiene nombres de archivo ligeramente diferentes
					// Usa movimiento arriba para ataque arriba
					ataqueArriba = arriba1;

					// Carga ataque abajo 0001 (Doom usa 0002)
					ataqueAbajo = miTool.escalarImagen(
							ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "ataqueAbajo_0001.png")),
							pj.tamanioTile, pj.tamanioTile);

					ataqueDer = miTool.escalarImagen(
							ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "ataqueDer_0001.png")),
							pj.tamanioTile, pj.tamanioTile);
					ataqueIzq = miTool.escalarImagen(
							ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "ataqueIzq_0001.png")),
							pj.tamanioTile, pj.tamanioTile);

				} else {
					// Lógica estándar (Doom/Mago)
					ataqueArriba = miTool.escalarImagen(
							ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "ataqueArriba_0001.png")),
							pj.tamanioTile, pj.tamanioTile);
					ataqueAbajo = miTool.escalarImagen(
							ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "ataqueAbajo_0002.png")),
							pj.tamanioTile, pj.tamanioTile);
					ataqueDer = miTool.escalarImagen(
							ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "ataqueDer_0001.png")),
							pj.tamanioTile, pj.tamanioTile);
					ataqueIzq = miTool.escalarImagen(
							ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "ataqueIzq_0001.png")),
							pj.tamanioTile, pj.tamanioTile);
				}
			} else {
				// Placeholders: se dibujarán como cuadros rojos en draw()
				ataqueArriba = null;
				ataqueAbajo = null;
				ataqueDer = null;
				ataqueIzq = null;
			}

			// ===== Sprites de muerte (si existen) =====
			if (tieneSpritesmuerte) {
				muerte1 = miTool.escalarImagen(
						ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "muerte_0001.png")),
						pj.tamanioTile, pj.tamanioTile);
				muerte2 = miTool.escalarImagen(
						ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "muerte_0002.png")),
						pj.tamanioTile, pj.tamanioTile);
				muerte3 = miTool.escalarImagen(
						ImageIO.read(getClass().getResourceAsStream(rutaCarpeta + "muerte_0003.png")),
						pj.tamanioTile, pj.tamanioTile);
			} else {
				// Placeholders: se dibujarán como cuadros rojos en draw()
				muerte1 = null;
				muerte2 = null;
				muerte3 = null;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ===================================================================
	// UPDATE
	// ===================================================================

	public void update() {
		// Actualizar invulnerabilidad
		actualizarInvulnerabilidad();

		// ===== Animación de muerte =====
		if (!estaVivo) {
			estado = EstadoEntidad.MURIENDO;
			contadorMuerte++;
			if (contadorMuerte >= duracionFrameMuerte) {
				frameMuerte++;
				contadorMuerte = 0;
				if (frameMuerte >= 3) {
					frameMuerte = 2; // Quedarse en el último frame
				}
			}
			return;
		}

		// ===== Decrementar animación de ataque =====
		if (contadorAnimAtaque > 0) {
			contadorAnimAtaque--;
			estado = EstadoEntidad.ATACANDO;

			// Doom: hacer daño melee mientras ataca
			if (esMelee) {
				atacarMelee();
			}

			if (contadorAnimAtaque == 0) {
				estado = EstadoEntidad.IDLE;
			}
		}

		// ===== Movimiento =====
		if (hayMovimiento == false) {
			if (kh.arribaPres == true) {
				direccion = "arriba";
				hayMovimiento = true;
			} else if (kh.abajoPres == true) {
				direccion = "abajo";
				hayMovimiento = true;
			} else if (kh.izqPres == true) {
				direccion = "izquierda";
				hayMovimiento = true;
			} else if (kh.drchPres == true) {
				direccion = "derecha";
				hayMovimiento = true;
			}
		}

		if (hayMovimiento == true) {
			// Detección de colisiones
			hayColision = false;
			pj.dColisiones.chektile(this);

			// Verificar colisión con objetos
			int objIndex = pj.dColisiones.checkObjeto(this, true);
			recogerObjeto(objIndex);

			if (hayColision == false) {
				switch (direccion) {
					case "arriba":
						worldy -= vel;
						break;
					case "abajo":
						worldy += vel;
						break;
					case "izquierda":
						worldx -= vel;
						break;
					case "derecha":
						worldx += vel;
						break;
				}
			}

			contadorPixeles += vel;

			if (contadorPixeles >= pj.tamanioTile) {
				hayMovimiento = false;
				contadorPixeles = 0;
			}

			contadorSpites++;
			if (contadorSpites > 10) {
				if (numeroSpites == 1) {
					numeroSpites = 2;
				} else if (numeroSpites == 2) {
					numeroSpites = 3;
				} else if (numeroSpites == 3) {
					numeroSpites = 1;
				}
				contadorSpites = 0;
			}

		} else {
			contadorReposo++;
			if (contadorReposo == 20) {
				numeroSpites = 1;
				contadorReposo = 0;
			}
		}

		// Actualizar estado (solo si no está atacando)
		if (estado != EstadoEntidad.ATACANDO) {
			if (hayMovimiento) {
				estado = EstadoEntidad.MOVIENDO;
			} else {
				estado = EstadoEntidad.IDLE;
			}
		}

		// Actualizar power-ups
		powerUps.actualizar();

		// Aplicar multiplicador de velocidad
		vel = (int) (velocidadBase * powerUps.multiplicadorVelocidad);

		// Sistema de ataque automático
		contadorAtaque++;
		if (contadorAtaque >= intervaloAtaque) {
			ejecutarAtaque();
			contadorAtaque = 0;
		}
	}

	// ===================================================================
	// ATAQUE
	// ===================================================================

	/**
	 * Ejecuta el ataque según el tipo de personaje.
	 * Doom: activa animación melee (daño por contacto).
	 * Mago/Sideral: dispara proyectil.
	 */
	private void ejecutarAtaque() {
		// Activar animación de ataque
		contadorAnimAtaque = duracionAnimAtaque;
		estado = EstadoEntidad.ATACANDO;

		// Solo disparar proyectil si NO es melee
		if (!esMelee) {
			dispararProyectil();
		}
	}

	/**
	 * Dispara un proyectil en la dirección actual del jugador.
	 */
	private void dispararProyectil() {
		for (int i = 0; i < pj.proyectiles.length; i++) {
			if (pj.proyectiles[i] == null) {
				int dano = (int) (ataque * powerUps.multiplicadorAtaque);
				int proyectilX = worldx + pj.tamanioTile / 2 - 8;
				int proyectilY = worldy + pj.tamanioTile / 2 - 8;

				pj.proyectiles[i] = new Proyectil(pj, proyectilX, proyectilY, direccion, dano);

				// TODO: Descomentar cuando agregues el archivo res/sound/attack.wav
				// pj.playSE(7); // Efecto de sonido al atacar

				break;
			}
		}
	}

	/**
	 * Ataque melee (Doom): usa SpatialHashGrid para encontrar NPCs cercanos.
	 * Optimizado: solo verifica colisión con NPCs en celdas adyacentes.
	 */
	private void atacarMelee() {
		tempAreaJugador.setBounds(
				worldx + AreaSolida.x,
				worldy + AreaSolida.y,
				AreaSolida.width,
				AreaSolida.height);

		int dano = (int) (ataque * powerUps.multiplicadorAtaque);

		pj.spatialGrid.consultar(worldx, worldy);
		int[] cercanos = pj.spatialGrid.getResultado();
		int count = pj.spatialGrid.getResultadoCount();

		for (int j = 0; j < count; j++) {
			int i = cercanos[j];
			if (pj.npcs[i] != null && pj.npcs[i].activo && pj.npcs[i].estaVivo) {
				tempAreaNPC.setBounds(
						pj.npcs[i].worldx + pj.npcs[i].AreaSolida.x,
						pj.npcs[i].worldy + pj.npcs[i].AreaSolida.y,
						pj.npcs[i].AreaSolida.width,
						pj.npcs[i].AreaSolida.height);

				if (tempAreaJugador.intersects(tempAreaNPC)) {
					pj.npcs[i].recibirDanio(dano);

					if (!pj.npcs[i].estaVivo) {
						pj.stats.registrarEnemigoEliminado();
						pj.stats.ganarExperiencia(pj.npcs[i].experienciaAOtorgar);
					}
				}
			}
		}
	}

	// ===================================================================
	// DAÑO
	// ===================================================================

	@Override
	public void recibirDanio(int cantidad) {
		if (powerUps.invencibilidadActiva) {
			return;
		}
		pj.stats.registrarAtaqueRecibido(cantidad);
		super.recibirDanio(cantidad);
	}

	// ===================================================================
	// DRAW
	// ===================================================================

	public void draw(Graphics2D g2) {
		BufferedImage imagen = null;

		// ===== Animación de muerte =====
		if (estado == EstadoEntidad.MURIENDO) {
			if (tieneSpritesmuerte && muerte1 != null) {
				if (frameMuerte == 0) {
					imagen = muerte1;
				} else if (frameMuerte == 1) {
					imagen = muerte2;
				} else {
					imagen = muerte3;
				}
				g2.drawImage(imagen, screenX, screeny, null);
			} else {
				// Placeholder: cuadro rojo para Sideral
				g2.setColor(Color.RED);
				g2.fillRect(screenX, screeny, pj.tamanioTile, pj.tamanioTile);
				g2.setColor(Color.WHITE);
				g2.drawString("X", screenX + pj.tamanioTile / 2 - 4, screeny + pj.tamanioTile / 2 + 4);
			}
			return;
		}

		// ===== Animación de ataque =====
		if (estado == EstadoEntidad.ATACANDO) {
			imagen = obtenerSpriteAtaque();
			if (imagen == null) {
				// Placeholder: cuadro rojo para Sideral
				g2.setColor(new Color(255, 50, 50, 180));
				g2.fillRect(screenX, screeny, pj.tamanioTile, pj.tamanioTile);
				g2.setColor(Color.WHITE);
				g2.drawString("⚔", screenX + pj.tamanioTile / 2 - 6, screeny + pj.tamanioTile / 2 + 6);
				return;
			}
		} else {
			// ===== Sprites normales de movimiento =====
			imagen = obtenerSpriteMovimiento();
		}

		g2.drawImage(imagen, screenX, screeny, null);

		// Efecto de daño (parpadeo)
		if (contadorInvulnerabilidad > 0 && contadorInvulnerabilidad % 10 < 5) {
			g2.setColor(new Color(255, 0, 0, 100));
			g2.fillRect(screenX, screeny, pj.tamanioTile, pj.tamanioTile);
		}

		// Hitbox debug
		if (debug) {
			g2.setColor(Color.RED);
			g2.drawRect(screenX + AreaSolida.x, screeny + AreaSolida.y, AreaSolida.width, AreaSolida.height);
		}
	}

	/**
	 * Retorna el sprite de ataque según la dirección actual.
	 */
	private BufferedImage obtenerSpriteAtaque() {
		switch (direccion) {
			case "arriba":
				return ataqueArriba;
			case "abajo":
				return ataqueAbajo;
			case "izquierda":
				return ataqueIzq;
			case "derecha":
				return ataqueDer;
			default:
				return null;
		}
	}

	/**
	 * Retorna el sprite de movimiento según la dirección y frame actual.
	 */
	private BufferedImage obtenerSpriteMovimiento() {
		switch (direccion) {
			case "arriba":
				return (numeroSpites == 1) ? arriba1 : arriba2;
			case "abajo":
				return (numeroSpites == 1) ? abajo1 : abajo2;
			case "izquierda":
				return (numeroSpites == 1) ? izquierda1 : izquierda2;
			case "derecha":
				return (numeroSpites == 1) ? derecha1 : derecha2;
			default:
				return abajo1;
		}
	}

	// ===================================================================
	// OBJETOS
	// ===================================================================

	/**
	 * Maneja la interacción con objetos del mundo.
	 *
	 * @param index - índice del objeto en el array pj.obj[]
	 */
	public void recogerObjeto(int index) {
		if (index != 999) {
			if (pj.objs[index] instanceof objetos.OBJ_CofrePowerUp) {
				objetos.OBJ_CofrePowerUp cofre = (objetos.OBJ_CofrePowerUp) pj.objs[index];

				switch (cofre.tipoPowerUp) {
					case INVENCIBILIDAD:
						powerUps.activarInvencibilidad(10);
						pj.agregarNotificacion("🛡 Invencibilidad activada!", Color.CYAN, 3);
						break;
					case VELOCIDAD:
						powerUps.aumentarVelocidad(50, 15);
						pj.agregarNotificacion("⚡ Velocidad aumentada!", Color.YELLOW, 3);
						break;
					case ATAQUE:
						powerUps.aumentarAtaque(30, 20);
						pj.agregarNotificacion("💪 Ataque aumentado!", Color.RED, 3);
						break;
					case CURACION:
						vidaActual = Math.min(vidaActual + 30, vidaMaxima);
						pj.agregarNotificacion("❤ +30 de vida!", Color.GREEN, 3);
						break;
				}

				pj.stats.registrarCofreRecogido();
				pj.objs[index] = null;
			} else if (pj.objs[index] instanceof objetos.OBJ_cofre) {
				// Cofre normal: otorga puntos y experiencia
				pj.stats.registrarCofreRecogido();
				pj.agregarNotificacion("📦 Cofre encontrado! +50 EXP", Color.ORANGE, 3);
				pj.stats.ganarExperiencia(50);
				pj.objs[index] = null;
			}
		}
	}
}
