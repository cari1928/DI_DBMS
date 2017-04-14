package SGBD;

import java.io.File;
import java.io.IOException;
import java.util.List;
import Archivos.GestionArchivos;

/**
 *
 * @author Tenistas
 */
public class Errores {

    private int dslerr;
    private final BaseDatos objBD;
    private final GestionArchivos objG;
    private String RUTABD;

    public Errores(BaseDatos objBD, GestionArchivos objG) {
        this.objBD = objBD;
        this.objG = objG;
        dslerr = 0;
    }

    public int getDslerr() {
        return dslerr;
    }

    public void setDslerr(int dslerr) {
        this.dslerr = dslerr;
    }

    public String getRUTABD() {
        return RUTABD;
    }

    public void setRUTABD(String RUTABD) {
        this.RUTABD = RUTABD;
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

    public boolean chTablaDifusa(int idtabla) {
        try {
            String tabla = objG.obtenerRegistroByID(RUTABD + "tablas", idtabla);
            String parts[] = tabla.split(" ");
            if (!parts[(parts.length - 2)].equals("f")) {
                return false;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return true;
    }

    public boolean chColumnaDifusa(int idtabla) {
        try {
            String columna = objG.obtenerRegistroByID(RUTABD + "columnas", idtabla);
            String parts[] = columna.split(" ");
            if (!parts[7].trim().equals("f")) {
                return false;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return true;
    }

    public boolean chVariableLinguistica(String archivo, String etiqueta) {
        String parts[];
        try {
            List<String> etiquetas = objG.leer("SED\\" + archivo);
            for (int i = 0; i < etiquetas.size(); i++) {
                parts = etiquetas.get(i).split(" ");
                if (parts[3].equals(etiqueta)) {
                    return true;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void chBdActiva(String accion) {
        if (objBD.getNombre() == null) {
            asignarCodigo(accion, "chBdActiva");
        }
    }

    //return id de la tabla
    public int chTablaExiste(String accion, String nomtab) {
        List<String> list;
        String[] parts;
        File archivo = new File(RUTABD + nomtab + ".dat");
        if (!archivo.exists()) {
            if (!accion.equals("crearTabla")) {
                asignarCodigo(accion, "chTablaExiste");
                return -1;
            }
        }

        try {
            String prueba = RUTABD + "tablas";
            list = objG.leer(prueba);
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
            list = objG.leer(RUTABD + "columnas");
            for (int i = 0; i < list.size(); i++) {
                parts = list.get(i).split(" "); //8 campos
                if (Integer.parseInt(parts[2]) == idtab) { //busca el id de la tabla en cuestión
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
                if (Integer.parseInt(parts[1]) == objI.getTabid()) {
                    if (parts[0].equals(objI.getNomind())) {
                        asignarCodigo(accion, "chNombreIndice");
                        return false;
                    }
                    if (Integer.parseInt(parts[2]) == objI.getColsid()[0]
                            && Integer.parseInt(parts[3]) == objI.getColsid()[1]
                            && Integer.parseInt(parts[4]) == objI.getColsid()[2]
                            && Integer.parseInt(parts[5]) == objI.getColsid()[3]
                            && parts[7].equals(objI.getIndtipo())) {
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

            for (int i = 0; i < objBD.getListTablas().size(); i++) {

                if (objBD.getListTablas().get(i).tabid == idtab[k]) { //Compara id para encontrar la tabla indicada

                    for (int j = 0; j < objBD.getListTablas().get(i).listColumnas.size(); j++) {

                        if (objBD.getListTablas().get(i).listColumnas.get(j).getColid() == idcolumn[k]) {//Compara id para encontrar la columna indicada
                            tipo[k] = objBD.getListTablas().get(i).listColumnas.get(j).getColtipo();
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

    public boolean chComparaTipoColumnas(String accion, int tabid, int colid, String colvalue) {
        List<String> list;
        String[] parts;

        try {
            list = objG.leer(RUTABD + "columnas");
            for (String list1 : list) {
                parts = list1.split(" ");

                //checa que sea la tabla 
                if (Integer.parseInt(parts[2]) == tabid) {

                    //checa que sea la columna
                    if (Integer.parseInt(parts[3]) == colid) {

                        if (parts[0].contains("char")) {
                            //checa que tenga comillas
                            if (colvalue.charAt(0) != '\'' && colvalue.charAt(colvalue.length()) != '\'') {
                                asignarCodigo(accion, "chComparaTipoColumnas");
                                return false;
                            }

                        } else if (parts[0].contains("int")) {
                            try {
                                Integer.parseInt(colvalue);
                            } catch (Exception e) {
                                asignarCodigo(accion, "chComparaTipoColumnas");
                                return false;
                            }

                        } else if (parts[0].equals("float")) {
                            //quizá no es necesario checar si tiene comillas
                            if (!colvalue.contains("f")) { //checa si tiene una f
                                asignarCodigo(accion, "chComparaTipoColumnas");
                                return false;
                            }
                            try {
                                Float.parseFloat(colvalue); //comprueba que sea flotante
                            } catch (Exception e) {
                                asignarCodigo(accion, "chComparaTipoColumnas");
                                return false;
                            }

                        } else if (parts[0].equals("double")) {
                            try {
                                Double.parseDouble(parts[0]); //comprueba que sea double
                            } catch (Exception e) {
                                asignarCodigo(accion, "chComparaTipoColumnas");
                                return false;
                            }
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("ERROR: CHCOMPARATIPOCOLUMNAS");
            return false;
        }
    }

    public void chTipoIndices(String accion, String[] columnas, int tabid) {
        //columnas para checar si hay algun indice en la tabla y si hay, ver qué columas tiene
        //Después checar los tipos de cada columna

        Tabla objT = obtenerTabla(tabid);
        List<Indice> listIndices = objT.listIndices;

        for (int i = 0; i < listIndices.size(); i++) {
            //Indice objI = listIndices.
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
                        dslerr = 600;//numero inventado
                        break;
                    case "chTablaExiste":
                        dslerr = 601;
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
                    case "???":
                        dslerr = 704; //???
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
        List<Tabla> listTablas = objBD.getListTablas();

        for (int i = 0; i < listTablas.size(); i++) {
            Tabla objT = listTablas.get(i);

            if (objT.tabid == tabid) {
                return objT;
            }
        }
        return null;
    }
}
