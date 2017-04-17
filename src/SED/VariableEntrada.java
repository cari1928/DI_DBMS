package SED;

import java.io.IOException;
import java.util.Scanner;
import Archivos.GestionArchivos;
import SGBD.Automatas;

/**
 *
 * @author Tenistas
 */
public class VariableEntrada {

    private final String query;
    private final UniversoDiscurso objU;
    private final Scanner teclado;
    private final GestionArchivos objG;
    private final Automatas objA;
    private double aux;
    private int countShape;
    private Trapezoide objT;
    private SemiTrapezoide objSemiT;

    //PUEDE ADAPTARSE PARA TRABAJAR TAMBIÉN CON VARIABLES DE SALIDA
    public VariableEntrada(String query, Automatas objA) {
        this.query = query.toLowerCase();
        this.objA = objA;
        teclado = new Scanner(System.in);
        objU = new UniversoDiscurso();
        objG = new GestionArchivos();
    }

    public void init() throws IOException {
        String[] parts, parts2;
        countShape = 1;

        //Corta el query para obtener las columnas
        parts = query.split("create table ");
        parts = parts[1].split(" \\( ");
        objU.setTable(parts[0]);
        parts = parts[1].split(" \\)");
        parts = parts[0].split(", ");

        for (String part : parts) {
            parts2 = part.split(":");
            objU.setVariable(parts2[0]);
            parts2 = parts2[1].split(" ");

            if (parts2[1].equals("f")) {
                objG.crearDirectorio(objA.getRUTA() + "SED");//crea el directorio SED si no existe
                askDiscourseUniverse(); //pide datos del universo de discurso y escribe en archivos
                createTrapezoids();
            }
        }
    }

    private void askDiscourseUniverse() throws IOException {
        String registro, ruta = objA.getRUTA() + "SED/";

        registro = getRange(); //pide el rango de origen - fin del universo de discurso
        registro += " " + getUnits(); //pide las unidades 
        registro += " " + objU.getVariable();

        //crea el archivo con el nombre de la objU.getVariable()
        objG.escribir(ruta + objU.getTable() + "." + objU.getVariable(), 1, registro, "nuevo");
        //guarda el nombre de la objU.getVariable() en el archivo Datos
        objG.escribir(ruta + "Datos", 1, objU.getTable() + "." + objU.getVariable(), "final");
    }

    private String getUnits() {
        String units;
        System.out.println("DATOS CORRESPONDIENTES A LA VARIABLE " + objU.getVariable());
        units = askStringData("INGRESE LAS unidades: ");
        objU.setUnidad(units);
        return units;
    }

    private String getRange() {
        Double origen, fin;
        boolean flag = true;

        System.out.println("UNIVERSO DE DISCURSO PARA LA VARIABLE " + objU.getVariable());
        do {
            origen = askNumericData("INGRESE EL origen: "); //regresa un valor válido 
            fin = askNumericData("INGRESE EL fin: "); //regresa un valor válido 

            if (origen < fin) {
                flag = false; //sale del ciclo
            } else {
                System.out.println("ERROR, EL origen DEBE SER MENOR QUE EL fin");
            }
        } while (flag);

        objU.setOrigen(origen);
        objU.setFin(fin);
        return origen + " " + fin;
    }

    private Double askNumericData(String msg) {
        boolean flag;
        Double data = null;
        do {
            try {
                System.out.print(msg);
                data = teclado.nextDouble();
                flag = false; //saldrá del ciclo
            } catch (Exception e) {
                e.printStackTrace();
                flag = true; //seguirá en el ciclo
            }
        } while (flag);

        return data;
    }

    private String askStringData(String msg) {
        boolean flag;
        String data;

        do {
            System.out.print(msg);
            data = teclado.next();
            if (data.isEmpty()) {
                System.out.println("Error, LLENE EL CAMPO");
                flag = true; //continua en el ciclo
            } else {
                flag = false; //sale del ciclo
            }
        } while (flag);

        return data;
    }

