package SGBD;

import SED.VariableEntrada;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author Tenistas
 */
public class Principal {

    private static Automatas objA;
    private static Scanner consola;
    private static String query;

    public static void main(String[] args) {
        VariableEntrada objV;

        consola = new Scanner(System.in);
        objA = new Automatas();
        mostrarBienvenida();

        while (true) {
            System.out.print(">");
            query = pedirDatos();
            objA.setQuery(query);
            objA.iniAutomatas();

            //verifica si hay que pedir variables de entrada
            if (objA.isVarEntrada()) {
                try {
                    objV = new VariableEntrada(query, objA);
                    objV.init();
                    objA.setVarEntrada(false);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

    }

    static void mostrarBienvenida() {
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n"
                + "+                       HOLA BIENVENIDO                       +\n"
                + "+                              A                              +\n"
                + "+                          TENISFSQL                          +\n"
                + "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    }

    static String pedirDatos() {
        return consola.nextLine();
    }
}
