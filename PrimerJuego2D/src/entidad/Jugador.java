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

	public int numeroLlaves = 0; // Contador de llaves recolectadas
	
	// Sistema de power-ups
	public PowerUpManager powerUps = new PowerUpManager();
	
	// Sistema de ataque automático
	private int contadorAtaque = 0;
	private int intervaloAtaque = 30; // Dispara cada 30 frames (0.5 segundos a 60 FPS)
	private int velocidadBase = 4;

	/**
	 * Constructor de la clase Jugador. Inicializa la posición en pantalla y el área
	 * sólida (hitbox).
	 * 
	 * @param pj - Referencia al PanelJuego principal.
	 * @param kh - Referencia al manejador de teclas.
	 */
	public Jugador(PanelJuego pj, keyHandler kh) {
		this.pj = pj;
		this.kh = kh;

		screenX = pj.anchoPantalla / 2 - (pj.tamanioTile / 2);
		screeny = pj.altoPantalla / 2 - (pj.tamanioTile / 2);

		AreaSolida = new Rectangle();
		AreaSolida.x = 1;
		AreaSolida.y = 1;
		AreaSolida.height = pj.tamanioTile-2;
		AreaSolida.width = pj.tamanioTile-2;
		

		AreaSolidaDefaultX = AreaSolida.x;
		AreaSolidaDefaultY = AreaSolida.y;

		setValorePorDefecto();
	}

	/**
	 * Establece los valores iniciales del jugador. Posición en el mundo, velocidad
	 * y dirección.
	 */
	public void setValorePorDefecto() {
		// Posicionar al jugador en el CENTRO del mapa
		worldx = pj.tamanioTile * (pj.maxWorldcol / 2);
		worldy = pj.tamanioTile * (pj.maxWorldfilas / 2);
		vel = velocidadBase;
		direccion = "abajo";
		
		// ===== Estadísticas del jugador =====
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
			arriba1 = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream("/jugador/arriba_0001.png")),pj.tamanioTile , pj.tamanioTile);
			arriba2 = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream("/jugador/arriba_0002.png")),pj.tamanioTile , pj.tamanioTile);
			abajo1 = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream("/jugador/abajo_0001.png")),pj.tamanioTile , pj.tamanioTile);
			abajo2 = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream("/jugador/abajo_0002.png")),pj.tamanioTile , pj.tamanioTile);
			derecha1 = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream("/jugador/derecha_0001.png")),pj.tamanioTile , pj.tamanioTile);
			derecha2 = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream("/jugador/derecha_0002.png")),pj.tamanioTile , pj.tamanioTile);
			derecha3 = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream("/jugador/derecha_0003.png")),pj.tamanioTile , pj.tamanioTile);
			izquierda1 = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream("/jugador/izquierda_0001.png")),pj.tamanioTile , pj.tamanioTile);
			izquierda2 = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream("/jugador/izquierda_0002.png")),pj.tamanioTile , pj.tamanioTile);
			izquierda3 = miTool.escalarImagen(ImageIO.read(getClass().getResourceAsStream("/jugador/izquierda_0003.png")),pj.tamanioTile , pj.tamanioTile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Actualiza la lógica del jugador. Procesa la entrada del usuario, mueve al
	 * jugador y verifica colisiones.
	 */
	
	public void update() {
		// Actualizar invulnerabilidad
		actualizarInvulnerabilidad();
		
		// No actualizar si está muerto
		if (!estaVivo) {
			estado = EstadoEntidad.MURIENDO;
			return;
		}
		
		if(hayMovimiento == false ) {
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
			
			contadorPixeles +=vel;
			
			if (contadorPixeles >= pj.tamanioTile) { 
				 hayMovimiento = false; 
				 contadorPixeles= 0; 
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
			

		} else { //nueva logica agregada para un reposo mas sueve no que se detenga en seco
			//en si es mas para evitar glitches
			 contadorReposo++;
			if (contadorReposo == 20) { 
			 numeroSpites = 1;  
			 contadorReposo = 0; 
			}
		}
		
		// Actualizar estado del jugador
		if (hayMovimiento) {
			estado = EstadoEntidad.MOVIENDO;
		} else {
			estado = EstadoEntidad.IDLE;
		}
		
		// Actualizar power-ups
		powerUps.actualizar();
		
		// Aplicar multiplicador de velocidad
		vel = (int)(velocidadBase * powerUps.multiplicadorVelocidad);
		
		// Sistema de ataque automático
		contadorAtaque++;
		if (contadorAtaque >= intervaloAtaque) {
			dispararProyectil();
			contadorAtaque = 0;
		}
	}
	
	/**
	 * Dispara un proyectil en la dirección actual del jugador
	 */
	private void dispararProyectil() {
		// Buscar espacio vacío en el array de proyectiles 
		for (int i = 0; i < pj.proyectiles.length; i++) {
			if (pj.proyectiles[i] == null) {
				int dano = (int)(ataque * powerUps.multiplicadorAtaque);
				int proyectilX = worldx + pj.tamanioTile / 2 - 8;
				int proyectilY = worldy + pj.tamanioTile / 2 - 8;
				
				pj.proyectiles[i] = new Proyectil(pj, proyectilX, proyectilY, direccion, dano);
				break;
			}
		}
	}
	
	/**
	 * Sobrescribe recibirDanio para registrar estadísticas y aplicar invencibilidad
	 */
	@Override
	public void recibirDanio(int cantidad) {
		// Verificar invencibilidad por power-up
		if (powerUps.invencibilidadActiva) {
			return;
		}
		
		// Registrar estadística
		pj.stats.registrarAtaqueRecibido(cantidad);
		
		// Llamar al método padre
		super.recibirDanio(cantidad);
	}
	


	/**
	 * Dibuja al jugador en la pantalla.
	 * 
	 * @param g2 - Contexto gráfico 2D.
	 */
	public void draw(Graphics2D g2) {

		BufferedImage imagen = null;

		switch (direccion) {
		case "arriba":
			if (numeroSpites == 1) {
				imagen = arriba1;
			}
			if (numeroSpites == 2) {
				imagen = arriba2;
			}
			if (numeroSpites == 3) {
				imagen = arriba2;
			}

			break;
		case "abajo":
			if (numeroSpites == 1) {
				imagen = abajo1;
			}
			if (numeroSpites == 2) {
				imagen = abajo2;
			}
			if (numeroSpites == 3) {
				imagen = abajo2;
			}

			break;
		case "izquierda":
			if (numeroSpites == 1) {
				imagen = izquierda1;
			}
			if (numeroSpites == 2) {
				imagen = izquierda3;
			}
			if (numeroSpites == 3) {
				imagen = izquierda2;
			}

			break;
		case "derecha":
			if (numeroSpites == 1) {
				imagen = derecha1;
			}
			if (numeroSpites == 2) {
				imagen = derecha3;
			}
			if (numeroSpites == 3) {
				imagen = derecha2;
			}

			break;

		default:
			break;
		}

		g2.drawImage(imagen, screenX, screeny,  null);
		
		// Efecto de daño (parpadeo)
		if (contadorInvulnerabilidad > 0 && contadorInvulnerabilidad % 10 < 5) {
			g2.setColor(new Color(255, 0, 0, 100));
			g2.fillRect(screenX, screeny, pj.tamanioTile, pj.tamanioTile);
		}
		
		//hitbox
		if(debug) {
			g2.setColor(Color.RED);
			g2.drawRect(screenX + AreaSolida.x, screeny + AreaSolida.y, AreaSolida.width, AreaSolida.height);
		}
	}

	// metodos del juego

	/**
	* Maneja la interacción con objetos del mundo.
	 * @param index - índice del objeto en el array pj.obj[]
	 */
	public void recogerObjeto(int index) {
		if (index != 999) {
			// Verificar si es un cofre power-up
			if (pj.objs[index] instanceof objetos.OBJ_CofrePowerUp) {
				objetos.OBJ_CofrePowerUp cofre = (objetos.OBJ_CofrePowerUp) pj.objs[index];
				
				// Aplicar power-up
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
				pj.objs[index] = null; // Eliminar cofre del mapa
			}
		}
	}

}
