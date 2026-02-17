package items;

/**
 * Enum que define todos los tipos de amuletos/items del juego.
 * Cada tipo tiene: nivel máximo, rutas de sprites, pesos para el pool de aparición.
 */
public enum Amuleto {

    ESPADA(3, new String[]{
        "/objetos/Amuletos/EspadaMagicaMadera.png",
        "/objetos/Amuletos/EspadaMagicaHierro.png",
        "/objetos/Amuletos/EspadaMagicaOro.png"
    }, 18, null),

    ARMADURA(2, new String[]{
        "/objetos/Amuletos/armaduraBronce.png",
        "/objetos/Amuletos/armaduraHierro.png"
    }, 20, null),

    BEBIDA(2, new String[]{
        "/objetos/Amuletos/BebidaEnergia.png",
        "/objetos/Amuletos/BebidaEnergia2.png"
    }, 20, null),

    LIBRO(2, new String[]{
        "/objetos/Amuletos/LibroMagico.png",
        "/objetos/Amuletos/LibroMagico2.png"
    }, 18, new String[]{"Mago", "Sideral"}),

    GEMA(1, new String[]{
        "/objetos/Amuletos/GemaDeRegeneracion.png"
    }, 15, null),

    ANILLO(1, new String[]{
        "/objetos/Amuletos/AnilloDeRegeneracion.png"
    }, 3, null),

    CARBON(1, new String[]{
        "/objetos/Amuletos/carbon.png"
    }, 25, null),

    DINAMITA(1, new String[]{
        "/objetos/Amuletos/Dinamita.png"
    }, 0, null); // peso 0: la dinamita no entra al pool normal

    public final int nivelMaximo;
    public final String[] spritePaths;
    public final int pesoBase;
    public final String[] personajesPermitidos; // null = todos

    Amuleto(int nivelMaximo, String[] spritePaths, int pesoBase, String[] personajesPermitidos) {
        this.nivelMaximo = nivelMaximo;
        this.spritePaths = spritePaths;
        this.pesoBase = pesoBase;
        this.personajesPermitidos = personajesPermitidos;
    }

    /**
     * Retorna la ruta del sprite para un nivel dado (1-based).
     */
    public String getSpritePath(int nivel) {
        if (nivel < 1 || nivel > spritePaths.length) return spritePaths[0];
        return spritePaths[nivel - 1];
    }

    /**
     * Retorna true si este item es negativo (no otorga beneficios).
     */
    public boolean esNegativo() {
        return this == CARBON || this == DINAMITA;
    }

    /**
     * Retorna true si este item es especial (no aparece en selección normal de 3).
     */
    public boolean esEspecial() {
        return this == ANILLO || this == DINAMITA;
    }

    /**
     * Retorna true si este item puede ser usado por el personaje dado.
     */
    public boolean esCompatible(String tipoPersonaje) {
        if (personajesPermitidos == null) return true;
        for (String p : personajesPermitidos) {
            if (p.equals(tipoPersonaje)) return true;
        }
        return false;
    }

    /**
     * Clave base para el archivo .properties.
     */
    public String getClaveProperties() {
        switch (this) {
            case ESPADA: return "espada";
            case ARMADURA: return "armadura";
            case BEBIDA: return "bebida";
            case LIBRO: return "libro";
            case GEMA: return "gema";
            case ANILLO: return "anillo";
            case CARBON: return "carbon";
            case DINAMITA: return "dinamita";
            default: return name().toLowerCase();
        }
    }
}
