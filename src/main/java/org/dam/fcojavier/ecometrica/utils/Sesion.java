package org.dam.fcojavier.ecometrica.utils;

import org.dam.fcojavier.ecometrica.entities.Usuario;

/**
 * Gestor de estado de sesión de usuario implementado como Singleton.
 * Mantiene la información del usuario autenticado de forma global en la aplicación.
 */
public class Sesion {
    private static Sesion instancia;
    private Usuario usuarioLogueado;

    /** Constructor privado para evitar instanciación externa. */
    private Sesion() {
        if (instancia != null) {
            throw new RuntimeException("Use getInstancia() para obtener la sesión.");
        }
    }

    /**
     * Obtiene la instancia única de la sesión.
     * * @return Instancia única de Sesion.
     */
    public static Sesion getInstancia() {
        if (instancia == null) {
            instancia = new Sesion();
        }
        return instancia;
    }

    /**
     * Registra un usuario en el sistema tras una autenticación exitosa.
     * @param usuario El objeto Usuario que ha iniciado sesión.
     */
    public void login(Usuario usuario) {
        this.usuarioLogueado = usuario;
    }

    /**
     * Invalida la sesión actual eliminando la referencia al usuario.
     */
    public void logout() {
        this.usuarioLogueado = null;
    }

    /**
     * Obtiene los datos del usuario actualmente conectado.
     * @return El Usuario en sesión, o null si no hay ninguno.
     */
    public Usuario getUsuarioLogueado() {
        return usuarioLogueado;
    }

    /**
     * Verifica si existe una sesión activa en la aplicación.
     * * @return true si hay un usuario logueado, false en caso contrario.
     */
    public boolean haySesionActiva() {
        return usuarioLogueado != null;
    }
}
