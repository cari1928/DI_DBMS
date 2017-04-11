package SED;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tenistas
 */
public class GestionArchivos {

    RandomAccessFile raf;
    final int TAMAÑO = 150;

    /*
     * tipo = nuevo | final. 
     * nuevo = borrar contenido e insertar nuevo
     * final = agrega después del últmo registro
     */
    public void escribir(String nomFile, int llave, String registro, String tipo) throws IOException {
        StringBuilder builder = null;
        File archivo = new File(nomFile);

        if (tipo.equals("nuevo")) {
            archivo.delete();
        }

        raf = new RandomAccessFile(nomFile, "rw");

        if (tipo.equals("final")) {
            raf.seek(raf.length());
        }

        raf.writeInt(llave);
        builder = new StringBuilder(registro);
        builder.setLength(TAMAÑO);
        raf.writeChars(builder.toString());
        raf.close();
    }

    public List leer(String nomFile) throws FileNotFoundException, IOException {
        long ap_actual, ap_final;
        int tamaño = contarRengs(nomFile.trim()), llave; //cantidad de objetos
        List<String> list = new ArrayList<>();
        String convert;

        raf = new RandomAccessFile(nomFile, "r");

        while ((ap_actual = raf.getFilePointer()) != (ap_final = raf.length())) {
            llave = raf.readInt(); //lee la llave
            char[] registro = new char[TAMAÑO];
            char tmp;
            for (int i = 0; i < registro.length; i++) {
                tmp = raf.readChar();
                registro[i] = tmp;
            }

            new String(registro).replace('\0', ' ');

            convert = "";
            for (int i = 0; i < registro.length; i++) {
                convert += registro[i];
            }

            list.add(convert);
        }

        raf.close();
        return list;
    }

    public Etiqueta obtenerEtByRegistro() {
        return null;
    }

    public String obtenerRegistroByID(String nomFile, int llave) throws IOException {
        String convert;
        StringBuilder builder = null;
        //File archivo = new File(nomFile);
        raf = new RandomAccessFile(nomFile, "rw");
        if (llave > 1) {
            raf.seek((llave * 300) + (2 + (llave - 1) * 4));
        }
        if (llave == 1) {
            raf.seek(302);
        }
        if (llave != 0) {
            llave = raf.readInt(); //lee la llave
        }
        char[] registro = new char[TAMAÑO];
        char tmp;
        for (int i = 0; i < registro.length; i++) {
            tmp = raf.readChar();
            registro[i] = tmp;
        }

        new String(registro).replace('\0', ' ');

        convert = "";
        for (int i = 1; i < registro.length; i++) {
            convert += registro[i] + "";
            //System.out.println(registro[i] + i);
        }
        raf.close();

        return convert;
    }

    public void actualizar(String nomFile, int llave, String registro) throws IOException {
        StringBuilder builder = null;
        File archivo = new File(nomFile);
        raf = new RandomAccessFile(nomFile, "rw");
        if (llave > 0) {
            raf.seek((llave * 300) + (llave * 4));
        }
        //if(llave>0)
        raf.writeInt(llave);
        builder = new StringBuilder(registro);
        builder.setLength(TAMAÑO);
        raf.writeChars(builder.toString());
        raf.close();
    }

    public int contarRengs(String nomFile) throws FileNotFoundException, IOException {
        long ap_actual, ap_final;
        int cont = 0, llaves;
        raf = new RandomAccessFile(nomFile, "r");

        while ((ap_actual = raf.getFilePointer()) != (ap_final = raf.length())) {
            raf.readInt(); //lee la llave
            char[] registro = new char[TAMAÑO];
            char tmp;
            for (int i = 0; i < registro.length; i++) {
                tmp = raf.readChar();
                registro[i] = tmp;
            }

            new String(registro).replace('\0', ' ');
            ++cont;
        }

        raf.close();
        return cont;
    }

    //ejemplo de uso
    public static void main(String[] args) {
        GestionArchivos objG = new GestionArchivos();
        List<String> list = null;
        try {
            /*objG.escribir("pruebas", 0, "00 00 unidades variable jdsdfferrtgrt gtttgrtgewrfe weferferferferfef wfwefwefwefwe", "nuevo");
            objG.escribir("pruebas", 1, "10 01 unidades variable jdsdfferrtgrt gtttgrtgewrfe weferferferferfef wfwefwefwefwe", "final");
            objG.escribir("pruebas", 2, "20 02 unidades variable jdsdfferrtgrt gtttgrtgewrfe ", "final");
            objG.escribir("pruebas", 3, "30 03 unidades variable jdsdfferrtgrt gtttgrtgewrfe weferferferferfef wfwefwefwefwe", "final");
            objG.escribir("pruebas", 4, "40 04 unidades variable jdsdfferrtgrt gtttgrtgewrfe weferferferferfef wfwefwefwefwe", "final");
            objG.escribir("pruebas", 5, "50 05 unidades variable jdsdfferrtgrt gtttgrtgewrfe weferferferferfef wfwefwefwefwe", "final");
            objG.escribir("pruebas", 6, "60 06 unidades variable jdsdfferrtgrt gtttgrtgewrfe weferferferferfef wfwefwefwefwe", "final");
            objG.escribir("pruebas", 7, "70 07 unidades variable jdsdfferrtgrt gtttgrtgewrfe weferferferferfef wfwefwefwefwe", "final");*/
            list = objG.leer("SED/v1");
            for (int i = 0; i < list.size(); i++) {
                System.out.println(list.get(i));
            }
            System.out.println("------------------------------------------------------------------");
            /*String registro = objG.obtenerRegistroByID("pruebas", 7);
            System.out.println(registro);
            //registro += "9999";
            System.out.println(registro);*/
            
            //objG.actualizar("pruebas", 2, registro);
            list = objG.leer("SED/v2");
            for (int i = 0; i < list.size(); i++) {
                System.out.println(list.get(i));
            }
            System.out.println("-------------------------------------------------------------------");
            list = objG.leer("SED/v3");
            for (int i = 0; i < list.size(); i++) {
                System.out.println(list.get(i));
            }
                        System.out.println("-------------------------------------------------------------------");
            list = objG.leer("SED/FAM");
            for (int i = 0; i < list.size(); i++) {
                System.out.println(list.get(i));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
