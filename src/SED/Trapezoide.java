package SED;

/**
 *
 * @author Tenistas
 */
public class Trapezoide extends Figura {

    //ya existen en la clase Figura
    //private double[] puntoC1; 
    //private double[] puntoIzq; 
    //private double membresiaY; 
    //private String etiqueta; 
    //private int turno; 
    private double[] puntoC2;
    private double[] puntoDer;

    public Trapezoide() {
        super();
        //ya existen en la clase Figura
        //puntoIzq = new double[2];
        //puntoC1 = new double[2];
        puntoC2 = new double[2];
        puntoDer = new double[2];
    }

    public double[] getPuntoC2() {
        return puntoC2;
    }

    public void setPuntoC2(double[] puntoC2) {
        this.puntoC2 = puntoC2;
    }

    public double[] getPuntoDer() {
        return puntoDer;
    }

    public void setPuntoDer(double[] puntoDer) {
        this.puntoDer = puntoDer;
    }

    /**
     *
     * @return
     */
    @Override
    public double calculaTraslape() {
        double nuevoOrigen;
        if (aux == fin) {
            nuevoOrigen = (fin - puntoC2[0]) * 0.6;
        } else {
            nuevoOrigen = (puntoC[0] - origen) * 0.6;
        }
        nuevoOrigen = (puntoC2[0] + nuevoOrigen);
        return nuevoOrigen;
    }

}
