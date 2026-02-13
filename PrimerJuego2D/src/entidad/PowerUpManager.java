package entidad;

/**
 * Gestiona los power-ups temporales del jugador
 */
public class PowerUpManager {
    
    // Buffs activos
    public boolean invencibilidadActiva = false;
    public boolean velocidadAumentada = false;
    public boolean ataqueAumentado = false;
    
    // Duración de buffs (en frames a 60 FPS)
    private int duracionInvencibilidad = 0;
    private int duracionVelocidad = 0;
    private int duracionAtaque = 0;
    
    // Multiplicadores
    public double multiplicadorVelocidad = 1.0;
    public double multiplicadorAtaque = 1.0;
    
    /**
     * Activa invencibilidad temporal
     * @param segundos Duración en segundos
     */
    public void activarInvencibilidad(int segundos) {
        invencibilidadActiva = true;
        duracionInvencibilidad = segundos * 60; // Convertir a frames
    }
    
    /**
     * Aumenta la velocidad temporalmente
     * @param porcentaje Porcentaje de aumento (ej: 50 = 50% más rápido)
     * @param segundos Duración en segundos
     */
    public void aumentarVelocidad(double porcentaje, int segundos) {
        velocidadAumentada = true;
        multiplicadorVelocidad = 1.0 + (porcentaje / 100.0);
        duracionVelocidad = segundos * 60;
    }
    
    /**
     * Aumenta el ataque temporalmente
     * @param porcentaje Porcentaje de aumento (ej: 30 = 30% más daño)
     * @param segundos Duración en segundos
     */
    public void aumentarAtaque(double porcentaje, int segundos) {
        ataqueAumentado = true;
        multiplicadorAtaque = 1.0 + (porcentaje / 100.0);
        duracionAtaque = segundos * 60;
    }
    
    /**
     * Actualiza los buffs activos (llamar cada frame)
     */
    public void actualizar() {
        // Invencibilidad
        if (invencibilidadActiva) {
            duracionInvencibilidad--;
            if (duracionInvencibilidad <= 0) {
                invencibilidadActiva = false;
            }
        }
        
        // Velocidad
        if (velocidadAumentada) {
            duracionVelocidad--;
            if (duracionVelocidad <= 0) {
                velocidadAumentada = false;
                multiplicadorVelocidad = 1.0;
            }
        }
        
        // Ataque
        if (ataqueAumentado) {
            duracionAtaque--;
            if (duracionAtaque <= 0) {
                ataqueAumentado = false;
                multiplicadorAtaque = 1.0;
            }
        }
    }
    
    /**
     * Obtiene el tiempo restante de invencibilidad
     */
    public int getTiempoInvencibilidad() {
        return duracionInvencibilidad / 60;
    }
    
    /**
     * Obtiene el tiempo restante de velocidad aumentada
     */
    public int getTiempoVelocidad() {
        return duracionVelocidad / 60;
    }
    
    /**
     * Obtiene el tiempo restante de ataque aumentado
     */
    public int getTiempoAtaque() {
        return duracionAtaque / 60;
    }
}
