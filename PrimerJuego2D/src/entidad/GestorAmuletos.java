package entidad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import configuracion.Configuracion;
import mundo.MundoJuego;
import utilidades.Herramientas;

/**
 * Gestiona los amuletos/items equipados del jugador.
 * 
 * Responsabilidades:
 * - Equipar/mejorar amuletos con progresión obligatoria
 * - Generar opciones de cofre (pool ponderado)
 * - Aplicar efectos permanentes (armadura, velocidad)
 * - Detectar sinergias entre items
 * - Manejar el Anillo de Regeneración (destruye todo)
 */
public class GestorAmuletos {

    // Items equipados: tipo → nivel actual (1-based)
    private HashMap<Amuleto, Integer> itemsEquipados = new HashMap<>();

    // Items destruidos por el Anillo (no pueden volver a aparecer)
    private HashSet<Amuleto> itemsDestruidos = new HashSet<>();

    // Flags especiales
    public boolean anilloEquipado = false;
    private int contadorRegeneracion = 0;

    // Contador de veces que se eligió carbón (para easter egg futuro)
    public int vecesCarbon = 0;

    // Caché de iconos cargados
    private HashMap<String, BufferedImage> cacheIconos = new HashMap<>();
    private Herramientas herramientas = new Herramientas();

    // Textos del properties
    private Properties textos;

    // Resultado de la última generación de opciones
    public Amuleto[] opcionesActuales = null;
    public int modoPanel = 0; // 0=normal(3 opciones), 1=bomba, 2=anillo

    public GestorAmuletos() {
        cargarTextos();
    }

    private void cargarTextos() {
        textos = new Properties();
        try {
            textos.load(getClass().getResourceAsStream("/items/items.properties"));
        } catch (Exception e) {
            System.err.println("[GestorAmuletos] Error cargando items.properties: " + e.getMessage());
            textos = new Properties();
        }
    }

    // ===== CONSULTAS =====

    public boolean tieneAmuleto(Amuleto tipo) {
        return itemsEquipados.containsKey(tipo);
    }

    public int getNivel(Amuleto tipo) {
        return itemsEquipados.getOrDefault(tipo, 0);
    }

    /**
     * Retorna el siguiente nivel que se obtendría al equipar este amuleto.
     * 0 = no se puede obtener (ya tiene máximo o está destruido).
     */
    public int getSiguienteNivel(Amuleto tipo) {
        if (itemsDestruidos.contains(tipo)) return 0;
        if (anilloEquipado && tipo != Amuleto.ANILLO) return 0;
        int nivelActual = getNivel(tipo);
        if (nivelActual >= tipo.nivelMaximo) return 0;
        return nivelActual + 1;
    }

    /**
     * Retorna true si este amuleto puede aparecer en el pool.
     */
    public boolean puedeAparecer(Amuleto tipo, String tipoPersonaje) {
        // El anillo y la dinamita no van en el pool normal
        if (tipo.esEspecial()) return false;
        // Items destruidos no reaparecen
        if (itemsDestruidos.contains(tipo)) return false;
        // Si tiene anillo, nada más puede obtenerse
        if (anilloEquipado) return false;
        // Restricción de personaje
        if (!tipo.esCompatible(tipoPersonaje)) return false;
        // Ya tiene el nivel máximo
        if (getNivel(tipo) >= tipo.nivelMaximo) return false;
        // Items de nivel 2+ necesitan tener el nivel anterior
        // EXCEPTO nivel 1 que siempre puede aparecer
        int siguiente = getSiguienteNivel(tipo);
        if (siguiente <= 0) return false;
        // Para nivel 2+, debe tener el nivel anterior
        if (siguiente > 1 && getNivel(tipo) != siguiente - 1) return false;
        return true;
    }

    // ===== EQUIPAR =====

    /**
     * Equipa o mejora un amuleto. Retorna true si se equipó correctamente.
     */
    public boolean equipar(Amuleto tipo, Jugador jugador) {
        if (tipo == Amuleto.CARBON) {
            vecesCarbon++;
            return true; // No hace nada
        }

        if (tipo == Amuleto.DINAMITA) {
            // Reduce vida a la mitad (mínimo 1)
            jugador.vidaActual = Math.max(1, jugador.vidaActual / 2);
            jugador.mundo.playSE(audio.GestorAudio.SE_EXPLOSION);
            return true;
        }

        if (tipo == Amuleto.ANILLO) {
            return equiparAnillo(jugador);
        }

        int siguiente = getSiguienteNivel(tipo);
        if (siguiente <= 0) return false;

        itemsEquipados.put(tipo, siguiente);
        aplicarEfectoInmediato(tipo, siguiente, jugador);
        jugador.mundo.playSE(audio.GestorAudio.SE_AMULETO_EQUIPAR);
        return true;
    }

