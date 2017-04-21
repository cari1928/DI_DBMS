/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SGBD;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jorge
 */
public class resulWhere {

    private int posicion;
    private List<String[]> gradosPertenencia = new ArrayList<>();

    public int getPosicion() {
        return posicion;
    }

    public void setPosicion(int posicion) {
        this.posicion = posicion;
    }

    public List<String[]> getGradosPertenencia() {
        return gradosPertenencia;
    }

    public void setGradosPertenencia(List<String[]> gradosPertenencia) {
        this.gradosPertenencia = gradosPertenencia;
    }

    String[] prepararGrados(String cond1, String[] estrucruta1) {
        boolean tipo; // T: determinista, F: Difusa
        String[] grados;
        //Verificar que tipo de condicion1 es
        switch (cond1.split(" ")[1].charAt(0)) {
            case '\'':
            case '=':
            case '<':
            case '>':   //Es dete..
                tipo = true;
                break;
            default:    //Es difu
                tipo = false;
        }
        if (tipo) {
            return null;
        } else {
            grados = new String[2];
            grados[1] = estrucruta1[1];
            grados[0] = cond1.split(" ")[0];
            return grados;
        }
    }
}
