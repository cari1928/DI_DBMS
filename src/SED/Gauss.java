package SED;

/**
 *
 * @author Tenistas
 */
public class Gauss {

    private final double[][] matriz;
    private int cont;
    private double[] intPoint; //punto de interseccion

    public Gauss(double[] arr1, double[] arr2) {
        matriz = fusionArrays(arr1, arr2); //se llena la matriz con los dos arreglos unidimensionales
        intPoint = null;
        cont = 0;
        resolverGauss();
    }

    public double[] getIntPoint() {
        return intPoint;
    }

    /**
     * Traspasa el contenido de dos arreglos unidimensionales a un arreglo
     * bidimensional
     *
     * @param array1 Array unidimensional
     * @param array2 Array unidimensional
     * @return arrays fusionados en un arreglo bidimensional
     */
    private double[][] fusionArrays(double[] array1, double[] array2) {
        double[][] tmp = new double[2][2 + 1]; //2 ecuaciones 3 elementos (X,Y,Constante)
        //espero funcione XD
        System.arraycopy(array1, 0, tmp[0], 0, array1.length);
        System.arraycopy(array2, 0, tmp[1], 0, array2.length);
        return tmp;
    }

    /**
     * Ejecuta el procedimiento de GAUSS-JORDAN
     */
    private void resolverGauss() {
        while (cont < matriz.length) {
            Iteraciones(); //modifica matriz y contador

            if (cont == matriz.length) {
                //RESULTADO FINAL
                intPoint = VariablesGaussJordan(); //envía el punto de intersección
            }
        }
    }

    /**
     * Método encargado de las iteraciones
     */
    private void Iteraciones() {
        //Dividimos el renglón entre la cantidad que tiene la casilla [cont][cont]
        ArreglaPivote();
        //Convertimos en cero los números debajo del pivote y al mismo tiempo modificamos esos renglones
        ConvertZero();
        //Llevamos la cuenta de la iteración
        ++cont;
    }

    /**
     * Encargado de preparar el pivote (número 1) para poder usar después el
     * método ConvertZero
     */
    private void ArreglaPivote() {
        //cont nos indica el renglón en donde estamos
        //Guardamos el número de la casilla [cont][cont]
        double num = matriz[cont][cont];
        for (int i = 0; i < matriz[cont].length; i++) {
            matriz[cont][i] /= num;
        }
    }

    /**
     * Convierte a 0 los elementos de un renglón que no son pivote
     */
    private void ConvertZero() {
        double num;
        for (int j = 0; j < matriz.length; j++) {
            if (j != cont) {
                //Guardamos nuestro pivote (1)
                num = matriz[j][cont];
                for (int i = 0; i < matriz[0].length; i++) {
                    matriz[j][i] -= num * matriz[cont][i];
                }
            }
        }
    }

    /**
     * Pasa las coordenadas X y Y finales a un arreglo unidimensional
     *
     * @return double[] con las coordenadas del punto de interseccion
     */
    private double[] VariablesGaussJordan() {
        double ec[] = new double[matriz.length];
        for (int i = 0; i < ec.length; i++) {
            ec[i] = matriz[i][matriz[0].length - 1];
        }
        return ec;
    }
}
