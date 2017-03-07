package Clases;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author Tenistas
 */
public class Automatas {

    BaseDatos objBD;
    Errores error;
    GestionArchivos objG;
    boolean resultado;
    String query;

    public Automatas() {
        objBD = new BaseDatos();
        objG = new GestionArchivos(objBD);
        error = new Errores(objBD, objG);
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public boolean iniAutomatas() {

        if (chCreaBD()) {
            return true;
        } else if (chUsarBD()) {
            return true;
        } else if (chCrearTabla()) {
            return true;
        } else if (chCrearIndice()) {
            return true;
        } else if (chCrearReferencia()) {
            return true;
        } else if (chInsertInto()) {
            return true;
        } else if (chSelect()) {
            return true;
        } //agregar los que hagan falta

        return false; //paso por todos los autómatas y aún así llegó hasta este punto
    }

    public boolean chCreaBD() {
        query = query.toLowerCase();
        int res = query.indexOf("create database ");
        if (res == -1) {
            return false;
        }

        //obtener nombre de la BD
        String[] parts = query.split("database ");
        String nombd = parts[1];

        error.chCrearBD("crearBD", nombd);
        if (error.dslerr != 0) {
            return false;
        }

        objG.crearDirectorio("BD\\" + nombd + ".dbs"); //no se que parametros vaya a llevar
        return true;
    }

    public boolean chUsarBD() {
        String[] parts;

        query = query.toLowerCase();
        int res = query.indexOf("use database ");
        if (res == -1) {
            return false;
        }
        parts = query.split("database ");
        String nombbd = parts[1];

        error.chExisteBD("usarBD", nombbd);
        if (error.dslerr != 0) {
            return false;
        }

        objBD.nombre = nombbd; //no es necesario crear la estructura de datos
        System.out.println("USANDO " + objBD.nombre);
        return true;
    }

    public boolean chCrearTabla() {
        String[] parts, parts2, parts3;
        String registro, nombtab;
        int n;

        error.chBdActiva("crearTabla");
        if (error.dslerr != 0) {
            return false;
        }

        query = query.toLowerCase();
        int res = query.indexOf("create table ");
        if (res == -1) {
            return false;
        }

        parts = query.split("create table ");
        String par = "\\(";
        parts = parts[1].split(" " + par + " ");
        registro = parts[0]; //guarda nombtab

        if (registro.length() > 8) {
            char partecitas[] = getChars(registro, 8);
            registro = "";
            for (int i = 0; i < partecitas.length; i++) {
                registro += partecitas[i]; //nombre cortado en 8
            }
        }
        nombtab = registro;
        registro += " ";

        error.chTablaExiste("crearTabla", registro);
        if (error.dslerr != 0) {
            return false;
        }

        try {
            n = objG.contarRengs("tablas");
            registro += n + 501 + " ";
            registro += nombtab + ".dat ";
            registro += 150 + " ";

            parts = parts[1].split(" \\)");
            parts = parts[0].split(", ");
            registro += parts.length + " ";
            registro += 0 + " ";
            registro += 0 + " ";
            objG.escribir(n + 1, "tablas", registro, "final");

            //columnas
            registro = "";
            for (int i = 0; i < parts.length; i++) {
                parts2 = parts[i].split(":"); //pegados
                registro += parts2[1] + " "; //coltipo

                //obtener tamaño
                if (parts2[1].contains("char")) {
                    parts3 = parts2[1].split("\\[");
//                    parts3 = parts2[0].split("]"); //no sería mas bien con parts3???
                    parts3 = parts3[1].split("]");
                    registro += parts3[0] + " "; //coltam
                } else {
                    registro += 0 + " ";
                }
                registro += n + 501 + " "; //tabid
                registro += (objG.contarRengs("columnas") + 1) + " "; //colid
                if (parts2[0].length() > 10) {
                    char partecitas[] = getChars(parts2[0], 10);
                    for (int j = 0; j < partecitas.length; j++) {
                        registro += partecitas[i]; //nombre cortado en 8
                    }
                } else {
                    registro += parts2[0] + " ";
                }
                registro += "-1 ";
                registro += "-1 ";
                objG.escribir(objG.contarRengs("columnas") + 1, "columnas", registro, "final");
            }

            objG.crearArchivo("BD\\" + objBD.nombre + ".dbs\\" + nombtab + ".dat");
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }

    }

    public char[] getChars(String cadena, int tamaño) {
        char[] tmp = new char[tamaño];
        for (int i = 0; i < tmp.length; i++) {
            if (i >= cadena.length()) {
                tmp[i] = ' ';
            } else {
                tmp[i] = cadena.charAt(i);
            }
        }
        return tmp;
    }

    public boolean chCrearIndice() {
        char indtipo = '0';
        String registro;
        int indid, idcols;
        int[] colsid;
        List<String> list;
        Indice objI = new Indice();
        boolean flag = true;

        //checa si la base de datos está activa
        error.chBdActiva("crearIndice");
        if (error.dslerr != 0) {
            return false;
        }

        //posibles casos
        //String query = "CREATE UNIQUE ASC INDEX iNomb ON tNomb(col1, col2, col3)";
        //String query = "CREATE UNIQUE DESC INDEX iNomb ON tNomb(col1)";
        //String query = "CREATE ASC INDEX iNomb ON tNomb(col1)";
        //String query = "CREATE DESC INDEX iNomb ON tNomb(col1)";
        //String query = "CREATE UNIQUE INDEX iNomb ON tNomb(col1)";
        //String query = "CREATE INDEX iNomb ON tNomb(col1)";
        query = query.toLowerCase();
        int casos = 1, res = 0;
        while (flag) {

            switch (casos) {
                case 1:
                    res = query.indexOf("create unique asc index ");
                    indtipo = 'U';
                    break;

                case 2:
                    res = query.indexOf("create unique desc index ");
                    indtipo = 'U';
                    break;

                case 3:
                    res = query.indexOf("create asc index ");
                    indtipo = 'D';
                    break;

                case 4:
                    res = query.indexOf("create desc index ");
                    indtipo = 'D';
                    break;

                case 5:
                    res = query.indexOf("create unique index ");
                    indtipo = 'U';
                    break;

                case 6:
                    res = query.indexOf("create index ");
                    indtipo = 'D';
                    break;
            }

            if (res == -1) { //si no encontró esas sentencias
                ++casos; //pasa al siguente caso
            } else {
                flag = false; //sintaxis del query incorrecta
            }

            if (casos == 7) { //ya terminó todos los casos
                return false; //no fue ninguno de los casos
            }
        }

        //obtener nombre del indice
        String[] parts = query.split("index ");
        res = parts[1].indexOf(" on "); //checa que exista: on
        if (res == -1) {
            return false;
        }
        parts = parts[1].split(" on ");
        //nomind = parts[0];
        objI.nomind = parts[0];
        registro = objI.nomind + " ";

        //obtener nombre de la tabla y verifica si existe en la BD
        parts = parts[1].split(" \\( ");
        objI.tabid = error.chTablaExiste("crearIndice", parts[0]);
        if (error.dslerr != 0) {
            return false;
        }
        registro += objI.tabid + " ";

        //obtener columnas y verificar si existen en la tabla
        parts = parts[1].split(" \\)");
        String[] nomcols = parts[0].split(", ");
        if (nomcols.length > 4) { //deben ser 4 columnas por índice
            return false;
        }

        colsid = new int[4];
        for (int i = 0; i < 4; i++) {
            if (i < nomcols.length) {
                idcols = error.chColumnasExisten("crearIndice", nomcols[i], objI.tabid);
                if (error.dslerr != 0) {
                    return false;
                }
                colsid[i] = idcols;
                registro += idcols + " ";
            } else {
                colsid[i] = -1;
                registro += -1 + " ";
            }
        }
        objI.colsid = colsid;

        //obtener el indice id
        indid = getMayorIndiceId(objI.tabid) + 1;
        registro += indid + " ";
        objI.indid = indid;
        registro += indtipo + " ";
        objI.indtipo = indtipo;

        //checar si ya existe un indice con ese nombre
        flag = error.chIndiceExiste("chCrearIndice", objI);
        if (error.dslerr != 0) {
            return false;
        }

        try {
            //escribir en indices
            indid = objG.contarRengs("indices");
            objG.escribir(++indid, "indices", registro, "final");

            //actualizar tablas
            list = objG.leer("tablas");
            for (int i = 0; i < list.size(); i++) {
                parts = list.get(i).split(" ");
                if (Integer.parseInt(parts[1]) == objI.tabid) {
                    parts[6] = (Integer.parseInt(parts[6]) + 1) + "";
                    registro = "";
                    for (String part : parts) {
                        registro += part + " ";
                    }
                    list.set(i, registro);
                    i = list.size(); //para que no recorra lo demás
                }
            }

            for (int i = 0; i < list.size(); i++) {
                if (i == 0) {
                    objG.escribir(i, "tablas", list.get(i), "nuevo");
                } else {
                    objG.escribir(i, "tablas", list.get(i), "final");
                }
            }

            objG.crearArchivo("BD\\" + objBD.nombre + ".dbs\\" + objI.nomind + ".ix" + objI.indid);

        } catch (Exception e) {
            System.out.println("ERROR: ESCRIBIRINIDCE");
        }

        return true;
    }

    public int getMayorIndiceId(int tabid) {
        String[] parts;
        List<String> list;
        int mayor = 0;
        try {
            list = objG.leer("indices");
            for (int i = 0; i < list.size(); i++) {

                parts = list.get(i).split(" ");
                if (Integer.parseInt(parts[1]) == tabid) {

                    if (mayor < Integer.parseInt(parts[6])) {
                        mayor = Integer.parseInt(parts[6]);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: GETMAYORID");
        }
        return mayor;
    }

    public boolean chCrearReferencia() {
        error.chBdActiva("chCrearReferencia");
        if (error.dslerr != 0) {
            return false;
        }

        query = query.toLowerCase();
        //VERIFICAR QUE TENGA CREATE REFERENCE
        int res = query.indexOf("create reference");
        if (res == -1) {
            return false;
        }

        //separar las tablas de las columnas
        String parts[] = query.split("create reference");
        parts = parts[0].split(" ");
        String parts2[];
        int idtab[] = new int[2];
        int idcolumn[] = new int[2];
        String columns[] = new String[2];
        //valida existencia de tablas y columnas
        for (int i = 0; i < 2; i++) {
            parts2 = parts[i].split(".");
            if (parts2.length != 2) {
                return false;
            }
            idtab[i] = error.chTablaExiste("chCrearReferencia", parts2[0]);
            if (idtab[i] != 0) {
                return false;
            }
            columns[i] = parts2[1];
            idcolumn[i] = error.chColumnasExisten("chCrearReferencia", columns, idtab[i])[0];
            if (idcolumn[i] != 0) {
                return false;
            }
        }
        if (!error.chComparaTipoColumnas("chCrearReferencia", idtab, idcolumn)) {
            return false;
        }
        //List<Tabla> listTablas = objBD.listTablas;
        //List<Columna> listColumnas;
        //for (int i = 0; i < 2; i++) {

        for (int j = 0; j < objBD.listTablas.size(); j++) {
            //Tabla objT = listTablas.get(j); //Saca tabla por tabla
            if (objBD.listTablas.get(j).tabid == idtab[0]) { //Compara id para encontrar la tabla indicada
                for (int k = 0; k < objBD.listTablas.get(j).listColumnas.size(); k++) {
                    //Columna objC=objT.listColumnas.get(k);//Saca columna por columna
                    if (objBD.listTablas.get(j).listColumnas.get(k).colid == idcolumn[0]) {//Compara id para encontrar la columna indicada
                        objBD.listTablas.get(j).listColumnas.get(k).tabref = idtab[1];
                        objBD.listTablas.get(j).listColumnas.get(k).colref = idcolumn[1];
                    }
                }
            }
        }
        //}

        return true;
    }

    public boolean chInsertInto() {
        error.chBdActiva("chInsert");
        if (error.dslerr != 0) {
            return false;
        }

        String tokens[] = query.split(" ");
        return true;
    }

    public boolean chSelect() {
        error.chBdActiva("chSelect");
        if (error.dslerr != 0) {
            return false;
        }
        query = query.toLowerCase();
        String columns[];
        int res = query.indexOf("select");
        if (res == -1) {
            return false;
        }
        res = query.indexOf("*");
        if (res == -1) {
            String parts[] = query.split("select");
            String auxcolumns[] = query.split("from");
            columns = auxcolumns[0].split(", ");
        } else {
            columns = null;
        }
        return true;
    }

    public boolean chUpdate() {
        int res;
        Tabla objT = new Tabla();
        String[] columnas;

        //checa si la base de datos está activa
        error.chBdActiva("update");
        if (error.dslerr != 0) {
            return false;
        }

        //posibles casos
//        query = "UPDATE prueba SET col1=val1, col2=val2 WHERE condicion";
//        query = "UPDATE prueba SET col1=val1 WHERE condicion";
//        query = "UPDATE prueba SET col1=val1";
        query = query.toLowerCase();
        String[] parts = query.split("update ");
        if (parts.length == 1) { //no hay update
            return false;
        }
        parts = parts[1].split(" set ");
        objT.nombtab = parts[0];
        res = error.chTablaExiste("update", objT.nombtab);
        if (error.dslerr != 0) {
            return false;
        }
        objT.tabid = res;

        //obtiene columnas
        parts = parts[1].split(" where ");
        columnas = parts[0].split(",");
        error.chColumnasExisten("update", columnas, objT.tabid);
        if (error.dslerr != 0) {
            return false;
        }

        error.chComparaTipoColumnas("update", columnas, objT.tabid);
        if (error.dslerr != 0) {
            return false;
        }

        //checar integridad
        //checar valores de indices
        objG.escribir(objT.nombtab, objT); //crea de nuevo el archivo de esa tabla

    }

    public void mostrarArchivos() {
        try {
            List<String> lista = objG.leer("tablas");
            System.out.println("TABLAS");
            for (int i = 0; i < lista.size(); i++) {
                System.out.println(lista.get(i));
            }

            System.out.println("COLUMNAS");
            lista = objG.leer("columnas");
            for (int i = 0; i < lista.size(); i++) {
                System.out.println(lista.get(i));
            }

            System.out.println("INDICES");
            lista = objG.leer("indices");
            for (int i = 0; i < lista.size(); i++) {
                System.out.println(lista.get(i));
            }
        } catch (Exception e) {
        }
    }

}
