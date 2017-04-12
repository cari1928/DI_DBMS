package SGBD;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tenistas
 */
public class BaseDatos {

    private String nombre;
    private List<Tabla> listTablas;

    public BaseDatos() {
        listTablas = new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Tabla> getListTablas() {
        return listTablas;
    }

    public void setListTablas(List<Tabla> listTablas) {
        this.listTablas = listTablas;
    }

}
