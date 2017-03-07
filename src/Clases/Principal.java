package Clases;

import java.util.Scanner;

/**
 *
 * @author Tenistas
 */
public class Principal {

    static Automatas objA;
    static Scanner consola;
    static String query;

    public static void main(String[] args) {
       
        consola = new Scanner(System.in);
        objA = new Automatas();
        mostrarBienvenida();

        while (true) {
            System.out.print(">");
            query = pedirDatos();
            objA.setQuery(query);
            objA.iniAutomatas();
            objA.mostrarArchivos();
        }
    }

    static void mostrarBienvenida() {
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n"
                + "+                       HOLA BIENVENIDO                       +\n"
                + "+                              A                              +\n"
                + "+                          TENIS RSQL                         +\n"
                + "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    }

    static String pedirDatos() {
        return consola.nextLine();
    }
}
