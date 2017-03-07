package Clases;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tenistas
 */
public class BaseDatos {

    String nombre;
    List<Tabla> listTablas;
    
    public BaseDatos() {
        listTablas = new ArrayList<>();
    }
}