    private boolean equiparAnillo(Jugador jugador) {
        // Destruir todos los items existentes
        for (Amuleto item : itemsEquipados.keySet()) {
            itemsDestruidos.add(item);
        }

        // Revertir efectos de items destruidos
        revertirTodosLosEfectos(jugador);

        itemsEquipados.clear();
        itemsEquipados.put(Amuleto.ANILLO, 1);
        anilloEquipado = true;
        contadorRegeneracion = 0;

        // Desactivar espada orbital si existe
        if (jugador.mundo.espadaOrbital != null) {
            jugador.mundo.espadaOrbital.desactivar();
        }
        // Desactivar marcas de suelo
        if (jugador.mundo.marcasSuelo != null) {
            jugador.mundo.marcasSuelo.desactivarTodas();
        }

        return true;
    }

    private void revertirTodosLosEfectos(Jugador jugador) {
        // Revertir armadura
        if (tieneAmuleto(Amuleto.ARMADURA)) {
            int nivel = getNivel(Amuleto.ARMADURA);
            jugador.defensa -= (nivel == 1) ? 3 : 6;
        }
        // Revertir gema
        if (tieneAmuleto(Amuleto.GEMA)) {
            int bonus = (int)(jugador.vidaMaxima * 0.10 / 1.10); // Deshacer +10%
            jugador.vidaMaxima -= bonus;
            jugador.vidaActual = Math.min(jugador.vidaActual, jugador.vidaMaxima);
        }
        // Velocidad se recalcula en Jugador.update() basándose en getMultiplicadorVelocidad()
    }

    /**
     * Aplica el efecto inmediato al equipar/mejorar un item.
     */
    private void aplicarEfectoInmediato(Amuleto tipo, int nivel, Jugador jugador) {
        switch (tipo) {
            case ARMADURA:
                // Nivel 1: +3 def, Nivel 2: +3 más (total +6)
                jugador.defensa += 3;
                break;

            case GEMA:
                // +10% vida máxima
                int bonus = (int)(jugador.vidaMaxima * 0.10);
                jugador.vidaMaxima += bonus;
                // NO cura vida actual
                break;

            case ESPADA:
                // Activar/actualizar espada orbital
                if (jugador.mundo.espadaOrbital == null) {
                    jugador.mundo.espadaOrbital = new EspadaOrbital(jugador.mundo);
                }
                jugador.mundo.espadaOrbital.setNivel(nivel);
                break;

            case LIBRO:
                // Activar marcas de suelo para Sideral
                if (jugador.tipoPersonaje.equals("Sideral")) {
                    if (jugador.mundo.marcasSuelo == null) {
                        jugador.mundo.marcasSuelo = new MarcaSuelo(jugador.mundo);
                    }
                    jugador.mundo.marcasSuelo.setNivel(nivel);
                }
                // Para Mago, el efecto se aplica en Jugador.dispararProyectil()
                break;

            case BEBIDA:
                // El efecto se aplica en getMultiplicadorVelocidad()
                break;

            default:
                break;
        }
    }

    // ===== ACTUALIZAR (cada frame) =====

    /**
     * Actualizar efectos tick-based. Llamado cada frame.
     */
    public void actualizar(Jugador jugador) {
        // Regeneración del Anillo: 1HP cada 2 segundos (120 frames)
        if (anilloEquipado) {
            contadorRegeneracion++;
            if (contadorRegeneracion >= 120) {
                if (jugador.vidaActual < jugador.vidaMaxima) {
                    jugador.vidaActual = Math.min(jugador.vidaActual + 1, jugador.vidaMaxima);
                }
                contadorRegeneracion = 0;
            }
        }

        // Sinergia Vampirismo (Libro + Gema): se maneja al matar enemigos
    }

    // ===== MULTIPLICADORES =====

    /**
     * Multiplicador de velocidad basado en items equipados.
     */
    public double getMultiplicadorVelocidad() {
        if (!tieneAmuleto(Amuleto.BEBIDA)) return 1.0;
        int nivel = getNivel(Amuleto.BEBIDA);
        return (nivel == 1) ? 1.25 : 1.50;
    }

