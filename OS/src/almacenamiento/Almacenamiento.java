package almacenamiento;

import java.util.Scanner;

/** Modulo 4: Almacenamiento. Planificacion de disco con FCFS, SSTF y SCAN. */
public class Almacenamiento {

    private final Scanner entrada;

    // Datos del disco
    private int maxCilindro = 199;          // los cilindros van de 0 a maxCilindro
    private int cabezal = 0;                // posicion inicial del cabezal
    private boolean haciaMayores = true;    // direccion inicial (la usa SCAN)
    private int[] cola = new int[0];        // cola de solicitudes

    // Para el algoritmo que se esta ejecutando
    private int total;
    private StringBuilder orden;

    public Almacenamiento(Scanner entrada) {
        this.entrada = entrada;
    }

    public void ejecutar() {
        int opcion;
        do {
            System.out.println("\n--- Almacenamiento (planificacion de disco) ---");
            System.out.println("1. Ingresar datos por teclado");
            System.out.println("2. Usar datos de prueba del enunciado");
            System.out.println("3. Ejecutar FCFS, SSTF y SCAN");
            System.out.println("0. Volver");
            System.out.print("Opcion: ");
            opcion = leerEntero(0, Integer.MAX_VALUE);

            switch (opcion) {
                case 1: ingresarDatos(); break;
                case 2: datosDePrueba(); break;
                case 3: comparar(); break;
                case 0: break;
                default: System.out.println("Opcion no valida.");
            }
        } while (opcion != 0);
    }

    // ---------------- Datos de entrada ----------------

