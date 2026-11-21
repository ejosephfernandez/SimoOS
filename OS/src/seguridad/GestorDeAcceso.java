package seguridad;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Administra los usuarios y la matriz de acceso (rol -> modulos permitidos). */
public class GestorDeAcceso {

    public static final String MOD_CPU = "Planificacion de CPU";
    public static final String MOD_CONCURRENCIA = "Concurrencia y sincronizacion";
    public static final String MOD_MEMORIA = "Gestion de memoria";
    public static final String MOD_ALMACENAMIENTO = "Almacenamiento (planificacion de disco)";

    private final List<Usuarios> usuarios = new ArrayList<>();
    private final Map<Rol, Set<String>> permisos = new EnumMap<>(Rol.class);

    public GestorDeAcceso() {
        cargarUsuarios();
        cargarPermisos();
    }

    private void cargarUsuarios() {
        usuarios.add(new Usuarios("admin", "admin123", Rol.ADMINISTRADOR));
        usuarios.add(new Usuarios("ana", "ana123", Rol.USUARIO));
        usuarios.add(new Usuarios("luis", "luis123", Rol.USUARIO));
    }

    private void cargarPermisos() {
        Set<String> admin = new HashSet<>();
        admin.add(MOD_CPU);
        admin.add(MOD_CONCURRENCIA);
        admin.add(MOD_MEMORIA);
        admin.add(MOD_ALMACENAMIENTO);
        permisos.put(Rol.ADMINISTRADOR, admin);

        Set<String> usuario = new HashSet<>();
        usuario.add(MOD_CPU);
        usuario.add(MOD_MEMORIA);
        permisos.put(Rol.USUARIO, usuario);
    }

    public Usuarios buscar(String nombre) {
        for (Usuarios u : usuarios) {
            if (u.getNombre().equalsIgnoreCase(nombre)) {
                return u;
            }
        }
        return null;
    }

    public boolean puedeAcceder(Usuarios usuario, String modulo) {
        return permisos.get(usuario.getRol()).contains(modulo);
    }
}