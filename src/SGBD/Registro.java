package SGBD;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tenistas
 */
public class Registro {

    List<Columna> list_columnas;

    public Registro() {
        list_columnas = new ArrayList<>();
    }

    public List<Columna> getList_columnas() {
        return list_columnas;
    }

    public void setList_columnas(List<Columna> list_columnas) {
        this.list_columnas = list_columnas;
    }

}
