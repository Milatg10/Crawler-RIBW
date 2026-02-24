import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class Ocurrencia implements Serializable {
    private static final long serialVersionUID = 1L; // Buena práctica para Serializable
    
    private Integer FTG; // Total Term Frequency (TTF)
    private Map<String, Integer> tfDocs; // Term Frequency (TF) por documento

    public Ocurrencia() {
        this.FTG = 0;
        this.tfDocs = new TreeMap<>(); // TreeMap para que los archivos salgan ordenados
    }

    // Método principal para registrar una palabra encontrada en un archivo
    public void registrarAparicion(String nombreArchivo) {
        this.FTG++; // Sumamos 1 al total global
        // Sumamos 1 a la cuenta específica de este archivo (si no existe, empieza en 0 + 1)
        this.tfDocs.put(nombreArchivo, this.tfDocs.getOrDefault(nombreArchivo, 0) + 1);
    }

    public Integer getFTG() {
        return FTG;
    }

    public Map<String, Integer> getTfDocs() {
        return tfDocs;
    }

    @Override
    public String toString() {
        return "Total: " + FTG + " | Archivos: " + tfDocs.toString();
    }
}