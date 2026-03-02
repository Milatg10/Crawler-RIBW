import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

public class FichContPalabras {

    // Variable global con los caracteres que separan palabras
    private static final String SEPARADORES = "[ \",.:;(){}!¡°?¿\\t\\n\\r''%/|\\[\\]<=>&#+*$\\-¨^~]+";

    /*
     * Acumula las palabras de un archivo en el heap.
     * @param rutaArchivo La ruta del archivo a procesar.
     * @param diccionario El heap donde se acumulan las palabras.
     */
    public void acumularPalabras(Path rutaArchivo, Map<String, Ocurrencia> diccionario) {
        
        System.out.println("Procesando archivo: " + rutaArchivo.getFileName());
        String nombreArchivo = rutaArchivo.toAbsolutePath().toString(); //Guarda ruta absoluta en el heap

        // Lee el archivo línea a línea y va acumulando las palabras en el heap según los separadores definidos. 
        try (var lineas = Files.lines(rutaArchivo)) {
            lineas
                // Se divide cada línea en palabras usando los separadores y devuelve un Stream<String[]> (cada elemento es un array de palabras de esa línea)
                .map(linea -> linea.split(SEPARADORES))
                // se aplana el Stream<String[]> a Stream<String> para poder procesar cada palabra de forma separada
                .flatMap(Arrays::stream)
                // se filtran las palabras vacías (múltiples separadores seguidos)
                .filter(palabra -> !palabra.isEmpty())
                .map(String::toLowerCase) // Se convierte a minúsculas 
                .forEach(palabra -> {
                    // Para cada palabra, se va actualizando la Ocurrencia en el heap
                    diccionario.computeIfAbsent(palabra, k -> new Ocurrencia())
                               .registrarAparicion(nombreArchivo);
                });
        } catch (IOException e) {
            System.err.println("Error leyendo " + rutaArchivo + ": " + e.getMessage());
        }
    }
}