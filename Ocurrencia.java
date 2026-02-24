import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class Ocurrencia implements Serializable, Comparable<Ocurrencia> {
    
    private Integer FTG; // Total Term Frequency (TTF) en todos los documentos
    private Map<String, Integer> tfDocs; // Term Frequency (TF) por documento 

    /*
     * Constructor de la clase Ocurrencia.
     * Inicializa FTG a 0 y tfDocs como un TreeMap vacío.
     */
    public Ocurrencia() {
        this.FTG = 0;
        this.tfDocs = new TreeMap<>(); // Heap para que los archivos salgan ordenados
    }

    /*
     * Registra la aparición de una palabra en un archivo específico.
     * @param nombreArchivo El nombre del archivo donde se encontró la palabra.
     */
    public void registrarAparicion(String nombreArchivo) {
        this.FTG++; // Sumamos 1 al total global
        // Sumamos 1 a la cuenta específica de este archivo
        this.tfDocs.put(nombreArchivo, this.tfDocs.getOrDefault(nombreArchivo, 0) + 1);
    }

    /*
     * Devuelve el total de apariciones de la palabra en todos los archivos .
     * @return El total de apariciones.
     */
    public Integer getFTG() {
        return FTG;
    }

    /*
     * Devuelve el heap de apariciones por archivo.
     * @return El mapa con las apariciones por archivo.
     */
    public Map<String, Integer> getTfDocs() {
        return tfDocs;
    }

    /*
     * Devuelve un mensaje con el número de apariciones y los archivos donde aparece.
     * @return Una cadena con el total de apariciones y los archivos donde aparece.
     */
    @Override
    public String toString() {
        return FTG + ", en archivos: " + tfDocs.toString();
    }
    
    @Override
    public int compareTo(Ocurrencia otra) {
        // Compara el FTG de este objeto con el de 'otra' Ocurrencia.
        // Devuelve:
        // un número negativo si este.FTG < otra.FTG
        // 0 si son iguales
        // un número positivo si este.FTG > otra.FTG
        return this.FTG.compareTo(otra.getFTG());
    }

    @Override
    public boolean equals(Object o) {
        // Compara para el equivalente a "==" y "!="
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ocurrencia que = (Ocurrencia) o;
        return Objects.equals(FTG, que.FTG);
    }

    // @Override
    // public int hashCode() {
    //     return Objects.hash(FTG);
    // }
}