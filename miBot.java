import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/*
    * miBot.java: Programa que va a actuar como principal (clase main) para explorar un directorio o fichero 
    * y generar un índice invertido de palabras que se encontrarán en los archivos. 
 */
public class miBot {

    private static final String ARCHIVO_CACHE = "fi.dir";

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {

        // Se verifica que se ha proporcionado más de un argumento
        if (args.length < 1) {
            System.err.println("ERROR. Uso: >java miBot <directorio_o_archivo> [salida.txt]");
            return;
        }

        // Se declaran las rutas de salida (es opcional) y entrada
        String rutaEntrada = args[0];
        String rutaSalida = (args.length == 2) ? args[1] : null; 

        // Heap con la finalidad de almacenar un String (palabra) y una Ocurrencia (un Integer con el total global (FTG) y un 
        // TreeMap con la ruta absoluta del archivo y el número de veces que aparece en cada archivo (TF))
        TreeMap<String, Ocurrencia> diccionario = null;

        // ArrayList para almacenar la ruta absoluta de los files, asignando un ID a cada uno
        ArrayList<String> listaArchivos = null;

        // Se verifica si existe un archivo ya creado fi.dir para cargarlo directamente en memoria y evitar recorrelo todo de nuevo
        File fCache = new File(ARCHIVO_CACHE);
        
        //Si existe y no es directorio, se intenta cargar el heap 
        if (fCache.exists() && !fCache.isDirectory()) {
            System.out.println("--- Cache encontrado (" + ARCHIVO_CACHE + "). Cargando datos... ---");
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fCache))) {
                diccionario = (TreeMap<String, Ocurrencia>) ois.readObject();
                listaArchivos = (ArrayList<String>) ois.readObject();
                System.out.println("Datos cargados correctamente desde memoria.");
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error leyendo caché, se recalculará: " + e.getMessage());
            }
        }

        // Si no existe el heap ya, se crea una cola, se añade a la cola la ruta de entrada y mientras la cola no esté vacía, 
        // se llama a procesar (función de ListIt) que va añadiendo el contenido de los directorios a la cola 
        if (diccionario == null || listaArchivos == null) {
            System.out.println("--- No hay caché. Iniciando recorrido y conteo ---");
            diccionario = new TreeMap<>(); 
            listaArchivos = new ArrayList<>();

            //Cola que almacena los directorios y archivos que se procesarán
            Queue<String> frontier = new LinkedList<>();
            //Se instancia ListIt para llamar a la función procesar
            ListIt listador = new ListIt();
            
            //Se añade la ruta proporcionada en la entrada a la cola para que se inicie el proceso 
            frontier.add(rutaEntrada);

            //Mientras la cola no esté vacía, se añade y se procesa cada directorio o archivo 
            while (!frontier.isEmpty()) {
                String rutaActual = frontier.poll();
                listador.procesar(rutaActual, frontier, diccionario, listaArchivos);
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARCHIVO_CACHE))) {
                oos.writeObject(diccionario);
                oos.writeObject(listaArchivos);
                System.out.println("--- Estructura guardada en " + ARCHIVO_CACHE + " ---");
            } catch (IOException e) {
                System.err.println("Error guardando caché: " + e.getMessage());
            }
        }

        emitirResultados(diccionario, listaArchivos, rutaSalida);
    }

    /*
     * Saca los resultados del heap en consola o en un archivo de salida.
     * @param diccionario El heap con las palabras y sus ocurrencias.
     * @param listaArchivos La lista de archivos procesados.
     * @param rutaSalida La ruta del archivo de salida, o null si se imprime en consola.
     */
    private static void emitirResultados(TreeMap<String, Ocurrencia> diccionario, ArrayList<String> listaArchivos, String rutaSalida) {
        Scanner sc;
        // Si ejecutamos desde una terminal real (CMD, PowerShell, etc.), usamos su codificación nativa
        if (System.console() != null) {
            sc = new Scanner(System.console().reader());
        } else {
            // Si ejecutamos desde un IDE (Eclipse, IntelliJ, NetBeans), usamos la por defecto
            sc = new Scanner(System.in);
        }
        // Mientras no se pulse enter por consola, se muestra el índice invertido de la palabra que se escriba por consola
        while(true) {
            System.out.println("Escribe la palabra a buscar o # para salir: ");
            String palabra = sc.nextLine(); 
            if(palabra.equals("#")){
                System.out.println("Saliendo del programa...");
                break;
            }
            else if(diccionario.containsKey(palabra)){
                Ocurrencia ocurrencia = diccionario.get(palabra);
                
                // --- RECONSTRUIR EL STRING DE SALIDA ---
                StringBuilder resultadoLegible = new StringBuilder();
                resultadoLegible.append(ocurrencia.getFTG()).append(", en archivos: {");
                
                boolean primero = true;
                // Iteramos sobre las ocurrencias (ID -> TF) y sacamos la ruta de la lista
                for (Map.Entry<Integer, Integer> entry : ocurrencia.getTfDocs().entrySet()) {
                    if (!primero) resultadoLegible.append(", ");
                    String rutaAbsoluta = listaArchivos.get(entry.getKey()); // Traducción O(1)
                    resultadoLegible.append("\n").append(rutaAbsoluta).append("=").append(entry.getValue());
                    primero = false;
                }
                resultadoLegible.append("}");
                // Si no es null la ruta de salida proporcionada, se escribe el resultado en esa ruta
                if(rutaSalida != null) {
                    try {
                        Files.writeString(Path.of(rutaSalida), palabra + ": " + resultadoLegible.toString() + "\n");
                        System.out.println("Resultados guardados exitosamente en: " + rutaSalida);
                    } catch (IOException e) {
                        System.err.println("No se pudo escribir el archivo de salida: " + e.getMessage());
                    }
                }
                else{
                    // Se usa para construir la salida de forma eficiente, ya que si usáramos System.out.println en cada iteración, 
                    // sería mucho más ineficente dado que cada palabra se imprimiría por separado 
                    StringBuilder sb = new StringBuilder();
                    sb.append("--- ÍNDICE INVERTIDO (Total palabras únicas: ").append(diccionario.size()).append(") ---\n");
                    sb.append(palabra).append(": ").append(resultadoLegible.toString()).append("\n");
                    System.out.println(sb.toString());
                }
            }
            else {
                System.out.println("La palabra '" + palabra + "' no se encuentra en el índice invertido.");
            }
        }
        sc.close();
    }
}