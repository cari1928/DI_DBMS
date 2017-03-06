package Clases;

import java.io.File;
import java.util.List;

/**
 *
 * @author Tenistas
 */
public class Errores {

    int dslerr;
    BaseDatos objBD;

    public Errores(BaseDatos objBD) {
        dslerr = 0;
        this.objBD = objBD;
    }

    public void chCrearBD(String accion, String nomBD) {
        File subdir = new File("BD\\" + nomBD + ".dbs");

        if (!subdir.exists()) {
            asignarCodigo(accion, "chBdActiva");
        }
    }

    public void chBdActiva(String accion) {
        if (!objBD.status) {
            asignarCodigo(accion, "chBdActiva");
        }
    }

    public int chTablaExiste(String accion, String nomtab) {
        List<Tabla> listTablas = objBD.listTablas;

        for (int i = 0; i < listTablas.size(); i++) {
            Tabla objT = listTablas.get(i);
            if (nomtab.equals(objT.nombtab)) {
                return objT.tabid;
            }
        }

        asignarCodigo(accion, "chTablaExiste");
        return -1;
    }

    public int[] chColumnasExisten(String accion, String[] nomcols, int idtab) {
        boolean flag = false;
        List<Tabla> listTablas = objBD.listTablas;
        int[] idcols = new int[nomcols.length];
        int cont = 0;

        for (int i = 0; i < listTablas.size(); i++) {
            Tabla objT = listTablas.get(i); //agarra una tabla de la bd

            if (objT.tabid == idtab) { //verifica si es la tabla a checar
                List<Columna> listColumnas = objT.listColumnas; //obtiene las columnas de esa tabla

                for (Columna listColumna : listColumnas) { //para recorrer cada columna de la BD    
                    //Columna objC = listColumnas.get(i); //obtiene la primer columna de la BD
                    Columna objC = listColumna;

                    for (String nomcol : nomcols) { //para recorrer cada columna del arreglo
                        if (objC.nomcol.equals(nomcol)) {
                            idcols[cont] = objC.colid;
                            flag = true;
                        }
                    }
                }
            }
        }

        if (!flag) {
            asignarCodigo(accion, "chColumnasExisten");
        }

        return idcols;
    }

    public int chIndiceExiste(String accion, Indice p_objI) {
        List<Tabla> listTablas = objBD.listTablas;
        int indid = -1;

        for (int i = 0; i < listTablas.size(); i++) {
            Tabla objT = listTablas.get(i); //agarra una tabla de la bd

            if (objT.tabid == p_objI.tabid) { //verifica si es la tabla a checar
                List<Indice> listIndices = objBD.listIndices;

                for (int j = 0; j < listIndices.size(); j++) {
                    Indice objI = listIndices.get(j);

                    if (objI.nomind.equals(p_objI.nomind)) {
                        asignarCodigo(accion, "chNombreIndice");
                        return -1;
                    }

                    if (objI.indtipo.equals(p_objI.indtipo)
                            && objI.colid1 == p_objI.colid1
                            && objI.colid2 == p_objI.colid2
                            && objI.colid3 == p_objI.colid3
                            && objI.colid4 == p_objI.colid4) {
                        asignarCodigo(accion, "chIndiceExiste");
                        return -1;
                    }
                    indid = objI.indid; //guardará el último id del indice asignado
                }
                i = listTablas.size(); //para que no recorra las demás tablas
            }
        }
        return indid;
    }

    public boolean chComparaTipoColumnas(String accion, int idtab[], int idcolumn[]) {
        boolean flag = true;
        char tipo[] = new char[2];
        for (int k = 0; k < 2; k++) {
            for (int i = 0; i < objBD.listTablas.size(); i++) {
                if (objBD.listTablas.get(i).tabid == idtab[k]) { //Compara id para encontrar la tabla indicada
                    for (int j = 0; j < objBD.listTablas.get(i).listColumnas.size(); j++) {
                        if (objBD.listTablas.get(i).listColumnas.get(j).colid == idcolumn[k]) {//Compara id para encontrar la columna indicada
                            tipo[k] = objBD.listTablas.get(i).listColumnas.get(j).coltipo;
                        }
                    }
                }
            }
        }
        if (tipo[0] != tipo[1]) {
            flag = false;
        }
        asignarCodigo(accion, "chComparaTipoColumnas");
        return flag;
    }

    public void asignarCodigo(String accion, String metodo) {
        switch (accion) {
            case "crearBD":
                switch (metodo) {
                    case "chCrearBD":
                        dslerr = 100;
                        break;
                }
                break;

            case "crearTabla":
                switch (metodo) {
                    case "chBdActiva":
                        dslerr = 288;//numero inventado
                        break;
                }
                break;

            case "crearIndice":
                switch (metodo) {
                    case "chBdActiva":
                        dslerr = 300;
                        break;
                    case "chTablaExiste":
                        dslerr = 301;
                        break;
                    case "chColumnasExisten":
                        dslerr = 302;
                        break;
                    case "chNombreIndice":
                        dslerr = 303;
                        break;
                    case "chIndiceExiste":
                        dslerr = 304;
                        break;
                }
                break;

            case "crearReferencia":
                switch (metodo) {
                    case "chBdActiva":
                        dslerr = 288;//numero inventado
                        break;
                }
                break;

            case "usarBD":
                switch (metodo) {
                    case "chBdActiva":
                        dslerr = 288;//numero inventado
                        break;
                }
                break;

            case "insert":
                switch (metodo) {
                    case "chBdActiva":
                        dslerr = 288;//numero inventado
                        break;
                }
                break;

            case "update":
                switch (metodo) {
                    case "chBdActiva":
                        dslerr = 288;//numero inventado
                        break;
                }
                break;

            case "delete":
                switch (metodo) {
                    case "chBdActiva":
                        dslerr = 288;//numero inventado
                        break;
                }
                break;
        }

        System.out.println("ERROR: " + dslerr);
    }

}
