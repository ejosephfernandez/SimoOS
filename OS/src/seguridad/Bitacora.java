package seguridad;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Bitacora de auditoria: fecha | hora | usuario | accion | resultado. */
public class Bitacora {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd | HH:mm:ss");

    private final Path archivo;

    public Bitacora() {
        this(Paths.get("datos", "bitacora.txt"));
    }

    public Bitacora(Path archivo) {
        this.archivo = archivo;
    }

    public synchronized void registrar(String usuario, String accion, boolean permitido) {
        String linea = String.format("%s | %s | %s | %s%n",
                LocalDateTime.now().format(FORMATO),
                usuario,
                accion,
                permitido ? "PERMITIDO" : "DENEGADO");
        try {
            Path carpeta = archivo.getParent();
            if (carpeta != null) {
                Files.createDirectories(carpeta);
            }
            Files.writeString(archivo, linea,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("No se pudo escribir en la bitacora: " + e.getMessage());
        }
    }
}