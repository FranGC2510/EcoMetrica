package org.dam.fcojavier.ecometrica.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.dam.fcojavier.ecometrica.MainApp;
import org.dam.fcojavier.ecometrica.entities.Usuario;
import org.dam.fcojavier.ecometrica.utils.Sesion;
import org.dam.fcojavier.ecometrica.utils.VistaNavegador;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Controlador principal de la aplicación (Layout).
 * Gestiona la navegación entre las diferentes vistas del dashboard y el cierre de sesión.
 */
public class MainController {

    @FXML private Label lblBienvenida;
    @FXML private BorderPane contentPane;
    @FXML private Button btnInicio, btnHuellas, btnHabitos, btnEstadisticas;

    /**
     * Inicializa el controlador.
     * Verifica la sesión del usuario, muestra el saludo y carga la vista de inicio por defecto.
     */
    @FXML
    public void initialize() {
        Usuario usuario = Sesion.getInstancia().getUsuarioLogueado();

        if (usuario != null) {
            System.out.println("Usuario logueado: " + usuario.getNombre());
            lblBienvenida.setText("Hola, " + usuario.getNombre());
            gestionarEstadoBotones(btnInicio);
            cargarPantalla("InicioView.fxml");
        }
    }

    /**
     * Cierra la sesión del usuario actual y redirige a la pantalla de login.
     *
     * @param event Evento de acción que desencadena el cierre de sesión.
     */
    @FXML
    public void onCerrarSesion(ActionEvent event) {
        System.out.println("Cerrando sesión, volviendo al login...");
        Sesion.getInstancia().logout();
        VistaNavegador.cargarVista(event, "views/Login.fxml");
    }

    /**
     * Carga una vista FXML en el área central del BorderPane principal.
     *
     * @param fxml Nombre del archivo FXML a cargar (debe estar en la carpeta 'views').
     */
    private void cargarPantalla(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("views/" + fxml));
            Parent vista = loader.load();
            contentPane.setCenter(vista);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gestiona el estado visual de los botones del menú lateral.
     * Resalta el botón activo y desactiva el resaltado en los demás.
     *
     * @param botonActivo El botón que se ha pulsado y debe aparecer activo.
     */
    private void gestionarEstadoBotones(Button botonActivo) {
        List<Button> todosLosBotones = Arrays.asList(btnInicio, btnHuellas, btnHabitos, btnEstadisticas);

        for (Button btn : todosLosBotones) {
            btn.getStyleClass().removeAll("btn-menu-active");
        }

        if (botonActivo != null) {
            botonActivo.getStyleClass().add("btn-menu-active");
        }
    }

    // --- Métodos de Navegación del Menú ---

    @FXML
    public void onMenuHuellasClick(ActionEvent event) {
        gestionarEstadoBotones(btnHuellas);
        System.out.println("Ir a gestión de huellas");
        cargarPantalla("HuellaView.fxml");
    }

    @FXML
    public void onMenuHabitosClick(ActionEvent event) {
        gestionarEstadoBotones(btnHabitos);
        System.out.println("Ir a gestión de hábitos");
        cargarPantalla("HabitosView.fxml");
    }

    @FXML
    public void onMenuInicioClick(ActionEvent event) {
        gestionarEstadoBotones(btnInicio);
        System.out.println("Ir al inicio");
        cargarPantalla("InicioView.fxml");
    }

    @FXML
    public void onMenuEstadisticasClick(ActionEvent event) {
        gestionarEstadoBotones(btnEstadisticas);
        System.out.println("Ir a estadísticas");
        cargarPantalla("EstadisticasView.fxml");
    }

    @FXML
    public void onMenuPerfilClick() {
        gestionarEstadoBotones(null);
        System.out.println("Ir a perfil");
        cargarPantalla("PerfilView.fxml");
    }
}
