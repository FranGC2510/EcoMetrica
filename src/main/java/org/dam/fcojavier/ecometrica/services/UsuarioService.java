package org.dam.fcojavier.ecometrica.services;

import org.dam.fcojavier.ecometrica.dao.UsuarioDAO;
import org.dam.fcojavier.ecometrica.entities.Usuario;
import org.dam.fcojavier.ecometrica.utils.PasswordUtilidades;

/**
 * Servicio para gestionar la lógica de negocio relacionada con Usuarios.
 * Actúa de intermediario entre el Controlador (Ventana) y el DAO (Base de Datos).
 */
public class UsuarioService {

    private final UsuarioDAO usuarioDAO;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    /**
     * Lógica de Login:
     * 1. Busca si el usuario existe.
     * 2. Si existe, comprueba la contraseña.
     * @return El usuario si el login es correcto, o null si falla.
     */
    public Usuario login(String email, String password) {
        // 1. Llamamos al DAO solo para pedir datos (sin lógica)
        Usuario usuario = usuarioDAO.findByEmail(email);

        // 2. Si existe, verificamos el hash de la contraseña usando BCrypt
        if (usuario != null) {
            if (PasswordUtilidades.checkPassword(password, usuario.getContraseña())) {
                return usuario;
            }
        }

        return null; // Login Fallido (Usuario no existe o pass incorrecta)
    }
}