    private void createTrapezoids() throws IOException {
        String registro, opt, ruta = objA.getRUTA() + "SED/" + objU.getTable() + "." + objU.getVariable(), orientacion;
        Double pc1, pc2;
        boolean flag;
        countShape = 1;
        do {
            opt = "1";
            pc1 = askNumericData("INGRESE EL punto crítico 1: ");
            pc2 = askNumericData("INGRESE EL punto crítico 2 O -1 PARA TERMINAR: ");

            if (pc2 != -1) {
                //Trapecio completo
                flag = fullTrapezoid(pc1, pc2); //verifica varios aspectos de un trapezoide completo
                if (!flag) {
                    objT.setEtiqueta(askStringData("Ingrese la Etiqueta: "));
                    registro = "Trapezoide " + objT.getPuntoC()[0] + " " + objT.getPuntoC2()[0] + " "
                            + objT.getEtiqueta() + " " + objU.getOrigen() + " 0";
                    objG.escribir(ruta, countShape, registro, "final");

                    objT.setAux(aux);
                    objT.setOrigen(objU.getOrigen());
                    objT.setFin(objU.getFin());
                    objU.setOrigen(objT.calculaTraslape());
                    //no necesito modificar flag porque ya es = false, para poder salir del ciclo
                }
            } else {
                //SemiTrapezoide
                if (countShape == 1) {
                    flag = halfLeftTrapezoid(pc1); //verifica aspectos de un trapezoide a la izquierda
                    orientacion = "i";
                } else {
                    flag = halfRightTrapezoid(pc1); //verifica aspectos de un trapezoide a la derecha
                    orientacion = "d";
                }

                if (!flag) {
                    objSemiT.setEtiqueta(askStringData("Ingrese la Etiqueta: "));
                    registro = "SemiTrapezoide " + objSemiT.getPuntoC()[0] + " " + orientacion + " "
                            + objSemiT.getEtiqueta() + " " + objU.getOrigen() + " 0";
                    objG.escribir(ruta, countShape, registro, "final");

                    objSemiT.setAux(aux);
                    objSemiT.setOrigen(objU.getOrigen());
                    objSemiT.setFin(objU.getFin());
                    objU.setOrigen(objSemiT.calculaTraslape());
                    //no necesito modificar flag porque ya es = false, para poder salir del ciclo
                }
            }
            if (!flag) {
                ++countShape;
                opt = askStringData("PRESIONE CUALQUIER TECLA PARA AGREGAR OTRA ETIQUETA Ó"
                        + "\nPRESIONE 0 PARA TERMINAR: ");
            }
        } while (flag || !opt.equals("0"));
    }

    /**
     * Verifica que los puntos c1 y c2 estén dentro del universo de discurso.
     * También, si la función abarcará el resto del universo de discurso
     *
     * @return true si hubo algún error
     *
     */
    private boolean fullTrapezoid(double pc1, double pc2) {
        objT = new Trapezoide(); //prepara un nuevo trapezoide 
        if (pc1 > pc2) {
            System.out.println("Error, PC1 no debe ser mayor al PC2");
            return true;
        }

        if (pc1 < objU.getOrigen() || pc1 > objU.getFin() || pc2 < objU.getOrigen() || pc2 > objU.getFin()) {
            System.out.println("Error, los puntos críticos no se encuentran dentro del Universo de Discurso disponible");
            return true;

        } else if ((objU.getFin() - objU.getOrigen())
                < ((pc1 - objU.getOrigen()) + (pc2 - pc2) + (objU.getFin() - pc2))) {
            System.out.println("La función abarcará todo el Universo de Discurso disponible");
            aux = objU.getFin();

        }
        objT.setPuntoC(new double[]{pc1, 1});
        objT.setPuntoC2(new double[]{pc2, 1});
        return false;
    }

    /**
     * Verifica que el PC esté dentro del universo de discurso. También, si el
     * punto es igual al origen. Y, si abarcará todo el universo disponible
     *
     * @param pc Punto Crítico
     * @return false si hubo algún error
     */
    private boolean halfLeftTrapezoid(double pc) {
        objSemiT = new SemiTrapezoide();
        if (objU.getOrigen() > pc || pc > objU.getFin()) {
            System.out.println("Error, el punto crítico no está dentro del universo de disponible");
            return true;
        } else if (pc == objU.getOrigen()) {
            //TODO cambiar el mensaje mostrado por otro mas apropiado
            System.out.println("ERROR CHECK LEFT ORIENTATION");
            return true;
        }
        setAux(pc);
        return false;
    }

    private boolean halfRightTrapezoid(double pc) {
        objSemiT = new SemiTrapezoide();
        if (objU.getOrigen() > pc || pc > objU.getFin()) {
            System.out.println("Error, el punto crítico no está dentro del universo de disponible");
            return true;
        } else if (pc == objU.getFin()) {
            //TODO cambiar el mensaje mostrado por otro mas apropiado
            System.out.println("ERROR CHECK RIGHT ORIENTATION");
            return true;
        }
        setAux(pc);
        return false;
    }

    private void setAux(double pc) {
        double rango = pc - objU.getOrigen();
        if ((objU.getFin() - objU.getOrigen()) < (rango + rango)) {
            System.out.println("La función abarcará todo el discurso disponible");
            aux = objU.getFin();
        }

        objSemiT.setPuntoC(new double[]{pc, 1});
    }

}
