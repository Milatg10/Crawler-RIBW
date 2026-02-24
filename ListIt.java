import java.io.File;
import java.util.Map;
import java.util.Queue;

public class ListIt {

    /*
     * Revisa si es un directorio o archivo, añadiendo directorios a la cola y procesando archivos de texto plano.
     * @param rutaActual La ruta del directorio o archivo a procesar.
     * @param frontier La cola de rutas pendientes de procesar.
     * @param diccionario El heap donde se acumulan las palabras.
     */
    public void procesar(String rutaActual, Queue<String> frontier, Map<String, Ocurrencia> diccionario) {
        File f = new File(rutaActual);

        // Si es un directorio, se añaden sus directorios o archivos a la cola
        if (f.isDirectory()) {
            // listFiles() devuelve un array de File con el contenido del directorio, o null si no se puede acceder
            File[] archivos = f.listFiles();
            // Si el directorio no es null, se añaden sus subdirectorios y archivos a la cola (path absoluto)
            if (archivos != null) {
                for (File hijo : archivos) {
                    frontier.add(hijo.getAbsolutePath());
                }
            }
        // Si es un archivo, se crea una instancia de FichContPalabras para llamar a su función acumularPalabras, a la que se le envía
        // la ruta del archivo a procesar y el heap donde se van a guardar las palabras
        } else if (f.isFile()) {
            //String nombre = f.getName().toLowerCase();
            //String extension = obtenerExtension(nombre);

            // Switch para saltar las extensiones que NO queremos o elegir las que SÍ queremos
            // switch (extension) {
            //     case "txt":
            //     case "csv":
            //     case "md":
            //         // Solo procesamos archivos de texto plano
                     FichContPalabras lector = new FichContPalabras();
                     lector.acumularPalabras(f.toPath(), diccionario);
            //         break;
            //     default:
            //         // Saltamos ejecutables, imágenes, etc.
            //         // System.out.println("[SALTADO] Extensión no soportada: " + f.getName());
            //         break;
            // }
        }
    }

    /*
     * Obtiene la extensión de un archivo a partir de su nombre.
     * @param nombreArchivo El nombre del archivo.
     * @return La extensión del archivo o una cadena vacía si no tiene extensión.
     */
    // private String obtenerExtension(String nombreArchivo) {
    //     // lastIndexOf
    //     int lastIndexOf = nombreArchivo.lastIndexOf(".");
    //     if (lastIndexOf == -1) {
    //         return ""; // No hay extensión
    //     }
    //     return nombreArchivo.substring(lastIndexOf + 1);
    // }
}