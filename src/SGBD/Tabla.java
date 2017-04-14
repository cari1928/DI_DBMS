package SGBD;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tenistas
 */
public class Tabla {

    List<Columna> listColumnas;
    List<Indice> listIndices;
    List<String> columnas;
    String nombtab;
    String archivo;
    int tabid;
    int tamreng;
    int ncols;
    double nrengs;
    int nindex;

    public Tabla() {
        columnas = new ArrayList<>();
    }
}
