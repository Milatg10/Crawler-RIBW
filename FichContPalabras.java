import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

public class FichContPalabras {

    // Separadores actualizados
    private static final String SEPARADORES = "[ \",.:;(){}!¡°?¿\\t\\n\\r''%/|\\[\\]<=>&#+*$\\-¨^~]+";

    // Recibe el mapa con la nueva clase Ocurrencia
    public void acumularPalabras(Path rutaArchivo, Map<String, Ocurrencia> diccionario) {
        
        System.out.println("[PROCESANDO] Archivo: " + rutaArchivo.getFileName());
        String nombreArchivo = rutaArchivo.getFileName().toString(); // Usamos solo el nombre (o getAbsolutePath si prefieres rutas completas)

        try (var lineas = Files.lines(rutaArchivo, StandardCharsets.UTF_8)) {
            lineas
                .map(linea -> linea.split(SEPARADORES))
                .flatMap(Arrays::stream)
                .filter(palabra -> !palabra.isEmpty())
                .map(String::toLowerCase)
                .forEach(palabra -> {
                    // Si la palabra no está, crea una nueva Ocurrencia. Luego registra el archivo.
                    diccionario.computeIfAbsent(palabra, k -> new Ocurrencia())
                               .registrarAparicion(nombreArchivo);
                });
        } catch (IOException e) {
            System.err.println("ERROR leyendo " + rutaArchivo + ": " + e.getMessage());
        }
    }
}