    private void ingresarDatos() {
        System.out.print("Ultimo cilindro del disco (van de 0 hasta ese numero): ");
        int max = leerEntero(1, Integer.MAX_VALUE);

        System.out.print("Posicion inicial del cabezal (0 a " + max + "): ");
        int posicion = leerEntero(0, max);

        System.out.println("Direccion inicial del cabezal:");
        System.out.println("  1. Hacia los cilindros mayores");
        System.out.println("  2. Hacia los cilindros menores");
        System.out.print("Opcion: ");
        boolean mayores = (leerEntero(1, 2) == 1);

        System.out.println("Escriba la cola de solicitudes (cilindros de 0 a " + max
                + ") separados por espacios o comas.");
        System.out.print("Cola: ");
        String[] partes = entrada.nextLine().trim().split("[ ,]+");

        int[] lista = new int[partes.length];
        try {
            for (int i = 0; i < partes.length; i++) {
                lista[i] = Integer.parseInt(partes[i]);
                if (lista[i] < 0 || lista[i] > max) throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            System.out.println("Cola invalida: use numeros enteros entre 0 y " + max + ". No se guardaron los datos.");
            return;
        }

        maxCilindro = max;
        cabezal = posicion;
        haciaMayores = mayores;
        cola = lista;
        System.out.println("Datos cargados: " + cola.length + " solicitudes.");
    }

    private void datosDePrueba() {
        maxCilindro = 199;
        cabezal = 70;
        haciaMayores = true;
        cola = new int[]{130, 25, 160, 45, 90, 10, 185, 75};
        System.out.println("Datos de prueba cargados:");
        System.out.println("Cilindros 0-199, cabezal en 70 hacia los mayores.");
        System.out.println("Cola: 130 25 160 45 90 10 185 75");
    }

    // ---------------- Comparacion ----------------

    private void comparar() {
        if (cola.length == 0) {
            System.out.println("Primero cargue los datos (opcion 1 o 2).");
            return;
        }
        int totalFcfs = fcfs();
        int totalSstf = sstf();
        int totalScan = scan();

        System.out.println("\n=========== COMPARACION ===========");
        System.out.println("Desplazamiento total FCFS: " + totalFcfs);
        System.out.println("Desplazamiento total SSTF: " + totalSstf);
        System.out.println("Desplazamiento total SCAN: " + totalScan);

        int menor = Math.min(totalFcfs, Math.min(totalSstf, totalScan));
        StringBuilder mejores = new StringBuilder();
        if (totalFcfs == menor) mejores.append("FCFS ");
        if (totalSstf == menor) mejores.append("SSTF ");
        if (totalScan == menor) mejores.append("SCAN ");
        System.out.println("Mejor algoritmo (menor desplazamiento): " + mejores.toString().trim());
    }

    // ---------------- Algoritmo 1: FCFS ----------------

    /** Atiende las solicitudes en el orden en que llegaron. */
    private int fcfs() {
        iniciar("FCFS");
        int actual = cabezal;
        for (int pedido : cola) {
            mover(actual, pedido);
            atender(pedido);
            actual = pedido;
        }
        return terminar();
    }

    // ---------------- Algoritmo 2: SSTF ----------------

    /** Siempre atiende la solicitud mas cercana a la posicion actual del cabezal. */
    private int sstf() {
        iniciar("SSTF");
        boolean[] atendido = new boolean[cola.length];
        int actual = cabezal;

        for (int k = 0; k < cola.length; k++) {
            int mejor = -1;
            for (int i = 0; i < cola.length; i++) {
                if (!atendido[i]
                        && (mejor == -1 || Math.abs(cola[i] - actual) < Math.abs(cola[mejor] - actual))) {
                    mejor = i;
                }
            }
            mover(actual, cola[mejor]);
            atender(cola[mejor]);
            atendido[mejor] = true;
            actual = cola[mejor];
        }
        return terminar();
    }

    // ---------------- Algoritmo 3: SCAN ----------------

    /**
     * Algoritmo del ascensor: el cabezal avanza en una direccion atendiendo lo que
     * encuentra, llega hasta el extremo del disco, cambia de direccion y atiende el resto.
     */
    private int scan() {
        iniciar("SCAN");
        boolean[] atendido = new boolean[cola.length];
        int actual = cabezal;
        boolean sentido = haciaMayores;

        for (int barrido = 0; barrido < 2; barrido++) {
            int i = siguienteEnDireccion(atendido, actual, sentido);
            while (i != -1) {
                mover(actual, cola[i]);
                atender(cola[i]);
                atendido[i] = true;
                actual = cola[i];
                i = siguienteEnDireccion(atendido, actual, sentido);
            }
            if (barrido == 0) {                       // llegar al extremo y cambiar de direccion
                int extremo = sentido ? maxCilindro : 0;
                if (actual != extremo) {
                    System.out.println("  (el cabezal llega al extremo " + extremo + " y cambia de direccion)");
                    mover(actual, extremo);
                    actual = extremo;
                }
                sentido = !sentido;
            }
        }
        return terminar();
    }

    /** Solicitud pendiente mas cercana en la direccion dada (o -1 si no hay ninguna). */
    private int siguienteEnDireccion(boolean[] atendido, int actual, boolean haciaMayores) {
        int mejor = -1;
        for (int i = 0; i < cola.length; i++) {
            if (atendido[i]) continue;
            boolean enDireccion = haciaMayores ? cola[i] >= actual : cola[i] <= actual;
            if (!enDireccion) continue;
            if (mejor == -1 || Math.abs(cola[i] - actual) < Math.abs(cola[mejor] - actual)) {
                mejor = i;
            }
        }
        return mejor;
    }

    // ---------------- Auxiliares ----------------

    private void iniciar(String nombre) {
        total = 0;
        orden = new StringBuilder();
        System.out.println("\n=========== " + nombre + " ===========");
        System.out.println("Cabezal inicial: " + cabezal);
        System.out.printf("%-8s %-8s %-14s%n", "Desde", "Hasta", "Desplazamiento");
    }

    /** Mueve el cabezal, suma la distancia al total y muestra el movimiento. */
    private void mover(int desde, int hasta) {
        int distancia = Math.abs(hasta - desde);
        total += distancia;
        System.out.printf("%-8d %-8d %-14d%n", desde, hasta, distancia);
    }

    private void atender(int cilindro) {
        if (orden.length() > 0) orden.append(" -> ");
        orden.append(cilindro);
    }

    private int terminar() {
        System.out.println("Orden de atencion: " + orden);
        System.out.println("Desplazamiento total: " + total);
        return total;
    }

    /** Lee un entero entre minimo y maximo; si escriben otra cosa, vuelve a preguntar. */
    private int leerEntero(int minimo, int maximo) {
        while (true) {
            try {
                int valor = Integer.parseInt(entrada.nextLine().trim());
                if (valor >= minimo && valor <= maximo) return valor;
            } catch (NumberFormatException e) {
                // cae al mensaje de abajo
            }
            System.out.print("Escriba un numero entero entre " + minimo + " y "
                    + (maximo == Integer.MAX_VALUE ? "infinito" : String.valueOf(maximo)) + ": ");
        }
    }
}