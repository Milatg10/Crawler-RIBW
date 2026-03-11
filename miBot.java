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
     * Saca los resultados dividiendo el ranking en dos bloques: primero la palabra literal 
     * y debajo los sinónimos. Ambos bloques se ordenan por el peso FT/FTG de mayor a menor.
     */
    private static void emitirResultados(TreeMap<String, Ocurrencia> diccionario, ArrayList<String> listaArchivos, String rutaSalida, ThesauroGestion thesauro) {
        Scanner sc;
        if (System.console() != null) {
            sc = new Scanner(System.console().reader());
        } else {
            sc = new Scanner(System.in);
        }

        while (true) {
            System.out.println("Escribe la palabra a buscar o # para salir: ");
            String palabraBuscada = sc.nextLine().toLowerCase().trim();
            if (palabraBuscada.equals("#")) {
                System.out.println("Saliendo del programa...");
                break;
            }

            // Creamos dos listas separadas. 
            // Formato array: { idDoc (int), termino (String), FT (int), FTG (int), peso (double) }
            List<Object[]> listaLiterales = new ArrayList<>();
            List<Object[]> listaSinonimos = new ArrayList<>();
            
            // Set para no repetir el archivo abajo si ya apareció arriba con la palabra literal
            Set<Integer> docsYaProcesados = new HashSet<>();

            // 1. BLOQUE DE LA PALABRA LITERAL ("ababol")
            if (diccionario.containsKey(palabraBuscada)) {
                Ocurrencia oc = diccionario.get(palabraBuscada);
                int ftg = oc.getFTG();
                for (Map.Entry<Integer, Integer> entry : oc.getTfDocs().entrySet()) {
                    int idDoc = entry.getKey();
                    int ft = entry.getValue(); // Frecuencia en este archivo
                    double peso = (double) ft / ftg; // Calculamos la división FT / FTG
                    
                    listaLiterales.add(new Object[]{idDoc, palabraBuscada, ft, ftg, peso});
                    docsYaProcesados.add(idDoc); 
                }
            }

            // 2. BLOQUE DE LOS SINÓNIMOS ("amapola", "necio"...)
            ArrayList<String> sinonimos = thesauro.getSinonimos(palabraBuscada);
            for (String sin : sinonimos) {
                if (diccionario.containsKey(sin)) {
                    Ocurrencia oc = diccionario.get(sin);
                    int ftg = oc.getFTG();
                    for (Map.Entry<Integer, Integer> entry : oc.getTfDocs().entrySet()) {
                        int idDoc = entry.getKey();
                        int ft = entry.getValue();
                        
                        // Solo lo metemos en sinónimos si no salió ya en la lista de literales
                        if (!docsYaProcesados.contains(idDoc)) {
                            double peso = (double) ft / ftg; // Calculamos la división FT / FTG
                            listaSinonimos.add(new Object[]{idDoc, sin, ft, ftg, peso});
                            docsYaProcesados.add(idDoc);
                        }
                    }
                }
            }

            // 3. ORDENAR AMBOS BLOQUES POR EL PESO (FT / FTG) DE MAYOR A MENOR
            // Compara la posición [4] del array, que es donde guardamos el 'peso' (el double)
            Comparator<Object[]> comparadorPeso = (a, b) -> Double.compare((Double) b[4], (Double) a[4]);
            listaLiterales.sort(comparadorPeso);
            listaSinonimos.sort(comparadorPeso);

            // 4. IMPRIMIR LOS RESULTADOS (Primero toda la lista literal, luego toda la lista de sinónimos)
            if (listaLiterales.isEmpty() && listaSinonimos.isEmpty()) {
                System.out.println("La palabra '" + palabraBuscada + "' y sus sinónimos no se encuentran indexados.");
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("--- RESULTADOS PARA '").append(palabraBuscada).append("' ---\n");
                
                // Imprimir primero los literales
                for (Object[] doc : listaLiterales) {
                    int idDoc = (Integer) doc[0];
                    String termino = (String) doc[1];
                    int ft = (Integer) doc[2];
                    int ftg = (Integer) doc[3];
                    double peso = (Double) doc[4];
                    String ruta = listaArchivos.get(idDoc);
                    
                    // Mostramos la ruta, la palabra, el peso formateado con 4 decimales y los valores originales
                    sb.append("-> ").append(ruta)
                      .append(" | [LITERAL] '").append(termino).append("'")
                      .append(" | Peso: ").append(String.format("%.4f", peso))
                      .append(" (FT:").append(ft).append(" / FTG:").append(ftg).append(")\n");
                }

                // Imprimir debajo los sinónimos
                for (Object[] doc : listaSinonimos) {
                    int idDoc = (Integer) doc[0];
                    String termino = (String) doc[1];
                    int ft = (Integer) doc[2];
                    int ftg = (Integer) doc[3];
                    double peso = (Double) doc[4];
                    String ruta = listaArchivos.get(idDoc);
                    
                    sb.append("-> ").append(ruta)
                      .append(" | [SINÓNIMO] '").append(termino).append("'")
                      .append(" | Peso: ").append(String.format("%.4f", peso))
                      .append(" (FT:").append(ft).append(" / FTG:").append(ftg).append(")\n");
                }

                if (rutaSalida != null) {
                    try {
                        Files.writeString(Path.of(rutaSalida), sb.toString());
                        System.out.println("Resultados guardados en: " + rutaSalida);
                    } catch (IOException e) {
                        System.err.println("Error al escribir el archivo: " + e.getMessage());
                    }
                } else {
                    System.out.print(sb.toString());
                }
            }
        }
        sc.close();
    }
}