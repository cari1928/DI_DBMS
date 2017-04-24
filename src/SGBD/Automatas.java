package SGBD;

import java.io.IOException;
import java.util.List;
import GestionSistema.GestionArchivos;
import GestionSistema.Sistema;
import SED.VariableEntrada;
import java.util.ArrayList;

/**
 *
 * @author Tenistas
 */
public class Automatas {

    private final Errores error;
    private final GestionArchivos objG;
    private final BaseDatos objBD;
    private String RUTA;
    private String query;
    private boolean varEntrada;

    //PARA EL SELECT
    private List<String> Lcondiciones; //guarda condiciones y operadores lógicos
    private List<Tabla> Lregistros; //guarda todos los registros de todas las tablas
    private Tabla LtabResAll; //guarda en una tabla todos los registros con toda la informacion de todas las tablas
    private String columnas[];

    public Automatas() {
        objBD = new BaseDatos();
        objG = new GestionArchivos();
        error = new Errores(objBD, objG);
        varEntrada = false;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public boolean isVarEntrada() {
        return varEntrada;
    }

    public void setVarEntrada(boolean varEntrada) {
        this.varEntrada = varEntrada;
    }

    public BaseDatos getObjBD() {
        return objBD;
    }

    public String getRUTA() {
        return RUTA;
    }

    public boolean iniAutomatas() {
        error.setDslerr(0); //para futuras ejecuciones

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
        } else if (chSelect()) {
            imprimeResultadoFinal(columnas);
            return true;
        } else if (chInsert()) {
            return true;
        } else if (chUpdate()) {
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
            type = checkFuzziness(parts[0].split(", "));
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

        } catch (Exception ex) {
            //ex.printStackTrace();
            System.out.println("ERROR EN LA SINTAXIS");
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
        registro += type;
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
        String type = "d";

        for (String part : parts) {
            parts2 = part.split(":");
            parts2 = parts2[1].split(" ");

            if (parts2[1].equals("f")) {
                return parts2[1];
            }
        }
        return type;
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

//posibles casos
    //String query = "CREATE UNIQUE ASC INDEX iNomb ON tNomb(col1, col2, col3)";
    //String query = "CREATE UNIQUE DESC INDEX iNomb ON tNomb(col1)";
    //String query = "CREATE ASC INDEX iNomb ON tNomb(col1)";
    //String query = "CREATE DESC INDEX iNomb ON tNomb(col1)";
    //String query = "CREATE UNIQUE INDEX iNomb ON tNomb(col1)";
    //String query = "CREATE INDEX iNomb ON tNomb(col1)";
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
        Columna objC;
        Tabla objT;
        String[] parts, parts2;
        int idtab[] = new int[2];
        int idcolumn[] = new int[2];
        String columns[] = new String[2];

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
        parts = query.split("create reference");
        parts = parts[0].split(" ");
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
            if (idcolumn[i] != 0) {
                return false;
            }
        }
        if (!error.chComparaTipoColumnas("chCrearReferencia", idtab, idcolumn)) {
            return false;
        }

        for (int j = 0; j < objBD.getListTablas().size(); j++) {
            objT = objBD.getListTablas().get(j); //para simplificar el código siguiente

            if (objT.getTabid() == idtab[0]) { //Compara id para encontrar la tabla indicada
                for (int k = 0; k < objT.getListColumnas().size(); k++) {
                    objC = objT.getListColumnas().get(k); //para simplificar el código siguiente

                    if (objC.getColid() == idcolumn[0]) {//Compara id para encontrar la columna indicada
                        objC.setTabref(idtab[1]);
                        objC.setColref(idcolumn[1]);
                    }
                }
            }
        }
        return true;
    }

    //TODO, FALTA VERIFICAR LA REFERENCIA!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private boolean chInsert() {
        String[] parts, columnas = null, tabla, valores;
        String[][] ordenColumnas;
        String columnasaux, nomtab, aux;
        List<String> tablas;
        int tabid, nCols = 0, bancol = 0;
        error.chBdActiva("chInsert");
        if (error.getDslerr() != 0) {
            return false;
        }

        query = query.toLowerCase(); //minúsculas
        if (!query.contains("insert into ")) {
            //System.out.println("SENTENCIA NO VÁLIDA"); //Ése mensaje no es adecuado para la lógica del programa
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
            if (columnas[0].contains(",")) {
                columnas = columnas[0].split(", "); //obtengo las columnas ya separadas
            }
        }
        nomtab = tabla[0]; //obtengo el nombre de la tabla
        tabid = error.chTablaExiste("insert", nomtab);  //verifico si la tabla existe y guardo el id de la tabla
        if (error.getDslerr() != 0) {
            return false;
        }

