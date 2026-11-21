# SimOS – Simulador de un sistema operativo

Proyecto final de Sistemas Operativos. Lenguaje: Java (solo consola).

## Compilar y ejecutar
    javac -encoding UTF-8 -d out $(find src -name "*.java")
    java -Dfile.encoding=UTF-8 -cp out simos.Main

## Usuarios de prueba
| Usuario | Contraseña | Rol           |
|---------|-----------|---------------|
| admin   | admin123  | Administrador |
| ana     | ana123    | Usuario       |
| luis    | luis123   | Usuario       |

La bitácora de auditoría se guarda en `bitacora.txt`.