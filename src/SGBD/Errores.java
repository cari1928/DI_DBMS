package SGBD;

import java.io.File;
import java.io.IOException;
import java.util.List;
import GestionSistema.GestionArchivos;

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
            if (!parts[(parts.length - 1)].trim().equals("f")) {
                return false;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return true;
    }

    public boolean chColumnaDifusa(int colid) {
        try {
            String columna = objG.obtenerRegistroByID(RUTABD + "columnas", (colid - 1));
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
            List<String> etiquetas = objG.leer(RUTABD + "SED\\" + archivo);
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

    public String chComparaTipoColumnas(String accion, int idcolumn[]) {
        
        String columna1 = "", columna2 = "";
        try {
            columna1 = objG.obtenerRegistroByID(RUTABD + "columnas", (idcolumn[0] -1));
            columna2 = objG.obtenerRegistroByID(RUTABD + "columnas", (idcolumn[1] -1));
        } catch (Exception e) {
            System.out.println("Error en los tipos de columnas");
        }
        
        if(!columna1.split(" ")[0].trim().equals(columna2.split(" ")[0].trim())){
            asignarCodigo(accion, "chComparaTipoColumnas");
            return "";
        }
        
        return columna1;
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

                        } else if (parts[0].contains("integer")) {
                            if (colvalue.contains("<")) {
                                return true;
                            }
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
                                Double.parseDouble(colvalue); //comprueba que sea double
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
        List<Indice> listIndices = objT.getListIndices();

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
                        dslerr = 800;
                        break;
                    case "chTablaExiste":
                        dslerr = 801;
                        break;
                }
                break;

            case "select":
                switch (metodo) {
                    case "chBdActiva":
                        dslerr = 900;
                        break;

                    case "chTablaExiste":
                        dslerr = 901;
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

            if (objT.getTabid() == tabid) {
                return objT;
            }
        }
        return null;
    }

    public boolean chCondDet(String condicion) {
        String parts[] = condicion.split(" ");
        Tabla objT = new Tabla();
        double prueba;
        Columna objC = new Columna();
        if (!condicion.contains(" ")) {
            return false;
        }

        objT.setTabid(chTablaExiste("select", parts[0].split("\\.")[0]));
        if (getDslerr() != 0) {
            System.out.println("Error cerca de la condicion where, la tabla no existe");
            return false;
        }

        objC.setColid(chColumnasExisten("select", parts[0].split("\\.")[1], objT.getTabid()));
        if (getDslerr() != 0) {
            System.out.println("Error cerca de la condicion where la columna no existe");
            return false;
        }

        switch (parts[1]) {
            case "=":
                switch (parts[2].charAt(0)) {
                    case '\'':

                        if (parts[2].charAt((parts[2].length() - 1)) != '\'' || parts[2].length() - 1 == 0) {
                            System.out.println("Error cerca de la condición where, sintaxis");
                            return false;
                        }
                        try {
                            String parts2[] = objG.obtenerRegistroByID("BD\\" + objBD.getNombre() + "\\columnas", objC.getColid()).split(" ");
                            if (parts2[0].trim().equals("integer") || parts2[0].trim().equals("double") || parts2[0].trim().equals("float")) {
                                System.out.println("Error cerca de la condicion where, la columna no es de tipo char");
                                return false;
                            }
                        } catch (Exception e) {
                        }
                        break;

                    default:
                        try {
                            prueba = Double.parseDouble(parts[2]);
                        } catch (Exception e) {
                            System.out.println("Falta mensaje de error ");
                            return false;
                        }
                }
                break;
            case ">":
            case "<":
            case ">=":
            case "<=":
                try {
                    String parts2[] = objG.obtenerRegistroByID("BD\\" + objBD.getNombre() + ".dbs\\columnas", (objC.getColid() - 1)).split(" ");
                    if (!parts2[0].trim().equals("integer") && !parts2[0].trim().equals("double") && !parts2[0].trim().equals("float")) {
                        return false;
                    }
                    prueba = Double.parseDouble(parts[2]);
                } catch (Exception e) {
                    System.out.println("Error cerca de la condición where, el dato no es numerico y/o la columna no es de tipo numerico");
                    return false;
                }
                break;
            default:
                System.out.println("Error cerca de la condición where, sintaxis");
                return false;
        }

        return true;
    }

    /**
     * Checa los elementos necesarios para que una condición difusa sea
     * sintácticamente correcta.
     *
     * @param condicion persona.edad FEQ $joven --> ejemplo
     * @return true-sintaxis correcta
     */
    //ejemplo de condición
    //columna FEQ $Joven
    //columna FEQ $Joven THOLD 0.6
    //columna FGEQ #20 
    //columna FGEQ #20 THOLD 0.2
    public boolean chCondDifusa(String condicion) {
        /*
        //ejemplo de condición
        //columna FEQ $Joven
        //columna FEQ $Joven THOLD 0.6
        //columna FGEQ #20 
        //columna FGEQ #20 THOLD 0.2
        Elementos a verificar:
        1 En total deben ser entre 3 y 5 caracteres, ni más ni menos 
        2 Sintaxis para cada columna de la forma: tabla.columna 
        3 Existencia de la tabla 
        4 La columna pertenece a esa tabla
        5 La columna es difusa
        6 Contiene alguna palabra reservada: {FEQ, FGEQ, FLEQ}
        7 Contiene un $ o un #, $ seguido de letras y # seguido de números
        8 Verifica que la etiqueta linguistica esté disponible para esa tabla.columna
        9 Contiene la palabra reservada THOLD y seguido de él un número flotante
         */

        int tabid, colid;
        String[] parts = condicion.split(" "), parts2;

        //NO TIENE LA ESTRUCTURA BÁSICA
        if (parts.length != 3 && parts.length != 5) {
            return false; //la estructura de la condición es incorrecta
        }

        if (!parts[0].contains(".")) { //verifica que se cumpla la estructura tabla.columna
            return false;
        }

        tabid = chTablaExiste("condicionDifusa", parts[0].split("\\.")[0]);  //verifica si la tabla existe y regresa el id de la tabla
        if (dslerr != 0) {
            return false;
        }

        colid = chColumnasExisten("condicionDifusa", parts[0].split("\\.")[1], tabid);
        if (dslerr != 0) {
            return false;
        }

        if (!chColumnaDifusa(colid)) { //verifica que la columna sea difusa
            return false; //no es difusa
        }

        //NO TIENE LAS PALABRAS RESERVADAS
        if (!parts[1].equals("fgeq") // >=
                && !parts[1].equals("fleq")) {//<=
            //&& !parts[1].equals("FEQ")) {//==
            return false;
        }

        //NO TIENE # O $
        if (!parts[2].contains("#") && !parts[2].contains("$")) {
            return false;
        }

        //verifica que, si tiene #, éste venga acompañado de un número
        try {
            if (parts[2].contains("#")) {
                parts2 = parts[2].split("\\#");
                Double.parseDouble(parts2[1]); //solo es para comprobar
            } else {
                //verificar que la etiqueta sea válida para la tabla seleccionada
                parts2 = parts[2].split("\\$");
                if (!chVariableLinguistica(parts[0], parts2[1])) {
                    return false;
                }
            }
        } catch (Exception e) {
            return false; //no es número
        }

        //NO TIENE LA PALABRA RESERVADA THOLD
        if (parts.length == 5) {
            if (!parts[3].equalsIgnoreCase("THOLD")) {
                return false;
            }

            //verifica que los grados de membresía tengan un valor numérico
            try {
                Double.parseDouble(parts[4]); //solo es para comprobar
            } catch (Exception e) {
                return false;
            }
        }

        return true;
    }

}
