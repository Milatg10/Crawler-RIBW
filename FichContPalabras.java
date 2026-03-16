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
     * @param thesauro El gestor del thesauro para validar palabras.
     */
    public void acumularPalabras(Path rutaArchivo, Integer idArchivo, Map<String, Ocurrencia> diccionario, ThesauroGestion thesauro) {
        
        System.out.println("Procesando archivo: " + rutaArchivo.getFileName());
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
                .filter(palabra -> thesauro.contiene(palabra)) // Se meten solo las palabras que existen en el thesauro
                .forEach(palabra -> {
                    // Para cada palabra, se va actualizando la Ocurrencia en el heap
                    diccionario.computeIfAbsent(palabra, k -> new Ocurrencia())
                               .registrarAparicion(idArchivo);
                });
        } catch (IOException e) {
            System.err.println("Error leyendo " + rutaArchivo + ": " + e.getMessage());
        }
    }

    /*
     * Acumula las palabras extraídas por Apache Tika en forma de String directo.
     */
    public void acumularPalabrasTika(String textoExtraido, String nombreArchivo, Integer idArchivo, Map<String, Ocurrencia> diccionario, ThesauroGestion thesauro) {
        
        System.out.println("Procesando con Tika: " + nombreArchivo);
        
        // Troceamos el mega String que nos ha devuelto Tika usando tus separadores
        String[] palabrasArray = textoExtraido.split(SEPARADORES);
        
        // Lo convertimos a Stream para aprovechar la misma lógica fluida que ya tenías
        Arrays.stream(palabrasArray)
              .filter(palabra -> !palabra.isEmpty())
              .map(String::toLowerCase)
              .filter(palabra -> thesauro.contiene(palabra)) 
              .forEach(palabra -> {
                  diccionario.computeIfAbsent(palabra, k -> new Ocurrencia())
                             .registrarAparicion(idArchivo);
              });
    }
}