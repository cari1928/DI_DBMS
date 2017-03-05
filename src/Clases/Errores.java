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

    public void chSubdir(String accion, String nomBD) {
        File subdir = new File("BD\\" + nomBD + ".dbs");

        if (!subdir.exists()) {

            switch (accion) {
                case "crearDB":
                    dslerr = 100;
                    System.out.println("ERROR: " + dslerr);
                    break;
            }
        }
    }

    public void chBdActiva(String accion) {
        if (objBD.status == 0) {

            switch (accion) {
                case "crearIndice":
                    dslerr = 300;
                    System.out.println("ERROR " + dslerr);
                    break;
            }

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

        switch (accion) {
            case "crearIndice":
                dslerr = 301;
                System.out.println("ERROR " + dslerr);
                break;
        }

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

            switch (accion) {
                case "crearIndice":
                    dslerr = 302;
                    System.out.println("ERROR " + dslerr);
                    break;
            }

        }

        return idcols;
    }

    public int[] chIndicesExisten(String accion, String[] nomcols, int idtab) {
        boolean flag = false;
        List<Tabla> listTablas = objBD.listTablas;
        int[] idcols = new int[4];
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

            switch (accion) {
                case "crearIndice":
                    dslerr = 302;
                    System.out.println("ERROR " + dslerr);
                    break;
            }

        }

        return idcols;
    }

    public int chIndiceExiste(Indice p_objI) {
        List<Tabla> listTablas = objBD.listTablas;
        int indid = -1;

        for (int i = 0; i < listTablas.size(); i++) {
            Tabla objT = listTablas.get(i); //agarra una tabla de la bd

            if (objT.tabid == p_objI.tabid) { //verifica si es la tabla a checar
                List<Indice> listIndices = objBD.listIndices;

                for (int j = 0; j < listIndices.size(); j++) {
                    Indice objI = listIndices.get(j);

                    if (objI.nomind.equals(p_objI.nomind)) {
                        dslerr = 303;
                        System.out.println("ERROR " + dslerr);
                        return -1;
                    }

                    if (objI.indtipo.equals(p_objI.indtipo)
                            && objI.colid1 == p_objI.colid1
                            && objI.colid2 == p_objI.colid2
                            && objI.colid3 == p_objI.colid3
                            && objI.colid4 == p_objI.colid4) {
                        dslerr = 304;
                        System.out.println("ERROR " + dslerr);
                        return -1;
                    }

                    indid = objI.indid; //guardará el último id del indice asignado
                }

                i = listTablas.size(); //para que no recorra las demás tablas
            }
        }

        return indid;
    }

}
