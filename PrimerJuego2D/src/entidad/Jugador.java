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
		worldx = pj.tamanioTile * 24;
		worldy = pj.tamanioTile * 20;
		vel = 4;
		direccion = "abajo";
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
		// Por ahora vacío - se implementará el nuevo sistema de
		//objetos del Action RPG
	}

}
