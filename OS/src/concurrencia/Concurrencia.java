package concurrencia;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/** Módulo 2: Concurrencia y sincronizacion (contador con y sin lock, productor-consumidor). */
public class Concurrencia {

    // Valores del caso de prueba del enunciado (se pueden cambiar aqui)
    private static final int HILOS = 10;
    private static final int INCREMENTOS = 1000;
    private static final int TAM_BUFFER = 5;
    private static final int TOTAL_ITEMS = 20;

    private final Scanner entrada;

    // --- Parte 1: contador compartido ---
    private int contador;
    private final ReentrantLock lock = new ReentrantLock();

    // --- Parte 2: productor-consumidor ---
    private final Queue<String> buffer = new ArrayDeque<>();
    private Semaphore vacios;   // cuántos espacios libres hay en el buffer
    private Semaphore llenos;   // cuántos elementos hay en el buffer
    private Semaphore mutex;    // solo un hilo toca el buffer a la vez
    private int producidos;
    private int consumidos;

    public Concurrencia(Scanner entrada) {
        this.entrada = entrada;
    }

    public void ejecutar() {
        int opcion;
        do {
            System.out.println("\n--- Concurrencia y sincronizacion ---");
            System.out.println("1. Contador compartido (sin lock y con lock)");
            System.out.println("2. Productor - consumidor");
            System.out.println("0. Volver");
            System.out.print("Opción: ");
            try {
                opcion = Integer.parseInt(entrada.nextLine().trim());
            } catch (NumberFormatException e) {
                opcion = -1;
            }

            switch (opcion) {
                case 1: probarContador(); break;
                case 2: probarProductorConsumidor(); break;
                case 0: break;
                default: System.out.println("Opcion no válida.");
            }
        } while (opcion != 0);
    }

    // =====================================================================
    // Parte 1: contador compartido
    // =====================================================================

    private void probarContador() {
        int esperado = HILOS * INCREMENTOS;
        System.out.println("\n" + HILOS + " hilos, cada uno incrementa " + INCREMENTOS
                + " veces. Resultado esperado: " + esperado);

        int sinLock = correrHilos(false);
        System.out.println("Sin sincronización: " + sinLock
                + "  (se perdieron " + (esperado - sinLock) + " incrementos)");

        int conLock = correrHilos(true);
        System.out.println("Con lock (mutex):   " + conLock
                + "  (se perdieron " + (esperado - conLock) + " incrementos)");
    }

    /** Crea los hilos, los ejecuta y devuelve el valor final del contador. */
    private int correrHilos(boolean usarLock) {
        contador = 0;
        Thread[] hilos = new Thread[HILOS];

        for (int i = 0; i < HILOS; i++) {
            hilos[i] = new Thread(() -> {
                for (int j = 0; j < INCREMENTOS; j++) {
                    if (usarLock) incrementarConLock();
                    else incrementarSinLock();
                }
            });
        }
        for (Thread h : hilos) h.start();
        try {
            for (Thread h : hilos) h.join();   // esperar a que todos terminen
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return contador;
    }

    /**
     * Incrementar = leer, sumar 1 y escribir. Si otro hilo escribe en medio,
     * se pierde un incremento (condición de carrera). El yield() cede la CPU
     * justo en ese momento para que el problema se vea siempre.
     */
    private void incrementarSinLock() {
        int temporal = contador;       // 1. leer
        Thread.yield();                // otro hilo puede entrar aquí
        contador = temporal + 1;       // 2. escribir
    }

    /** Mismo incremento, pero protegido: solo un hilo a la vez entra a la sección crítica. */
    private void incrementarConLock() {
        lock.lock();
        try {
            incrementarSinLock();
        } finally {
            lock.unlock();             // siempre se libera, aunque haya error
        }
    }

    // =====================================================================
    // Parte 2: productor - consumidor con semáforos
    // =====================================================================

    private void probarProductorConsumidor() {
        System.out.println("\nBuffer de " + TAM_BUFFER + " posiciones, " + TOTAL_ITEMS + " elementos.");
        System.out.println("2 productores (rápidos) y 2 consumidores (lentos).\n");

        buffer.clear();
        vacios = new Semaphore(TAM_BUFFER);
        llenos = new Semaphore(0);
        mutex = new Semaphore(1);
        producidos = 0;
        consumidos = 0;

        int porHilo = TOTAL_ITEMS / 2;
        Thread[] hilos = {
                crearProductor("Productor-1", porHilo, 50),
                crearProductor("Productor-2", porHilo, 50),
                crearConsumidor("Consumidor-1", porHilo, 150),
                crearConsumidor("Consumidor-2", porHilo, 150)
        };
        for (Thread h : hilos) h.start();
        try {
            for (Thread h : hilos) h.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("\nProducidos: " + producidos + "   Consumidos: " + consumidos
                + "   Buffer final: " + buffer);
    }

    private Thread crearProductor(String nombre, int cantidad, int pausaMs) {
        return new Thread(() -> {
            try {
                for (int i = 1; i <= cantidad; i++) {
                    String elemento = nombre.replace("Productor-", "P") + "-" + i;
                    Thread.sleep(pausaMs);                       // tiempo de "fabricar" el elemento

                    if (!vacios.tryAcquire()) {                  // no hay espacio libre
                        System.out.println("[" + nombre + "] espera: buffer lleno");
                        vacios.acquire();                        // se bloquea hasta que haya espacio
                    }
                    mutex.acquire();                             // entra a la sección crítica
                    buffer.add(elemento);
                    producidos++;
                    System.out.println("[" + nombre + "] PRODUCE " + elemento
                            + "   buffer (" + buffer.size() + "/" + TAM_BUFFER + "): " + buffer);
                    mutex.release();
                    llenos.release();                            // avisa: hay un elemento más
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, nombre);
    }

    private Thread crearConsumidor(String nombre, int cantidad, int pausaMs) {
        return new Thread(() -> {
            try {
                for (int i = 1; i <= cantidad; i++) {
                    if (!llenos.tryAcquire()) {                  // no hay nada que consumir
                        System.out.println("[" + nombre + "] espera: buffer vacío");
                        llenos.acquire();                        // se bloquea hasta que haya un elemento
                    }
                    mutex.acquire();
                    String elemento = buffer.poll();
                    consumidos++;
                    System.out.println("[" + nombre + "] CONSUME " + elemento
                            + "   buffer (" + buffer.size() + "/" + TAM_BUFFER + "): " + buffer);
                    mutex.release();
                    vacios.release();                            // avisa: hay un espacio libre más

                    Thread.sleep(pausaMs);                       // tiempo de "usar" el elemento
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, nombre);
    }
}