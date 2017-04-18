package GestionSistema;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author Tenistas
 */
public class Sistema {

    private final GestionArchivos objG;

    public Sistema() {
        objG = new GestionArchivos();
    }

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
        List<String> registros = objG.leer(ruta);
        String[] parts;

        for (String r : registros) {
            parts = r.split(" ");
            return parts[0] + " " + parts[1] + " " + parts[2] + " tmp";
        }

        return null;
    }

    //ruta = RUTA + "/SED/" + partsCondition[0]
    public Double[] getCriticPoints(String[] partsCondition, String ruta) throws IOException {
        String[] parts2;
        double tmpDistance;
        Double[] criticPoints;
        List<String> listR;

        if (partsCondition[2].contains("$")) {
            criticPoints = new Double[2]; //prepara el contenedor de los puntos críticos
            //se hará el proceso en base a una etiqueta linguística
            //obtiene la información de la variable linguística
            listR = objG.leer(ruta);
            for (String r : listR) {
                if (r.contains(partsCondition[2].split("\\$")[1])) {
                    //contiene la etiqueta linguistica
                    parts2 = r.split(" ");

                    if (partsCondition[1].equals("fleq")) {
                        criticPoints[0] = Double.parseDouble(parts2[2]); //obtiene el punto crítico más a la derecha
                        //obtiene la distancia entre el punto inferior izquierdo y el punto crítico 1
                        tmpDistance = Double.parseDouble(parts2[1]) - Double.parseDouble(parts2[4]);
                        //obtiene el punto inferior de la derecha
                        criticPoints[1] = criticPoints[0] + tmpDistance;
                    } else {
                        //partsC == FGEG
                        //toma el punto crítico más a la izquierda
                        criticPoints[0] = Double.parseDouble(parts2[1]);
                        criticPoints[1] = Double.parseDouble(parts2[4]); //toma el punto que va a la izquierda
                    }
                    return criticPoints; //ya obtuvo los puntos críticos 
                }
            }
        } else {
            //partsCondition[2] == #
            criticPoints = new Double[1]; //será un semtrapezoide
            criticPoints[0] = Double.parseDouble(partsCondition[2].split("#")[0]); //obtiene el valor a lado del #
            return criticPoints;
        }

        return null; //no encontró la etiqueta linguistica
    }

    public void showFile(String ruta) throws IOException {
        List<String> listRegistros = objG.leer(ruta);
        listRegistros.forEach((registro) -> {
            System.out.println(registro);
        });
    }
}
