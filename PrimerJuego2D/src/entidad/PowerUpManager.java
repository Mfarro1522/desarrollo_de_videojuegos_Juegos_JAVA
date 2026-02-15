package entidad;

/**
 * Gestiona los power-ups temporales del jugador.
 */
public class PowerUpManager {

    public boolean invencibilidadActiva = false;
    public boolean velocidadAumentada = false;
    public boolean ataqueAumentado = false;

    private int duracionInvencibilidad = 0;
    private int duracionVelocidad = 0;
    private int duracionAtaque = 0;

    public double multiplicadorVelocidad = 1.0;
    public double multiplicadorAtaque = 1.0;

    public void activarInvencibilidad(int segundos) {
        invencibilidadActiva = true;
        duracionInvencibilidad = segundos * 60;
    }

    public void aumentarVelocidad(double porcentaje, int segundos) {
        velocidadAumentada = true;
        multiplicadorVelocidad = 1.0 + (porcentaje / 100.0);
        duracionVelocidad = segundos * 60;
    }

    public void aumentarAtaque(double porcentaje, int segundos) {
        ataqueAumentado = true;
        multiplicadorAtaque = 1.0 + (porcentaje / 100.0);
        duracionAtaque = segundos * 60;
    }

    public void actualizar() {
        if (invencibilidadActiva) {
            duracionInvencibilidad--;
            if (duracionInvencibilidad <= 0) {
                invencibilidadActiva = false;
            }
        }
        if (velocidadAumentada) {
            duracionVelocidad--;
            if (duracionVelocidad <= 0) {
                velocidadAumentada = false;
                multiplicadorVelocidad = 1.0;
            }
        }
        if (ataqueAumentado) {
            duracionAtaque--;
            if (duracionAtaque <= 0) {
                ataqueAumentado = false;
                multiplicadorAtaque = 1.0;
            }
        }
    }

    public int getTiempoInvencibilidad() {
        return duracionInvencibilidad / 60;
    }

    public int getTiempoVelocidad() {
        return duracionVelocidad / 60;
    }

    public int getTiempoAtaque() {
        return duracionAtaque / 60;
    }
}
