package SGBD;

/**
 *
 * @author Tenistas
 */
public class Columna {

    private int tabid;
    private int colid; //id
    private char[] nomcol = new char[10]; //nombre
    private char coltipo;
    private int coltam;
    private int tabref;
    private int colref;

    private String contenido;
    private String NomColS;

    public String getNomColS() {
        return NomColS;
    }

    public void setNomColS(String NomColS) {
        this.NomColS = NomColS;
    }
    
    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public int getTabid() {
        return tabid;
    }

    public void setTabid(int tabid) {
        this.tabid = tabid;
    }

    public int getColid() {
        return colid;
    }

    public void setColid(int colid) {
        this.colid = colid;
    }

    public char[] getNomcol() {
        return nomcol;
    }

    public void setNomcol(char[] nomcol) {
        this.nomcol = nomcol;
    }

    public char getColtipo() {
        return coltipo;
    }

    public void setColtipo(char coltipo) {
        this.coltipo = coltipo;
    }

    public int getColtam() {
        return coltam;
    }

    public void setColtam(int coltam) {
        this.coltam = coltam;
    }

    public int getTabref() {
        return tabref;
    }

    public void setTabref(int tabref) {
        this.tabref = tabref;
    }

    public int getColref() {
        return colref;
    }

    public void setColref(int colref) {
        this.colref = colref;
    }

}
