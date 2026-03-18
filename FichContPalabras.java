import java.util.Arrays;
import java.util.Map;

public class FichContPalabras {

    // Variable global con los caracteres que separan palabras
    private static final String SEPARADORES = "[ \",.:;(){}!¡°?¿\\t\\n\\r''%/|\\[\\]<=>&#+*$\\-¨^~]+";

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