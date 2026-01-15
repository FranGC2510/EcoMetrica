package org.dam.fcojavier.ecometrica.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.dam.fcojavier.ecometrica.entities.Usuario;
import org.dam.fcojavier.ecometrica.utils.Sesion;
import org.dam.fcojavier.ecometrica.utils.VistaNavegador;

public class MainController {

    @FXML
    private Label lblBienvenida;

    @FXML
    private BorderPane contentPane; // El área donde cargaremos las distintas pantallas

    /**
     * Este método se ejecuta automáticamente al cargar la vista.
     */
    @FXML
    public void initialize() {
        Usuario usuario = Sesion.getInstancia().getUsuarioLogueado();

        if (usuario != null) {
            lblBienvenida.setText("Hola, " + usuario.getNombre());
        } else {
            // Seguridad: Si alguien intenta entrar sin login, fuera.
            System.out.println("Acceso no autorizado. Sin sesión.");
        }
    }

    @FXML
    public void onCerrarSesion(ActionEvent event) {
        // 1. Limpiamos la sesión
        Sesion.getInstancia().logout();

        // 2. Volvemos al Login
        VistaNavegador.cargarVista(event, "views/Login.fxml");
    }

    // Aquí añadiremos más adelante los métodos para cambiar el contenido central
    // public void mostrarResumen() { ... }
    // public void mostrarHuellas() { ... }
}
