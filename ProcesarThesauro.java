import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ProcesarThesauro {
    
    private Map<String, List<String>> thesauro;

    public ProcesarThesauro() {
        thesauro = new TreeMap<>();
    }
    
    public void cargarThesauro(String rutaArchivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                // 1. Omitir líneas vacías
                if (linea.trim().isEmpty()) continue;

                // 2. Separar la línea por el punto y coma
                String[] partes = linea.split(";");
                
                if (partes.length > 0) {
                    String palabraPrincipal = partes[0].trim();

                    // 3. REGLA: Si la palabra principal contiene "(", ignoramos toda la línea
                    if (palabraPrincipal.contains("(")) {
                        continue; 
                    }

                    // 4. Si no tiene paréntesis, procesamos sus sinónimos
                    List<String> sinonimos = new ArrayList<>();
                    for (int i = 1; i < partes.length; i++) {
                        String sinonimo = partes[i].trim();
                        // Opcional: solo agregar si el sinónimo no está vacío
                        if (!sinonimo.isEmpty()) {
                            sinonimos.add(sinonimo);
                        }
                    }

                    // 5. Guardar en el Map (usamos computeIfAbsent por si la palabra aparece en varias líneas)
                    thesauro.computeIfAbsent(palabraPrincipal, k -> new ArrayList<>())
                            .addAll(sinonimos);
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
    }
}
