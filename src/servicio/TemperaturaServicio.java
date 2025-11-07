package servicio;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import entidad.CambioTemperatura;

public class TemperaturaServicio {

    public static Map<String, Double> calcularPromedioPorCiudadEnRango(LocalDate desde, LocalDate hasta) {
        String nombreArchivo = "src/datos/CambiosTemperaturas.csv";
        List<CambioTemperatura> registros = getDatos(nombreArchivo);
        return promedioPorCiudad(registros, desde, hasta);
    }

    // Leer el CSV con los datos de temperatura
    public static List<CambioTemperatura> getDatos(String nombreArchivo) {
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("d/M/yyyy");
        try (Stream<String> lineas = Files.lines(Paths.get(nombreArchivo))) {

            return lineas.skip(1)
                    .map(linea -> linea.split(","))
                    .map(textos -> new CambioTemperatura(
                            LocalDate.parse(textos[1], formatoFecha), // fecha
                            textos[0], // ciudad
                            Double.parseDouble(textos[2]) // temperatura
                    ))
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            System.out.println("Error al leer el archivo: " + ex.getMessage());
            return Collections.emptyList();
        }
    }

    // Obtener lista de ciudades únicas
    public static List<String> getCiudades(List<CambioTemperatura> registros) {
        return registros.stream()
                .map(CambioTemperatura::getCiudad)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // Filtrar registros por ciudad y rango de fechas
    public static List<CambioTemperatura> filtrar(List<CambioTemperatura> registros,
            String ciudad, LocalDate desde, LocalDate hasta) {
        return registros.stream()
                .filter(r -> r.getCiudad().equals(ciudad)
                        && !r.getFecha().isBefore(desde)
                        && !r.getFecha().isAfter(hasta))
                .collect(Collectors.toList());
    }

    // Extraer solo temperaturas
    public static List<Double> extraerTemperaturas(List<CambioTemperatura> registros) {
        return registros.stream()
                .map(CambioTemperatura::getTemperatura)
                .collect(Collectors.toList());
    }

    // Promedio de una ciudad
    public static double getPromedio(List<Double> datos) {
        return datos.isEmpty() ? 0
                : datos.stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0);
    }

    // Máximo
    public static double getMaximo(List<Double> datos) {
        return datos.isEmpty() ? 0
                : datos.stream()
                        .mapToDouble(Double::doubleValue)
                        .max()
                        .orElse(0);
    }

    // Mínimo
    public static double getMinimo(List<Double> datos) {
        return datos.isEmpty() ? 0
                : datos.stream()
                        .mapToDouble(Double::doubleValue)
                        .min()
                        .orElse(0);
    }

    // Obtener estadísticas (promedio, máximo, mínimo)
    public static Map<String, Double> getEstadisticas(List<CambioTemperatura> registros,
            String ciudad, LocalDate desde, LocalDate hasta) {

        var registrosFiltrados = filtrar(registros, ciudad, desde, hasta);
        var temperaturas = extraerTemperaturas(registrosFiltrados);

        Map<String, Double> estadisticas = new LinkedHashMap<>();
        estadisticas.put("Promedio", getPromedio(temperaturas));
        estadisticas.put("Máximo", getMaximo(temperaturas));
        estadisticas.put("Mínimo", getMinimo(temperaturas));

        return estadisticas;
    }

    // Promedio de todas las ciudades en un rango de fechas
    public static Map<String, Double> promedioPorCiudad(List<CambioTemperatura> registros,
            LocalDate desde, LocalDate hasta) {

        List<String> ciudades = getCiudades(registros);

        return ciudades.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        ciudad -> getPromedio(
                                extraerTemperaturas(filtrar(registros, ciudad, desde, hasta)))));
    }

    // lógica para ciudad más y menos calurosa
    public static String analizarExtremosPorFecha(LocalDate fecha) {

        String nombreArchivo = "src/datos/CambiosTemperaturas.csv";
        List<CambioTemperatura> registros = getDatos(nombreArchivo);

        // Filtrar por la fecha indicada
        List<CambioTemperatura> registrosFecha = registros.stream()
                .filter(r -> r.getFecha().equals(fecha))
                .collect(Collectors.toList());

        if (registrosFecha.isEmpty()) {
            return "No se encontraron datos para la fecha: " + fecha;
        }

        // Ciudad más calurosa
        CambioTemperatura max = registrosFecha.stream()
                .max(Comparator.comparingDouble(CambioTemperatura::getTemperatura))
                .orElse(null);

        // Ciudad menos calurosa
        CambioTemperatura min = registrosFecha.stream()
                .min(Comparator.comparingDouble(CambioTemperatura::getTemperatura))
                .orElse(null);

        return String.format(
                "Fecha: %s\n\nCiudad más calurosa: %s (%.1f °C)\nCiudad menos calurosa: %s (%.1f °C)",
                fecha, max.getCiudad(), max.getTemperatura(),
                min.getCiudad(), min.getTemperatura());
    }
}
