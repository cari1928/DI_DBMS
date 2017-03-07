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
    GestionArchivos objG;

    public Errores(BaseDatos objBD, GestionArchivos objG) {
        dslerr = 0;
        this.objBD = objBD;
        this.objG = objG;
    }

    public void chCrearBD(String accion, String nomBD) {
        File subdir = new File("BD\\" + nomBD + ".dbs");

        if (subdir.exists()) {
            asignarCodigo(accion, "chCrearBD");
        }
    }

    public void chExisteBD(String accion, String nomBD) {
        File subdir = new File("BD\\" + nomBD + ".dbs");

        if (!subdir.exists()) {
            asignarCodigo(accion, "chExisteBD");
        }
    }

    public void chBdActiva(String accion) {
        if (objBD.nombre == null) {
            asignarCodigo(accion, "chBdActiva");
        }
    }

    //return id de la tabla
    public int chTablaExiste(String accion, String nomtab) {
        List<String> list = null;
        String[] parts;
        File archivo = new File("BD\\" + objBD.nombre + ".dbs\\" + nomtab + ".dat");
        if (!archivo.exists()) {
            if (!accion.equals("crearTabla")) {
                asignarCodigo(accion, "chTablaExiste");
                return -1;
            }
        }

        try {
            list = objG.leer("tablas");
            for (int i = 0; i < list.size(); i++) {
                parts = list.get(i).split(" "); //8 campos
                if (parts[0].equals(nomtab)) {
                    return Integer.parseInt(parts[1]);
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR LISTA TABLAS CHTABLAEXISTE");
        }
        return -1;
    }

    public int chColumnasExisten(String accion, String nomcols, int idtab) {
        List<String> list;
        String[] parts;
        try {
            list = objG.leer("columnas");
            for (int i = 0; i < list.size(); i++) {
                parts = list.get(i).split(" "); //7 campos
                if (Integer.parseInt(parts[2]) == idtab) {
                    if (parts[4].equals(nomcols)) {
                        return Integer.parseInt(parts[3]);
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("ERROR: CHCOLUMNASEXISTEN");
        }

        asignarCodigo(accion, "chColumnasExisten");
        return -1;
    }

    public boolean chIndiceExiste(String accion, Indice objI) {
        List<String> list;
        String[] parts;
        try {
            list = objG.leer("indices");

            for (int i = 0; i < list.size(); i++) {
                parts = list.get(i).split(" ");
                if (Integer.parseInt(parts[1]) == objI.tabid) {
                    if (parts[0].equals(objI.nomind)) {
                        asignarCodigo(accion, "chNombreIndice");
                        return false;
                    }
                    if (Integer.parseInt(parts[2]) == objI.colsid[0]
                            && Integer.parseInt(parts[3]) == objI.colsid[1]
                            && Integer.parseInt(parts[4]) == objI.colsid[2]
                            && Integer.parseInt(parts[5]) == objI.colsid[3]
                            && parts[7].equals(objI.indtipo)) {
                        asignarCodigo(accion, "chIndiceExiste");
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: CHINDICEEXISTE");
            return false;
        }
        return true;
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

    public void chComparaTipoColumnas(String accion, String[] columnas, int tabid) {
        String[] parts;
        Tabla objT = obtenerTabla(tabid);
        List<Columna> listColumnas = objT.listColumnas;

        for (int j = 0; j < listColumnas.size(); j++) {
            Columna objC = listColumnas.get(j);

            for (String columna : columnas) {
                if (objC.nomcol.equals(columna)) { //compara los nombres de las columnas
                    parts = columna.split("="); //part0 = nombrecol, part1 = valcol
                    char tipoDato = chTipoDato(parts[1]);

                    if (objC.coltipo != tipoDato) {
                        asignarCodigo(accion, "chComparaTipoColumnas");
                    }
                }
            }
        }
    }

    public void chTipoIndices(String accion, String[] columnas, int tabid) {
        //columnas para checar si hay algun indice en la tabla y si hay, ver qué columas tiene
        //Después checar los tipos de cada columna

        Tabla objT = obtenerTabla(tabid);
        List<Indice> listIndices = objT.listIndices;

        for (int i = 0; i < listIndices.size(); i++) {
            Indice objI = listIndices.
        }
    }

    public char chTipoDato(String valColumnas) {
        try {
            Integer.parseInt(valColumnas);
            return 'i'; //int
        } catch (NumberFormatException e) {
        }
        try {
            Float.parseFloat(valColumnas);
            return 'f'; //float
        } catch (NumberFormatException e) {
        }
        try {
            Double.parseDouble(valColumnas);
            return 'd'; //double
        } catch (NumberFormatException e) {
            return 'c'; //char
        }
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
                        dslerr = 200;
                        break;
                    case "chTablaExiste":
                        dslerr = 201;
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
                    case "chExisteBD":
                        dslerr = 500;
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
                        dslerr = 700;
                        break;
                    case "chTablaExiste":
                        dslerr = 701;
                        break;
                    case "chColumnasExisten":
                        dslerr = 702;
                        break;
                    case "chComparaTipoColumnas":
                        dslerr = 703;
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

        System.out.println("ERROR: " + dslerr + " " + accion + " " + metodo);
    }

    public Tabla obtenerTabla(int tabid) {
        List<Tabla> listTablas = objBD.listTablas;

        for (int i = 0; i < listTablas.size(); i++) {
            Tabla objT = listTablas.get(i);

            if (objT.tabid == tabid) {
                return objT;
            }
        }
        return null;
    }

    public int[] obtenerColumnasID(int tabid, String[] columnas) {
        Tabla objT = obtenerTabla(tabid);
        List<Columna> listColumnas = objT.listColumnas;
        int[] ids = new int[columnas.length];

        for (int i = 0; i < listColumnas.size(); i++) {
            Columna objC = listColumnas.get(i);

            for (String columna : columnas) {
                if (objC.nomcol.equals(columna)) {
                }
            }
        }
    }

}
