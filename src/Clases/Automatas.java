package Clases;

import java.util.List;

/**
 *
 * @author Tenistas
 */
public class Automatas {

    public boolean chCreaBD() {
        //String query = "CREATE DATABASE prueba";

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
        int id = error.chIndiceExiste(objI); //objI ya incluye la tabla en donde esta
        if (error.dslerr != 0) {
            return false;
        }
        if (id == 10) {
            return false; //no se pueden más de 10 índices por tabla
        }
        objI.indid = ++id;

        //aumenta en 1 el número de indices de la tabla
        List<Tabla> listTablas = objBD.listTablas;
        Tabla objT;
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

    public static void main(String[] args) {
        Automatas objA = new Automatas();
//        boolean flag = objA.chCreaBD();
//        boolean flag = objA.chCrearIndice();

        System.out.println(flag);

    }

}