    /**
     * Bonus de velocidad para proyectiles (sinergia Libro + Bebida para Mago).
     */
    public double getMultiplicadorVelocidadProyectil() {
        if (tieneSinergia("libro_bebida") && tieneAmuleto(Amuleto.LIBRO)) {
            return 1.30;
        }
        return 1.0;
    }

    /**
     * Cantidad de proyectiles del Mago según nivel de Libro.
     */
    public int getProyectilesMago() {
        if (!tieneAmuleto(Amuleto.LIBRO)) return 1;
        return (getNivel(Amuleto.LIBRO) == 1) ? 2 : 3;
    }

    /**
     * Bonus de radio orbital por sinergia Espada + Bebida.
     */
    public double getBonusRadioOrbital() {
        if (tieneSinergia("espada_bebida")) return 1.20;
        return 1.0;
    }

    // ===== SINERGIAS =====

    public boolean tieneSinergia(String nombre) {
        if (anilloEquipado) return false; // Anillo anula sinergias
        switch (nombre) {
            case "espada_armadura":
                return tieneAmuleto(Amuleto.ESPADA) && tieneAmuleto(Amuleto.ARMADURA);
            case "espada_bebida":
                return tieneAmuleto(Amuleto.ESPADA) && tieneAmuleto(Amuleto.BEBIDA);
            case "libro_gema":
                return tieneAmuleto(Amuleto.LIBRO) && tieneAmuleto(Amuleto.GEMA);
            case "armadura_gema":
                return tieneAmuleto(Amuleto.ARMADURA) && tieneAmuleto(Amuleto.GEMA);
            case "libro_bebida":
                return tieneAmuleto(Amuleto.LIBRO) && tieneAmuleto(Amuleto.BEBIDA);
            case "libro_espada":
                return tieneAmuleto(Amuleto.LIBRO) && tieneAmuleto(Amuleto.ESPADA);
            default:
                return false;
        }
    }

    /**
     * Bonus de vida máxima por sinergia Armadura + Gema (+5% extra).
     * Se aplica una sola vez al detectar nueva sinergia.
     */
    public int getBonusVidaSinergia() {
        if (tieneSinergia("armadura_gema")) return 5; // +5%
        return 0;
    }

    /**
     * Chance de vampirismo (Libro + Gema): 5% de chance de +1HP al matar.
     */
    public boolean rollVampirismo() {
        if (!tieneSinergia("libro_gema")) return false;
        return Math.random() < 0.05;
    }

    // ===== GENERACIÓN DE OPCIONES =====

    /**
     * Genera las opciones para un cofre. Determina modo (normal, bomba, anillo).
     * Retorna el modo: 0=normal, 1=bomba, 2=anillo
     */
    public int generarOpcionesCofre(String tipoPersonaje) {
        // Si tiene anillo, no hay opciones
        if (anilloEquipado) {
            modoPanel = -1;
            opcionesActuales = null;
            return -1;
        }

        // 8% chance de bomba
        if (Math.random() < 0.08) {
            modoPanel = 1;
            opcionesActuales = new Amuleto[]{Amuleto.DINAMITA};
            return 1;
        }

        // 3% chance de evento Anillo (si no lo tiene ya destruido)
        if (!itemsDestruidos.contains(Amuleto.ANILLO) && Math.random() < 0.03) {
            modoPanel = 2;
            opcionesActuales = new Amuleto[]{Amuleto.ANILLO};
            return 2;
        }

        // Construir pool ponderado
        ArrayList<Amuleto> pool = new ArrayList<>();
        ArrayList<Integer> pesos = new ArrayList<>();

        for (Amuleto tipo : Amuleto.values()) {
            if (tipo == Amuleto.CARBON) {
                // Carbón siempre está disponible
                pool.add(tipo);
                pesos.add(tipo.pesoBase);
                continue;
            }
            if (puedeAparecer(tipo, tipoPersonaje)) {
                pool.add(tipo);
                // Items de nivel superior tienen peso ligeramente menor
                int siguiente = getSiguienteNivel(tipo);
                int peso = tipo.pesoBase;
                if (siguiente > 1) peso = (int)(peso * 0.75);
                pesos.add(peso);
            }
        }

        // Si el pool tiene menos de 3, rellenar con carbón
        while (pool.size() < 3) {
            pool.add(Amuleto.CARBON);
            pesos.add(Amuleto.CARBON.pesoBase);
        }

        // Seleccionar 3 sin repetir (excepto carbón puede repetir)
        opcionesActuales = new Amuleto[3];
        for (int i = 0; i < 3; i++) {
            int totalPeso = 0;
            for (int p : pesos) totalPeso += p;
            if (totalPeso <= 0) {
                opcionesActuales[i] = Amuleto.CARBON;
                continue;
            }

            int roll = (int)(Math.random() * totalPeso);
            int acumulado = 0;
            int elegido = 0;
            for (int j = 0; j < pool.size(); j++) {
                acumulado += pesos.get(j);
                if (roll < acumulado) {
                    elegido = j;
                    break;
                }
            }

            opcionesActuales[i] = pool.get(elegido);

            // Remover del pool (excepto carbón)
            if (opcionesActuales[i] != Amuleto.CARBON) {
                pool.remove(elegido);
                pesos.remove(elegido);
            }
        }

        modoPanel = 0;
        return 0;
    }

