package entidad;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import main.PanelJuego;
import main.keyHandler;

public class Jugador extends Entidad {

	PanelJuego pj;
	keyHandler kh;

	public final int screenX;
	public final int screeny;

	public int numeroLlaves = 0; // Contador de llaves recolectadas
	

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
		AreaSolida.height = (int) Math.round(pj.tamanioTile * 0.6);
		AreaSolida.width = (int) Math.round(pj.tamanioTile * 0.5);
		AreaSolida.x = (pj.tamanioTile - AreaSolida.width) / 2;
		AreaSolida.y = (pj.tamanioTile - AreaSolida.height) / 2;

		AreaSolidaDefaultX = AreaSolida.x;
		AreaSolidaDefaultY = AreaSolida.y;

		setValorePorDefecto();
	}

	/**
	 * Establece los valores iniciales del jugador. Posición en el mundo, velocidad
	 * y dirección.
	 */
	public void setValorePorDefecto() {
		worldx = pj.tamanioTile * 23;
		worldy = pj.tamanioTile * 21;
		vel = 4;
		direccion = "abajo";
		getImagenDelJugador();
	}

	/**
	 * Carga las imágenes de los sprites del jugador desde los recursos.
	 */
	public void getImagenDelJugador() {

		try {
			arriba1 = ImageIO.read(getClass().getResourceAsStream("/jugador/arriba_0001.png"));
			arriba2 = ImageIO.read(getClass().getResourceAsStream("/jugador/arriba_0002.png"));
			abajo1 = ImageIO.read(getClass().getResourceAsStream("/jugador/abajo_0001.png"));
			abajo2 = ImageIO.read(getClass().getResourceAsStream("/jugador/abajo_0002.png"));
			derecha1 = ImageIO.read(getClass().getResourceAsStream("/jugador/derecha_0001.png"));
			derecha2 = ImageIO.read(getClass().getResourceAsStream("/jugador/derecha_0002.png"));
			derecha3 = ImageIO.read(getClass().getResourceAsStream("/jugador/derecha_0003.png"));
			izquierda1 = ImageIO.read(getClass().getResourceAsStream("/jugador/izquierda_0001.png"));
			izquierda2 = ImageIO.read(getClass().getResourceAsStream("/jugador/izquierda_0002.png"));
			izquierda3 = ImageIO.read(getClass().getResourceAsStream("/jugador/izquierda_0003.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Actualiza la lógica del jugador. Procesa la entrada del usuario, mueve al
	 * jugador y verifica colisiones.
	 */
	public void update() {

		if (kh.arribaPres == true || kh.abajoPres == true || kh.izqPres == true || kh.drchPres == true) {

			if (kh.arribaPres == true) {
				direccion = "arriba";
				worldy -= vel;
			} else if (kh.abajoPres == true) {
				direccion = "abajo";
				worldy += vel;
			} else if (kh.izqPres == true) {
				direccion = "izquierda";
				worldx -= vel;
			} else if (kh.drchPres == true) {
				direccion = "derecha";
				worldx += vel;
			}

			// Detección de colisiones
			hayColision = false;
			pj.dColisiones.chektile(this);

			int objIndex = pj.dColisiones.checkObjeto(this, true);
			recogerObjeto(objIndex);

			// Si detecta colisión, revertimos el movimiento
			if (hayColision == true) {
				//System.out.println("hay colision");
				switch (direccion) {
				case "arriba":
					worldy += vel;
					break;
				case "abajo":
					worldy -= vel;
					break;
				case "izquierda":
					worldx += vel;
					break;
				case "derecha":
					worldx -= vel;
					break;
				}
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

		}

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

		g2.drawImage(imagen, screenX, screeny, pj.tamanioTile, pj.tamanioTile, null);
	}

	// metodos del juego

	/**
	 * Recoge un objeto del mundo y ejecuta la acción correspondiente
	 * 
	 * @param index - índice del objeto en el array pj.obj[]
	 */
	public void recogerObjeto(int index) {
		if (index != 999) { // 999 = sin colisión
			String nombreObjeto = pj.objs[index].nombre;
			switch (nombreObjeto) {
			case "llave":
				numeroLlaves++;
				pj.objs[index] = null; // Eliminar objeto del mundo
				System.out.println("Llaves: " + numeroLlaves);
				break;
			case "puerta":
				if (numeroLlaves > 0) {
					pj.objs[index] = null; // Eliminar puerta
					numeroLlaves--; // Consumir una llave
					System.out.println("¡Puerta abierta! Llaves restantes: " + numeroLlaves);
				} else {
					System.out.println("Necesitas una llave para abrir esta puerta");
				}
				break;
			case "cofre":
				// Placeholder: puedes añadir lógica propia
				pj.objs[index] = null;
				System.out.println("¡Cofre abierto!");
				break;
			}
		}
	}

}
