package GestionSistema;

import SED.Gauss;
import SED.MotorInferencia;
import java.io.IOException;
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
        double tmpDistance;
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

                    if (partsCondition[1].equals("fleq")) {
                        criticPoints[0] = Double.parseDouble(parts2[2]); //obtiene el punto crítico más a la derecha
                        //obtiene la distancia entre el punto inferior izquierdo y el punto crítico 1
                        tmpDistance = Double.parseDouble(parts2[1]) - Double.parseDouble(parts2[4]);
                        //obtiene el punto inferior de la derecha
                        criticPoints[1] = criticPoints[0] + tmpDistance;
                    } else {
                        //partsC == FGEG
                        //toma el punto crítico más a la izquierda
                        criticPoints[0] = Double.parseDouble(parts2[1]);
                        criticPoints[1] = Double.parseDouble(parts2[4]); //toma el punto que va a la izquierda
                    }
                    return criticPoints; //ya obtuvo los puntos críticos 
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
    public void fuzzyResult(String rutaBase, String[] partsCondition, String whereType) throws IOException {
        String[] tableCol = partsCondition[0].split("\\."), parts; //pos 0 = tabla, pos 1 = columna
        List<String> lTablas = objG.leer(rutaBase + "tablas");
        List<String> lColumnas = objG.leer(rutaBase + "columnas");
        List<String> lRegistro = objG.leer(rutaBase + tableCol[0] + ".dat");
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

            if (whereType.equals("$")) {
                //manda: registro de la columna, ruta
                fuzzyRes = fuzzyProcess$(parts[poscol], partsCondition, rutaBase + "SED/" + partsCondition[0] + ".tmp");
            } else {
                //type == #
                fuzzyRes = fuzzyProcessHash();
            }

            if (Double.parseDouble(fuzzyRes) != 0) {
                //AGREGAR A ESTRUCTURA
                //INCLUIR COLUMNA DE RESULTADO DIFUSO
                System.out.println(fuzzyRes + " AGREGADO A LA ESTRUCTURA");
            }
        }
    }

    private String fuzzyProcess$(String valor, String[] partsCondition, String ruta) throws IOException {
        MotorInferencia objMI = new MotorInferencia();
        double value;
        String simpleValue;

        if (!valor.contains("<")) {
            //valor numérico
            try {
                value = Double.parseDouble(valor);
                objMI.fuzzyfication(value, ruta, "$"); //CHECAR ESTO!!
                return objMI.getResultado();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            //quito los <>
            simpleValue = valor.split("<")[1].split(">")[0];
            //veo si es la et linguistica indicada en el where
            if (partsCondition[2].contains(simpleValue)) {
                return "1"; //porque es la misma etiqueta linguistica
            } else {
                //si no lo es, buscar algún posible punto de intersección
                return intPoint(partsCondition, simpleValue, ruta.split("SED")[0]) + "";
            }
        }
        return null;
    }

    //checa el caso de #
    private String fuzzyProcessHash() {
        return null;
    }

    private double intPoint(String[] partsCondition, String simpleValue, String rutaBase) throws IOException {
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
            return 0;
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
                    if (parts[1].equals("Trapezoide")) {
                        points[0] = Double.parseDouble(parts[2]);
                    } else {
                        //parts[1] == SemiTrapezoide
                        points[0] = Double.parseDouble(parts[1]);
                    }
                    points[1] = Double.parseDouble(parts[4]);
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

}
