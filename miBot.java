import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class miBot {

    private static final String ARCHIVO_CACHE = "fi.dir";

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {

        // compruebo si me han pasado argumentos, si no me salgo
        if (args.length < 1) {
            System.err.println("ERROR. Uso: >java miBot <directorio_o_archivo> [salida.txt]");
            return;
        }

        String rutaEntrada = args[0];
        // si hay un segundo argumento es el fichero de salida, si no null
        String rutaSalida = (args.length == 2) ? args[1] : null; 

        TreeMap<String, Long> mapaPalabras = null;

        // miro si ya existe el archivo fi.dir para no tener que leer todo otra vez
        File fCache = new File(ARCHIVO_CACHE);
        
        if (fCache.exists() && !fCache.isDirectory()) {
            System.out.println("--- Cache encontrado (" + ARCHIVO_CACHE + "). Cargando datos... ---");
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fCache))) {
                // cargo el mapa directamente del archivo (deserializar)
                mapaPalabras = (TreeMap<String, Long>) ois.readObject();
                System.out.println("Datos cargados correctamente desde memoria.");
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error leyendo caché, se recalculará: " + e.getMessage());
            }
        }

        // si el mapa sigue siendo null es que no habia cache o fallo, asi que toca currar
        if (mapaPalabras == null) {
            System.out.println("--- No hay caché. Iniciando recorrido y conteo ---");
            mapaPalabras = new TreeMap<>(); // uso treemap para que salga ordenado
            Queue<String> cola = new LinkedList<>();
            ListIt listador = new ListIt();

            // meto la primera ruta en la cola para empezar
            cola.add(rutaEntrada);

            // bucle principal: mientras queden cosas en la cola voy sacando
            while (!cola.isEmpty()) {
                String rutaActual = cola.poll();
                // llamo a procesar pasandole el mapa para que lo vaya rellenando
                listador.procesar(rutaActual, cola, mapaPalabras);
            }

            // guardo todo en el archivo fi.dir para la proxima vez (serializar)
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARCHIVO_CACHE))) {
                oos.writeObject(mapaPalabras);
                System.out.println("--- Estructura guardada en " + ARCHIVO_CACHE + " ---");
            } catch (IOException e) {
                System.err.println("Error guardando caché: " + e.getMessage());
            }
        }

        // al final muestro los resultados o los guardo
        emitirResultados(mapaPalabras, rutaSalida);
    }

    // metodo para decidir si escribo en fichero o por pantalla
    private static void emitirResultados(TreeMap<String, Long> mapa, String rutaSalida) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- RESULTADOS (Total palabras únicas: ").append(mapa.size()).append(") ---\n");
        
        for (Map.Entry<String, Long> entrada : mapa.entrySet()) {
            sb.append(entrada.getKey()).append(" : ").append(entrada.getValue()).append("\n"); 
        }

        if (rutaSalida != null) {
            // si hay ruta de salida, escribo en el archivo
            try {
                Files.writeString(Path.of(rutaSalida), sb.toString());
                System.out.println("Resultados guardados exitosamente en: " + rutaSalida);
            } catch (IOException e) {
                System.err.println("No se pudo escribir el archivo de salida: " + e.getMessage());
            }
        } else {
            // si no, lo saco por la consola
            System.out.println(sb.toString());
        }
    }
}