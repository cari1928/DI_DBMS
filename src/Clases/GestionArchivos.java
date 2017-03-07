package Clases;

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

    BaseDatos objBD;
    RandomAccessFile raf;
    final int TAMAÑO = 150;

    public GestionArchivos(BaseDatos objBD) {
        this.objBD = objBD;
    }

    //tipo = nuevo, agregar al final (final)
    public void escribir(int llave, String nomFile, String registro, String tipo) throws IOException {
        StringBuilder builder = null;
        raf = new RandomAccessFile("BD\\" + objBD.nombre + ".dbs\\" + nomFile, "rw");

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
        int tamaño = contarRengs(nomFile), llave; //cantidad de objetos
        List<String> list = new ArrayList<>();
        String convert;

        raf = new RandomAccessFile("BD\\" + objBD.nombre + ".dbs\\" + nomFile, "r");

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

            list.add(convert);
        }

        raf.close();
        return list;
    }

    public int contarRengs(String nomFile) throws FileNotFoundException, IOException {
        long ap_actual, ap_final;
        int cont = 0, llaves;
        raf = new RandomAccessFile("BD\\" + objBD.nombre + ".dbs\\" + nomFile, "r");

        while ((ap_actual = raf.getFilePointer()) != (ap_final = raf.length())) {
            char[] registro = new char[TAMAÑO];
            char tmp;
            tmp = raf.readChar();
            tmp = raf.readChar();
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

    public void crearDirectorio(String rutaDirectorio) {
        File directorio = new File(rutaDirectorio);
        directorio.mkdir();
        crearArchivo(rutaDirectorio + "\\tablas");
        crearArchivo(rutaDirectorio + "\\columnas");
        crearArchivo(rutaDirectorio + "\\indices");
        System.out.println("Jerarquía de archivos creada con éxito");
    }

    public void crearArchivo(String rutaArchivo) {
        try {
            File archivo = new File(rutaArchivo);
            archivo.createNewFile();
        } catch (Exception e) {
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
            raf = new RandomAccessFile("BD\\" + objBD.nombre + ".dbs\\" + nomFile, "rw");

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
}
