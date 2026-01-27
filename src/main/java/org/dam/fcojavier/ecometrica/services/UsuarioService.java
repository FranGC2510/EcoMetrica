package org.dam.fcojavier.ecometrica.services;

import org.dam.fcojavier.ecometrica.dao.UsuarioDAO;
import org.dam.fcojavier.ecometrica.entities.Usuario;
import org.dam.fcojavier.ecometrica.utils.PasswordUtilidades;

/**
 * Servicio encargado de la lógica de autenticación y gestión de usuarios.
 * Implementa las reglas de negocio para el registro y el inicio de sesión.
 */
public class UsuarioService {

    private final UsuarioDAO usuarioDAO;

    /**
     * Constructor por defecto. Inicializa el DAO de usuario.
     */
    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    /**
     * Valida las credenciales de un usuario.
     *
     * @param email    Correo electrónico del usuario.
     * @param password Contraseña en texto plano.
     * @return El objeto {@link Usuario} si las credenciales son válidas, null en caso contrario.
     */
    public Usuario login(String email, String password) {
        if (email == null || password == null) return null;

        Usuario usuario = usuarioDAO.findByEmail(email);

        if (usuario != null && PasswordUtilidades.checkPassword(password, usuario.getContraseña())) {
            return usuario;
        }

        return null;
    }

    /**
     * Registra un nuevo usuario aplicando las políticas de seguridad y unicidad.
     *
     * @param usuario       Objeto con los datos del usuario.
     * @param passwordPlana Contraseña sin hashear proporcionada en el formulario.
     * @return true si el registro es exitoso, false si el email ya existe.
     */
    public boolean registrarUsuario(Usuario usuario, String passwordPlana) {
        if (usuarioDAO.findByEmail(usuario.getEmail()) != null) {
            return false; // El usuario ya existe
        }

        String hash = PasswordUtilidades.hashPassword(passwordPlana);
        usuario.setContraseña(hash);

        usuarioDAO.save(usuario);
        return true;
    }

    /**
     * Actualiza los datos básicos (nombre, email) usando el método update del GenericDAO.
     */
    public boolean actualizarPerfil(Usuario usuario) {
        try {
            usuarioDAO.update(usuario);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gestiona el cambio de contraseña: verifica la actual, hashea la nueva y guarda.
     */
    public boolean cambiarContrasena(Usuario usuario, String passActual, String passNueva) {
        if (!PasswordUtilidades.checkPassword(passActual, usuario.getContraseña())) {
            return false;
        }

        String nuevoHash = PasswordUtilidades.hashPassword(passNueva);
        usuario.setContraseña(nuevoHash);

        try {
            usuarioDAO.update(usuario);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
