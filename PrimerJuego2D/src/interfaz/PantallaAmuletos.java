package interfaz;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import configuracion.Configuracion;
import items.Amuleto;
import items.GestorAmuletos;
import mundo.MundoJuego;

/**
 * Panel de selección de amuletos al abrir un cofre.
 *
 * Tres modos:
 *   0 (Normal):  3 tarjetas con items para elegir
 *   1 (Bomba):   Evento forzado de dinamita, sin elección
 *   2 (Anillo):  Evento especial con Aceptar/Rechazar
 */
public class PantallaAmuletos {

    private MundoJuego mundo;
    public int seleccion = 0;        // 0, 1, 2 para tarjetas / 0=aceptar, 1=rechazar para anillo
    private int modo = 0;

    // Animación de entrada
    private int contadorEntrada = 0;
    private static final int DURACION_ENTRADA = 15;

    // Bomba: delay antes de aplicar efecto
    private int contadorBomba = 0;
    private static final int DELAY_BOMBA = 90; // 1.5 segundos
    private boolean bombaAplicada = false;

    // Colores del panel
    private static final Color COLOR_FONDO = new Color(0, 0, 0, 180);
    private static final Color COLOR_PANEL = new Color(25, 20, 50, 230);
    private static final Color COLOR_BORDE = new Color(120, 80, 200);
    private static final Color COLOR_SELECCION = new Color(255, 215, 0);
    private static final Color COLOR_MEJORAR = new Color(100, 255, 100);
    private static final Color COLOR_NUEVO = new Color(100, 180, 255);
    private static final Color COLOR_CARBON = new Color(120, 120, 120);
    private static final Color COLOR_PELIGRO = new Color(255, 50, 50);
    private static final Color COLOR_ANILLO = new Color(180, 50, 255);

    private static final Font FUENTE_TITULO = new Font("Arial", Font.BOLD, 28);
    private static final Font FUENTE_NOMBRE = new Font("Arial", Font.BOLD, 16);
    private static final Font FUENTE_DESC = new Font("Arial", Font.PLAIN, 13);
    private static final Font FUENTE_ETIQUETA = new Font("Arial", Font.BOLD, 12);
    private static final Font FUENTE_BOTON = new Font("Arial", Font.BOLD, 20);
    private static final Font FUENTE_WARN = new Font("Arial", Font.BOLD, 14);

    public PantallaAmuletos(MundoJuego mundo) {
        this.mundo = mundo;
    }

    /**
     * Prepara el panel para un nuevo cofre. Llamado al cambiar a ESTADO_SELECCION_AMULETO.
     */
    public void preparar(int modo) {
        this.modo = modo;
        this.seleccion = 0;
        this.contadorEntrada = 0;
        this.contadorBomba = 0;
        this.bombaAplicada = false;
    }

    /**
     * Actualiza animaciones del panel (llamado cada frame mientras está visible).
     */
    public void actualizar() {
        if (contadorEntrada < DURACION_ENTRADA) {
            contadorEntrada++;
        }

        // Bomba: delay y luego aplicar
        if (modo == 1 && !bombaAplicada) {
            contadorBomba++;
            if (contadorBomba >= DELAY_BOMBA) {
                // Aplicar bomba
                mundo.jugador.gestorAmuletos.equipar(Amuleto.DINAMITA, mundo.jugador);
                bombaAplicada = true;
                // Notificación
                GestorAmuletos ga = mundo.jugador.gestorAmuletos;
                mundo.agregarNotificacion(ga.getNotificacion(Amuleto.DINAMITA, 1), Color.RED, 3);
            }
        }
    }

    /**
     * Dibujar el panel según el modo actual.
     */
    public void dibujar(Graphics2D g2) {
        // Overlay oscuro
        float alphaEntrada = Math.min(1.0f, (float) contadorEntrada / DURACION_ENTRADA);
        g2.setColor(new Color(0, 0, 0, (int)(180 * alphaEntrada)));
        g2.fillRect(0, 0, Configuracion.ANCHO_PANTALLA, Configuracion.ALTO_PANTALLA);

        switch (modo) {
            case 0: dibujarSeleccionNormal(g2); break;
            case 1: dibujarBomba(g2); break;
            case 2: dibujarAnillo(g2); break;
        }
    }

    // ===== MODO 0: SELECCIÓN NORMAL DE 3 OPTIONS =====

