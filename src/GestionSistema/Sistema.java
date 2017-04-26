package GestionSistema;

import SED.Gauss;
import SED.MotorInferencia;
import SGBD.Automatas;
import SGBD.Columna;
import SGBD.Errores;
import SGBD.Registro;
import SGBD.Tabla;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Tenistas
 */
public class Sistema {
    
    private final GestionArchivos objG;
    
    public Sistema() {
        objG = new GestionArchivos();
    }

    /**
     * Obtiene el universo de discurso, las unidades y el nombre de la variable
     * linguística correspondiente a una tabla y etiqueta específicas
     *
     * @param ruta Contiene el nombre de la tabla y la columna
     * @param etiqueta Etiqueta linguística
     * @return Registro con todos los datos o null
     * @throws IOException
     */
    public String getUniverse(String ruta, String etiqueta) throws IOException {
        List<String> registros = objG.leer(ruta);
        String[] parts;
        
        for (String r : registros) {
            parts = r.split(" ");
            return parts[0] + " " + parts[1] + " " + parts[2] + " tmp";
        }
        
        return null;
    }

    /**
     * Obtiene los puntos críticos para una condición difusa
     *
     * @param partsCondition Elementos de la condición difusa
     * @param ruta Ruta a escribir los datos = RUTA + "/SED/" +
     * partsCondition[0]
     * @return Double[] con los puntos críticos o null si no se encontró algo
     * @throws IOException
     */
    public Double[] getCriticPoints(String[] partsCondition, String ruta) throws IOException {
        String[] parts2;
        double tmpDistance, fin = 0, origen;
        Double[] criticPoints = new Double[2]; //prepara el contenedor de los puntos críticos
        List<String> listR;
        
        if (partsCondition[2].contains("$")) {
            //se hará el proceso en base a una etiqueta linguística
            //obtiene la información de la variable linguística
            listR = objG.leer(ruta);
            for (String r : listR) {
                if (r.contains(partsCondition[2].split("\\$")[1])) {
                    //contiene la etiqueta linguistica
                    parts2 = r.split(" ");

                    //obtiene la distancia entre el punto inferior izquierdo y el punto crítico 1
                    tmpDistance = Math.abs(Double.parseDouble(parts2[1]) - Double.parseDouble(parts2[4]));
                    
                    if (partsCondition[1].equals("fleq")) {
                        if (parts2[0].equals("Trapezoide")) {
                            criticPoints[0] = Double.parseDouble(parts2[2]); //obtiene el punto crítico más a la derecha
                            //obtiene el punto inferior de la derecha
                            criticPoints[1] = criticPoints[0] + tmpDistance;
                        } else {
                            //parts2 == SemiTrapezoide
                            if (parts2[2].equals("i")) {
                                criticPoints[0] = Double.parseDouble(parts2[1]); //obtiene el punto crítico
                                criticPoints[1] = Double.parseDouble(parts2[4]); //obtiene el punto crítico
                            } else {
                                //parts2 == "d"
                                criticPoints[0] = fin; //obtiene el punto crítico
                                criticPoints[1] = fin + tmpDistance;
                            }
                        }
                    } else {
                        //partsC == FGEG
                        //toma el punto crítico más a la izquierda
                        criticPoints[0] = Double.parseDouble(parts2[1]);
                        criticPoints[1] = Double.parseDouble(parts2[4]); //toma el punto que va a la izquierda
                        if (parts2[0].equals("SemiTrapezoide")) {
                            if (parts2[2].equals("i")) {
                                criticPoints[0] = 0.0;
                                criticPoints[1] = criticPoints[0] - tmpDistance;
                            }
                        }
                    }
                    return criticPoints; //ya obtuvo los puntos críticos 

                } else if (r.split(" ").length == 4) {
                    //universo de discurso
                    origen = Double.parseDouble(r.split(" ")[0]);
                    fin = Double.parseDouble(r.split(" ")[1]);
                }
            }
        } else {
            //partsCondition[2] == #
            criticPoints[0] = Double.parseDouble(partsCondition[2].split("#")[1]); //obtiene el valor a lado del #
            if (partsCondition[1].equals("fleq")) {
                criticPoints[1] = 2 * criticPoints[0];
            } else {
                listR = objG.leer(ruta);
                for (String r : listR) {
                    if (r.split(" ").length == 4) {
                        //universo de discurso
                        origen = Double.parseDouble(r.split(" ")[0]);
                        fin = Double.parseDouble(r.split(" ")[1]);
                        break;
                    }
                }
                tmpDistance = fin - criticPoints[0];
                criticPoints[1] = criticPoints[0] - tmpDistance;
            }
            
            return criticPoints;
        }
        
        return null; //no encontró la etiqueta linguistica
    }

