import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.List;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.parser.AutoDetectParser;


public class ListIt {

    /*
     * Revisa si es un directorio o archivo, añadiendo directorios a la cola y procesando archivos de texto plano.
     * @param rutaActual La ruta del directorio o archivo a procesar.
     * @param frontier La cola de rutas pendientes de procesar.
     * @param diccionario El heap donde se acumulan las palabras.
     * @param listaArchivos La lista de archivos procesados.
     * @param thesauro El gestor del thesauro para validar palabras.
     */
    public void procesar(String rutaActual, Queue<String> frontier, Map<String, Ocurrencia> diccionario, List<String> listaArchivos, ThesauroGestion thesauro) {
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
            String nombre = f.getName().toLowerCase();
            String extension = obtenerExtension(nombre);

            //Switch para saltar las extensiones que NO queremos o elegir las que SÍ queremos
            switch (extension) {
                case "txt":
                case "csv":
                case "md":
                case "cpp":
                case "java":
                case "json":
                    String pathAbsoluto = f.getAbsolutePath();
                    
                    // Asignamos ID. Como se inserta al final, el índice será el tamaño actual de la lista.
                    Integer idArchivo = listaArchivos.size(); 
                    listaArchivos.add(pathAbsoluto);

                    FichContPalabras lector = new FichContPalabras();
                    // Pasamos el ID al lector
                    lector.acumularPalabras(f.toPath(), idArchivo, diccionario, thesauro);
                    break;
                case "pdf":
                case "xml":
                case "html":
                case "docx":
                    String pathAbsolutoTika = f.getAbsolutePath();
                    Integer idArchivoTika = listaArchivos.size(); 
                    listaArchivos.add(pathAbsolutoTika);

                    try (FileInputStream inputStream = new FileInputStream(f)) {
                        // Ponemos -1 para que no haya límite de caracteres 
                        BodyContentHandler handler = new BodyContentHandler(-1); 
                        Metadata metadata = new Metadata();
                        ParseContext pcontext = new ParseContext();
                        
                        // Usamos el AutoDetectParser para que sirva para PDF, XML, HTML, etc.
                        AutoDetectParser parser = new AutoDetectParser(); 
                        parser.parse(inputStream, handler, metadata, pcontext);

                        // El texto extraído por Tika se obtiene del handler
                        String textoLimpio = handler.toString();

                        // Pasamos el texto extraído a nuestro diccionario
                        FichContPalabras lectorTika = new FichContPalabras();
                        lectorTika.acumularPalabrasTika(textoLimpio, f.getName(), idArchivoTika, diccionario, thesauro);

                    } catch (Exception e) {
                        System.err.println("Error analizando con Tika el archivo: " + f.getName() + " - " + e.getMessage());
                    }
                    break;
                default:
                    // Saltamos ejecutables, imágenes, etc.
                    System.out.println("[SALTADO] Extensión no soportada: " + f.getName());
                    break;
            }
        }
    }

    /*
     * Obtiene la extensión de un archivo a partir de su nombre.
     * @param nombreArchivo El nombre del archivo.
     * @return La extensión del archivo o una cadena vacía si no tiene extensión.
     */
    private String obtenerExtension(String nombreArchivo) {
        // lastIndexOf
        int lastIndexOf = nombreArchivo.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // No hay extensión
        }
        return nombreArchivo.substring(lastIndexOf + 1);
    }
}