    private void dibujarSeleccionNormal(Graphics2D g2) {
        GestorAmuletos ga = mundo.jugador.gestorAmuletos;
        Amuleto[] opciones = ga.opcionesActuales;
        if (opciones == null) return;

        int ancho = Configuracion.ANCHO_PANTALLA;
        int alto = Configuracion.ALTO_PANTALLA;

        // Panel central
        int panelAncho = 650;
        int panelAlto = 380;
        int panelX = (ancho - panelAncho) / 2;
        int panelY = (alto - panelAlto) / 2;

        g2.setColor(COLOR_PANEL);
        g2.fillRoundRect(panelX, panelY, panelAncho, panelAlto, 15, 15);
        g2.setColor(COLOR_BORDE);
        g2.drawRoundRect(panelX, panelY, panelAncho, panelAlto, 15, 15);

        // Título
        g2.setFont(FUENTE_TITULO);
        g2.setColor(COLOR_SELECCION);
        String titulo = ga.getTextoUI("cofre.titulo");
        int tituloX = panelX + (panelAncho - g2.getFontMetrics().stringWidth(titulo)) / 2;
        g2.drawString(titulo, tituloX, panelY + 35);

        // Subtítulo
        g2.setFont(FUENTE_DESC);
        g2.setColor(new Color(200, 200, 220));
        String subtitulo = ga.getTextoUI("cofre.subtitulo");
        int subX = panelX + (panelAncho - g2.getFontMetrics().stringWidth(subtitulo)) / 2;
        g2.drawString(subtitulo, subX, panelY + 55);

        // 3 tarjetas
        int tarjetaAncho = 180;
        int tarjetaAlto = 260;
        int espacio = 20;
        int startX = panelX + (panelAncho - (tarjetaAncho * 3 + espacio * 2)) / 2;
        int tarjetaY = panelY + 70;

        for (int i = 0; i < 3 && i < opciones.length; i++) {
            int tx = startX + i * (tarjetaAncho + espacio);
            dibujarTarjeta(g2, opciones[i], tx, tarjetaY, tarjetaAncho, tarjetaAlto, i == seleccion);
        }

        // Instrucciones
        g2.setFont(FUENTE_DESC);
        g2.setColor(new Color(150, 150, 170));
        String instrucciones = "← → para navegar   |   ENTER para elegir";
        int instrX = panelX + (panelAncho - g2.getFontMetrics().stringWidth(instrucciones)) / 2;
        g2.drawString(instrucciones, instrX, panelY + panelAlto - 10);
    }

    private void dibujarTarjeta(Graphics2D g2, Amuleto tipo, int x, int y, int w, int h, boolean seleccionada) {
        GestorAmuletos ga = mundo.jugador.gestorAmuletos;
        int siguienteNivel = ga.getSiguienteNivel(tipo);
        if (siguienteNivel <= 0 && !tipo.esNegativo()) siguienteNivel = 1;
        if (tipo.esNegativo()) siguienteNivel = 1;

        // Fondo de tarjeta
        Color colorFondo = seleccionada ? new Color(50, 40, 80, 250) : new Color(30, 25, 55, 220);
        g2.setColor(colorFondo);
        g2.fillRoundRect(x, y, w, h, 10, 10);

        // Borde
        Color colorBorde;
        if (seleccionada) {
            colorBorde = COLOR_SELECCION;
        } else if (tipo == Amuleto.CARBON) {
            colorBorde = COLOR_CARBON;
        } else if (ga.tieneAmuleto(tipo)) {
            colorBorde = COLOR_MEJORAR;
        } else {
            colorBorde = COLOR_NUEVO;
        }
        g2.setColor(colorBorde);
        g2.drawRoundRect(x, y, w, h, 10, 10);
        if (seleccionada) {
            g2.drawRoundRect(x + 1, y + 1, w - 2, h - 2, 10, 10);
        }

        // Etiqueta NUEVO / MEJORAR
        g2.setFont(FUENTE_ETIQUETA);
        if (tipo == Amuleto.CARBON) {
            g2.setColor(COLOR_CARBON);
            g2.drawString("???", x + (w - g2.getFontMetrics().stringWidth("???")) / 2, y + 18);
        } else if (ga.tieneAmuleto(tipo)) {
            g2.setColor(COLOR_MEJORAR);
            String etiqueta = ga.getTextoUI("cofre.mejorar") + " ↑";
            g2.drawString(etiqueta, x + (w - g2.getFontMetrics().stringWidth(etiqueta)) / 2, y + 18);
        } else {
            g2.setColor(COLOR_NUEVO);
            String etiqueta = ga.getTextoUI("cofre.nuevo");
            g2.drawString(etiqueta, x + (w - g2.getFontMetrics().stringWidth(etiqueta)) / 2, y + 18);
        }

        // Icono del amuleto (centrado)
        int iconSize = 64;
        BufferedImage icono = ga.getIcono(tipo, siguienteNivel, iconSize);
        if (icono != null) {
            int iconX = x + (w - iconSize) / 2;
            int iconY = y + 30;
            g2.drawImage(icono, iconX, iconY, null);
        }

        // Nombre
        g2.setFont(FUENTE_NOMBRE);
        g2.setColor(Color.WHITE);
        String nombre = ga.getNombre(tipo, siguienteNivel);
        int nombreAncho = g2.getFontMetrics().stringWidth(nombre);
        // Si el nombre es muy largo, reducir fuente
        if (nombreAncho > w - 10) {
            g2.setFont(new Font("Arial", Font.BOLD, 13));
            nombreAncho = g2.getFontMetrics().stringWidth(nombre);
        }
        g2.drawString(nombre, x + (w - nombreAncho) / 2, y + 115);

        // Descripción (con word wrap básico)
        g2.setFont(FUENTE_DESC);
        g2.setColor(new Color(180, 180, 200));
        String desc = ga.getDescripcion(tipo, siguienteNivel, mundo.jugador.tipoPersonaje);
        dibujarTextoWrapped(g2, desc, x + 10, y + 135, w - 20);

        // Nivel actual → siguiente
        if (!tipo.esNegativo() && tipo.nivelMaximo > 1) {
            g2.setFont(FUENTE_ETIQUETA);
            g2.setColor(new Color(160, 160, 180));
            String nivelStr = "Nv." + ga.getNivel(tipo) + " → " + siguienteNivel;
            g2.drawString(nivelStr, x + (w - g2.getFontMetrics().stringWidth(nivelStr)) / 2, y + h - 10);
        }
    }

