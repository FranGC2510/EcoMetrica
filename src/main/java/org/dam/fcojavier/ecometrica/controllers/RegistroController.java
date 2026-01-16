package org.dam.fcojavier.ecometrica.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.dam.fcojavier.ecometrica.entities.Usuario;
import org.dam.fcojavier.ecometrica.services.UsuarioService; // Usamos el Servicio
import org.dam.fcojavier.ecometrica.utils.VistaNavegador;
import javafx.event.ActionEvent;

import java.time.LocalDate;

public class RegistroController {

    // CAMBIO: Usamos Service en vez de DAO
    private final UsuarioService usuarioService = new UsuarioService();

    @FXML private TextField txtNombre;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label lblMensaje;

    @FXML
    protected void onRegistrarClick() {
        String nombre = txtNombre.getText();
        String email = txtEmail.getText();
        String pass = txtPassword.getText();
        String confirmPass = txtConfirmPassword.getText();

        // 1. Validaciones de Vista (campos vacíos)
        if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            mostrarError("Todos los campos son obligatorios.");
            return;
        }

        if (!pass.equals(confirmPass)) {
            mostrarError("Las contraseñas no coinciden.");
            return;
        }

        // 2. Preparamos el objeto (Sin la contraseña todavía)
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(nombre);
        nuevoUsuario.setEmail(email);
        nuevoUsuario.setFechaRegistro(LocalDate.now());

        // 3. Delegamos toda la lógica compleja al Servicio
        boolean exito = usuarioService.registrarUsuario(nuevoUsuario, pass);

        if (exito) {
            lblMensaje.setText("¡Usuario registrado con éxito!");
            lblMensaje.setStyle("-fx-text-fill: -fx-color-exito;");
            limpiarFormulario();
        } else {
            mostrarError("El email ya está registrado.");
        }
    }

    private void mostrarError(String mensaje) {
        lblMensaje.setText(mensaje);
        lblMensaje.setStyle("-fx-text-fill: -fx-color-error;");
    }

    private void limpiarFormulario() {
        txtNombre.clear();
        txtEmail.clear();
        txtPassword.clear();
        txtConfirmPassword.clear();
    }

    @FXML
    protected void onVolverClick(ActionEvent event) {
        VistaNavegador.cargarVista(event, "views/Login.fxml");
    }
}