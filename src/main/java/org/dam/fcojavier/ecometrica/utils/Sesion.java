package org.dam.fcojavier.ecometrica.utils;

import org.dam.fcojavier.ecometrica.entities.Usuario;

/**
 * Clase Singleton para gestionar el usuario conectado actualmente.
 * Permite acceder a los datos del usuario desde cualquier pantalla.
 */
public class Sesion {
    private static Sesion instancia;
    private Usuario usuarioLogueado;

    private Sesion() {}

    public static Sesion getInstancia() {
        if (instancia == null) {
            instancia = new Sesion();
        }
        return instancia;
    }

    public void login(Usuario usuario) {
        this.usuarioLogueado = usuario;
    }

    public void logout() {
        this.usuarioLogueado = null;
    }

    public Usuario getUsuarioLogueado() {
        return usuarioLogueado;
    }

    public boolean haySesionActiva() {
        return usuarioLogueado != null;
    }
}
