package entidad;
import java.time.LocalDate;

public class CambioTemperatura {
    private LocalDate fecha;
    private String ciudad;
    private double temperatura;

    public CambioTemperatura(LocalDate fecha, String ciudad, double temperatura) {
        this.fecha = fecha;
        this.ciudad = ciudad;
        this.temperatura = temperatura;
    }

    public LocalDate getFecha() { return fecha; }
    public String getCiudad() { return ciudad; }
    public double getTemperatura() { return temperatura; }
}