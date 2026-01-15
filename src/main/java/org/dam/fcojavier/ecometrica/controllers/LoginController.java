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

public class LoginController {

    // Instanciamos el servicio para poder usar la lógica de login
    private final UsuarioService usuarioService = new UsuarioService();

    // Elementos de la interfaz (View) que manipularemos desde aquí
    // Los nombres de las variables deben coincidir con los fx:id del FXML
    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblMensaje; // Para mostrar errores (ej: "Contraseña incorrecta")

    /**
     * Este método se ejecutará cuando se pulse el botón "Iniciar Sesión"
     */
    @FXML
    protected void onLoginButtonClick() {
        String email = txtEmail.getText();
        String password = txtPassword.getText();

        // 1. Validaciones básicas de interfaz (campos vacíos)
        if (email.isEmpty() || password.isEmpty()) {
            lblMensaje.setText("Por favor, rellena todos los campos.");
            lblMensaje.setStyle("-fx-text-fill: -fx-color-error;"); // Rojo
            return;
        }

        // 2. Llamamos al Servicio para verificar credenciales
        Usuario usuarioLogueado = usuarioService.login(email, password);

        if (usuarioLogueado != null) {
            // LOGIN ÉXITO
            Sesion.getInstancia().login(usuarioLogueado);

            System.out.println("Login correcto. Accediendo al Dashboard...");

            VistaNavegador.cargarVista(lblMensaje.getScene(), "views/MainLayout.fxml");
        } else {
            // LOGIN FALLIDO
            lblMensaje.setText("Email o contraseña incorrectos.");
            lblMensaje.setStyle("-fx-text-fill: -fx-color-error;");
        }
    }

    @FXML
    public void onRegistrarLabelClick(MouseEvent event) {
        // Usamos la utilidad para ir a la vista de Registro
        VistaNavegador.cargarVista(event, "views/Registro.fxml");
    }
}
