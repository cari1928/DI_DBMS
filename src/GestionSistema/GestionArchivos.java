package GestionSistema;

import SED.*;
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

    private RandomAccessFile raf;
    private final int TAMAÑO = 150;

    /*
     * tipo = nuevo | final. 
     * nuevo = borrar contenido e insertar nuevo
     * final = agrega después del últmo registro
     */
    public void escribir(String nomFile, int llave, String registro, String tipo) throws IOException {
        StringBuilder builder;
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
        char[] registro = new char[TAMAÑO];
        String convert;
        char tmp;

        raf = new RandomAccessFile(nomFile, "rw");
        if (llave > 1) {
            raf.seek((llave * 300) + (2 + (llave - 1) * 4));
        }
        if (llave == 1) {
            raf.seek(302);
        } else if (llave != 0) {
            llave = raf.readInt();
        }
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
        raf = new RandomAccessFile(nomFile, "rw");
        if (llave > 0) {
            raf.seek((llave * 300) + (llave * 4));
        }
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

    public void crearDirectorio(String ruta) {
        File index = new File(ruta);
        if (!index.exists()) {
            index.mkdir();
            System.out.println("CARPETA " + ruta + " CREADA CORRECTAMENTE");
        }

        if (!ruta.contains("SED")) {
            crearArchivo(ruta + "\\tablas");
            crearArchivo(ruta + "\\columnas");
            crearArchivo(ruta + "\\indices");
            System.out.println("JERARQUÍA DE ARCHIVOS CREADA CON ÉXITO");
        }
    }

    public void crearArchivo(String rutaArchivo) {
        try {
            File archivo = new File(rutaArchivo);
            archivo.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("No se ha podido crear la jerarquía de archivos");
        }
    }

    //pos dentro del arreglo, id para buscar el registro
    public void actualizarArchivo(String nomFile, String nuevoRegistro, int id, int pos) {
        StringBuilder builder = null;
        long ap_actual, ap_final;
        int tamaño, llave; //cantidad de objetos
        String convert;
        String[] parts;
        RandomAccessFile tmpRaf;
        List<String> list;

        try {
            list = new ArrayList<>();
            tmpRaf = new RandomAccessFile("tmp", "rw");
            tamaño = contarRengs(nomFile);
            raf = new RandomAccessFile(nomFile, "rw");

            while ((ap_actual = raf.getFilePointer()) != (ap_final = raf.length())) {

                llave = raf.readInt();
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
                parts = convert.split(" ");

                if (Integer.parseInt(parts[pos] + "") == id) {
                    list.add(nuevoRegistro);
                } else {
                    list.add(convert);
                }
            }
            raf.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
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
            list = objG.leer("BD\\empresa.dbs\\tablas");
            for (int i = 0; i < list.size(); i++) {
                System.out.println(list.get(i));
            }
            System.out.println("------------------------------------------------------------------");
            /*String registro = objG.obtenerRegistroByID("pruebas", 7);
            System.out.println(registro);
            //registro += "9999";
            System.out.println(registro);*/

            //objG.actualizar("pruebas", 2, registro);
//            list = objG.leer("SED/v2");
//            for (int i = 0; i < list.size(); i++) {
//                System.out.println(list.get(i));
//            }
//            System.out.println("-------------------------------------------------------------------");
//            list = objG.leer("SED/v3");
//            for (int i = 0; i < list.size(); i++) {
//                System.out.println(list.get(i));
//            }
//            System.out.println("-------------------------------------------------------------------");
//            list = objG.leer("SED/FAM");
//            for (int i = 0; i < list.size(); i++) {
//                System.out.println(list.get(i));
//            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void deleteFile(String ruta) {
        File file = new File(ruta);
        file.delete();
    }

}
