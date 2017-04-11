package SED;

/**
 *
 * @author Tenistas
 */
public class Etiqueta {

    private String etiqueta;
    private double membresia;

    public Etiqueta(String etiqueta, double membresia) {
        this.etiqueta = etiqueta;
        this.membresia = membresia;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public double getMembresia() {
        return membresia;
    }

    public void setMembresia(double membresia) {
        this.membresia = membresia;
    }
}
