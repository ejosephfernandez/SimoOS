package seguridad;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Representa a un usuario del sistema. La contrasena se guarda como hash SHA-256. */
public class Usuarios {

    public static final int MAX_INTENTOS = 3;

    private final String nombre;
    private final String hashClave;
    private final Rol rol;
    private int intentosFallidos = 0;
    private boolean bloqueado = false;

    public Usuarios(String nombre, String clave, Rol rol) {
        this.nombre = nombre;
        this.hashClave = sha256(clave);
        this.rol = rol;
    }

    public static String sha256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    public boolean verificarClave(String clave) {
        return hashClave.equals(sha256(clave));
    }

    public void registrarFallo() {
        intentosFallidos++;
        if (intentosFallidos >= MAX_INTENTOS) {
            bloqueado = true;
        }
    }

    public void reiniciarIntentos() {
        intentosFallidos = 0;
    }

    public String getNombre() {
        return nombre;
    }

    public Rol getRol() {
        return rol;
    }

    public boolean isBloqueado() {
        return bloqueado;
    }
}