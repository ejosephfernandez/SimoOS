package cpu;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Scanner;

/** Módulo 1: Planificación de CPU con FCFS y Round Robin. */
public class PlanificacionCPU {

    private final Scanner entrada;

    // Datos de los procesos (la posición i de cada arreglo es el proceso i)
    private String[] nombre = new String[0];
    private int[] llegada = new int[0];
    private int[] rafaga = new int[0];

    // Diagrama de Gantt: se va armando mientras corre el algoritmo
    private StringBuilder arriba;
    private StringBuilder abajo;

    public PlanificacionCPU(Scanner entrada) {
        this.entrada = entrada;
    }

    public void ejecutar() {
        int opcion;
        do {
            System.out.println("\n--- Planificación de CPU ---");
            System.out.println("1. Ingresar procesos por teclado");
            System.out.println("2. Usar datos de prueba del enunciado");
            System.out.println("3. Ejecutar FCFS y Round Robin");
            System.out.println("0. Volver");
            System.out.print("Opción: ");
            opcion = leerEntero(0);

            switch (opcion) {
                case 1: ingresarProcesos(); break;
                case 2: datosDePrueba(); break;
                case 3: comparar(); break;
                case 0: break;
                default: System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }

    // ---------------- Datos de entrada ----------------

    private void ingresarProcesos() {
        System.out.print("¿Cuántos procesos? ");
        int n = leerEntero(1);
        nombre = new String[n];
        llegada = new int[n];
        rafaga = new int[n];

        System.out.println("Ingréselos en orden de llegada (de menor a mayor).");
        for (int i = 0; i < n; i++) {
            System.out.println("Proceso " + (i + 1) + ":");
            System.out.print("  Nombre: ");
            nombre[i] = entrada.nextLine().trim();
            System.out.print("  Tiempo de llegada: ");
            int minimo = (i == 0) ? 0 : llegada[i - 1]; // no puede llegar antes que el anterior
            llegada[i] = leerEntero(minimo);
            System.out.print("  Ráfaga de CPU: ");
            rafaga[i] = leerEntero(1);
        }
    }

    private void datosDePrueba() {
        nombre = new String[]{"P1", "P2", "P3", "P4", "P5"};
        llegada = new int[]{0, 1, 2, 3, 6};
        rafaga = new int[]{8, 4, 9, 5, 2};
        System.out.println("Datos de prueba cargados: P1(0,8) P2(1,4) P3(2,9) P4(3,5) P5(6,2)");
    }

    // ---------------- Algoritmo 1: FCFS ----------------

    /** Cada proceso corre completo, en el orden en que llegó. Devuelve los tiempos de finalización. */
    private int[] fcfs() {
        int n = nombre.length;
        int[] fin = new int[n];
        int tiempo = 0;

        for (int i = 0; i < n; i++) {
            if (tiempo < llegada[i]) {          // la CPU espera a que llegue el proceso
                agregarAlGantt("libre", tiempo);
                tiempo = llegada[i];
            }
            agregarAlGantt(nombre[i], tiempo);
            tiempo += rafaga[i];
            fin[i] = tiempo;
        }
        return fin;
    }

    // ---------------- Algoritmo 2: Round Robin ----------------

    /** Cada proceso usa la CPU un quantum y, si no termina, vuelve al final de la cola. */
    private int[] roundRobin(int quantum) {
        int n = nombre.length;
        int[] fin = new int[n];
        int[] restante = rafaga.clone();
        Queue<Integer> cola = new ArrayDeque<>();   // guarda la posición de cada proceso
        int tiempo = 0;
        int siguiente = 0;      // próximo proceso que aún no ha entrado a la cola
        int terminados = 0;

        while (terminados < n) {
            // Entran a la cola los que ya llegaron
            while (siguiente < n && llegada[siguiente] <= tiempo) {
                cola.add(siguiente++);
            }
            // Si no hay nadie listo, la CPU espera hasta la próxima llegada
            if (cola.isEmpty()) {
                agregarAlGantt("libre", tiempo);
                tiempo = llegada[siguiente];
                continue;
            }
            // Se atiende al primero de la cola durante un quantum (o menos si termina antes)
            int i = cola.poll();
            int uso = Math.min(quantum, restante[i]);
            agregarAlGantt(nombre[i], tiempo);
            tiempo += uso;
            restante[i] -= uso;

            // Los que llegaron durante este turno entran ANTES que el proceso que sale
            while (siguiente < n && llegada[siguiente] <= tiempo) {
                cola.add(siguiente++);
            }
            if (restante[i] > 0) {
                cola.add(i);            // no terminó: al final de la cola
            } else {
                fin[i] = tiempo;        // terminó
                terminados++;
            }
        }
        return fin;
    }

    // ---------------- Resultados ----------------

    private void comparar() {
        if (nombre.length == 0) {
            System.out.println("Primero ingrese procesos (opción 1 o 2).");
            return;
        }
        System.out.print("Quantum para Round Robin: ");
        int quantum = leerEntero(1);

        iniciarGantt();
        int[] finFcfs = fcfs();
        double esperaFcfs = mostrar("FCFS", finFcfs);

        iniciarGantt();
        int[] finRr = roundRobin(quantum);
        double esperaRr = mostrar("Round Robin (quantum = " + quantum + ")", finRr);

        System.out.println("\n=========== COMPARACIÓN ===========");
        System.out.printf("Espera promedio FCFS:        %.2f%n", esperaFcfs);
        System.out.printf("Espera promedio Round Robin: %.2f%n", esperaRr);
        if (esperaFcfs < esperaRr) {
            System.out.println("Mejor algoritmo: FCFS");
        } else if (esperaRr < esperaFcfs) {
            System.out.println("Mejor algoritmo: Round Robin");
        } else {
            System.out.println("Empate: ambos tienen la misma espera promedio.");
        }
    }

    /** Imprime el Gantt y la tabla. Retorno = fin - llegada. Espera = retorno - ráfaga. */
    private double mostrar(String titulo, int[] fin) {
        System.out.println("\n=========== " + titulo + " ===========");
        System.out.println("Diagrama de Gantt:");
        System.out.println(arriba + "|");
        System.out.println(abajo.toString() + ultimoInstante(fin));

        System.out.printf("%n%-10s %-9s %-8s %-13s %-9s %-8s%n",
                "Proceso", "Llegada", "Ráfaga", "Finalización", "Retorno", "Espera");
        int sumaEspera = 0;
        for (int i = 0; i < nombre.length; i++) {
            int retorno = fin[i] - llegada[i];
            int espera = retorno - rafaga[i];
            sumaEspera += espera;
            System.out.printf("%-10s %-9d %-8d %-13d %-9d %-8d%n",
                    nombre[i], llegada[i], rafaga[i], fin[i], retorno, espera);
        }
        double promedio = (double) sumaEspera / nombre.length;
        System.out.printf("Espera promedio: %.2f%n", promedio);
        return promedio;
    }

    // ---------------- Auxiliares ----------------

    private void iniciarGantt() {
        arriba = new StringBuilder();
        abajo = new StringBuilder();
    }

    /** Agrega un bloque al Gantt: el nombre arriba y el instante en que empieza abajo. */
    private void agregarAlGantt(String proceso, int inicio) {
        int ancho = Math.max(4, proceso.length());
        arriba.append("| ").append(String.format("%-" + ancho + "s", proceso)).append(" ");
        abajo.append(String.format("%-" + (ancho + 3) + "d", inicio));
    }

    /** El instante final del Gantt es el mayor tiempo de finalización. */
    private int ultimoInstante(int[] fin) {
        int mayor = 0;
        for (int f : fin) mayor = Math.max(mayor, f);
        return mayor;
    }

    /** Lee un entero >= minimo; si escriben texto o un valor menor, vuelve a preguntar. */
    private int leerEntero(int minimo) {
        while (true) {
            try {
                int valor = Integer.parseInt(entrada.nextLine().trim());
                if (valor >= minimo) return valor;
            } catch (NumberFormatException e) {
                // cae al mensaje de abajo
            }
            System.out.print("Escriba un número entero mayor o igual a " + minimo + ": ");
        }
    }
}