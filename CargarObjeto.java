/*
 * CargarObjeto.java: Lee un archivo serializado con un Map<String, Object> (diccionario) que contiene tanto Strings como números, 
 * y muestra su contenido por consola.
 */

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class CargarObjeto {

    public static void main(String[] args) {
        // Si no existe un argumento, mostrar el mensaje de error y salir
        String nombreArchivo = (args.length > 0) ? args[0] : "diccionario.ser";
        Path ruta = Path.of(nombreArchivo);

        System.out.println("Intentando leer desde: " + ruta.toAbsolutePath());

        // Verificamos si el archivo existe antes de intentar leerlo
        if (Files.notExists(ruta)) {
            System.err.println("ERROR: El archivo no existe.");
            return;
        }

        // Try-with-resources para asegurar el cierre de los streams
        try (InputStream fis = Files.newInputStream(ruta);
            // ObjectInputStream para leer objetos serializados
            ObjectInputStream ois = new ObjectInputStream(fis)) {
            // Leemos el objeto del archivo. Este objeto es un Map<String, Object> que contiene tanto Strings como números.
            Object objetoLeido = ois.readObject();

            // CAMBIO IMPORTANTE: Usamos Map<String, Object> en lugar de <String, Integer>
            // para que pueda aceptar tanto textos como números.
            if (objetoLeido instanceof Map) {
                // El cast es seguro porque sabemos que el objeto es un Map, pero no sabemos los tipos exactos de los valores.
                @SuppressWarnings("unchecked")
                // Ahora 'mapa' es un Map<String, Object>, lo que nos permite almacenar tanto Strings como Integer y Double.
                Map<String, Object> mapa = (Map<String, Object>) objetoLeido;

                System.out.println("--- Objeto cargado correctamente en memoria ---");
                System.out.println("Tipo de clase real: " + mapa.getClass().getSimpleName());
                System.out.println("Contenido:");
                
                // Iteramos sobre las entradas del mapa y mostramos tanto la clave como el valor, y el tipo de valor.
                mapa.forEach((clave, valor) -> {
                    // Ahora 'valor' es un Object. Java llamará automáticamente a .toString()
                    // e imprimirá correctamente tanto el 23 como el nombre "Luis...".
                    System.out.println("  Clave: " + clave + " -> Valor: " + valor + " (" + valor.getClass().getSimpleName() + ")");
                });

            } else {
                System.out.println("El objeto leído no es un Mapa.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}