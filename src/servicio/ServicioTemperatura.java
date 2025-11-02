package servicio;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ServicioTemperatura {
    public static Map<String, Double> calcularPromedioPorCiudadEnRango(LocalDate desde, LocalDate hasta) {
        Map<String, Double> promedios = new HashMap<>();
        return promedios;
    }

    public static String analizarExtremosPorFecha(LocalDate fecha) {
        return String.format(
            fecha.toString(), fecha.toString());
    }
}