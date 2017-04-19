package SGBD;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tenistas
 */
public class Tabla {

    private List<Columna> listColumnas;
    private List<Indice> listIndices;
    private List<String> columnas;
    private String nombtab; //nombre
    private String archivo;
    private int tabid; //id
    private int tamreng;
    private int ncols;
    private double nrengs;
    private int nindex;
    
    private List<Registro> listRegistro = new ArrayList<>();

    public Tabla() {
        columnas = new ArrayList<>();
    }

    public List<Registro> getListRegistro() {
        return listRegistro;
    }

    public void setListRegistro(List<Registro> listRegistro) {
        this.listRegistro = listRegistro;
    }
    
    

    public List<Columna> getListColumnas() {
        return listColumnas;
    }

    public void setListColumnas(List<Columna> listColumnas) {
        this.listColumnas = listColumnas;
    }

    public List<Indice> getListIndices() {
        return listIndices;
    }

    public void setListIndices(List<Indice> listIndices) {
        this.listIndices = listIndices;
    }

    public List<String> getColumnas() {
        return columnas;
    }

    public void setColumnas(List<String> columnas) {
        this.columnas = columnas;
    }

    public String getNombtab() {
        return nombtab;
    }

    public void setNombtab(String nombtab) {
        this.nombtab = nombtab;
    }

    public String getArchivo() {
        return archivo;
    }

    public void setArchivo(String archivo) {
        this.archivo = archivo;
    }

    public int getTabid() {
        return tabid;
    }

    public void setTabid(int tabid) {
        this.tabid = tabid;
    }

    public int getTamreng() {
        return tamreng;
    }

    public void setTamreng(int tamreng) {
        this.tamreng = tamreng;
    }

    public int getNcols() {
        return ncols;
    }

    public void setNcols(int ncols) {
        this.ncols = ncols;
    }

    public double getNrengs() {
        return nrengs;
    }

    public void setNrengs(double nrengs) {
        this.nrengs = nrengs;
    }

    public int getNindex() {
        return nindex;
    }

    public void setNindex(int nindex) {
        this.nindex = nindex;
    }
}
