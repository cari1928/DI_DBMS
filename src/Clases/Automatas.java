package Clases;

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
        error = new Errores(objBD);
        objG = new GestionArchivos();
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public boolean iniAutomatas() {

        if (!chCreaBD()) {
            return false;
        } else if (!chCrearIndice()) {
            return false;
        } else if (!chCrearReferencia()) {
            return false;
        } else if (!chInsertInto()) {
            return false;
        } else if (!chSelect()) {
            return false;
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
        String nombbd = parts[1];

        error.chCrearBD("crearBD", nombbd);
        if (error.dslerr != 0) {
            return false;
        }

        objG.crearDirectorio(nombbd); //no se que parametros vaya a llevar
        return true;
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
        String query = "CREATE UNIQUE ASC INDEX iNomb ON tNomb(col1, col2, col3)";
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

}
