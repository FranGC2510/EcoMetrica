package org.dam.fcojavier.ecometrica.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.dam.fcojavier.ecometrica.entities.Usuario;
import org.dam.fcojavier.ecometrica.services.UsuarioService;
import org.dam.fcojavier.ecometrica.utils.Sesion;
import org.dam.fcojavier.ecometrica.utils.VistaNavegador;
import javafx.event.ActionEvent;

import java.time.LocalDate;

/**
 * Controlador para la vista de registro de nuevos usuarios.
 * Gestiona la creación de cuentas y la validación de datos.
 */
public class RegistroController {

    private final UsuarioService usuarioService = new UsuarioService();

    @FXML private TextField txtNombre;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label lblMensaje;

    /**
     * Acción ejecutada al hacer clic en el botón "Registrarse".
     * Valida los datos del formulario y crea un nuevo usuario si todo es correcto.
     */
    @FXML
    protected void onRegistrarClick() {
        String nombre = txtNombre.getText();
        String email = txtEmail.getText();
        String pass = txtPassword.getText();
        String confirmPass = txtConfirmPassword.getText();

        if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            mostrarMensaje("Todos los campos son obligatorios.", true);
            return;
        }

        if (!pass.equals(confirmPass)) {
            mostrarMensaje("Las contraseñas no coinciden.", true);
            return;
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(nombre);
        nuevoUsuario.setEmail(email);
        nuevoUsuario.setFechaRegistro(LocalDate.now());

        boolean exito = usuarioService.registrarUsuario(nuevoUsuario, pass);

        if (exito) {
            Sesion.getInstancia().login(nuevoUsuario);
            try {
                VistaNavegador.cargarVista(txtEmail.getScene(), "/org/dam/fcojavier/ecometrica/views/MainLayout.fxml");
            } catch (Exception e) {
                mostrarMensaje("Error al entrar a la aplicación: " + e.getMessage(), true);
                e.printStackTrace();
            }
        } else {
            mostrarMensaje("Error: El email ya está registrado.", true);
        }
    }

    /**
     * Acción ejecutada al hacer clic en "Volver".
     * Regresa a la pantalla de inicio de sesión.
     *
     * @param event Evento de acción que desencadena la navegación.
     */
    @FXML
    protected void onVolverClick(ActionEvent event) {
        VistaNavegador.cargarVista(event, "views/Login.fxml");
    }

    /**
     * Muestra un mensaje de feedback en la interfaz.
     *
     * @param texto   El mensaje a mostrar.
     * @param esError Si es true, aplica estilo de error.
     */
    private void mostrarMensaje(String texto, boolean esError) {
        lblMensaje.setText(texto);
        lblMensaje.getStyleClass().removeAll("mensaje-error", "mensaje-exito");
        lblMensaje.getStyleClass().add("mensaje-base");
        if (esError) {
            lblMensaje.getStyleClass().add("mensaje-error");
        }
    }

    /**
     * Limpia los campos del formulario de registro.
     */
    private void limpiarFormulario() {
        txtNombre.clear();
        txtEmail.clear();
        txtPassword.clear();
        txtConfirmPassword.clear();
    }
}
