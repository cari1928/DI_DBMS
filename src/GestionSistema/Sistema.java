package GestionSistema;

import SED.VariableEntrada;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Tenistas
 */
public class Sistema {

    /**
     * Obtiene el universo de discurso, las unidades y el nombre de la variable
     * linguística correspondiente a una tabla y etiqueta específicas
     *
     * @param ruta Contiene el nombre de la tabla y la columna
     * @param etiqueta Etiqueta linguística
     * @return Registro con todos los datos o null
     * @throws IOException
     */
    public String getUniverse(String ruta, String etiqueta) throws IOException {
        GestionArchivos objG = new GestionArchivos();
        List<String> registros = objG.leer(ruta);
        String[] parts;

        for (String r : registros) {
            parts = r.split(" ");
            return parts[0] + " " + parts[1] + " " + parts[2] + " tmp";
        }

        return null;
    }
}
