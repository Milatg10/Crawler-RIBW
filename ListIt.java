import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Queue;
import java.util.stream.Stream;
import java.util.TreeMap;

public class ListIt {

    private FichContPalabras contador = new FichContPalabras();

    // este metodo mira si es carpeta o archivo
    public void procesar(String rutaString, Queue<String> cola, TreeMap<String, Long> mapaGlobal) {
        Path ruta = Path.of(rutaString);

        // si no existe o no se puede leer, paso
        if (Files.notExists(ruta) || !Files.isReadable(ruta)) {
            return; 
        }

        try {
            if (Files.isDirectory(ruta)) {
                // si es un directorio, meto todo lo que tenga dentro a la cola
                // para que el bucle del main lo procese despues
                try (Stream<Path> stream = Files.list(ruta)) {
                    stream.forEach(hijo -> cola.add(hijo.toString()));
                }
            } else {
                // si es un archivo normal, llamo a la clase que cuenta las palabras
                contador.acumularPalabras(ruta, mapaGlobal);
            }
        } catch (IOException e) {
            System.err.println("Error I/O en " + ruta + ": " + e.getMessage());
        }
    }
}