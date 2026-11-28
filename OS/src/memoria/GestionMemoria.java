package memoria;

import java.util.Scanner;

/** Módulo 3: Gestión de memoria. Memoria paginada con reemplazo de páginas FIFO y LRU. */
public class GestionMemoria {

    private final Scanner entrada;
    private int[] referencias = new int[0];   // cadena de referencias a páginas

    public GestionMemoria(Scanner entrada) {
        this.entrada = entrada;
    }

    public void ejecutar() {
        int opcion;
        do {
            System.out.println("\n--- Gestión de memoria ---");
            System.out.println("1. Ingresar cadena de referencias por teclado");
            System.out.println("2. Usar cadena de prueba del enunciado");
            System.out.println("3. Ejecutar FIFO y LRU");
            System.out.println("0. Volver");
            System.out.print("Opción: ");
            opcion = leerEntero(0);

            switch (opcion) {
                case 1: ingresarReferencias(); break;
                case 2: cadenaDePrueba(); break;
                case 3: comparar(); break;
                case 0: break;
                default: System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }

    // ---------------- Datos de entrada ----------------

    private void ingresarReferencias() {
        System.out.println("Escriba los números de página separados por espacios o comas.");
        System.out.print("Cadena: ");
        String[] partes = entrada.nextLine().trim().split("[ ,]+");

        int[] lista = new int[partes.length];
        try {
            for (int i = 0; i < partes.length; i++) {
                lista[i] = Integer.parseInt(partes[i]);
                if (lista[i] < 0) throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            System.out.println("Cadena inválida: use solo números enteros mayores o iguales a 0.");
            return;
        }
        referencias = lista;
        System.out.println("Se cargaron " + referencias.length + " referencias.");
    }

    private void cadenaDePrueba() {
        referencias = new int[]{7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2, 1, 2, 0, 1, 7, 0, 1};
        System.out.println("Cadena de prueba cargada (20 referencias):");
        System.out.println("7 0 1 2 0 3 0 4 2 3 0 3 2 1 2 0 1 7 0 1");
    }

    // ---------------- Comparación ----------------

    private void comparar() {
        if (referencias.length == 0) {
            System.out.println("Primero cargue una cadena de referencias (opción 1 o 2).");
            return;
        }
        System.out.print("Número de marcos: ");
        int marcos = leerEntero(1);

        int fallosFifo = fifo(marcos);
        int fallosLru = lru(marcos);

        System.out.println("\n=========== COMPARACIÓN (" + marcos + " marcos) ===========");
        System.out.println("Fallos de página FIFO: " + fallosFifo);
        System.out.println("Fallos de página LRU:  " + fallosLru);
        if (fallosFifo < fallosLru) {
            System.out.println("Mejor algoritmo: FIFO (menos fallos)");
        } else if (fallosLru < fallosFifo) {
            System.out.println("Mejor algoritmo: LRU (menos fallos)");
        } else {
            System.out.println("Empate: ambos tienen los mismos fallos.");
        }
    }

    // ---------------- Algoritmo 1: FIFO ----------------

    /** Reemplaza la página que lleva MÁS TIEMPO en memoria (la primera que entró). */
    private int fifo(int numMarcos) {
        int[] marcos = vacios(numMarcos);
        int puntero = 0;       // marco que se reemplaza en el próximo fallo
        int fallos = 0;

        imprimirEncabezado("FIFO", numMarcos);
        for (int paso = 0; paso < referencias.length; paso++) {
            int pagina = referencias[paso];
            boolean fallo = !estaEnMemoria(marcos, pagina);

            if (fallo) {
                marcos[puntero] = pagina;                 // entra la página nueva
                puntero = (puntero + 1) % numMarcos;      // el siguiente más antiguo
                fallos++;
            }
            imprimirPaso(paso + 1, pagina, marcos, fallo);
        }
        System.out.println("Total de fallos FIFO: " + fallos);
        return fallos;
    }

    // ---------------- Algoritmo 2: LRU ----------------

    /** Reemplaza la página que lleva MÁS TIEMPO SIN USARSE (la menos recientemente usada). */
    private int lru(int numMarcos) {
        int[] marcos = vacios(numMarcos);
        int[] ultimoUso = new int[numMarcos];   // en qué paso se usó por última vez cada marco
        int fallos = 0;

        imprimirEncabezado("LRU", numMarcos);
        for (int paso = 0; paso < referencias.length; paso++) {
            int pagina = referencias[paso];
            int posicion = buscar(marcos, pagina);
            boolean fallo = (posicion == -1);

            if (fallo) {
                int victima = elegirVictimaLru(marcos, ultimoUso);
                marcos[victima] = pagina;
                posicion = victima;
                fallos++;
            }
            ultimoUso[posicion] = paso;     // se acaba de usar (ya sea acierto o carga nueva)
            imprimirPaso(paso + 1, pagina, marcos, fallo);
        }
        System.out.println("Total de fallos LRU: " + fallos);
        return fallos;
    }

    /** Si hay un marco vacío lo usa; si no, elige el de menor "último uso". */
    private int elegirVictimaLru(int[] marcos, int[] ultimoUso) {
        int victima = 0;
        for (int i = 0; i < marcos.length; i++) {
            if (marcos[i] == -1) return i;
            if (ultimoUso[i] < ultimoUso[victima]) victima = i;
        }
        return victima;
    }

    // ---------------- Auxiliares ----------------

    private int[] vacios(int n) {
        int[] marcos = new int[n];
        for (int i = 0; i < n; i++) marcos[i] = -1;   // -1 significa marco vacío
        return marcos;
    }

    /** Devuelve la posición de la página en los marcos, o -1 si no está. */
    private int buscar(int[] marcos, int pagina) {
        for (int i = 0; i < marcos.length; i++) {
            if (marcos[i] == pagina) return i;
        }
        return -1;
    }

    private boolean estaEnMemoria(int[] marcos, int pagina) {
        return buscar(marcos, pagina) != -1;
    }

    private void imprimirEncabezado(String nombre, int numMarcos) {
        System.out.println("\n=========== " + nombre + " ===========");
        StringBuilder linea = new StringBuilder(String.format("%-6s %-8s", "Paso", "Página"));
        for (int i = 1; i <= numMarcos; i++) linea.append(String.format("%-6s", "M" + i));
        linea.append("Resultado");
        System.out.println(linea);
    }

    private void imprimirPaso(int paso, int pagina, int[] marcos, boolean fallo) {
        StringBuilder linea = new StringBuilder(String.format("%-6d %-8d", paso, pagina));
        for (int m : marcos) linea.append(String.format("%-6s", m == -1 ? "-" : String.valueOf(m)));
        linea.append(fallo ? "FALLO" : "acierto");
        System.out.println(linea);
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