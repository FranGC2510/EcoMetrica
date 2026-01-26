package org.dam.fcojavier.ecometrica.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.dam.fcojavier.ecometrica.MainApp;
import org.dam.fcojavier.ecometrica.entities.Usuario;
import org.dam.fcojavier.ecometrica.utils.Sesion;
import org.dam.fcojavier.ecometrica.utils.VistaNavegador;

import java.io.IOException;

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
            System.out.println("Acceso autorizado.");
            lblBienvenida.setText("Hola, " + usuario.getNombre());
        } else {
            // Seguridad: Si alguien intenta entrar sin login, fuera.
            System.out.println("Acceso no autorizado. Sin sesión.");
        }
    }

    @FXML
    public void onCerrarSesion(ActionEvent event) {
        System.out.println("Cerrando sesión, volviendo al login...");
        Sesion.getInstancia().logout();
        VistaNavegador.cargarVista(event, "views/Login.fxml");
    }

    private void cargarPantalla(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("views/" + fxml));
            Parent vista = loader.load();
            contentPane.setCenter(vista); // Cambio dinámico del centro
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onMenuHuellasClick(ActionEvent event) {
        System.out.println("Ir a gestión de huellas");
        cargarPantalla("HuellaView.fxml");
    }

    @FXML
    public void onMenuHabitosClick(ActionEvent event) {
        System.out.println("Ir a gestión de hábitos");
        cargarPantalla("HabitosView.fxml");
    }

    @FXML
    public void onMenuResumenClick(ActionEvent event) {
        cargarPantalla("ResumenView.fxml");
    }
}