        //Obtener la candidad de columnas que tiene la tabla
        try {
            tablas = objG.leer(RUTA + "tablas");
            for (int i = 0; i < tablas.size(); i++) {
                aux = tablas.get(i);
                parts = aux.split(" ");
                if (Integer.parseInt(parts[1]) == tabid) {
                    nCols = Integer.parseInt(parts[4]); //toma el número de columnas de la tabla
                    i = tablas.size(); //para terminar el ciclo
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        ordenColumnas = new String[nCols][3]; //tendra columnas de la tabla con id, nombre y valor a insertar
        if (columnas == null) { //preguntar si el usuario especifico las columnas en el insert si no se hace un arreglo con las columnas...
            try {
                columnas = new String[nCols];
                List Lcolumnas;
                int cont = 0;
                Lcolumnas = objG.leer(RUTA + "columnas");
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
            bancol = 1;
            for (String columna : columnas) {
                error.chColumnasExisten("chInsert", columna, tabid);
                if (error.getDslerr() != 0) {
                    return false;
                }
            }
            try {
                List<String> Lcolumnas; //aqui estaran todas las columnas de la BD
                int cont = 0;
                Lcolumnas = objG.leer(RUTA + "columnas"); //Se obtiene las columnas de la BD
                for (int i = 0; i < Lcolumnas.size(); i++) {
                    columnasaux = Lcolumnas.get(i); //Se obtiene columna por columna
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
        for (String[] ordenColumna : ordenColumnas) {
            ordenColumna[2] = "null"; //Se coloca la palabra null en todas las columnas para posteriormente escribir null en los campos que no se especificaron
        }

        for (int i = 0; i < columnas.length; i++) {
            for (String[] ordenColumna : ordenColumnas) {
                if (columnas[i].equals(ordenColumna[bancol])) {
                    ordenColumna[2] = valores[i];
                }
            }
        }

        //checa variables difusas
        for (String[] ordenColumna : ordenColumnas) {

            if (!ordenColumna[2].equals("null")) {

                if (!ordenColumna[2].contains("'")) {
                    // no tiene comillas simples
                    if (ordenColumna[2].contains("<")) {
                        // contiene <...>
                        if (!FFT) {
                            //No se ha verificado que la tabla sea difusa
                            if (FFT = error.chTablaDifusa((tabid - 501))) {
                                //Se checa que la tabla sea difusa
                                if (error.chColumnaDifusa(Integer.parseInt(ordenColumna[0]))) {
                                    //Verifica que la columna sea difusa...
                                    if (!error.chVariableLinguistica(nomtab + "." + ordenColumna[1], ordenColumna[2].split("<")[1].split(">")[0])) {
                                        //Verifica que la variable linguistica exista
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
                if (!error.chComparaTipoColumnas("chInsert", tabid, Integer.parseInt(ordenColumnas[i][0]), valores[i])) {
                    return false;
                }
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
        } catch (Exception ex) {
            //ex.printStackTrace();
            System.out.println("ERROR EN LA SINTAXIS");
        }
        System.out.println("REGISTRO INSERTADO");
        return true;
    }

    /**
     *
     * @return
     */
    private boolean chSelect() {
        String pColumnas[] = null, tablas[] = null, parts[], condiciones[] = null;
        List<List<String[]>> Lresultados;
        List<resulWhere> LregW;
        Sistema objS = new Sistema();

        error.chBdActiva("select");
        if (error.getDslerr() != 0) {
            return false;
        }

        query = query.toLowerCase();
        Lcondiciones = new ArrayList<>(); //guarda las condiciones y sus operadores lógicos
        Lregistros = new ArrayList<>(); //guarda la info de todas las tablas

        if (!query.contains("select")) {
            return false;
        }

        if (!query.contains("*")) {
            //llena arreglo columnas
            parts = query.split("select ")[1].split(" from"); //obtengo las columnas que se desean seleccionar
            if (parts[0].contains(", ")) {
                //Tiene varias columnas seleccionadas...
                pColumnas = parts[0].split(", ");
            } else {
                //Tiene solo una columna seleccionada...
                pColumnas = new String[]{parts[0]};
            }
        }
        //Si no se indico ninguna columna la variable columnas estara en null.
        parts = query.split(" from ");
        parts[0] = parts[1];
        if (parts[0].contains(" where ")) {
            parts = parts[0].split(" where "); //quita el where
        }
        if (parts[0].contains(" inner join ")) {
            //Tienen varias tablas a las que se hace referencia...
            //llena arreglo tablas
            tablas = parts[0].split(" inner join ");
        } else {
            //Tiene solo una tabla a la que se hace referencia
            tablas = new String[]{parts[0]};
        }

        if (!objS.obtenerTablas(tablas, error)) {
            return false;
        }

        //regresa todos los registros de las tablas fuera del where
        obtener_todos_registros(tablas, 0); //Lregistros es llenado
        LtabResAll = obtener_tabla_resultante(tablas, 0, new Tabla());

        if (query.contains(" where ")) { // Si hay condiciones...
//            LselecCond = new ArrayList<>(); //guarda las condiciones a mostrar al usuario
            tratado_condiciones(query.split(" where ")[1]); //Gurda todas las condiciones que tiene en una lista de String la forma en como guarda es condicion y operafor logico la siguiente condicion asi sucesivamente

            Lresultados = verificarCondiciones();
            if (Lresultados == null) {
                return false;
            }

            LregW = resTotCond(Lresultados, null, 0, 1);
            preparaLtabResAll(LregW);
        }

        this.columnas = pColumnas;
        return true;
    }

    void preparaLtabResAll(List<resulWhere> LregW) {
        boolean coincidencia;
        int pos = 0;
        String NomCol;
        for (int i = 0; i < LtabResAll.getListRegistro().size(); i++) {
            coincidencia = false;

            for (int j = 0; j < LregW.size() && !coincidencia; j++) {
                if (i == LregW.get(j).getPosicion()) {
                    coincidencia = true;
                    pos = j;
                }
            }
            if (!coincidencia) { //Si no coincidieron se elimina y ya
                LtabResAll.getListRegistro().remove(i); //Se eliminan los registros que no fueron aceptados en la condicion
                i--;
                for (int j = 0; j < LregW.size(); j++) {
                    LregW.get(j).setPosicion((LregW.get(j).getPosicion() - 1));
                }
            } else { //si si coincidieron se verifique si tiene grados de pertenencia y si si se agregan las columnas que se encontraron
                if (!LregW.get(pos).getGradosPertenencia().isEmpty()) {
                    agregarColumna(LregW.get(pos).getGradosPertenencia());

                    for (int j = 0; j < LtabResAll.getListRegistro().get(i).list_columnas.size(); j++) {

                        for (int k = 0; k < LregW.get(pos).getGradosPertenencia().size(); k++) {
                            NomCol = LtabResAll.getListRegistro().get(i).list_columnas.get(j).getNomtab() + "." + obtenerNombresColumnas(LtabResAll.getListRegistro().get(i).list_columnas.get(j).getNomcol());
                            if (NomCol.equals(LregW.get(pos).getGradosPertenencia().get(k)[0]) && LtabResAll.getListRegistro().get(i).list_columnas.get(j).getDifusa()) {
                                LtabResAll.getListRegistro().get(i).list_columnas.get(j).setContenido(LregW.get(pos).getGradosPertenencia().get(k)[1]);
                            }
                        }
                    }
                }
            }
        }
    }

    void agregarColumna(List<String[]> columnas) {
        String nombreCol;
        Columna objC;
        boolean bandera = false;
        for (int i = 0; i < columnas.size(); i++) {
            nombreCol = columnas.get(i)[0];
            bandera = false;

            for (int j = 0; j < LtabResAll.getListRegistro().get(0).list_columnas.size() && !bandera; j++) {
                if (nombreCol.equals(LtabResAll.getListRegistro().get(i).list_columnas.get(j).getNomtab() + "." + obtenerNombresColumnas(LtabResAll.getListRegistro().get(i).list_columnas.get(j).getNomcol())) && LtabResAll.getListRegistro().get(i).list_columnas.get(j).getDifusa()) {
                    bandera = true;
                }
            }
            if (!bandera) {
                for (int j = 0; j < LtabResAll.getListRegistro().size(); j++) {
                    objC = new Columna();
                    objC.setNomtab(nombreCol.split("\\.")[0]);
                    objC.setNomcol(getChars(nombreCol.split("\\.")[1], 10));
                    objC.setDifusa(true);
                    LtabResAll.getListRegistro().get(j).getList_columnas().add(objC);
                }
            }
        }
    }

    private void imprimeResultadoFinal(String[] columnas) {
        String resultado = "| ";
        Registro objR;
        Columna objC;

        if (columnas != null) {
            for (int i = 0; i < LtabResAll.getListRegistro().get(0).getList_columnas().size(); i++) {
                objC = LtabResAll.getListRegistro().get(0).getList_columnas().get(i);

                if (objC.getDifusa()) {
                    //TODO
                    resultado += objC.getNomtab() + "." + obtenerNombresColumnas(objC.getNomcol()) + " | ";
                } else {
                    for (String columna : columnas) {
                        if (columna.equals(objC.getNomtab() + "." + obtenerNombresColumnas(objC.getNomcol()))) {
                            resultado += objC.getNomtab() + "." + obtenerNombresColumnas(objC.getNomcol());
                            System.out.printf("%-20s | ", resultado.trim());
                            resultado = "";
                        }
                    }
                }
            }
            System.out.println();
            resultado = "| ";

            for (int i = 0; i < LtabResAll.getListRegistro().size(); i++) {
                objR = LtabResAll.getListRegistro().get(i);

                for (int j = 0; j < objR.getList_columnas().size(); j++) {
                    objC = objR.getList_columnas().get(j);
                    if (objC.getDifusa()) {
                        //TODO
                        resultado += objC.getContenido() + " | ";
                    } else {
                        for (String columna : columnas) {
                            if (columna.equals(objC.getNomtab() + "." + obtenerNombresColumnas(objC.getNomcol()))) {
                                resultado += objC.getContenido();
                                System.out.printf("%-20s | ", resultado.trim());
                                resultado = "";
                            }
                        }
                    }
                }
                System.out.println();
                resultado = " | ";
            }
        } else {
            for (int i = 0; i < LtabResAll.getListRegistro().get(0).getList_columnas().size(); i++) {
                resultado += LtabResAll.getListRegistro().get(0).getList_columnas().get(i).getNomtab()
                        + "."
                        + obtenerNombresColumnas(LtabResAll.getListRegistro().get(0).getList_columnas().get(i).getNomcol());
                System.out.printf("%-20s | ", resultado);
                resultado = "";
            }
            System.out.println();

            for (int i = 0; i < LtabResAll.getListRegistro().size(); i++) {
                resultado = "| ";
                objR = LtabResAll.getListRegistro().get(i);
                for (int j = 0; j < objR.getList_columnas().size(); j++) {
                    objC = objR.getList_columnas().get(j);
                    resultado += objC.getContenido();
                    System.out.printf("%-20s | ", resultado.trim()); //no sé porque, pero necesita el .trim 
                    resultado = "";
                }
                System.out.println();
            }
        }
    }

    //regresa los registros de una o varias tablas
    private Tabla obtener_tabla_resultante(String tablas[], int posicion, Tabla objTresultante) {
        String[] parts; // para manupular o dividir el contenido del arreglo tablas
        String aux; //guarada la separacion de tabla.columna de que tiene parts en la posicion 2
        String aux2; //guarada la separacion de tabla.columna de que tiene parts en la posicion 4
        Registro objR_anterior;
        Registro objR_nueva;
        Tabla objT_anterior, objT_nueva;
        boolean bandera = false; //La bandera es para sabaer si el registro se pudo relacionar con todas las tablas indicadas, si si para guardar el registro en objTresultante y si no para no guardarla...

        //Trabajo desde los registros de la primera tabla... por eso pregunto si hay mas registros que utilizar si no se sale de la recursividad...
        if (posicion >= Lregistros.get(0).getListRegistro().size()) { //pregunto si
            return objTresultante;
        }

        objT_anterior = Lregistros.get(0);
        objR_anterior = objT_anterior.getListRegistro().get(posicion);  //obtengo el registro a trabajar o tratar

        for (int i = 0; i < objR_anterior.getList_columnas().size(); i++) {//Recorro todas las columnas para ponerles el nombre de la tabla a las que pertenecen...
            objR_anterior.getList_columnas().get(i).setNomtab(objT_anterior.getNombtab());  //Le asigno el nombre a la columna para saber a que tabla pertenece
            //puede que esté demás
            //objR_anterior.getList_columnas().get(i).setNomcol(getChars(obtenerNombresColumnas(objR_anterior.getList_columnas().get(i).getNomcol()), 10)); //Asigna la columna
        }

        if (tablas.length == 1) {
            objTresultante.getListRegistro().add(objR_anterior);
            objTresultante = obtener_tabla_resultante(tablas, posicion + 1, objTresultante);
        } else {

            for (int i = 1; i < Lregistros.size(); i++) { //Recorro todas las tablas...
                objT_nueva = Lregistros.get(i);  //Obtengo la i_esima tabla...
                parts = tablas[i].split(" ");    //obtengo la informacion de la nueva tabla a seleccionar que se mando en el query junto con la informacion del "on"...

                for (int j = 0; j < objT_nueva.getListRegistro().size() && !bandera; j++) { //Recorro todos los registros
                    objR_nueva = objT_nueva.getListRegistro().get(j);   //obtengo el jota_esimo registro
                    aux = parts[4]; //obtengo la columna de la tabla anterior con la que se indica la relacion (on)...

                    for (int k = 0; k < objR_anterior.getList_columnas().size() && !bandera; k++) { //recorro todas las columnas del objeto registro anterior
                        aux2 = objR_anterior.getList_columnas().get(k).getNomtab() + "." + obtenerNombresColumnas(objR_anterior.getList_columnas().get(k).getNomcol()).trim();
                        if (aux2.equals(aux.trim())) { //compara los nombres de las columnas
                            aux = parts[2]; //obtengo la columna de la tabla tabla con la que se indica la relacion (on)...

                            for (int l = 0; l < objR_nueva.getList_columnas().size() && !bandera; l++) { //Recorro todas las columnas que tiene el registro nuevo
                                if (obtenerNombresColumnas(objR_nueva.getList_columnas().get(l).getNomcol()).split(" ")[0].equals(aux.split("\\.")[1].trim())) { //compara el nombre de la columna seleccionada para la relacion en el on con la columna seleccionada del objeto objR_nueva
                                    if (objR_anterior.getList_columnas().get(k).getContenido().trim().equals(objR_nueva.getList_columnas().get(l).getContenido().trim())) { //Pregunta si los contenidos de als columnas con las que se hace la relacion entre tablas es el mismo (PERDON SI NO SOY BUENO REDACTANDO LOS COMENTARIOS :'( (Soy pesimo con las palabras y la redaccion  jaja ok ya) )
                                        bandera = true; //Se pone en true la bandera ya que si se pudo relaciona el registro conla tabla nueva...
                                        for (int m = 0; m < objR_nueva.getList_columnas().size(); m++) {//Recorro todas las columnas para ponerles el nombre de la tabla a las que pertenecen...
                                            //objR_nueva.getList_columnas().get(m).setNomColS(objT_nueva.getNombtab() + "." +  objR_nueva.getList_columnas().get(i).getNomcol());  //Le asigno el nombre a la columna para saber a que tabla pertenece
                                            objR_nueva.getList_columnas().get(m).setNomtab(objT_nueva.getNombtab());  //Le asigno el nombre a la columna para saber a que tabla pertenece
                                            //objR_nueva.getList_columnas().get(m).setNomcol(getChars(obtenerNombresColumnas(objR_nueva.getList_columnas().get(m).getNomcol()), 10)); //Asigna la columna
                                            objR_anterior.getList_columnas().add(objR_nueva.getList_columnas().get(m)); //Agrega las nueva columnas de la nueva tabla relacionada al objeto registro (objR)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                //ya acabo de recorrer los registros de la t
                if (!bandera) { //para saber si se pudo relacionar el registro con la siguiente tabla si la bandera es false quiere decir que no se relaciono
                    i = Lregistros.size(); // para salir del ciclo
                    objTresultante = obtener_tabla_resultante(tablas, (posicion + 1), objTresultante); //ya no tiene chiste verificar las demas tablas por que ya no hara ninguna relacion...
                } else {
                    objT_anterior = objT_nueva;
                    //objR_anterior = objR_nueva;

                    if (i == (Lregistros.size() - 1)) {
                        objTresultante.getListRegistro().add(objR_anterior);
                        objTresultante = obtener_tabla_resultante(tablas, (posicion + 1), objTresultante);
                    } else {
                        bandera = false;
                    }
                }
            }
        }
        return objTresultante;
    }

    public String obtenerNombresColumnas(char NomCol[]) {
        String nombre = "";

        for (int i = 0; i < NomCol.length; i++) {
            nombre += NomCol[i] + "";
            if (nombre.contains(" ")) {
                nombre = nombre.split(" ")[0];
                i = NomCol.length;
            }
        }

        return nombre;
    }

    private void obtener_todos_registros(String tablas[], int posicion) {
        Registro objR;
        Tabla objT;
        Columna objC; //se crea un objeto de la clase columnas
        String[] parts, parts2, parts4;
        List<Columna> list_columnas;
        List<String> tables, columns, registros;

        if (posicion < tablas.length) {
            try {
                objT = new Tabla();
                parts = tablas[posicion].split(" "); //Obtiene tabla
                tables = objG.leer("BD\\" + objBD.getNombre() + ".dbs\\tablas");
                columns = objG.leer("BD\\" + objBD.getNombre() + ".dbs\\columnas");
                registros = objG.leer("BD\\" + objBD.getNombre() + ".dbs\\" + parts[0] + ".dat");

                for (int i = 0; i < tables.size(); i++) {
                    parts2 = tables.get(i).split(" "); //se divide la informacion de cada registro de archivo tablas

                    if (parts2[0].equals(parts[0])) { //se compara los nombres de l tabla
                        objT.setNombtab(parts2[0]); //se obtiene el nombre de la tabla
                        objT.setTabid(Integer.parseInt(parts2[1]));
                        i = tables.size(); //i se iguala al tamaño de la lista tables para que salga del for

                        for (int j = 0; j < registros.size(); j++) {
                            objR = new Registro();

                            list_columnas = new ArrayList<>();
                            for (int k = 0; k < columns.size(); k++) { // se recorren todas las columnas
                                parts4 = columns.get(k).split(" "); //se dividen la informacion de cada registro del archivo columnas
                                if (Integer.parseInt(parts4[2]) == objT.getTabid()) { //preguntamos si el id de la tabla coincide con las columnas.
                                    objC = new Columna(); //creamos el objeto columnas.
                                    objC.setColid(Integer.parseInt(parts4[3]));  //colocamos el id de la columna.
                                    objC.setNomcol(getChars(parts4[4], 10)); //colocamos el nombre de la columna.
                                    list_columnas.add(objC); //Agregamos la columna a la lista columnas.
                                }
                            }

                            objR.setList_columnas(list_columnas);
                            String parts3[] = registros.get(j).split(" ");
                            for (int l = 0; l < parts3.length; l++) {
                                objR.getList_columnas().get(l).setContenido(parts3[l]);
                            }
                            objT.getListRegistro().add(objR); //agrega el registro a la tabla
                        }
                        Lregistros.add(objT); //guarda la tabla con todos los registros
                    }
                }

                obtener_todos_registros(tablas, (posicion + 1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //llena Lcondiciones con condiciones y operadores lógicos
    private void tratado_condiciones(String condiciones) {
        int and = condiciones.indexOf(" and "), or = condiciones.indexOf(" or ");
        if (and == -1 && or == -1) {
            Lcondiciones.add(condiciones);
        } else {
            String nueva_condiciones = "";
            if (and < or && (and != -1) || (or == -1 && and > -1)) {
                for (int j = 0; j < condiciones.length(); j++) {
                    if (j < and) {
                        nueva_condiciones += condiciones.charAt(j);
                        if (j == (and - 1)) {
                            Lcondiciones.add(nueva_condiciones);
                            Lcondiciones.add("and");
                            nueva_condiciones = "";
                            j = (and + 4);
                        }
                    } else {
                        nueva_condiciones += condiciones.charAt(j);
                    }
                }
                tratado_condiciones(nueva_condiciones);
            } else {
                //if (or < and && (or != -1 && and == -1)) {
                //or = condiciones.indexOf(" or ");
                for (int j = 0; j < condiciones.length(); j++) {
                    if (j < or) {
                        nueva_condiciones += condiciones.charAt(j);
                        if (j == (or - 1)) {
                            Lcondiciones.add(nueva_condiciones);
                            Lcondiciones.add("or");
                            nueva_condiciones = "";
                            j = (or + 3);
                        }
                    } else {
                        nueva_condiciones += condiciones.charAt(j);
                    }
                }
                tratado_condiciones(nueva_condiciones);
                //}
            }
        }

    }

    private List<List<String[]>> verificarCondiciones() {
        List<List<String[]>> Lresultados = new ArrayList<>();
        boolean aux, bandera = true;
        for (int i = 0; i < Lcondiciones.size(); i++) {
            try {
                switch (Lcondiciones.get(i).split(" ")[1].charAt(0)) {
                    case '\'':
                    case '=':
                    case '<':
                    case '>':
                        if (!error.chCondDet(Lcondiciones.get(i))) {
                            error.setDslerr(100);
                            return null;
                        }
                        Lresultados.add(chCondicionDeterminista(Lcondiciones.get(i), LtabResAll));
                        break;
                    default:
                        if (!error.chCondDifusa(Lcondiciones.get(i))) {
                            System.out.println("Error sintactico dentro de la condicion where difusa");
                            error.setDslerr(100);
                            return null;
                        }
                        Lresultados.add(chCondicionDifusa(Lcondiciones.get(i), LtabResAll));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (Lresultados.get((Lresultados.size() - 1)).size() != 0) {
                aux = true;
            } else {
                aux = false;
            }
            i++;

            if ((i - 1) == 0) {
                bandera = aux;
            } else if (((i - 1) % 2) == 0) {

                if (Lcondiciones.get(i - 2).equals("and")) {
                    bandera &= aux;
                } else {
                    bandera |= aux;
                }
            }

        }
        if (bandera) {
            return Lresultados;
        } else {
            return null;
        }
    }

    private List<resulWhere> resTotCond(List<List<String[]>> Lresultados, List<resulWhere> registrosA, int posicion, int contB) { //Obtiene el resultado total de las condiciones :3 

        List<String[]> registrosCondicion; //Posicion (i)
        List<resulWhere> registros = new ArrayList<>(); //registros
        String[] grados;
        int posAux;
        resulWhere objRW;
        boolean bandera = false;
        if (posicion == 0) {
            registrosA = new ArrayList<>();

            for (int i = 0; i < Lresultados.get(0).size(); i++) {
                objRW = new resulWhere();
                objRW.setPosicion(Integer.parseInt(Lresultados.get(0).get(i)[0]));
                grados = objRW.prepararGrados(Lcondiciones.get(0), Lresultados.get(0).get(i));

                if (grados != null) {
                    objRW.getGradosPertenencia().add(grados);
                }
                registrosA.add(objRW);
            }
            posicion++;
        }

        if (posicion >= Lresultados.size()) {
            return registrosA;
        }

        registrosCondicion = igualar_registros(Lresultados.get(posicion)); //obtiene todos los resultados 
        if (Lcondiciones.get(contB).equals("and")) {
            for (int i = 0; i < registrosA.size(); i++) {
                bandera = false;

                for (int j = 0; j < registrosCondicion.size() && !bandera; j++) {
                    if (registrosA.get(i).getPosicion() == Integer.parseInt(registrosCondicion.get(j)[0])) {
                        objRW = registrosA.get(i);
                        grados = objRW.prepararGrados(Lcondiciones.get((contB + 1)), registrosCondicion.get(j));

                        if (grados != null) {
                            objRW.getGradosPertenencia().add(grados);
                        }
                        registros.add(objRW);
                        bandera = true;
                    }
                }
            }

            registros = resTotCond(Lresultados, registros, (posicion + 1), (contB + 2));

        } else { //Es un or...
            //Recorre primero estructura registros A y agrega todo lo que tenga A
            for (int i = 0; i < registrosA.size(); i++) {
                bandera = false;

                for (int j = 0; j < registrosCondicion.size() && !bandera; j++) {
                    if (registrosA.get(i).getPosicion() == Integer.parseInt(registrosCondicion.get(j)[0])) {
                        objRW = registrosA.get(i);
                        grados = objRW.prepararGrados(Lcondiciones.get((contB + 1)), registrosCondicion.get(j));

                        if (grados != null) {
                            objRW.getGradosPertenencia().add(grados);
                        }
                        registros.add(objRW);
                        bandera = true;
                    }
                }

                if (!bandera) {
                    objRW = registrosA.get(i);
                    registros.add(objRW);
                }
            }

            //Recorre ahora la estructura registrosCondicion... para agregar los que estan en registroCondicion y no estan en registroA...
            for (int i = 0; i < registrosCondicion.size(); i++) {
                bandera = false;

                for (int j = 0; j < registrosA.size() && !bandera; j++) {
                    if (Integer.parseInt(registrosCondicion.get(i)[0]) == registrosA.get(j).getPosicion()) {
                        bandera = true;
                    }
                }
                if (!bandera) {
                    objRW = new resulWhere();
                    objRW.setPosicion(Integer.parseInt(registrosCondicion.get(i)[0]));
                    grados = objRW.prepararGrados(Lcondiciones.get((contB + 1)), registrosCondicion.get(i));

                    if (grados != null) {
                        objRW.getGradosPertenencia().add(grados);
                    }
                    registros.add(objRW);
                    bandera = true;
                }
            }

            registros = resTotCond(Lresultados, registros, (posicion + 1), (contB + 2));

        }

        /*List<String[]> registrosA; //registros  Anterior
         List<String[]> registros = new ArrayList<>(); //registros
        
         int contB = 1; //obtiene las posiciones de los operadores logicos dentro de la estructura Lcondiciones.
         int posicion = 0;
         boolean bandera = false;
         registrosA = Lresultados.get(0);
         registros = igualar_registros(registrosA);

         for (int i = 1; i < Lresultados.size(); i++) {
         registrosCondicion = Lresultados.get(i);
         if (Lcondiciones.get(contB).equals("and")) {
         if (registrosA.size() > 0 && registrosCondicion.size() > 0) { //Si la lista tiene registros... //μ
         try {
         for (int j = 0; j < registrosA.size(); j++) {

         for (int k = 0; k < registrosCondicion.size() && !bandera; k++) {
         if (registrosA.get(j)[0].equals(registrosCondicion.get(k)[0])) {
         bandera = true;
         posicion = k;
         }
         }
         if (bandera) {
         if (!registrosCondicion.get(posicion)[1].equals("")) {
         registros.get(j)[0] = (registrosCondicion.get(posicion)[0]);
         registros.get(j)[1] = (registrosCondicion.get(posicion)[1]);
         posicion = 0;
         }
         bandera = false;
         } else {
         registros.remove(j);
         }
         }
         } catch (Exception e) {
                        
         }

         }
         } else { //si es un or
         if (registrosA.isEmpty()) { //Si la lista tiene registros... //μ
         registros = registrosCondicion;
         } else {
         for (int j = 0; j < registrosA.size(); j++) {

         for (int k = 0; k < registrosCondicion.size() && !bandera; k++) {
         if (registrosA.get(j)[0].equals(registrosCondicion.get(k)[0])) {
         posicion = k;
         bandera = true;
         }
         }
         if (!bandera) {
         registros.add(registrosCondicion.get(posicion));
         posicion = 0;
         } else {
         if (!registrosCondicion.get(posicion)[1].equals("")) {
         registros.get(j)[0] = registrosCondicion.get(posicion)[0];
         registros.get(j)[1] = registrosCondicion.get(posicion)[1];
         posicion = 0;
         }
         bandera = false;
         }
         }
         }
         }
         contB += 2;
         registrosA = registros;
         }
         return registros;
         */
        return registros;
    }

    private List<String[]> igualar_registros(List<String[]> R) {
        List<String[]> Rc = new ArrayList<>();
        String[] info;
        for (int i = 0; i < R.size(); i++) {
            info = new String[2];
            info[0] = R.get(i)[0];
            info[1] = R.get(i)[1];
            Rc.add(info);
        }
        return Rc;
    }

    private List<String[]> chCondicionDeterminista(String condicion, Tabla objTresultante) {
        List<String[]> posiciones = new ArrayList<>();
        Registro objR;
        Columna objC;
        boolean bandera = false;

        for (int i = 0; i < objTresultante.getListRegistro().size(); i++) {
            objR = objTresultante.getListRegistro().get(i);

            for (int j = 0; j < objR.getList_columnas().size(); j++) {
                objC = objR.getList_columnas().get(j);

                if (objC.getNomtab().equals(condicion.split(" ")[0].split("\\.")[0].trim()) && obtenerNombresColumnas(objC.getNomcol()).equals(condicion.split(" ")[0].split("\\.")[1].trim())) {
                    try {
                        switch (condicion.split(" ")[1]) {
                            case "=":
                                if (objC.getContenido().trim().equals(condicion.split(" ")[2])) {
                                    bandera = true;
                                }
                                break;
                            case "<":
                                if (Double.parseDouble(objC.getContenido()) < Double.parseDouble(condicion.split(" ")[2])) {
                                    bandera = true;
                                }
                                break;
                            case ">":
                                if (Double.parseDouble(objC.getContenido()) > Double.parseDouble(condicion.split(" ")[2])) {
                                    bandera = true;
                                }
                                break;
                            case "<=":
                                if (Double.parseDouble(objC.getContenido()) <= Double.parseDouble(condicion.split(" ")[2])) {
                                    bandera = true;
                                }
                                break;
                            case ">=":
                                if (Double.parseDouble(objC.getContenido()) >= Double.parseDouble(condicion.split(" ")[2])) {
                                    bandera = true;
                                }
                                break;
                        }
                    } catch (Exception e) {
                    }

                    //j = objR.getList_columnas().size();
                    break;
                }
            }
            if (bandera) {
                String array[] = {i + "", ""};
                posiciones.add(array);
                bandera = false;
            }
        }

        return posiciones;
    }

    //ANTES DE USAR ESTE MÉTODO, DEBE USARSE: error.chCondDifusa
    //si ése método regresa false, la escritura de dicha condición es errónea
    //si regresa true, ya se puede usar este método
    /*
     * String condicion NO debe contener la palabra where
     * @return Tabla contiene los registros que cumplen con la condición
     */
    private List<String[]> chCondicionDifusa(String condicion, Tabla objTResultante) throws IOException {
        Sistema objS = new Sistema();
        List<Registro> lResultado;
        List<String[]> lResFinal;
        VariableEntrada objV = new VariableEntrada("", this);
        String[] partsCondition = condicion.split(" ");
        String registro, type = "#";
        Double[] criticPoints;

        //OBTENCIÓN DEL UNIVERSO DE DISCURSO
        registro = objS.getUniverse(RUTA + "SED/" + partsCondition[0], partsCondition[2]); //CUIDADO, podría contener null
        objV.getObjU().setTable(partsCondition[0].split("\\.")[0]); //asigna nombre de la tabla
        objV.getObjU().setVariable(partsCondition[0].split("\\.")[1]); //asigna nombre de la variable
        objV.askDiscourseUniverse(registro); //crea archivo de nueva variable, escribe origen, fin, unidades y variable(tmp)

        //CREACIÓN DEL TRAPECIO
        criticPoints = objS.getCriticPoints(partsCondition, RUTA + "/SED/" + partsCondition[0]); //PODRÍA CONTENER NULL 
        if (criticPoints.length == 2) {
            type = "$";
        }
        objV.createTrapezoids(
                criticPoints[0] + " " + criticPoints[1], partsCondition, RUTA + "SED/" + partsCondition[0] + ".tmp");

        //FUZZYFICATION
        lResultado = objS.fuzzyResult(RUTA, partsCondition, type);

        //Obtiene estructura final
        lResFinal = objS.comparaRegistros(objTResultante, lResultado);

        objG.deleteFile(RUTA + "SED/" + partsCondition[0] + ".tmp"); //ya no se necesita el archivo tmp

        return lResFinal;
    }

//        posibles casos
//        query = "UPDATE prueba SET col1=val1, col2=val2 WHERE col1=D AND col2=F";
//        query = "UPDATE prueba SET col1=val1 WHERE condicion col1=D OR col=D";
//        query = "UPDATE prueba SET col1=val1";
    public boolean chUpdate() {
        int res;
        Tabla objT = new Tabla();
        String[] columnas, parts;

        //checa si la base de datos está activa
        error.chBdActiva("update");
        if (error.getDslerr() != 0) {
            return false;
        }

        query = query.toLowerCase();
        parts = query.split("update ");
        if (parts.length == 1) { //no hay update
            return false;
        }
        parts = parts[1].split(" set ");
        objT.setNombtab(parts[0]); //Nombre de la tabla
        res = error.chTablaExiste("update", objT.getNombtab());
        if (error.getDslerr() != 0) {
            return false;
        }
        objT.setTabid(res); //id de la tabla

        if (parts[1].contains("where")) {
            parts = parts[1].split(" where "); //obtiene columnas del where

            //comenzar a recorrer buscando las condiciones
            //teniendo en cuenta los AND y OR
            whereConditions(parts, objT);

            columnas = parts[0].split(",");
            for (String columna : columnas) {
                parts = columna.split("=");
                res = error.chColumnasExisten("update", parts[0], objT.getTabid()); //obtiene el id de la columna
                if (error.getDslerr() != 0) {
                    return false;
                }
                objT.getColumnas().add(res + "");

                //verifica que cada valor corresponda al tipo de dato de cada columna
                error.chComparaTipoColumnas("update", objT.getTabid(), res, parts[1]);
                if (error.getDslerr() != 0) {
                    return false;
                }

                if (!whereConditions(parts, objT)) {
                    return false;
                }
            }
        }

        //TODO
        //checar tipo de dato de cada columna de update, no de where
        //checar integridad
        //checar valores de indices
        //actualizar archivo tablas
        //actualizar archivo indices
        return true;
    }

    //se encarga de procesar un estatuto where para obtener los operadores lógicos booleanos y las condiciones
    private boolean whereConditions(String[] where, Tabla objT) {
        List<String> logic = new ArrayList<>();
        List<Boolean> results = new ArrayList<>();
        String[] whereElements, parts;
        whereElements = where[1].split(" ");

        for (String whereE : whereElements) { //recorre cada elemento del estatuto where

            if (whereE.equals("and") || whereE.equals("or")) {
                logic.add(whereE); //guarda los operadores
            } else {
                //guarda el resultado booleano de cada condición
                if (whereE.contains("=")) {
                    //condición determinista
                    parts = whereE.split("=");
                    //valida si la columna existe en la tabla
                    error.chColumnasExisten("update", parts[0], objT.getTabid());
                    if (error.getDslerr() != 0) {
                        return false;
                    }
                    //TODO, modificar
                    //results.add(chCrispCondition(whereE));
                } else {
                    //condición difusa
                    //TODO, modificar
                    //results.add(chFuzzyCondition(whereE));
                }
            }
        }

        return chConditions(logic, results);

    }

    //procesa las condiciones concatenando los resultados booleanos
    private boolean chConditions(List<String> logic, List<Boolean> results) {
        boolean r = results.get(0); //obtiene el resultado de la primera condición
        for (int i = 1; i < results.size(); i++) { //recorre los resultados de las condiciones

            for (String log : logic) { //recorre los operadores lógicos guardados

                if (log.equals("and")) {
                    r &= results.get(i);
                } else {
                    r |= results.get(i);
                }

            }
        }

        return r;
    }

    private boolean chShowDBFiles() {
        error.chBdActiva("showSedFiles");
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
        String parts[];
        List<String> lista, listTables = new ArrayList<>();
        try {
            lista = objG.leer(RUTA + "tablas");
            System.out.println("TABLAS");
            for (int i = 0; i < lista.size(); i++) {
                parts = lista.get(i).split(" ");
                listTables.add(parts[0]); //guardo el nombre de las tablas
                System.out.println(lista.get(i));
            }

            for (String table : listTables) {
                System.out.println("TABLA " + table);
                lista = objG.leer(RUTA + table + ".dat");
                for (String listT : lista) {
                    System.out.println(listT);
                }
            }

            System.out.println("COLUMNAS");
            lista = objG.leer(RUTA + "columnas");
            for (int i = 0; i < lista.size(); i++) {
                System.out.println(lista.get(i));
            }

            System.out.println("INDICES");
            lista = objG.leer(RUTA + "indices");
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
            regDatos = objGsed.leer(RUTA + "\\SED\\Datos");
            for (String regDato : regDatos) {
                //System.out.println("SED/" + regDato);
                regFiles = objGsed.leer((RUTA + "SED\\" + regDato).trim());

                System.out.println("ARCHIVO " + regDato);
                for (String regFile : regFiles) {
                    System.out.println(regFile);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