    // ===== ICONOS =====

    /**
     * Obtiene el icono de un amuleto escalado. Cachea para reusar.
     */
    public BufferedImage getIcono(Amuleto tipo, int nivel, int tamanio) {
        String clave = tipo.name() + "_" + nivel + "_" + tamanio;
        if (cacheIconos.containsKey(clave)) {
            return cacheIconos.get(clave);
        }

        try {
            String path = tipo.getSpritePath(nivel);
            BufferedImage original = javax.imageio.ImageIO.read(getClass().getResourceAsStream(path));
            BufferedImage escalada = herramientas.escalarImagen(original, tamanio, tamanio);
            cacheIconos.put(clave, escalada);
            return escalada;
        } catch (Exception e) {
            // Fallback: si no existe el sprite (ej: EspadaMagicaOro.png)
            // intentar tintear el sprite anterior
            if (nivel > 1) {
                BufferedImage anterior = getIcono(tipo, nivel - 1, tamanio);
                if (anterior != null) {
                    BufferedImage tinted = herramientas.tintImage(anterior, new Color(255, 215, 0, 80));
                    cacheIconos.put(clave, tinted);
                    return tinted;
                }
            }
            System.err.println("[GestorAmuletos] No se pudo cargar icono: " + tipo.getSpritePath(nivel));
            return null;
        }
    }

    // ===== TEXTOS =====

    public String getNombre(Amuleto tipo, int nivel) {
        return textos.getProperty("nombre." + tipo.getClaveProperties() + "." + nivel,
                tipo.name());
    }

    public String getDescripcion(Amuleto tipo, int nivel, String tipoPersonaje) {
        // Primero buscar versión específica por personaje
        String claveEspecifica = "desc." + tipo.getClaveProperties() + "." + nivel + "." + tipoPersonaje.toLowerCase();
        String desc = textos.getProperty(claveEspecifica);
        if (desc != null) return desc;

        // Luego versión genérica
        String claveGenerica = "desc." + tipo.getClaveProperties() + "." + nivel;
        return textos.getProperty(claveGenerica, "");
    }

    public String getNotificacion(Amuleto tipo, int nivel) {
        return textos.getProperty("notif." + tipo.getClaveProperties() + "." + nivel, "");
    }

    public String getTextoUI(String clave) {
        return textos.getProperty("ui." + clave, clave);
    }

    public String getAdvertenciaAnillo(int linea) {
        return textos.getProperty("warn.anillo." + linea, "");
    }

    public String getTextoSinergia(String nombre) {
        return textos.getProperty("sinergia." + nombre, "");
    }

    // ===== ITEMS EQUIPADOS (para HUD) =====

    /**
     * Retorna lista de items equipados con sus niveles (para dibujar en HUD).
     */
    public ArrayList<int[]> getItemsEquipadosParaHUD() {
        ArrayList<int[]> lista = new ArrayList<>();
        for (HashMap.Entry<Amuleto, Integer> entry : itemsEquipados.entrySet()) {
            if (!entry.getKey().esNegativo()) {
                lista.add(new int[]{entry.getKey().ordinal(), entry.getValue()});
            }
        }
        return lista;
    }

    /**
     * Retorna la cantidad de items equipados (no negativos).
     */
    public int getCantidadEquipados() {
        int count = 0;
        for (Amuleto a : itemsEquipados.keySet()) {
            if (!a.esNegativo()) count++;
        }
        return count;
    }

    /**
     * Resetea todo el estado del gestor (para nueva partida).
     */
    public void reset() {
        itemsEquipados.clear();
        itemsDestruidos.clear();
        anilloEquipado = false;
        contadorRegeneracion = 0;
        vecesCarbon = 0;
        opcionesActuales = null;
        modoPanel = 0;
    }
}