    // ===== MODO 1: BOMBA =====

    private void dibujarBomba(Graphics2D g2) {
        GestorAmuletos ga = mundo.jugador.gestorAmuletos;
        int ancho = Configuracion.ANCHO_PANTALLA;
        int alto = Configuracion.ALTO_PANTALLA;

        int panelAncho = 400;
        int panelAlto = 300;
        int panelX = (ancho - panelAncho) / 2;
        int panelY = (alto - panelAlto) / 2;

        // Panel con borde rojo
        g2.setColor(new Color(40, 10, 10, 240));
        g2.fillRoundRect(panelX, panelY, panelAncho, panelAlto, 15, 15);
        g2.setColor(COLOR_PELIGRO);
        g2.drawRoundRect(panelX, panelY, panelAncho, panelAlto, 15, 15);

        // Texto de advertencia
        g2.setFont(FUENTE_WARN);
        g2.setColor(COLOR_PELIGRO);
        String warn = ga.getTextoUI("warn.dinamita") != null ? "Un brillo siniestro sale del cofre..." : "";
        int warnX = panelX + (panelAncho - g2.getFontMetrics().stringWidth(warn)) / 2;
        g2.drawString(warn, warnX, panelY + 40);

        // Icono de dinamita grande
        BufferedImage icono = ga.getIcono(Amuleto.DINAMITA, 1, 96);
        if (icono != null) {
            g2.drawImage(icono, panelX + (panelAncho - 96) / 2, panelY + 60, null);
        }

        // Nombre
        g2.setFont(FUENTE_TITULO);
        g2.setColor(Color.WHITE);
        String nombre = ga.getNombre(Amuleto.DINAMITA, 1);
        int nombreX = panelX + (panelAncho - g2.getFontMetrics().stringWidth(nombre)) / 2;
        g2.drawString(nombre, nombreX, panelY + 185);

        // Descripción
        g2.setFont(FUENTE_NOMBRE);
        g2.setColor(COLOR_PELIGRO);
        String desc = ga.getDescripcion(Amuleto.DINAMITA, 1, "");
        int descX = panelX + (panelAncho - g2.getFontMetrics().stringWidth(desc)) / 2;
        g2.drawString(desc, descX, panelY + 210);

        // Barra de progreso de la bomba
        if (!bombaAplicada) {
            int barraAncho = panelAncho - 80;
            int barraAlto = 12;
            int barraX = panelX + 40;
            int barraY = panelY + panelAlto - 50;

            g2.setColor(new Color(80, 0, 0));
            g2.fillRect(barraX, barraY, barraAncho, barraAlto);

            int progreso = (int)((float) contadorBomba / DELAY_BOMBA * barraAncho);
            g2.setColor(COLOR_PELIGRO);
            g2.fillRect(barraX, barraY, progreso, barraAlto);

            g2.setColor(Color.WHITE);
            g2.drawRect(barraX, barraY, barraAncho, barraAlto);
        } else {
            g2.setFont(FUENTE_BOTON);
            g2.setColor(Color.WHITE);
            String continuar = "Presiona ENTER para continuar";
            int contX = panelX + (panelAncho - g2.getFontMetrics().stringWidth(continuar)) / 2;
            g2.drawString(continuar, contX, panelY + panelAlto - 30);
        }
    }

