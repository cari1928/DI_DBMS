package Clases;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        error = new Errores(objBD);
        objG = new GestionArchivos(objBD);
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public boolean iniAutomatas() {

        if (chCreaBD()) {
            return true;
        } else if (chUsarBD()) {
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
        List<String> listTablas, listColumnas, listIndices;
        String[] parts, partsCols, partsInd;
        Tabla objT;
        Columna objC;
        Indice objI;

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

        objBD.nombre = nombbd;
        try {
            listTablas = objG.leer("tablas");
            listColumnas = objG.leer("columnas");
            listIndices = objG.leer("indices");

            for (int i = 0; i < listTablas.size(); i++) {
                parts = listTablas.get(i).split(" ");
                objT = new Tabla();
                objT.nombtab = parts[0];
                objT.tabid = Integer.parseInt(parts[1]);
                objT.archivo = parts[2];
                objT.tamreng = Integer.parseInt(parts[3]);
                objT.ncols = Integer.parseInt(parts[4]);
                objT.nrengs = Integer.parseInt(parts[5]);
                objT.nindex = Integer.parseInt(parts[6]);

                for (int j = 0; j < listColumnas.size(); j++) {
                    partsCols = listColumnas.get(j).split(" ");

                    if (Integer.parseInt(partsCols[2]) == objT.tabid) {
                        objC = new Columna();
                        objC.coltipo = partsCols[0].charAt(0);
                        objC.coltam = Integer.parseInt(partsCols[1]);
                        objC.tabid = Integer.parseInt(partsCols[2]);
                        objC.colid = Integer.parseInt(partsCols[3]);
                        objC.nomcol = getChars(partsCols[4], 10);
                        objC.tabref = Integer.parseInt(partsCols[5]);
                        objC.colref = Integer.parseInt(partsCols[6]);

                        for (int k = 0; k < listIndices.size(); k++) {
                            partsInd = listIndices.get(k).split(" ");
                            objI = new Indice();

                            if (Integer.parseInt(partsInd[1]) == objT.tabid
                                    && (Integer.parseInt(partsInd[2]) == objC.colid
                                    || Integer.parseInt(partsInd[3]) == objC.colid
                                    || Integer.parseInt(partsInd[4]) == objC.colid
                                    || Integer.parseInt(partsInd[5]) == objC.colid)) {
                                objI.nomid = getChars(partsInd[0], 10);
                                objI.tabid = Integer.parseInt(partsInd[1]);
                                objI.colid1 = Integer.parseInt(partsInd[2]);
                                objI.colid2 = Integer.parseInt(partsInd[3]);
                                objI.colid3 = Integer.parseInt(partsInd[4]);
                                objI.colid4 = Integer.parseInt(partsInd[5]);
                                objI.indid = Integer.parseInt(partsInd[6]);
                                objI.indtipo = partsInd[7].charAt(0);

                                objT.listIndices.add(objI);
                            }
                        }

                        objT.listColumnas.add(objC);
                    }
                }
                objBD.listTablas.add(objT);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("USANDO " + objBD.nombre);
        return true;
    }

    public boolean chCrearTabla() {
        String[] parts, parts2;
        String registro = "", nombtab;
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
        parts = parts[0].split(" ( ");
        registro += parts[0]; //guarda nombtab

        if (registro.length() > 8) {
            char partecitas[] = getChars(registro, 8);
            registro = "";
            for (int i = 0; i < partecitas.length; i++) {
                registro += partecitas[i];
            }
        }

        nombtab = registro;

        error.chTablaExiste("crearTabla", registro);
        if (error.dslerr != 0) {
            return false;
        }

        try {
            n = objG.contarRengs("tablas");
            registro += n + 501;
            registro += nombtab + ".dat";

            parts = parts[1].split(" )");
            parts = parts[0].split(", ");
            for (int i = 0; i < parts.length; i++) {
                parts2 = parts[i].split(":"); //pegados

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public char[] getChars(String cadena, int tamaño) {
        char[] tmp = new char[tamaño];
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = cadena.charAt(i);
        }
        return tmp;
    }

    public boolean chCrearIndice() {
        //checa si la base de datos está activa
        error.chBdActiva("crearIndice");
        if (error.dslerr != 0) {
            return false;
        }

        boolean flag = true;
        Indice objI = new Indice();

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
                    objI.indtipo = 'U';
                    break;

                case 2:
                    res = query.indexOf("create unique desc index ");
                    objI.indtipo = 'U';
                    break;

                case 3:
                    res = query.indexOf("create asc index ");
                    objI.indtipo = 'D';
                    break;

                case 4:
                    res = query.indexOf("create desc index ");
                    objI.indtipo = 'D';
                    break;

                case 5:
                    res = query.indexOf("create unique index ");
                    objI.indtipo = 'U';
                    break;

                case 6:
                    res = query.indexOf("create index ");
                    objI.indtipo = 'D';
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
        objI.nomind = parts[0];

        //obtener nombre de la tabla y verifica si existe en la BD
        parts = parts[1].split(" (");
        String nomtab = parts[0];
        int tabid = error.chTablaExiste("crearIndice", nomtab);
        if (error.dslerr != 0) {
            return false;
        }
        objI.tabid = tabid;

        //obtener columnas y verificar si existen en la tabla
        parts = parts[1].split(")");
        String[] nomcols = parts[0].split(",");
        if (nomcols.length != 4) { //deben ser 4 columnas por índice
            return false;
        }
        int[] idcols = error.chColumnasExisten("crearIndice", nomcols, objI.tabid);
        if (error.dslerr != 0) {
            return false;
        }
        objI.colid1 = idcols[0];
        objI.colid2 = idcols[1];
        objI.colid3 = idcols[2];
        objI.colid4 = idcols[3];

        //checa si el índice ya existe, checa nombre y otros atributos
        int id = error.chIndiceExiste("crearIndice", objI); //objI ya incluye la tabla en donde esta
        if (error.dslerr != 0) {
            return false;
        }
        if (id == 10) {
            return false; //no se pueden más de 10 índices por tabla
        }
        objI.indid = ++id;

        //aumenta en 1 el número de indices de la tabla
        List<Tabla> listTablas = objBD.listTablas;
        Tabla objT = null;
        for (int i = 0; i < listTablas.size(); i++) {
            objT = listTablas.get(i);
            if (objT.tabid == objI.tabid) {
                ++objT.nindex;
                i = listTablas.size(); //para que no recorra las demás tablas
            }
        }

        //(nombre_arcivo, indice)
        objG.escribir("INDICES", objI);
        objG.escribir("TABLAS", objT);
        //(nombre_archivo, extension)
        objG.crearArchivo(objT.nombtab, "ixn");

        return true;
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

}
