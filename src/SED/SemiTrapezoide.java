package SED;

/**
 *
 * @author Tenistas
 */
public class SemiTrapezoide extends Figura {

    //YA ESTÁN DENTRO DE LA CLASE FIGURA
    //private double puntoC[];
    //private double punto2[]; 
    //private double membresiaY;
    //private String etiqueta;
    //private int turno; 
    private char orientacion;

    public SemiTrapezoide() {
        super();
        //YA ESTÁN DENTRO DE LA CLASE FIGURA
        //puntoC = new double[2];
        //punto2 = new double[2];
    }

    public char getOrientacion() {
        return orientacion;
    }

    public void setOrientacion(char orientacion) {
        this.orientacion = orientacion;
    }

    /**
     *
     * @return
     */
    @Override
    public double calculaTraslape() {
        double rango;
        if (aux == fin) {
            rango = fin - puntoC[0];
        } else {
            rango = puntoC[0] - origen;
        }

        //TODO, checar que esto realmente funcione
        rango = rango * 0.6 + puntoC[0];
        return rango;
    }

}
