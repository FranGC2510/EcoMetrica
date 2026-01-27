package org.dam.fcojavier.ecometrica.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Proporciona utilidades para el manejo seguro de contraseñas.
 * Utiliza el algoritmo BCrypt para el hashing y verificación.
 */
public class PasswordUtilidades {
    /**
     * Genera un hash seguro a partir de una contraseña en texto plano.
     * Incluye una salt generada automáticamente.
     * @param password Contraseña original del usuario.
     * @return El hash generado para ser almacenado en la base de datos.
     * @throws IllegalArgumentException Si la contraseña proporcionada es nula o vacía.
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
    /**
     * Verifica si una contraseña en texto plano coincide con un hash almacenado.
     * @param password La contraseña en texto plano a verificar.
     * @param hashedPassword El hash de la contraseña con el que se comparará.
     * @return true si la contraseña coincide con el hash, false en caso contrario.
     */
    public static boolean checkPassword(String password, String hashedPassword) {
        if (password == null || hashedPassword == null) return false;
        try {
            return BCrypt.checkpw(password, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
