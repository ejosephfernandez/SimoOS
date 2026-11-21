import java.util.Scanner;

public class Menu {

    public void mostrar() {
        Scanner entrada = new Scanner(System.in);
        int opcion;

        do {
            System.out.println("\n========== SimOS v1.0 ==========");
            System.out.println("Usuario: admin   Rol: Administrador"); // Pendiente por acomodar con los valores !!
            System.out.println("--------------------------------");
            System.out.println("1. Planificacion de CPU");
            System.out.println("2. Concurrencia y sincronizacion");
            System.out.println("3. Gestion de memoria");
            System.out.println("4. Almacenamiento (planificacion de disco)");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");

            try {
                opcion = Integer.parseInt(entrada.nextLine().trim());
            } catch (NumberFormatException e) {
                opcion = -1;
            }

            switch (opcion) {
                case 1:
                    System.out.println("Modulo 1: Planificacion de CPU");
                    break;
                case 2:
                    System.out.println("Modulo 2: Concurrencia y sincronizacion");
                    break;
                case 3:
                    System.out.println("Modulo 3: Gestion de memoria");
                    break;
                case 4:
                    System.out.println("Modulo 4: Almacenamiento");
                    break;
                case 0:
                    System.out.println("Saliendo de SimOS...");
                    break;
                default:
                    System.out.println("Opcion invalida.");
            }
        } while (opcion != 0);
    }
}