    /**
     * Para pruebas
     *
     * @param ruta Archivo a mostrar
     * @throws IOException
     * @deprecated
     */
    public void showFile(String ruta) throws IOException {
        List<String> listRegistros = objG.leer(ruta);
        listRegistros.forEach((registro) -> {
            System.out.println(registro);
        });
    }

    //type = $ | #
    public List<Registro> fuzzyResult(String rutaBase, String[] partsCondition, String whereType) throws IOException {
        List<Registro> lReg = new ArrayList<>();
        Registro objR;
        List<Columna> lCol = new ArrayList<>();
        Columna objC;
        String[] tableCol = partsCondition[0].split("\\."), parts; //pos 0 = tabla, pos 1 = columna
        String nomtab = null;
        Automatas objA = new Automatas();
        
        List<String> lTablas = objG.leer(rutaBase + "tablas");
        List<String> lColumnas = objG.leer(rutaBase + "columnas");
        List<String> lRegistro = objG.leer(rutaBase + tableCol[0] + ".dat");
        
        int tabid = 0, cont;
        Integer poscol = null;
        String fuzzyRes;
        
        for (String rTabla : lTablas) {
            if (rTabla.contains(tableCol[0])) { //contiene el nombre de la tabla?
                parts = rTabla.split(" ");
                tabid = Integer.parseInt(parts[1]); //obtiene el id de la tabla
                nomtab = parts[0];
                break;
            }
        }
        
        cont = 0;
        for (String rColumna : lColumnas) {
            objC = new Columna();
            parts = rColumna.split(" ");
            if (Integer.parseInt(parts[2]) == tabid) { //es la tabla correcta?
                objC.setNomcol(objA.getChars(parts[4], 10));
                objC.setNomtab(nomtab);
                lCol.add(objC);
                if (parts[4].equals(tableCol[1])) {
                    poscol = cont;
                }
            }
            cont++;
        }
        
        for (int i = 0; i < lRegistro.size(); i++) {
            parts = lRegistro.get(i).split(" ");
            fuzzyRes = fuzzyProcess$(parts[poscol], partsCondition, rutaBase + "SED/" + partsCondition[0] + ".tmp");
            
            if (Double.parseDouble(fuzzyRes) != 0) {
                for (int j = 0; j < parts.length; j++) {
                    lCol.get(j).setContenido(parts[j]);
                    lCol.get(j).setMembresia(fuzzyRes);
                }
                //lCol = copiar(lCol, objA);
                objR = new Registro();
                objR.setList_columnas(copiaEstructura(lCol, objA));
                lReg.add(objR);
            }
        }
        
        return lReg;
    }
    
    private List<Columna> copiaEstructura(List<Columna> lCol, Automatas objA) {
        List<Columna> tmp = new ArrayList<>();
        Columna objC;
        String nomCol, membresia;
        
        for (Columna c : lCol) {
            objC = new Columna();
            
            nomCol = objA.obtenerNombresColumnas(c.getNomcol());
            objC.setNomcol(objA.getChars(nomCol, 10));
            objC.setNomtab(c.getNomtab());
            objC.setContenido(c.getContenido());
            objC.setMembresia(c.getMembresia());
            tmp.add(objC);
        }
        return tmp;
    }
    
