import seguridad.Bitacora;
import seguridad.GestorDeAcceso;
import seguridad.Usuarios;

import java.util.Scanner;

public class Menu {
    private final Usuarios usuario;
    private final GestorDeAcceso acceso;
    private final Bitacora bitacora;
    private final Scanner entrada;

    public Menu(Usuarios usuario, GestorDeAcceso acceso, Bitacora bitacora, Scanner entrada) {
        this.usuario = usuario;
        this.acceso = acceso;
        this.bitacora = bitacora;
        this.entrada = entrada;
    }

    public void mostrar() {
        int opcion;

        do {
            System.out.println("\n========== SimOS v1.0 ==========");
            System.out.println("Usuario: " + usuario.getNombre() + "   Rol: " + usuario.getRol());
            System.out.println("--------------------------------");
            System.out.println("1. Planificacion de CPU");
            System.out.println("2. Concurrencia y sincronizacion");
            System.out.println("3. Gestion de memoria");
            System.out.println("4. Almacenamiento (planificacion de disco)");
            System.out.println("0. Cerrar sesion");
            System.out.print("Seleccione una opcion: ");

            try {
                opcion = Integer.parseInt(entrada.nextLine().trim());
            } catch (NumberFormatException e) {
                opcion = -1;
            }

            switch (opcion) {
                case 1 -> abrirModulo(GestorDeAcceso.MOD_CPU);
                case 2 -> abrirModulo(GestorDeAcceso.MOD_CONCURRENCIA);
                case 3 -> abrirModulo(GestorDeAcceso.MOD_MEMORIA);
                case 4 -> abrirModulo(GestorDeAcceso.MOD_ALMACENAMIENTO);
                case 0 -> {
                    bitacora.registrar(usuario.getNombre(), "Cierre de sesion", true);
                    System.out.println("Sesion cerrada.");
                }
                default -> System.out.println("Opcion invalida.");
            }
        } while (opcion != 0);
    }

    /** Verifica el permiso del rol, registra el intento en la bitacora y abre el modulo. */
    private void abrirModulo(String modulo) {
        boolean permitido = acceso.puedeAcceder(usuario, modulo);
        bitacora.registrar(usuario.getNombre(), "Acceso a modulo: " + modulo, permitido);

        if (!permitido) {
            System.out.println("Acceso denegado: el rol " + usuario.getRol()
                    + " no tiene permiso para este modulo.");
            return;
        }

        System.out.println("Modulo: " + modulo);
        // TODO: aqui se invocara cada modulo (cpu, concurrencia, memoria, disco)
    }
}