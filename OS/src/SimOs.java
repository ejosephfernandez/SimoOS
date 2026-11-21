import seguridad.Bitacora;
import seguridad.GestorDeAcceso;
import seguridad.Usuarios;

import java.util.Scanner;

public class SimOs {

    public static void main(String[] args) {
        Scanner entrada = new Scanner(System.in);
        Bitacora bitacora = new Bitacora();
        GestorDeAcceso acceso = new GestorDeAcceso();

        boolean continuar = true;
        while (continuar) {
            Usuarios usuario = iniciarSesion(entrada, acceso, bitacora);
            if (usuario == null) {
                continuar = false;
            } else {
                new Menu(usuario, acceso, bitacora, entrada).mostrar();
                System.out.print("\nIniciar otra sesion? (s/n): ");
                continuar = entrada.nextLine().trim().equalsIgnoreCase("s");
            }
        }
        System.out.println("Hasta luego.");
        entrada.close();
    }

    private static Usuarios iniciarSesion(Scanner entrada, GestorDeAcceso acceso, Bitacora bitacora) {
        System.out.println("\n===== SimOS - Inicio de sesion =====");
        System.out.println("(escriba 'salir' como usuario para terminar)");

        while (true) {
            System.out.print("Usuario: ");
            String nombre = entrada.nextLine().trim();
            if (nombre.equalsIgnoreCase("salir")) {
                return null;
            }
            System.out.print("Contrasena: ");
            String clave = entrada.nextLine();

            Usuarios u = acceso.buscar(nombre);
            if (u == null) {
                bitacora.registrar(nombre, "Inicio de sesion (usuario inexistente)", false);
                System.out.println("Usuario o contrasena incorrectos.");
            } else if (u.isBloqueado()) {
                bitacora.registrar(u.getNombre(), "Inicio de sesion (cuenta bloqueada)", false);
                System.out.println("La cuenta esta bloqueada por intentos fallidos.");
            } else if (u.verificarClave(clave)) {
                u.reiniciarIntentos();
                bitacora.registrar(u.getNombre(), "Inicio de sesion", true);
                return u;
            } else {
                u.registrarFallo();
                bitacora.registrar(u.getNombre(), "Inicio de sesion (contrasena incorrecta)", false);
                if (u.isBloqueado()) {
                    System.out.println("Cuenta bloqueada tras " + Usuarios.MAX_INTENTOS + " intentos fallidos.");
                } else {
                    System.out.println("Usuario o contrasena incorrectos.");
                }
            }
        }
    }
}