    private String fuzzyProcess$(String valor, String[] partsCondition, String ruta) throws IOException {
        MotorInferencia objMI = new MotorInferencia();
        Double tmp, tmp2;
        double value;
        String etiqueta, simbolo = obtenerSimbolo(partsCondition[2]), whLabel = quitaSimbolo(partsCondition[2]);
        String[] shape;
        
        if (!valor.contains("<")) {
            //valor numérico
            try {
                value = Double.parseDouble(valor);
                objMI.fuzzyfication(value, ruta, simbolo); //CHECAR ESTO!!
                return objMI.getResultado();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            //quito los <>
            etiqueta = valor.split("<")[1].split(">")[0];
            shape = tipoFigura(etiqueta, ruta.split(".tmp")[0]);

            //veo si es la et linguistica indicada en el where
            if (whLabel.equals(etiqueta)) {
                return "1"; //porque es la misma etiqueta linguistica
            } else {
                //buscar algún posible punto de intersección
                tmp = intPoint(partsCondition, etiqueta, ruta.split("SED")[0], null);
                if (0 >= tmp || tmp >= 1) {
                    tmp2 = intPoint(partsCondition, etiqueta, ruta.split("SED")[0], "otro");
                    if (tmp2 != null) {
                        if (0 <= tmp2 && tmp2 <= 1) {
                            tmp = tmp2;
                        }
                    }
                }
                //se encontró un punto de intersección y está dentro del rango [0,1]
                if (tmp != null && (0 <= tmp && tmp <= 1)) {
                    return tmp + ""; //se regresa el valor en Y(grados membresia)
                } else {
                    if (tmp > 0) {
                        //es una etiqueta interna
                        return "1";
                    } else {
                        if (shape.length == 2) {
                            //es un semitrapezoide
                            if (partsCondition[1].equals("fleq")) {
                                if (shape[1].equals("i")) {
                                    return "1";
                                } else {
                                    return "0";
                                }
                            } else {
                                //fgeq
                                if (shape[1].equals("d")) {
                                    return "1";
                                } else {
                                    return "0";
                                }
                            }
                        }
                        return "0";
                    }
                }
            }
        }
        return null;
    }

    //checa el caso de #
    private String fuzzyProcessHash() {
        return null;
    }
    
    private Double intPoint(String[] partsCondition, String simpleValue, String rutaBase, String ind) throws IOException {
        Gauss objGauss;
        //obtengo registros del archivo temporal
        List<String> listSemiT = objG.leer(rutaBase + "SED/" + partsCondition[0] + ".tmp");
        //obtengo los registros de la variable linguistica
        List<String> listTrap = objG.leer(rutaBase + "SED/" + partsCondition[0]);
        double[] pSemiTrap, pTrap, ecSemiTrap, ecTrap, pInt; //contenedores de los puntos

        //obtengo dos puntos por cada trapecio
        pSemiTrap = getPointsSemiTrap(listSemiT);
        pTrap = getPointsTrap(partsCondition, simpleValue, listTrap, ind);

        //obtengo las ecuaciones de esos dos puntos
        //cada ecuacion tiene la estructura: X + Y = Constante (los signos varían)
        ecSemiTrap = obtenerEcuacion(pSemiTrap);
        ecTrap = obtenerEcuacion(pTrap);

        //mandar a gauss
        objGauss = new Gauss(ecSemiTrap, ecTrap);
        pInt = objGauss.getIntPoint(); //obtiene el punto de interseccion
        if (pInt == null) {
            return null;
        } else {
            return pInt[1]; //grado de membresía
        }
    }

    /**
     * obtiene los puntos del semitrapezoide
     *
     * @param lSemiTrap Lista de registros del archivo temporal
     * @return valor en x de dos puntos
     */
    private double[] getPointsSemiTrap(List<String> lSemiTrap) {
        String[] parts;
        double[] points = null;
        for (String r : lSemiTrap) {
            if (r.contains("SemiTrapezoide")) {
                points = new double[2];
                parts = r.split(" ");
                points[0] = Double.parseDouble(parts[1]); //punto crítico
                points[1] = Double.parseDouble(parts[4]); //punto inferior
            }
        }
        
        return points;
    }

    /**
     * obtiene los puntos del trapezoide
     *
     * @param partsCondition Elementos de la condición difusa
     * @param rEtiqueta Etiqueta obtenida de los registros
     * @param lTrap Lista de registros de la variable linguistica
     * @return valor en x de dos puntos
     */
    private double[] getPointsTrap(String[] partsCondition, String rEtiqueta, List<String> lTrap, String ind) {
        double[] points = null;
        String[] parts;
        double dist, origen = 0, fin = 0;
        
        for (String r : lTrap) {
            if (r.contains(rEtiqueta)) {
                points = new double[2];
                parts = r.split(" ");
                if (partsCondition[1].equals("fleq")) {
                    
                    if (parts[0].equals("Trapezoide")) {
                        //toma los puntos más a la izquierda del trapezoide o semitrap
                        if (ind == null) {
                            points[0] = Double.parseDouble(parts[1]); //punto crítico más a la izq
                            points[1] = Double.parseDouble(parts[4]); //obtengo el punto inferior que necesito
                        } else {
                            points[0] = Double.parseDouble(parts[2]);
                            dist = Double.parseDouble(parts[1]) - Double.parseDouble(parts[4]);
                            points[1] = points[0] + dist;
                        }
                    } else {
                        //parts[1] == SemiTrapezoide

                        //TODO, checar la orientación
                        points[0] = Double.parseDouble(parts[1]); //punto crítico más a la izq
                        points[1] = Double.parseDouble(parts[4]);
                    }
                } else {
                    //partsCondition[1] == fgeq
                    //toma los puntos más a la derecha del trapezoide o semitrap
                    if (parts[0].equals("Trapezoide")) {
                        if (ind == null) {
                            points[0] = Double.parseDouble(parts[2]);
                            dist = Math.abs(Double.parseDouble(parts[1]) - Double.parseDouble(parts[4]));
                            points[1] = points[0] + dist;
                        } else {
                            points[0] = Double.parseDouble(parts[1]);
                            points[1] = Double.parseDouble(parts[4]);
                        }
                    } else {
                        //parts[1] == SemiTrapezoide

                        //TODO, hace falta checar esto mismo para el caso fleq
                        if (ind != null) {
                            if (parts[2].equals("i")) {
                                points[0] = points[1] = origen;
                            } else {
                                //parts[2] == d
                                points[0] = points[1] = fin;
                            }
                        } else {
                            points[0] = Double.parseDouble(parts[1]);
                            points[1] = Double.parseDouble(parts[4]);
                        }
                    }
                }
                return points;
            } else if (r.split(" ").length == 4) {
                parts = r.split(" ");
                origen = Double.parseDouble(parts[0]);
                fin = Double.parseDouble(parts[1]);
            }
        }
        return points;
    }

    /**
     * quita el $ o el #
     *
     * @param etiqueta $joven o #19
     * @return Etiqueta sin el símbolo
     */
    public String quitaSimbolo(String etiqueta) {
        if (etiqueta.contains("$")) {
            return etiqueta.split("\\$")[1];
        } else { //tiene #
            return etiqueta.split("\\#")[1];
        }
    }

    /**
     * Genera una ecuación de la recta en base a dos puntos
     *
     * @param puntos valor en X de los dos puntos
     * @return ecuación de la recta (solo los valores)
     */
    private double[] obtenerEcuacion(double[] puntos) {
        //x1 = puntos[0];
        //x2 = puntos[1];
        //y1 = 1; por el punto critico
        //y2 = 0; por el punto inferior

        double partX, partY;
        double[] ecX = new double[2], ecY = new double[2], ecFinal = new double[3];
        
        partX = puntos[1] - puntos[0];
        partY = -1; //porque se hace siempre la operación 0 - 1

        ecX[0] = partY;
        ecX[1] = -(partY * puntos[0]);
        
        ecY[0] = partX;
        ecY[1] = -(partX * 1);

        //pasamos ecY al otro lado del igual
        ecY[0] *= -1;
        ecY[1] *= -1;
        
        ecFinal[0] = ecX[0];
        ecFinal[1] = ecY[0];
        //sumamos las constantes y las pasamos al otro lado del igual
        ecFinal[2] = (ecX[1] + ecY[1]) * -1;
        return ecFinal;
    }
    
    public List<String[]> comparaRegistros(Tabla objTResultante, List<Registro> lResultado) {
        List<String[]> lFinal = new ArrayList<>();
        Registro objR;
        Columna objC, objC2;
        boolean f1, f2;
        int contR;
        
        for (int i = 0; i < lResultado.size(); i++) {
            contR = 0;
            f1 = true;
            objR = lResultado.get(i);
            for (int j = 0; j < objR.getList_columnas().size() && f1; j++) {
                f2 = true;
                objC = objR.getList_columnas().get(j);
                
                for (int k = 0; k < objTResultante.getListRegistro().get(contR).getList_columnas().size() && f2; k++) {
                    objC2 = objTResultante.getListRegistro().get(contR).getList_columnas().get(k);
                    
                    if (objC.getNomtab().equals(objC2.getNomtab())
                            && Arrays.toString(objC.getNomcol()).equals(Arrays.toString(objC2.getNomcol()))) {
                        
                        if (objC.getContenido().equals(objC2.getContenido())) {
                            if (j == objR.getList_columnas().size() - 1) {
                                lFinal.add(new String[]{contR + "", objC.getMembresia()});
                                f1 = false;
                            }
                            f2 = false;
                        } else {
                            ++contR;
                            k = -1;
                        }
                    }
                }
            }
        }
        return lFinal;
    }

    //busca el tipo de figura de una etiqueta
    private String[] tipoFigura(String etiqueta, String ruta) throws IOException {
        List<String> lRegistros = objG.leer(ruta);
        String[] parts;
        
        for (String r : lRegistros) {
            if (r.contains(etiqueta)) {
                parts = r.split(" ");
                
                if (parts[0].equals("Trapezoide")) {
                    return new String[]{parts[0]}; //solo manda el tipo
                } else {
                    //semitrapezoide
                    return new String[]{parts[0], parts[2]}; //manda el semitrapezoide y su orientación
                }
            }
        }
        
        return null; //no encontró la etiqueta
    }
    
    private String obtenerSimbolo(String valor) {
        if (valor.contains("$")) {
            return "$";
        } else {
            return "#";
        }
    }

    /**
     * Usado por chSelect - Automatas
     *
     * @param sentencias
     * @param objE
     * @return
     */
    public boolean obtenerTablas(String[] sentencias, Errores objE) {
        String parts[];
        
        for (String tabla : sentencias) {
            parts = tabla.split(" ");
            
            if (parts.length == 1) {
                objE.chTablaExiste("select", tabla);
                if (objE.getDslerr() != 0) {
                    return false;
                }
            } else {
                for (String part : parts) {
                    if (part.contains(".")) {
                        tabla = part.split("\\.")[0]; //agarra la tabla
                    } else if (!part.equals("on") && !part.equals("=")) {
                        tabla = part;
                    }
                    objE.chTablaExiste("select", tabla);
                    if (objE.getDslerr() != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    public List<String> comparaRegistros(List<Registro> lResultado, List<String> lRegFiles) {
        List<Registro> lFinal = new ArrayList<>();
        List<Columna> lCols;
        String[] parts;
        boolean add;
        
        if (lResultado.size() == lRegFiles.size()) {
            return new ArrayList<>(); // lo envía vacío
        }
        
        for (int i = 0; i < lResultado.size(); i++) {
            lCols = lResultado.get(i).getList_columnas();
            
            for (int k = 0; k < lRegFiles.size(); k++) {
                add = true;
                parts = lRegFiles.get(k).split(" ");
                
                for (int j = 0; j < parts.length && add; j++) {
                    //parts.lenght y lCols.size debería tener el mismo tamaño
                    if (!lCols.get(j).getContenido().equals(parts[j])) {
                        add = false;
                    }
                }
                
                if (add) {
                    lRegFiles.remove(k);
                    break;
                }
            }
        }
        return lRegFiles;
    }
    
    public void escribeRegistros(List<String> lString, String nomtab, String rutaBase) throws IOException {
        objG.deleteFile(rutaBase + nomtab + ".dat");
        objG.crearArchivo(rutaBase + nomtab + ".dat");
        for (int i = 0; i < lString.size(); i++) {
            objG.escribir(rutaBase + "/" + nomtab + ".dat", i, lString.get(i), "final");
        }
    }
    
    public void obtieneColumnas(String[] condiciones) {
        List<String> lCols = new ArrayList<>();
        
    }
    
}
