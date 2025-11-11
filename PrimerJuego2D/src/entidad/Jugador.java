package entidad;

import java.awt.Graphics2D;
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

	public Jugador(PanelJuego pj, keyHandler kh) {
		this.pj = pj;
		this.kh = kh;
		
		screenX = pj.anchoPantalla/2-(pj.tamanioTile/2);
		screeny =pj.altoPantalla/2-(pj.tamanioTile/2);
		
		setValorePorDefecto();
	}

	public void setValorePorDefecto() {
		worldx = pj.tamanioTile*23;
		worldy = pj.tamanioTile*21;
		vel = 4;
		direccion = "abajo";
		getImagenDelJugador();
	}

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

	public void update() {

		if (kh.arribaPres == true || kh.abajoPres == true || kh.izqPres == true || kh.drchPres == true) {

			if (kh.arribaPres == true) {
				direccion = "arriba";
				worldy -= vel;
			}
			if (kh.abajoPres == true) {
				direccion = "abajo";
				worldy += vel;
			}
			if (kh.izqPres == true) {
				direccion = "izquierda";
				worldx -= vel;
			}
			if (kh.drchPres == true) {
				direccion = "derecha";
				worldx += vel;
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

	public void draw(Graphics2D g2) {

//		g2.setColor(Color.white);
//		g2.fillRect(x, y, pj.tamanioTile, pj.tamanioTile);
//
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

}
