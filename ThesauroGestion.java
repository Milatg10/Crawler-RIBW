import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ThesauroGestion implements Serializable {
    private static final String CACHE_THESAURO = "thesauro.dir";
    private TreeMap<String, ArrayList<String>> mapaSinonimos;

    public ThesauroGestion() {
        this.mapaSinonimos = new TreeMap<>();
    }

    // Carga el Thesauro desde caché o desde el txt original
    @SuppressWarnings("unchecked")
    public void inicializar(String rutaThesauroTxt) {
        File fCache = new File(CACHE_THESAURO);
        if (fCache.exists() && !fCache.isDirectory()) {
            System.out.println("--- Cargando Thesauro desde caché (" + CACHE_THESAURO + ") ---");
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fCache))) {
                this.mapaSinonimos = (TreeMap<String, ArrayList<String>>) ois.readObject();
                return;
            } catch (Exception e) {
                System.err.println("Error leyendo caché del thesauro, se regenerará.");
            }
        }

        System.out.println("--- Generando Thesauro desde archivo de texto ---");
        try (var lineas = Files.lines(Path.of(rutaThesauroTxt))) {
            lineas.forEach(linea -> {
                // Ignorar comentarios o líneas vacías
                if (linea.startsWith("#") || linea.trim().isEmpty()) return;

                // Separar por comas o punto y coma
                String[] tokensBrutos = linea.split("[,;]");
                List<String> tokensLimpios = new ArrayList<>();

                for (String token : tokensBrutos) {
                    // Limpieza: quitar etiquetas entre paréntesis y quitar espacios en blanco
                    String limpio = token.replaceAll("\\(.*?\\)", "").replaceAll("\\s+", "").toLowerCase().trim();
                    if (!limpio.isEmpty()) {
                        tokensLimpios.add(limpio);
                    }
                }

                // Meter en el TreeMap. La clave es cada palabra, el valor son todos los demás de esa línea
                for (String palabraClave : tokensLimpios) {
                    ArrayList<String> sinonimos = new ArrayList<>(tokensLimpios);
                    sinonimos.remove(palabraClave); // Nos quitamos a nosotros mismos de la lista de sinónimos
                    
                    // Si la palabra ya existe, añadimos los sinónimos nuevos sin duplicar
                    this.mapaSinonimos.putIfAbsent(palabraClave, new ArrayList<>());
                    for (String sin : sinonimos) {
                        if (!this.mapaSinonimos.get(palabraClave).contains(sin)) {
                            this.mapaSinonimos.get(palabraClave).add(sin);
                        }
                    }
                }
            });

            // Guardar en caché
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CACHE_THESAURO))) {
                oos.writeObject(this.mapaSinonimos);
            }
            System.out.println("Thesauro cargado. Palabras reconocidas: " + mapaSinonimos.size());

        } catch (IOException e) {
            System.err.println("Error procesando Thesauro: " + e.getMessage());
        }
    }

    // Comprueba si una palabra es válida para indexar
    public boolean contiene(String palabra) {
        return mapaSinonimos.containsKey(palabra);
    }

    // Devuelve los sinónimos de una palabra (o lista vacía si no tiene)
    public ArrayList<String> getSinonimos(String palabra) {
        return mapaSinonimos.getOrDefault(palabra, new ArrayList<>());
    }
}