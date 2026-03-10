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

        // Se instancia el gestor del thesauro y se inicializa con el archivo de texto del thesauro
        String rutaThesauroTxt = "Thesaurus_es_ES.txt"; 
        ThesauroGestion thesauro = new ThesauroGestion();
        thesauro.inicializar(rutaThesauroTxt);
        
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
                listador.procesar(rutaActual, frontier, diccionario, listaArchivos, thesauro);
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARCHIVO_CACHE))) {
                oos.writeObject(diccionario);
                oos.writeObject(listaArchivos);
                System.out.println("--- Estructura guardada en " + ARCHIVO_CACHE + " ---");
            } catch (IOException e) {
                System.err.println("Error guardando caché: " + e.getMessage());
            }
        }

        emitirResultados(diccionario, listaArchivos, rutaSalida, thesauro);
    }

    /*
     * Saca los resultados del heap en consola o en un archivo de salida,
     * incluyendo los sinónimos de la palabra buscada.
     * @param diccionario El heap con las palabras y sus ocurrencias.
     * @param listaArchivos La lista de archivos procesados.
     * @param rutaSalida La ruta del archivo de salida, o null si se imprime en consola.
     * @param thesauro El gestor del thesauro para validar y obtener sinónimos.
     */
    private static void emitirResultados(TreeMap<String, Ocurrencia> diccionario, ArrayList<String> listaArchivos, String rutaSalida, ThesauroGestion thesauro) {
        Scanner sc;
        if (System.console() != null) {
            sc = new Scanner(System.console().reader());
        } else {
            sc = new Scanner(System.in);
        }

        while(true) {
            System.out.println("Escribe la palabra a buscar o # para salir: ");
            // Lo guardamos todo junto tal cual lo escribe el usuario 
            String palabraBuscada = sc.nextLine().toLowerCase().trim(); 
            if(palabraBuscada.equals("#")){
                System.out.println("Saliendo del programa...");
                break;
            }

            //Juntar la palabra buscada y sus sinónimos en una lista
            List<String> terminosABuscar = new ArrayList<>();
            terminosABuscar.add(palabraBuscada);
            terminosABuscar.addAll(thesauro.getSinonimos(palabraBuscada));

            //Acumular resultados (ID Archivo -> Frecuencia total de palabra + sinónimos)
            TreeMap<Integer, Integer> resultadosAcumulados = new TreeMap<>();
            int ftgTotal = 0; // Frecuencia total global sumando sinónimos
            boolean encontrado = false;

            for (String termino : terminosABuscar) {
                if (diccionario.containsKey(termino)) {
                    encontrado = true;
                    Ocurrencia oc = diccionario.get(termino);
                    ftgTotal += oc.getFTG(); // Sumamos al global

                    // Sumamos las apariciones de cada archivo
                    for (Map.Entry<Integer, Integer> entry : oc.getTfDocs().entrySet()) {
                        int idDoc = entry.getKey();
                        int freq = entry.getValue();
                        resultadosAcumulados.put(idDoc, resultadosAcumulados.getOrDefault(idDoc, 0) + freq);
                    }
                }
            }

            //Imprimir resultados 
            if (!encontrado) {
                System.out.println("La palabra '" + palabraBuscada + "' (ni sus sinónimos) se encuentran en el índice invertido.");
            } else {
                StringBuilder resultadoLegible = new StringBuilder();
                resultadoLegible.append(ftgTotal).append(", en archivos: {");

                boolean primero = true;
                for (Map.Entry<Integer, Integer> entry : resultadosAcumulados.entrySet()) {
                    if (!primero) resultadoLegible.append(", ");
                    String rutaAbsoluta = listaArchivos.get(entry.getKey());
                    resultadoLegible.append("\n").append(rutaAbsoluta).append("=").append(entry.getValue());
                    primero = false;
                }
                resultadoLegible.append("}");

                if (rutaSalida != null) {
                    try {
                        Files.writeString(Path.of(rutaSalida), palabraBuscada + ": " + resultadoLegible.toString() + "\n");
                        System.out.println("Resultados guardados exitosamente en: " + rutaSalida);
                    } catch (IOException e) {
                        System.err.println("No se pudo escribir el archivo de salida: " + e.getMessage());
                    }
                } else {
                    System.out.println("--- RESULTADOS ---");
                    System.out.println(palabraBuscada + ": " + resultadoLegible.toString());
                }
            }
        }
        sc.close();
    }
}