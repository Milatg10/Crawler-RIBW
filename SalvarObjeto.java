/*
 * SalvarObjeto.java: Genera un archivo serializado con un Map<String, Object> (diccionario) que contiene tanto Strings como números.
 */

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SalvarObjeto {
    public static void main(String[] args) {
        
        /* 
         * Sustituir Hashtable (obsoleto y sincronizado) por HashMap o Map.
         */
        Map<String, Object> map = new HashMap<>();

        map.put("String", "Luis Rodriguez Duran");
        map.put("Integer", 23);   // Autoboxing int -> Integer
        map.put("Double", 0.96);  // Autoboxing double -> Double

        var path = Path.of("diccionario.ser");

        // Try-with-resources y NIO.2
        try (var os = Files.newOutputStream(path);
             var oos = new ObjectOutputStream(os)) {
            
            oos.writeObject(map);
            System.out.println("Objeto guardado correctamente en: " + path.toAbsolutePath());

        } catch (IOException e) {
            System.err.println("Error al guardar el archivo: " + e.getMessage());
        }
    }
}