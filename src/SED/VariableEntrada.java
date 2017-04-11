package SED;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author Tenistas
 */
public class VariableEntrada {

    private final String query;
    private final UniversoDiscurso objU;
    private Trapezoide objT;
    private Scanner teclado;
    private double aux;
    private int countShape;

    //PUEDE ADAPTARSE PARA TRABAJAR TAMBIÉN CON VARIABLES DE SALIDA
    public VariableEntrada(String query) {
        this.query = query;
        teclado = new Scanner(System.in);
        objU = new UniversoDiscurso();
    }

    private void getColumns() throws IOException {
        String[] parts, parts2;
        countShape = 0;

        //Corta el query para obtener las columnas
        parts = query.split("create table ");
        parts = parts[0].split(" \\( ");
        parts = parts[1].split(" \\)");
        parts = parts[0].split(", ");

        for (String part : parts) {
            parts2 = part.split(":");
            objU.setVariable(parts2[0]);
            parts2 = parts2[1].split(" ");

            if (parts2[1].equals("f")) {
                createDirectory(); //crea el directorio SED si no existe
                askDiscourseUniverse(); //pide datos del universo de discurso y escribe en archivos

            }

        }

    }

    private void createDirectory() {
        String directorio = "SED";
        File index = new File(directorio);
        if (!index.exists()) {
            index.mkdir();
            //Carpeta SED creada exitosamente
        }
    }

    private void askDiscourseUniverse() throws IOException {
        GestionArchivos objG = new GestionArchivos();
        String registro, ruta = "SED/";

        registro = getRange(); //pide el rango de origen - fin del universo de discurso
        registro += " " + getUnits(); //pide las unidades 
        registro += " " + objU.getVariable();

        objG.escribir(ruta + objU.getVariable(), 1, registro, "nuevo"); //crea el archivo con el nombre de la objU.getVariable()
        objG.escribir(ruta + "Datos", 1, objU.getVariable(), "final"); //guarda el nombre de la objU.getVariable() en el archivo Datos
    }

    private String getUnits() {
        String dato, units = "";
        String parts[];

        System.out.println("Se le pedirán datos correspondientes a la objU.getVariable() " + objU.getVariable());
        System.out.println("Ingrese las unidades: ");
        dato = teclado.nextLine();
        parts = dato.split(" ");
        if (parts.length > 1) {
            for (int i = 0; i < parts.length; i++) {
                units += parts;
                if (i < parts.length - 1) {
                    units += "_";
                }
            }
        } else {
            units += dato;
        }

        objU.setUnidad(units);
        return units;
    }

    private String getRange() {
        Double origen, fin;
        boolean flag = true;

        System.out.println("Universo de discurso");
        do {
            origen = askNumericData("Ingrese el origen: "); //regresa un valor válido 
            fin = askNumericData("Ingrese el fin: "); //regresa un valor válido 

            if (origen < fin) {
                flag = false; //sale del ciclo
            } else {
                System.out.println("ERROR, el origen debe ser menor que el fin");
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
                System.out.println(msg);
                data = teclado.nextDouble();
                flag = false; //saldrá del ciclo
            } catch (Exception e) {
                //TODO
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
            System.out.println(msg);
            data = teclado.next();
            if (data.isEmpty()) {
                System.out.println("Error, llene el campo");
                flag = true; //continua en el ciclo
            } else {
                flag = false; //sale del ciclo
            }
        } while (flag);

        return data;
    }

    private void createTrapezoid() throws IOException {
        GestionArchivos objG = new GestionArchivos();
        String registro, ruta = "SED/";
        Double pc1, pc2;
        boolean flag;

        do {
            pc1 = askNumericData("Ingrese el Punto Crítico 1: ");
            pc2 = askNumericData("Ingrese el Punto Crítico 2 o -1 para terminar");

            if (pc2 != -1) {
                //Trapecio completo
                flag = fullTrapezoid(pc1, pc2); //verifica varios aspectos de un trapezoide completo
                if (!flag) {
                    registro = "Trapezoide " + objT.getPuntoC1()[0] + " " + objT.getPuntoC2()[0] + " "
                            + objT.getEtiqueta() + " " + objU.getOrigen() + " 0";
                    objG.escribir(ruta + objU.getVariable(), (countShape + 1), registro, "final");
                    calcularTraslape(); //cambia el valor de objU.origen
                    ++countShape;
                    //no necesito modificar flag porque ya es = false, para poder salir del ciclo
                }

            } else {
                //SemiTrapezoide
                if (countShape == 0) {
                    //Orientación: Izquierda

                } else {
                    //Orientación: Derecha
                }

            }

            objT.setEtiqueta(askStringData("Ingrese la etiqueta: "));
        } while (flag);

    }

    /**
     * Verifica que los puntos c1 y c2 estén dentro del universo de discurso.
     * También, si la función abarcará el resto del universo de discurso
     *
     * @return true si hubo algún error
     *
     */
    private boolean fullTrapezoid(double pc1, double pc2) {
        String registro;
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

            objT.setPuntoC1(new double[]{pc1, 1});
            objT.setPuntoC2(new double[]{pc2, 1});
        }
        return false;
    }

    private void calcularTraslape() {
        double nuevoOrigen;
        if (aux == objU.getFin()) {
            nuevoOrigen = (objU.getFin() - objT.getPuntoC2()[0]) * 0.6;
        } else {
            nuevoOrigen = (objT.getPuntoC1()[0] - objU.getOrigen()) * 0.6;
        }

        nuevoOrigen = (objT.getPuntoC2()[0] + nuevoOrigen);
        objU.setOrigen(nuevoOrigen);
    }
}