    // ===== MODO 2: ANILLO =====

    private void dibujarAnillo(Graphics2D g2) {
        GestorAmuletos ga = mundo.jugador.gestorAmuletos;
        int ancho = Configuracion.ANCHO_PANTALLA;
        int alto = Configuracion.ALTO_PANTALLA;

        int panelAncho = 450;
        int panelAlto = 400;
        int panelX = (ancho - panelAncho) / 2;
        int panelY = (alto - panelAlto) / 2;

        // Panel con borde púrpura
        g2.setColor(new Color(20, 10, 40, 240));
        g2.fillRoundRect(panelX, panelY, panelAncho, panelAlto, 15, 15);
        g2.setColor(COLOR_ANILLO);
        g2.drawRoundRect(panelX, panelY, panelAncho, panelAlto, 15, 15);

        // Título
        g2.setFont(FUENTE_WARN);
        g2.setColor(COLOR_ANILLO);
        String titulo = ga.getTextoUI("anillo.titulo");
        int tituloX = panelX + (panelAncho - g2.getFontMetrics().stringWidth(titulo)) / 2;
        g2.drawString(titulo, tituloX, panelY + 30);

        // Icono grande
        BufferedImage icono = ga.getIcono(Amuleto.ANILLO, 1, 80);
        if (icono != null) {
            g2.drawImage(icono, panelX + (panelAncho - 80) / 2, panelY + 45, null);
        }

        // Nombre
        g2.setFont(FUENTE_TITULO);
        g2.setColor(Color.WHITE);
        String nombre = ga.getNombre(Amuleto.ANILLO, 1);
        int nombreX = panelX + (panelAncho - g2.getFontMetrics().stringWidth(nombre)) / 2;
        g2.drawString(nombre, nombreX, panelY + 155);

        // Descripción efecto
        g2.setFont(FUENTE_NOMBRE);
        g2.setColor(COLOR_MEJORAR);
        String desc = ga.getDescripcion(Amuleto.ANILLO, 1, "");
        int descX = panelX + (panelAncho - g2.getFontMetrics().stringWidth(desc)) / 2;
        g2.drawString(desc, descX, panelY + 180);

        // Caja de advertencia
        int warnBoxX = panelX + 30;
        int warnBoxY = panelY + 195;
        int warnBoxW = panelAncho - 60;
        int warnBoxH = 80;

        g2.setColor(new Color(80, 0, 0, 180));
        g2.fillRoundRect(warnBoxX, warnBoxY, warnBoxW, warnBoxH, 8, 8);
        g2.setColor(COLOR_PELIGRO);
        g2.drawRoundRect(warnBoxX, warnBoxY, warnBoxW, warnBoxH, 8, 8);

        g2.setFont(FUENTE_WARN);
        g2.setColor(COLOR_PELIGRO);
        g2.drawString("ADVERTENCIA:", warnBoxX + 10, warnBoxY + 20);

        g2.setFont(FUENTE_DESC);
        g2.setColor(new Color(255, 180, 180));
        for (int i = 1; i <= 3; i++) {
            String linea = ga.getAdvertenciaAnillo(i);
            g2.drawString("• " + linea, warnBoxX + 10, warnBoxY + 20 + i * 18);
        }

        // Botones ACEPTAR / RECHAZAR
        int btnAncho = 160;
        int btnAlto = 40;
        int btnY = panelY + panelAlto - 65;
        int btnEspacio = 30;
        int btnAceptarX = panelX + (panelAncho / 2) - btnAncho - btnEspacio / 2;
        int btnRechazarX = panelX + (panelAncho / 2) + btnEspacio / 2;

        // Botón Aceptar
        boolean aceptarSel = seleccion == 0;
        g2.setColor(aceptarSel ? new Color(0, 150, 0, 200) : new Color(30, 60, 30, 180));
        g2.fillRoundRect(btnAceptarX, btnY, btnAncho, btnAlto, 8, 8);
        g2.setColor(aceptarSel ? COLOR_SELECCION : Color.WHITE);
        g2.drawRoundRect(btnAceptarX, btnY, btnAncho, btnAlto, 8, 8);

        g2.setFont(FUENTE_BOTON);
        g2.setColor(aceptarSel ? COLOR_SELECCION : Color.WHITE);
        String textoAceptar = ga.getTextoUI("anillo.aceptar");
        int aceptarTextX = btnAceptarX + (btnAncho - g2.getFontMetrics().stringWidth(textoAceptar)) / 2;
        g2.drawString(textoAceptar, aceptarTextX, btnY + 27);

        // Botón Rechazar
        boolean rechazarSel = seleccion == 1;
        g2.setColor(rechazarSel ? new Color(150, 0, 0, 200) : new Color(60, 30, 30, 180));
        g2.fillRoundRect(btnRechazarX, btnY, btnAncho, btnAlto, 8, 8);
        g2.setColor(rechazarSel ? COLOR_SELECCION : Color.WHITE);
        g2.drawRoundRect(btnRechazarX, btnY, btnAncho, btnAlto, 8, 8);

        g2.setFont(FUENTE_BOTON);
        g2.setColor(rechazarSel ? COLOR_SELECCION : Color.WHITE);
        String textoRechazar = ga.getTextoUI("anillo.rechazar");
        int rechazarTextX = btnRechazarX + (btnAncho - g2.getFontMetrics().stringWidth(textoRechazar)) / 2;
        g2.drawString(textoRechazar, rechazarTextX, btnY + 27);
    }

