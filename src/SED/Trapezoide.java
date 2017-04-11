package SED;

/**
 *
 * @author Tenistas
 */
public class Trapezoide {

    private double[] puntoC1;
    private double[] puntoC2;
    private double[] puntoIzq;
    private double[] puntoDer;
    private double membresiaY;
    private String etiqueta;
    private int turno;

    public Trapezoide() {
        //0 = posición en x
        //1 = posición en y
        puntoC1 = new double[2];
        puntoC2 = new double[2];
        puntoIzq = new double[2];
        puntoDer = new double[2];
    }

    public double[] getPuntoC1() {
        return puntoC1;
    }

    public void setPuntoC1(double[] puntoC1) {
        this.puntoC1 = puntoC1;
    }

    public double[] getPuntoC2() {
        return puntoC2;
    }

    public void setPuntoC2(double[] puntoC2) {
        this.puntoC2 = puntoC2;
    }

    public double[] getPuntoIzq() {
        return puntoIzq;
    }

    public void setPuntoIzq(double[] puntoIzq) {
        this.puntoIzq = puntoIzq;
    }

    public double[] getPuntoDer() {
        return puntoDer;
    }

    public void setPuntoDer(double[] puntoDer) {
        this.puntoDer = puntoDer;
    }

    public double getMembresiaY() {
        return membresiaY;
    }

    public void setMembresiaY(double membresiaY) {
        this.membresiaY = membresiaY;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public int getTurno() {
        return turno;
    }

    public void setTurno(int turno) {
        this.turno = turno;
    }

}
