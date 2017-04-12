package SGBD;

import java.io.IOException;
import java.util.List;
import Archivos.GestionArchivos;

/**
 *
 * @author Tenistas
 */
public class Automatas {

    //TENER EN CUENTA
    //LOS MÉTODOS PRINCIPALES SON AQUELLOS CUYOS NOMBRES ESTÁN EN ESPAÑOL
    private final BaseDatos objBD;
    private final Errores error;
    private final GestionArchivos objG;
    private String RUTA;
    private String query;
    private boolean varEntrada;
    //boolean resultado;

    /**
     *
     */
    public Automatas() {
        objBD = new BaseDatos();
        objG = new GestionArchivos();
        error = new Errores(objBD, objG);
        varEntrada = false;
    }

    /**
     *
     * @param query
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     *
     * @return
     */
    public boolean iniAutomatas() {

        if (chCreaBD()) {
            return true;
        } else if (chUsarBD()) {
            return true;
        } else if (chCrearTabla()) {
            varEntrada = true;
            return true;
        } else if (chCrearIndice()) {
            return true;
        } else if (chCrearReferencia()) {
            return true;
        } else if (chUpdate()) {
            return true;
        } else if (chSelect()) {
            return true;
        } else if (chShowDBFiles()) {
            return true;
        } else if (chShowSEDFiles()) {
            return true;
        }//agregar los que hagan falta

        return false; //paso por todos los autómatas y aún así llegó hasta este punto
    }

    /**
     *
     * @return
     */
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
        if (error.getDslerr() != 0) {
            return false;
        }

