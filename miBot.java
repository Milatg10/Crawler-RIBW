import java.io.*;
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
     * Saca los resultados del heap en consola o en un archivo de salida.
     * @param diccionario El heap con las palabras y sus ocurrencias.
     * @param listaArchivos La lista de archivos procesados.
     * @param rutaSalida La ruta del archivo de salida, o null si se imprime en consola.
     * @param thesauro El gestor del thesauro para validar palabras.
     */
    private static void emitirResultados(TreeMap<String, Ocurrencia> diccionario, ArrayList<String> listaArchivos, String rutaSalida, ThesauroGestion thesauro) {
        Scanner sc = new Scanner(System.in);
        while(true) {
            System.out.println("Escribe los términos a buscar o # para salir: ");
            String input = sc.nextLine().toLowerCase().trim();
            if(input.equals("#")) break;

            // Separamos los términos por espacios y procesamos cada uno, buscando también sus sinónimos en el thesauro
            String[] terminosBuscados = input.split("\\s+");
            
            // Estructura para calcular el ranking: ID Archivo -> [NumTérminosDiferentesEncontrados, FrecuenciaTotal]
            // Usamos un array de 2 enteros: int[]{terminosDiferentes, frecuenciaAcumulada}
            Map<Integer, int[]> rankingDocs = new HashMap<>();

            for (String terminoOriginal : terminosBuscados) {
                // Montamos la lista de lo que vale para este término (La palabra + sus sinónimos)
                Set<String> terminosValidos = new HashSet<>();
                terminosValidos.add(terminoOriginal);
                terminosValidos.addAll(thesauro.getSinonimos(terminoOriginal));

                // Guardamos qué archivos ya han puntuado para ESTE término original (para no contar el sinónimo como un término distinto)
                Set<Integer> archivosPuntuadosParaEsteTermino = new HashSet<>();

                for (String tValido : terminosValidos) {
                    if (diccionario.containsKey(tValido)) {
                        Map<Integer, Integer> tfDocs = diccionario.get(tValido).getTfDocs(); 
                        for (Map.Entry<Integer, Integer> entry : tfDocs.entrySet()) {
                            int idDoc = entry.getKey();
                            int freq = entry.getValue();

                            rankingDocs.putIfAbsent(idDoc, new int[]{0, 0});
                            
                            // Si es la primera vez que este doc ve el término o sus sinónimos, le sumamos 1 a los términos encontrados
                            if (!archivosPuntuadosParaEsteTermino.contains(idDoc)) {
                                rankingDocs.get(idDoc)[0] += 1;
                                archivosPuntuadosParaEsteTermino.add(idDoc);
                            }
                            // Sumamos la frecuencia total
                            rankingDocs.get(idDoc)[1] += freq;
                        }
                    }
                }
            }

            // Ordenar los resultados (Ranking)
            List<Map.Entry<Integer, int[]>> listaRanking = new ArrayList<>(rankingDocs.entrySet());
            listaRanking.sort((a, b) -> {
                int[] datosA = a.getValue();
                int[] datosB = b.getValue();
                // 1º Prioridad: Mayor número de términos encontrados distintos (incluyendo sinónimos)
                if (datosA[0] != datosB[0]) {
                    return Integer.compare(datosB[0], datosA[0]); 
                }
                // 2º Prioridad: Mayor frecuencia total
                return Integer.compare(datosB[1], datosA[1]);
            });

            // Imprimir
            if (listaRanking.isEmpty()) {
                System.out.println("No se encontraron coincidencias.");
            } else {
                System.out.println("--- RESULTADOS RANKING ---");
                for (Map.Entry<Integer, int[]> entry : listaRanking) {
                    String ruta = listaArchivos.get(entry.getKey());
                    //int termsFound = entry.getValue()[0];
                    int totalFreq = entry.getValue()[1];
                    System.out.println("-> " + ruta + " | Apariciones en fichero: " + totalFreq);
                }
            }
        }
        sc.close();
    }
}