    // ===== INPUT =====

    /**
     * Mueve la selección (llamado desde GestorEntrada).
     */
    public void moverSeleccion(int direccion) {
        if (modo == 0) {
            // 3 tarjetas
            seleccion += direccion;
            if (seleccion < 0) seleccion = 2;
            if (seleccion > 2) seleccion = 0;
        } else if (modo == 2) {
            // 2 botones (aceptar/rechazar)
            seleccion = (seleccion == 0) ? 1 : 0;
        }
        // Modo 1 (bomba) no tiene selección
    }

    /**
     * Confirma la selección actual. Retorna true si se debe cerrar el panel.
     */
    public boolean confirmar() {
        GestorAmuletos ga = mundo.jugador.gestorAmuletos;

        if (modo == 0) {
            // Selección normal
            if (ga.opcionesActuales == null || seleccion >= ga.opcionesActuales.length) return false;
            Amuleto elegido = ga.opcionesActuales[seleccion];
            int siguienteNivel = ga.getSiguienteNivel(elegido);
            if (siguienteNivel <= 0 && !elegido.esNegativo()) siguienteNivel = 1;

            ga.equipar(elegido, mundo.jugador);

            // Notificación
            String notif = ga.getNotificacion(elegido, siguienteNivel);
            Color colorNotif = elegido == Amuleto.CARBON ? Color.GRAY : Color.CYAN;
            if (notif != null && !notif.isEmpty()) {
                mundo.agregarNotificacion(notif, colorNotif, 3);
            }

            mundo.estadisticas.registrarCofreRecogido();
            return true;
        } else if (modo == 1) {
            // Bomba - solo cerrar si ya se aplicó
            return bombaAplicada;
        } else if (modo == 2) {
            // Anillo
            if (seleccion == 0) {
                // Aceptar
                ga.equipar(Amuleto.ANILLO, mundo.jugador);
                String notif = ga.getNotificacion(Amuleto.ANILLO, 1);
                mundo.agregarNotificacion(notif, COLOR_ANILLO, 5);
            }
            // Rechazar o aceptar, ambos cierran el panel
            mundo.estadisticas.registrarCofreRecogido();
            return true;
        }

        return false;
    }

    public int getModo() { return modo; }

    // ===== UTILIDADES =====

    private void dibujarTextoWrapped(Graphics2D g2, String texto, int x, int y, int maxAncho) {
        if (texto == null || texto.isEmpty()) return;

        String[] palabras = texto.split(" ");
        StringBuilder linea = new StringBuilder();
        int lineaY = y;
        int alturaLinea = g2.getFontMetrics().getHeight();

        for (String palabra : palabras) {
            String prueba = linea.length() > 0 ? linea + " " + palabra : palabra;
            if (g2.getFontMetrics().stringWidth(prueba) > maxAncho && linea.length() > 0) {
                g2.drawString(linea.toString(), x, lineaY);
                linea = new StringBuilder(palabra);
                lineaY += alturaLinea;
            } else {
                if (linea.length() > 0) linea.append(" ");
                linea.append(palabra);
            }
        }
        if (linea.length() > 0) {
            g2.drawString(linea.toString(), x, lineaY);
        }
    }
}
