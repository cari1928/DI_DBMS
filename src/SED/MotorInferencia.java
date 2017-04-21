package SED;

import GestionSistema.GestionArchivos;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tenistas
 */
public class MotorInferencia {

    private UniversoDiscurso objU;
    private List<SemiTrapezoide> listSemiTrapezoide;
    private double punto;
    private String resultado, rutaArchivo;
    private final GestionArchivos objG;

    public MotorInferencia() {
        resultado = "";
        objG = new GestionArchivos();
    }

    public String getResultado() {
        return resultado;
    }

    public void fuzzyfication(double punto, String variable, String whereType) throws IOException {
        this.punto = punto;
        this.rutaArchivo = variable;
        crearModelo(rutaArchivo, whereType);

        if (listSemiTrapezoide != null) {
            for (SemiTrapezoide semiTrapezoide : listSemiTrapezoide) {
                semiTrapezoide.membresiaY = calcularY(semiTrapezoide);
                resultado = semiTrapezoide.membresiaY + "";
            }
        }
        if (resultado.equals("")) {
            resultado = "0";
        }
    }

    public void crearModelo(String ruta, String whereType) {
        List<String> listRegistros;
        SemiTrapezoide objSemTrap;
        String registro;
        String[] parts;
        int contFigura = 1;
        double distancia;

        iniLista();
        try {
            listRegistros = objG.leer(ruta);
            for (int i = 0; i < listRegistros.size(); i++) {
                registro = listRegistros.get(i);
                parts = registro.split(" ");

                switch (parts[0]) {
                    case "SemiTrapezoide":
                        objSemTrap = new SemiTrapezoide();

                        //punto critico
                        objSemTrap.puntoC[0] = Double.parseDouble(parts[1]);
                        objSemTrap.puntoC[1] = 1;

                        objSemTrap.setOrientacion(parts[2].charAt(0));
                        objSemTrap.etiqueta = parts[3];

                        //otro punto
                        if (objSemTrap.getOrientacion() == 'i') {
                            if (whereType.equals("$")) {
                                objSemTrap.puntoI1[0] = Double.parseDouble(parts[4]);
                            } else {
                                //type == #
                                distancia = objSemTrap.puntoC[0] - objU.getOrigen();
                                objSemTrap.puntoI1[0] = objSemTrap.puntoC[0] + distancia;
                            }
                        } else {
                            //mucho cuidado
                            //distancia = objU.fin - objSemTrap.puntoC[0];
                            //objSemTrap.punto2[0] = objSemTrap.puntoC[0] - distancia;

                            distancia = objSemTrap.puntoC[0] - Double.parseDouble(parts[parts.length - 2]);
                            objSemTrap.puntoI1[0] = objSemTrap.puntoC[0] - distancia;
                        }
                        objSemTrap.puntoI1[1] = 0;
                        objSemTrap.turno = contFigura;
                        listSemiTrapezoide.add(objSemTrap);
                        objSemTrap.membresiaY = Double.parseDouble(parts[5]);

                        ++contFigura;
                        break;

                    default: //universo de discurso
                        objU = new UniversoDiscurso();
                        objU.setOrigen(Double.parseDouble(parts[0]));
                        objU.setFin(Double.parseDouble(parts[1]));
                        objU.setUnidad(parts[2]);
                        objU.setVariable(parts[3]);
                }
            }

            nullLista(); //vuelve nulas las listas que no tienen nada
        } catch (Exception e) {
            e.printStackTrace(); //para pruebas
            System.out.println("ERROR, CREACIÓN DEL MODELO");
        }
    }

    private void iniLista() {
        listSemiTrapezoide = new ArrayList<>();
    }

    private void nullLista() {
        if (listSemiTrapezoide.isEmpty()) {
            listSemiTrapezoide = null;
        }
    }

    private double calcularY(SemiTrapezoide objSTrap) {
        char orientacion = objSTrap.getOrientacion();
        double y = 0;
        if (orientacion == 'i') {
            if (objU.getOrigen() < punto && punto < objSTrap.puntoI1[0]) {
                //Esta dentro izquierda
                if (objSTrap.puntoC[0] < punto && punto < objSTrap.puntoI1[0]) {
                    double p1[] = {objSTrap.puntoC[0], objSTrap.puntoC[1]};
                    double p2[] = {objSTrap.puntoI1[0], objSTrap.puntoI1[1]};
                    y = (punto - p1[0]) / (p2[0] - p1[0]) * (p2[1] - p1[1]) + (p1[1]);
                    resultado = y + "";
                } else {
                    //Esta en la linea Recta de 1's
                    resultado = 1 + ""; //y = 1
                }
            } else {
                //El punto no pertenece a la funcion
                resultado = 0 + ""; //y = 0
            }
        } else {
            if (punto < objU.getFin() && punto > objSTrap.puntoI1[0]) {
                //Esta dentro derecha
                if (punto < objSTrap.puntoC[0] && punto > objSTrap.puntoI1[0]) {
                    double p1[] = {objSTrap.puntoC[0], objSTrap.puntoC[1]};
                    double p2[] = {objSTrap.puntoI1[0], objSTrap.puntoI1[1]};
                    y = (punto - p1[0]) / (p2[0] - p1[0]) * (p2[1] - p1[1]) + (p1[1]);
                    resultado += objSTrap.etiqueta + " Y = " + y + "\n";
                } else {
                    //Esta en la linea recta de 1's
                    y = 1;
                    resultado += objSTrap.etiqueta + " Y = " + y + "\n";
                }
            } else {
                //El punto no pertenece a la funcion
                y = 0;
                resultado += objSTrap.etiqueta + " Y = " + y + "\n";
            }
        }
        return y;
    }
}
