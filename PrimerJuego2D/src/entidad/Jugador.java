package entidad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import main.PanelJuego;
import main.keyHandler;

public class Jugador extends Entidad{
	
	PanelJuego pj;
	keyHandler kh;
	
	public Jugador(PanelJuego pj , keyHandler kh) {
		this.pj = pj;
		this.kh = kh;
		setValorePorDefecto();
	}
	public void setValorePorDefecto() {
		x=100;
		y=100;
		vel = 4;
		direccion = "abajo";
		getImagenDelJugador();
	}
	
	public void getImagenDelJugador() {
		
		try {
			arriba1 = ImageIO.read(getClass().getResourceAsStream("/jugador/arriba_1.png"));
			arriba2 = ImageIO.read(getClass().getResourceAsStream("/jugador/arriba_2.png"));
			abajo1 = ImageIO.read(getClass().getResourceAsStream("/jugador/abajo_1.png"));
			abajo2 = ImageIO.read(getClass().getResourceAsStream("/jugador/abajo_2.png"));
			derecha1 = ImageIO.read(getClass().getResourceAsStream("/jugador/derecha_1.png"));
			derecha2 = ImageIO.read(getClass().getResourceAsStream("/jugador/derecha_2.png"));
			izquierda1 = ImageIO.read(getClass().getResourceAsStream("/jugador/izquierda_1.png"));
			izquierda2 = ImageIO.read(getClass().getResourceAsStream("/jugador/izquierda_2.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void update() {
		
		if(kh.arribaPres == true||kh.abajoPres == true||kh.izqPres == true||kh.drchPres == true) {
			
			if (kh.arribaPres == true) {
				direccion="arriba";
				y -= vel;
			}
			if (kh.abajoPres == true) {
				direccion="abajo";
				y += vel;
			}
			if (kh.izqPres == true) {
				direccion="izquierda";
				x -= vel;
			}
			if (kh.drchPres == true) {
				direccion = "derecha";
				x += vel;
			}
			
			contadorSpites++;
			if(contadorSpites>10) {
				if(numeroSpites == 1) {
					numeroSpites=2;
				} else if(numeroSpites == 2){
					numeroSpites= 1;
				}
				contadorSpites=0;
			}
			
		}
		
		
	}
	
	public void draw(Graphics2D g2) {
		
//		g2.setColor(Color.white);
//		g2.fillRect(x, y, pj.tamanioTile, pj.tamanioTile);
//
		BufferedImage imagen= null;
		
		switch (direccion) {
		case "arriba":
			if(numeroSpites==1) {
				imagen = arriba1;
			}
			if(numeroSpites == 2) {
				imagen = arriba2;
			}
			
			break;
		case "abajo":
			if(numeroSpites==1) {
				imagen = abajo1;
			}
			if(numeroSpites == 2) {
				imagen = abajo2;
			}

			break;
		case "izquierda":
			if(numeroSpites==1) {
				imagen = izquierda1;
			}
			if(numeroSpites == 2) {
				imagen = izquierda2;
			}

			break;
		case "derecha":
			if(numeroSpites==1) {
				imagen = derecha1;
			}
			if(numeroSpites == 2) {
				imagen = derecha2;
			}

			break;

		default:
			break;
		}
		
		g2.drawImage(imagen, x, y,pj.tamanioTile,pj.tamanioTile,null);
		
	}
	
	
}
