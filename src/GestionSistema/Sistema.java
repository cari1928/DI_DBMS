package GestionSistema;

import SED.Gauss;
import SED.MotorInferencia;
import SGBD.Columna;
import SGBD.Registro;
import SGBD.Tabla;
import java.io.IOException;
import java.util.ArrayList;
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
        Double[] criticPoints;
        List<String> listR;

        if (partsCondition[2].contains("$")) {
            criticPoints = new Double[2]; //prepara el contenedor de los puntos críticos
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
            criticPoints = new Double[1]; //será un semtrapezoide
            criticPoints[0] = Double.parseDouble(partsCondition[2].split("#")[0]); //obtiene el valor a lado del #
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
    public List<String[]> fuzzyResult(String rutaBase, String[] partsCondition, String whereType) throws IOException {
        String[] tableCol = partsCondition[0].split("\\."), parts; //pos 0 = tabla, pos 1 = columna

        List<String> lTablas = objG.leer(rutaBase + "tablas");
        List<String> lColumnas = objG.leer(rutaBase + "columnas");
        List<String> lRegistro = objG.leer(rutaBase + tableCol[0] + ".dat");
        List<String[]> rFinales = new ArrayList<>();

        int tabid = 0, poscol = 0, cont;
        String fuzzyRes;

        for (String rTabla : lTablas) {
            if (rTabla.contains(tableCol[0])) { //contiene el nombre de la tabla?
                tabid = Integer.parseInt(rTabla.split(" ")[1]); //obtiene el id de la tabla
                break;
            }
        }

        cont = 0;
        for (String rColumna : lColumnas) {
            parts = rColumna.split(" ");
            if (Integer.parseInt(parts[2]) == tabid) { //es la tabla correcta?
                if (parts[4].equals(tableCol[1])) { //coincide el nombre de la columna?
                    poscol = cont;
                    break;
                }
            }
            cont++;
        }

        for (String registro : lRegistro) {
            parts = registro.split(" ");
            fuzzyRes = fuzzyProcess$(parts[poscol], partsCondition, rutaBase + "SED/" + partsCondition[0] + ".tmp");

            if (Double.parseDouble(fuzzyRes) != 0) {
                //TODO, checar esto
                rFinales.add(new String[]{parts[0], fuzzyRes});
            }
        }
        return rFinales;
    }

    private String fuzzyProcess$(String valor, String[] partsCondition, String ruta) throws IOException {
        MotorInferencia objMI = new MotorInferencia();
        Double tmp;
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
                tmp = intPoint(partsCondition, etiqueta, ruta.split("SED")[0]);
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

    private Double intPoint(String[] partsCondition, String simpleValue, String rutaBase) throws IOException {
        Gauss objGauss;
        //obtengo registros del archivo temporal
        List<String> listSemiT = objG.leer(rutaBase + "SED/" + partsCondition[0] + ".tmp");
        //obtengo los registros de la variable linguistica
        List<String> listTrap = objG.leer(rutaBase + "SED/" + partsCondition[0]);
        double[] pSemiTrap, pTrap, ecSemiTrap, ecTrap, pInt; //contenedores de los puntos

        //obtengo dos puntos por cada trapecio
        pSemiTrap = getPointsSemiTrap(listSemiT);
        pTrap = getPointsTrap(partsCondition, simpleValue, listTrap);

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
    private double[] getPointsTrap(String[] partsCondition, String rEtiqueta, List<String> lTrap) {
        double[] points = null;
        String[] parts;
        double dist;

        for (String r : lTrap) {
            if (r.contains(rEtiqueta)) {
                points = new double[2];
                parts = r.split(" ");
                if (partsCondition[1].equals("fleq")) {
                    //toma los puntos más a la izquierda del trapezoide o semitrap
                    points[0] = Double.parseDouble(parts[1]); //punto crítico más a la izq

                    if (parts[0].equals("Trapezoide")) {
                        //obtiene la distancia que hay entre un punto crítico y un punto inferior
                        points[1] = Double.parseDouble(parts[4]); //obtengo el punto inferior que necesito
                    } else {
                        //parts[1] == SemiTrapezoide
                        points[1] = Double.parseDouble(parts[4]);
                    }
                } else {
                    //partsCondition[1] == fgeq
                    //toma los puntos más a la derecha del trapezoide o semitrap
                    if (parts[0].equals("Trapezoide")) {
                        points[0] = Double.parseDouble(parts[2]);
                        dist = Math.abs(Double.parseDouble(parts[1]) - Double.parseDouble(parts[4]));
                        points[1] = points[0] + dist;
                    } else {
                        //parts[1] == SemiTrapezoide
                        points[0] = Double.parseDouble(parts[1]);
                        points[1] = Double.parseDouble(parts[4]);
                    }
                }
                return points;
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
    private String quitaSimbolo(String etiqueta) {
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

    public List<String[]> comparaRegistros(Tabla objTResultante, List<String[]> lResultado) {
        List<Registro> lRegistro = objTResultante.getListRegistro();
        List<String[]> lFinal = new ArrayList<>();
        List<Columna> lColumnas;
        String[] res;
        boolean flag;

        for (int i = 0; i < lRegistro.size(); i++) {
            flag = true;
            lColumnas = lRegistro.get(i).getList_columnas();

            for (int j = 0; j < lColumnas.size() && flag; j++) {
                for (int k = 0; k < lResultado.size() && flag; k++) {
                    res = lResultado.get(k);

                    if (lColumnas.get(j).getContenido().equals(res[0])) {
                        //guarda la posición y el grado de membresía
                        lFinal.add(new String[]{i + "", res[1]});
                        flag = false;
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
}
