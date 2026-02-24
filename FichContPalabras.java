import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

public class FichContPalabras {

    // defino aqui todos los simbolos raros para separar palabras
    // he tenido que escapar la comilla doble porque si no daba problemas
    private static final String SEPARADORES = "[ \",.:;(){}!¡°?¿\\t\\n\\r''%/|\\[\\]<=>&#+*$\\-¨^~]+";

    public void acumularPalabras(Path rutaArchivo, Map<String, Long> mapaGlobal) {
        
        System.out.println("[PROCESANDO] Archivo: " + rutaArchivo.getFileName());

        try (var lineas = Files.lines(rutaArchivo, StandardCharsets.UTF_8)) {
            lineas
                .map(linea -> linea.split(SEPARADORES)) // parto la linea
                .flatMap(Arrays::stream)                // convierto el array en stream para procesarlo mejor
                .filter(palabra -> !palabra.isEmpty())  // quito los huecos vacios
                
                // paso todo a minusculas para que cuente igual Hola y hola
                .map(String::toLowerCase)               
                
                .forEach(palabra -> {
                    // voy sumando al mapa que me pasan. si ya esta sumo 1, si no la creo
                    mapaGlobal.merge(palabra, 1L, Long::sum);
                });
        } catch (IOException e) {
            System.err.println("ERROR leyendo " + rutaArchivo + ": " + e.getMessage());
        }
    }
}