        objG.crearDirectorio("BD"); //por si no existe
        objG.crearDirectorio("BD\\" + nombd + ".dbs");
        return true;
    }

    /**
     *
     * @return
     */
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
        if (error.getDslerr() != 0) {
            return false;
        }

        objBD.setNombre(nombbd); //no es necesario crear la estructura de datos
        RUTA = "BD\\" + objBD.getNombre() + ".dbs\\";
        error.setRUTABD(RUTA);
        System.out.println("USANDO " + objBD.getNombre());
        return true;
    }

    /**
     * Ejemplo: create table nomTabla ( col1:tipo, col2:tipo, ... ). Cuidado con
     * los espacios
     *
     * @return Si la creación fue exitosa o si hubo algún error
     */
    private boolean chCrearTabla() {
        String[] parts;
        String registro, nombtab, type;
        int n;

        //verifica que se cumpla la sentencia create table
        parts = checkCreateTable();
        if (parts == null) { //no cumplió
            return false;
        }

        //parts está con un split en base a la sentencia 'create table'
        parts = parts[1].split(" \\( ");

        //checa varios aspectos relacionados con el nombre de la tabla
        registro = checkNameTable(parts[0]);
        if (registro == null) { //si es null entonces la tabla ya existe
            return false;
        }
        nombtab = registro;
        registro += " ";

        try {
            n = objG.contarRengs(RUTA + "tablas");
            parts = parts[1].split(" \\)");

            //verifica si la tabla será difusa o determinista
            type = checkFuzziness(parts);
            if (type == null) {
                //TODO, intentar mantener una mejor gestión en los mensajes de error
                System.out.println("ERROR con tipo determinista o difuso");
                return false;
            }
            //prepara el registro a guardar en el archivo tablas
            parts = writeTableFile(registro, nombtab, parts, n, type);
            registro = "";

            //intenta escribir las columnas, si algún tipo de dato es erróneo = null
            if (writeTableCols(registro, parts, n) == null) {
                System.out.println("ERROR en tipos de dato");
                return false;
            }
            //crea un archivo con el nombre de la tabla
            objG.crearArchivo(RUTA + nombtab + ".dat");
            System.out.println("TABLA " + nombtab + " CREADA CORRECTAMENTE");
            return true;

        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Checa si la BD está activa Checa si está presente la sentencia 'create
     * table '
     *
     * @return si está presente se obtiene un arreglo String que contiene los
     * campos a crear de la tabla si no está presente, regresa un null
     */
    private String[] checkCreateTable() {
        error.chBdActiva("crearTabla");
        if (error.getDslerr() != 0) {
            return null;
        }

        query = query.toLowerCase();
        if (!query.contains("create table ")) {
            return null;
        }

        return query.split("create table ");
    }

    /**
     * Checa si el nombre de la tabla. Si el nombre tiene más de 8 caracteres,
     * éste se corta Checa si la tabla ya existe
     *
     * @return si la tabla ya existe, regresa un null Si no existe, regresa el
     * nombre de la tabla ya cortado o no
     */
    private String checkNameTable(String registro) {
        if (registro.length() > 8) { //checa que el nombre de la tabla tenga 8 caracteres
            char partecitas[] = getChars(registro, 8); //nombre cortado en 8 caracteres dentro de un arreglo char
            registro = "";
            for (int i = 0; i < partecitas.length; i++) { //se pasa el nombre ya cortado al registro
                registro += partecitas[i];
            }
        }

        error.chTablaExiste("crearTabla", registro);
        if (error.getDslerr() != 0) {
            return null;
        }

        return registro;
    }

    /**
     * Prepara y escribe un registro dentro del archivo Tablas. Llamado desde el
     * método chCrearTabla
     *
     * @return Número de renglones del archivo tablas
     *
     */
    private String[] writeTableFile(String registro, String nombtab, String[] parts, int n, String type) throws IOException {
        registro += n + 501 + " ";
        registro += nombtab + ".dat ";
        registro += 150 + " ";

        parts = parts[0].split(", ");
        registro += parts.length + " ";
        registro += 0 + " ";
        registro += 0 + " ";
        registro += type + " ";
        objG.escribir("BD\\" + objBD.getNombre() + ".dbs\\tablas", n + 1, registro, "final");
        return parts;
    }

    /**
     * Separa las columnas por , y :. Verifica que sea char, int, float o
     * double. Checa que contenga una D o F. Prepara el registro para ser
     * escrito.
     */
    private String writeTableCols(String registro, String[] parts, int n) throws IOException {
        String[] parts2, parts3;
        String type;
        if (!checkDataTypes(parts)) { //verifica que se hayan escrito tipos de dato válidos
            return null;
        }

        for (String part : parts) {
            registro = "";
            parts2 = part.split(" ");
            type = parts2[1];
            parts2 = parts2[0].split(":"); //pegados
            registro += parts2[1] + " "; //coltipo
            //obtener tamaño
            if (parts2[1].contains("char")) {
                parts3 = parts2[1].split("\\[");
                parts3 = parts3[1].split("]");
                registro += parts3[0] + " "; //coltam
            } else {
                registro += 0 + " ";
            }
            registro += n + 501 + " "; //tabid
            registro += (objG.contarRengs(RUTA + "columnas") + 1) + " "; //colid
            if (parts2[0].length() > 10) {
                char partecitas[] = getChars(parts2[0], 10);
                for (int j = 0; j < partecitas.length; j++) {
                    registro += partecitas[j]; //nombre cortado en 8
                }
            } else {
                registro += parts2[0] + " ";
            }
            registro += "-1 ";
            registro += "-1 ";
            registro += type;
            objG.escribir(RUTA + "columnas", objG.contarRengs(RUTA + "columnas"), registro, "final");
        }
        return registro;
    }

    /**
     * Verifica que los tipos de datos especificados estén dentro de los
     * disponibles: integer, char, double y float
     *
     * @return falso si no está bien especificado el tipo de dato
     */
    private boolean checkDataTypes(String[] parts) {
        String[] parts2, parts3;
        for (String part : parts) {
            parts2 = part.split(":"); //pegados

            if (!parts2[1].contains("integer") && !parts2[1].contains("char")
                    && !parts2[1].contains("float") && !parts2[1].contains("double")) {
                return false;
            }

            parts3 = parts2[1].split(" ");
            if (!parts3[1].contains("d") && !parts3[1].contains("f")) {
                return false;
            }
        }
        return true;
    }

    private String checkFuzziness(String[] parts) {
        String[] parts2;
        for (String part : parts) {
            parts2 = part.split(", ");
            if (parts2.length == 2) {
                parts2 = parts2[1].split(":");
            } else {
                parts2 = parts2[0].split(":");
            }

            parts2 = parts2[1].split(" ");

            if (parts2[1].equals("d") || parts2[1].equals("f")) {
                return parts2[1];
            } else {
                return null;
            }
        }
        return null;
    }

    private char[] getChars(String cadena, int tamaño) {
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

    /**
     *
     * @return
     */
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
        if (error.getDslerr() != 0) {
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
        objI.setNomind(parts[0]);
        registro = objI.getNomind() + " ";

        //obtener nombre de la tabla y verifica si existe en la BD
        parts = parts[1].split(" \\( ");
        objI.setTabid(error.chTablaExiste("crearIndice", parts[0]));
        if (error.getDslerr() != 0) {
            return false;
        }
        registro += objI.getTabid() + " ";

        //obtener columnas y verificar si existen en la tabla
        parts = parts[1].split(" \\)");
        String[] nomcols = parts[0].split(", ");
        if (nomcols.length > 4) { //deben ser 4 columnas por índice
            return false;
        }

        colsid = new int[4];
        for (int i = 0; i < 4; i++) {
            if (i < nomcols.length) {
                idcols = error.chColumnasExisten("crearIndice", nomcols[i], objI.getTabid());
                if (error.getDslerr() != 0) {
                    return false;
                }
                colsid[i] = idcols;
                registro += idcols + " ";
            } else {
                colsid[i] = -1;
                registro += -1 + " ";
            }
        }
        objI.setColsid(colsid);

        //obtener el indice id
        indid = getMayorIndiceId(objI.getTabid()) + 1;
        registro += indid + " ";
        objI.setIndid(indid);
        registro += indtipo + " ";
        objI.setIndtipo(indtipo);

        //checar si ya existe un indice con ese nombre
        error.chIndiceExiste("chCrearIndice", objI);
        if (error.getDslerr() != 0) {
            return false;
        }

        try {
            //escribir en indices
            indid = objG.contarRengs("indices");
            objG.escribir(RUTA + "indices", ++indid, registro, "final");

            //actualizar tablas
            list = objG.leer("tablas");
            for (int i = 0; i < list.size(); i++) {
                parts = list.get(i).split(" ");
                if (Integer.parseInt(parts[1]) == objI.getTabid()) {
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
                    objG.escribir(RUTA + "tablas", i, registro, "nuevo");
                } else {
                    objG.escribir(RUTA + "tablas", i, list.get(i), "final");
                }
            }
            //crear nuevo archivo
            objG.crearArchivo(RUTA + objI.getNomind() + ".ix" + objI.getIndid());
        } catch (Exception e) {
            System.out.println("ERROR: ESCRIBIRINIDCE");
        }

        return true;
    }

    /**
     *
     * @param tabid
     * @return
     */
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

    /**
     *
     * @return
     */
    public boolean chCrearReferencia() {
        error.chBdActiva("chCrearReferencia");
        if (error.getDslerr() != 0) {
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
//            idcolumn[i] = error.chColumnasExisten("chCrearReferencia", columns, idtab[i])[0];
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

        for (int j = 0; j < objBD.getListTablas().size(); j++) {
            //Tabla objT = listTablas.get(j); //Saca tabla por tabla
            if (objBD.getListTablas().get(j).tabid == idtab[0]) { //Compara id para encontrar la tabla indicada
                for (int k = 0; k < objBD.getListTablas().get(j).listColumnas.size(); k++) {
                    //Columna objC=objT.listColumnas.get(k);//Saca columna por columna
                    if (objBD.getListTablas().get(j).listColumnas.get(k).getColid() == idcolumn[0]) {//Compara id para encontrar la columna indicada
                        objBD.getListTablas().get(j).listColumnas.get(k).setTabref(idtab[1]);
                        objBD.getListTablas().get(j).listColumnas.get(k).setColref(idcolumn[1]);
                    }
                }
            }
        }
        //}

        return true;
    }

    /**
     *
     * @return
     */
    //TODO, FALTA VERIFICAR LA REFERENCIA!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private boolean chInsert() {
        error.chBdActiva("chInsert");
        if (error.getDslerr() != 0) {
            return false;
        }
        String parts[] = null, columnas[] = null, columnasaux, tabla[] = null, valores[] = null, nomtab;
        int tabid;
        String ordenColumnas[][];
        query = query.toLowerCase(); //Lo convierto todo en minusculas
        if (query.indexOf("insert into ") == -1) {
            System.out.println("no valido");
            return false;
        }
        tabla = query.split("insert into ");
        tabla = tabla[1].split(" values ");
        //tabla = tabla[0].split(" ");
        if (tabla[0].split(" ").length > 1) { // si se especifican las columnas en el query
            columnas = tabla;
            tabla = tabla[0].split(" "); //la tabla o nombtab se quedo en la posicion 0
            columnas = columnas[0].split(" \\( ");
            columnas = columnas[1].split(" \\)");
            columnas = columnas[0].split(" "); //obtengo las columnas ya separadas
        }
        nomtab = tabla[0]; //obtengo el nombre de la tabla
        tabid = error.chTablaExiste("chInsert", nomtab);  //verifico si la tabla existe y guardo el id de la tabla
        if (error.getDslerr() != 0) {
            return false;
        }

        //Obtener la candidad de columnas que tiene la tabla
        List tablas;
        String aux;
        int n = 0;
        try {
            tablas = objG.leer("tablas");
            for (int i = 0; i < tablas.size(); i++) {
                aux = tablas.get(i).toString();
                parts = aux.split(" ");
                if (Integer.parseInt(parts[1]) == tabid) {
                    n = Integer.parseInt(parts[4]);
                    i = tablas.size();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        ordenColumnas = new String[n][3]; //Se construlle un arreglo donde tendra todas las columnas de la tabla con su id, su nombre y su valor a insertar
        if (columnas == null) { //preguntar si el usuario especifico las columnas en el insert si no se hace un arreglo con las columnas...
            try {
                columnas = new String[n];
                List Lcolumnas;
                int cont = 0;
                Lcolumnas = objG.leer("columnas");
                for (int i = 0; i < Lcolumnas.size(); i++) {
                    columnasaux = Lcolumnas.get(i).toString(); //se obtiene una columna de todas las columnas de l BD
                    parts = columnasaux.split(" "); //Se divide por sus campos
                    if (Integer.parseInt(parts[2]) == tabid) { //Se compara con los id de las tablas para ver las columnas de la tabla a insertar
                        columnas[cont] = parts[3];    //se obtiene el id de las columnas de la tabla
                        ordenColumnas[cont][0] = parts[3];//obtiene el id de las columnas
                        ordenColumnas[cont][1] = parts[4];//obtiene el nombre de las columnas
                        cont++;
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            for (int i = 0; i < columnas.length; i++) {
                error.chColumnasExisten("chInsert", columnas[i], tabid);
                if (error.getDslerr() != 0) {
                    return false;
                }
            }
            try {
                List Lcolumnas; //aqui estaran todas las columnas de la BD
                int cont = 0;
                Lcolumnas = objG.leer("columnas"); //Se obtiene las columnas de la BD
                for (int i = 0; i < Lcolumnas.size(); i++) {
                    columnasaux = Lcolumnas.get(i).toString(); //Se obtiene columna por columna
                    parts = columnasaux.split(" "); //Se dividi en sus campos que tiene
                    if (Integer.parseInt(parts[2]) == tabid) { //Se compara con el id de la tabla para ver si esa columna pertenese a la tabla a insertar
                        ordenColumnas[cont][0] = parts[3]; //se coloca la columna en el arreglo donde estaran todas las columnas de la tabla a insertar
                        ordenColumnas[cont][1] = parts[4]; //Se coloca el nombre de la columna
                        cont++;
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        valores = query.split("values \\( ");
        valores = valores[1].split(" \\)");
        valores = valores[0].split(", ");
        boolean FFT = false; //bandera tabla difusa (Flag Fuzzy Table)
        for (int i = 0; i < ordenColumnas.length; i++) {
            ordenColumnas[i][2] = "null";        //Se coloca la palabra null en todas las columnas para posteriormente escribir null en los campos que no se especificaron
        }

        for (int i = 0; i < columnas.length; i++) {
            for (int j = 0; j < ordenColumnas.length; j++) {
                if (columnas[i].equals(ordenColumnas[j][0])) {
                    ordenColumnas[j][2] = valores[i];
                }
            }
        }

        for (int i = 0; i < ordenColumnas.length; i++) {
            if (!ordenColumnas[i][2].equals("null")) {
                if (ordenColumnas[i][2].indexOf("'") == -1) { // no tiene comillas simples
                    if (ordenColumnas[i][2].indexOf("<") != -1) { // contiene <...>
                        if (!FFT) { //No se a verificado que la tabla sea difusa
                            if (FFT = error.chTablaDifusa(tabid)) { //Se checa que la tabla sea difuca
                                if (error.chColumnaDifusa(Integer.parseInt(ordenColumnas[i][0]))) { //Verifica que la columna sea difusa...
                                    if (!error.chVariableLinguistica(nomtab + "." + ordenColumnas[i][1], ordenColumnas[i][2].split("<")[0].split(">")[0])) { //Verifica que la variable linguistica exista
                                        System.out.println("No se puede insertar una etiqueta lingüistica no existente en la variable difusa de la columna");
                                        return false;
                                    }
                                } else {
                                    System.out.println("No se puede insertar una etiqueta lingüistica en una columna determinista");
                                    return false;
                                }
                            } else {
                                System.out.println("No se puede insertar etiquetas lingüisticas en una tabla determinista");
                                return false;
                            }
                        } else {
                            //ya se avia veridicado anteriormente la tabla que sea difusa
                        }
                    }
                }
            }
        }

        for (int i = 0; i < ordenColumnas.length; i++) {
            if (!ordenColumnas[i][2].equals("null")) {
                error.chComparaTipoColumnas("chInsert", tabid, Integer.parseInt(ordenColumnas[i][0]), valores[i]);
            }
            if (error.getDslerr() != 0) {
                return false;
            }
        }

        //Hacer la cadena
        String registro = "";
        for (int i = 0; i < ordenColumnas.length; i++) {
            if (i != (ordenColumnas.length - 1)) {
                registro += ordenColumnas[i][2] + " ";
            } else {
                registro += ordenColumnas[i][2];
            }
        }
        try {
            objG.escribir(RUTA + nomtab + ".dat", objG.contarRengs(RUTA + nomtab + ".dat"), registro, "final");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return true;
    }

    /**
     *
     * @return
     */
    public boolean chSelect() {
        error.chBdActiva("chSelect");
        if (error.getDslerr() != 0) {
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

    /**
     *
     * @return
     */
    public boolean chUpdate() {
        int res;
        Tabla objT = new Tabla();
        String[] columnas;

        //checa si la base de datos está activa
        error.chBdActiva("update");
        if (error.getDslerr() != 0) {
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
        objT.nombtab = parts[0]; //nomtab
        res = error.chTablaExiste("update", objT.nombtab);
        if (error.getDslerr() != 0) {
            return false;
        }
        objT.tabid = res; //tabid

        //obtiene columnas
        parts = parts[1].split(" where ");
        columnas = parts[0].split(",");
        for (String columna : columnas) {
            parts = columna.split("=");
            res = error.chColumnasExisten("update", parts[0], objT.tabid);
            if (error.getDslerr() != 0) {
                return false;
            }
            objT.columnas.add(res + "");
            //verifica que cada valor corresponda al tipo de dato de cada columna
            error.chComparaTipoColumnas("update", objT.tabid, res, parts[1]);
            if (error.getDslerr() != 0) {
                return false;
            }
        }

        //checar integridad
        //checar valores de indices
        //actualizar archivo tablas
        //actualizar archivo indices
        return true;
    }

    private boolean chShowDBFiles() {
        error.chBdActiva("crearTabla");
        if (error.getDslerr() != 0) {
            return false;
        }

        if (query.contains("show db files")) {
            showBDFiles();
            return true;
        }
        return false;
    }

    private void showBDFiles() {
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
            e.printStackTrace();
        }
    }

    private boolean chShowSEDFiles() {
        error.chBdActiva("crearTabla");
        if (error.getDslerr() != 0) {
            return false;
        }

        if (query.contains("show sed files")) {
            showSEDFiles();
            return true;
        }
        return false;
    }

    private void showSEDFiles() {
        GestionArchivos objGsed = new GestionArchivos();
        List<String> regDatos, regFiles;
        try {
            regDatos = objGsed.leer("SED/Datos");
            for (String regDato : regDatos) {
                //System.out.println("SED/" + regDato);
                regFiles = objGsed.leer(("SED/" + regDato).trim());

                System.out.println("ARCHIVO " + regDato);
                for (String regFile : regFiles) {
                    System.out.println(regFile);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isVarEntrada() {
        return varEntrada;
    }

    public void setVarEntrada(boolean varEntrada) {
        this.varEntrada = varEntrada;
    }
}
