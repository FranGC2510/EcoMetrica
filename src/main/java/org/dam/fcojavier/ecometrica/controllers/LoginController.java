package org.dam.fcojavier.ecometrica.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.dam.fcojavier.ecometrica.entities.Usuario;
import org.dam.fcojavier.ecometrica.services.UsuarioService;
import org.dam.fcojavier.ecometrica.utils.Sesion;
import org.dam.fcojavier.ecometrica.utils.VistaNavegador;

/**
 * Controlador para la vista de inicio de sesión (Login).
 * Gestiona la autenticación de usuarios y la navegación al registro.
 */
public class LoginController {

    private final UsuarioService usuarioService = new UsuarioService();

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMensaje;

    /**
     * Acción ejecutada al hacer clic en el botón "Iniciar Sesión".
     * Valida las credenciales y redirige al dashboard si son correctas.
     */
    @FXML
    protected void onLoginButtonClick() {
        String email = txtEmail.getText();
        String password = txtPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            mostrarMensaje("Por favor, rellena todos los campos.", true);
            return;
        }

        Usuario usuarioLogueado = usuarioService.login(email, password);

        if (usuarioLogueado != null) {
            Sesion.getInstancia().login(usuarioLogueado);
            VistaNavegador.cargarVista(lblMensaje.getScene(), "views/MainLayout.fxml");
        } else {
            mostrarMensaje("Email o contraseña incorrectos.", true);
        }
    }

    /**
     * Acción ejecutada al hacer clic en el enlace de registro.
     * Redirige a la vista de registro de nuevos usuarios.
     *
     * @param event Evento de ratón que desencadena la navegación.
     */
    @FXML
    public void onRegistrarLabelClick(MouseEvent event) {
        VistaNavegador.cargarVista(event, "views/Registro.fxml");
    }

    /**
     * Muestra un mensaje de feedback en la interfaz.
     *
     * @param texto   El mensaje a mostrar.
     * @param esError Si es true, aplica estilo de error; si no, estilo normal/éxito.
     */
    private void mostrarMensaje(String texto, boolean esError) {
        lblMensaje.setText(texto);
        lblMensaje.getStyleClass().removeAll("mensaje-error", "mensaje-exito");
        lblMensaje.getStyleClass().add("mensaje-base");
        if (esError) {
            lblMensaje.getStyleClass().add("mensaje-error");
        }
    }
}
