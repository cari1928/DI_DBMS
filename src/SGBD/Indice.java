/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SGBD;

/**
 *
 * @author Macias
 */
public class Indice {

    private int tabid;
    private int indid;
    private String nomind;
    private char indtipo;
    private int[] colsid;
    private int colid1;
    private int colid2;
    private int colid3;
    private int colid4;

    public int getTabid() {
        return tabid;
    }

    public void setTabid(int tabid) {
        this.tabid = tabid;
    }

    public int getIndid() {
        return indid;
    }

    public void setIndid(int indid) {
        this.indid = indid;
    }

    public String getNomind() {
        return nomind;
    }

    public void setNomind(String nomind) {
        this.nomind = nomind;
    }

    public char getIndtipo() {
        return indtipo;
    }

    public void setIndtipo(char indtipo) {
        this.indtipo = indtipo;
    }

    public int[] getColsid() {
        return colsid;
    }

    public void setColsid(int[] colsid) {
        this.colsid = colsid;
    }

    public int getColid1() {
        return colid1;
    }

    public void setColid1(int colid1) {
        this.colid1 = colid1;
    }

    public int getColid2() {
        return colid2;
    }

    public void setColid2(int colid2) {
        this.colid2 = colid2;
    }

    public int getColid3() {
        return colid3;
    }

    public void setColid3(int colid3) {
        this.colid3 = colid3;
    }

    public int getColid4() {
        return colid4;
    }

    public void setColid4(int colid4) {
        this.colid4 = colid4;
    }

}
