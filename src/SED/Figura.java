package SED;

/**
 *
 * @author Tenistas
 */
public class Figura {

    //CLASE PADRE
    protected double[] puntoC;
    protected double[] puntoI1; //punto en (x,0)
    protected double membresiaY, aux, origen, fin;
    protected String etiqueta;
    protected int turno; //turno que tiene dentro de la gráfica

    public Figura() {
        //0 = posición en x
        //1 = posición en y
        puntoC = new double[2];
        puntoI1 = new double[2];
    }

    public double[] getPuntoC() {
        return puntoC;
    }

    public void setPuntoC(double[] puntoC) {
        this.puntoC = puntoC;
    }

    public double[] getPuntoI1() {
        return puntoI1;
    }

    public void setPuntoI1(double[] puntoI1) {
        this.puntoI1 = puntoI1;
    }

    public double getMembresiaY() {
        return membresiaY;
    }

    public void setMembresiaY(double membresiaY) {
        this.membresiaY = membresiaY;
    }

    public double getAux() {
        return aux;
    }

    public void setAux(double aux) {
        this.aux = aux;
    }

    public double getOrigen() {
        return origen;
    }

    public void setOrigen(double origen) {
        this.origen = origen;
    }

    public double getFin() {
        return fin;
    }

    public void setFin(double fin) {
        this.fin = fin;
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

    public double calculaTraslape() {
        return 0;
    }

}
