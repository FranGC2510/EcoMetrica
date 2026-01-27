package org.dam.fcojavier.ecometrica.utils;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.dam.fcojavier.ecometrica.MainApp;

import java.io.IOException;
import java.net.URL;

/**
 * Utilidad para la gestión de navegación entre pantallas en JavaFX.
 * Centraliza la lógica de carga de archivos FXML y aplicación de estilos CSS.
 */
public class VistaNavegador {

    private static final double DEFAULT_WIDTH = 900;
    private static final double DEFAULT_HEIGHT = 600;
    private static final String MAIN_CSS = "/styles/application.css";

    /**
     * Cambia la vista actual a partir de un evento de interfaz de usuario.
     * @param event El evento generado (clic, teclado, etc.).
     * @param fxmlPath La ruta relativa al archivo FXML de la nueva vista.
     */
    public static void cargarVista(Event event, String fxmlPath) {
        if (event == null || event.getSource() == null) return;
        Node node = (Node) event.getSource();
        cargarVista(node.getScene(), fxmlPath);
    }

    /**
     * Cambia la vista cargando un nuevo FXML en el Stage actual.
     * Aplica automáticamente la hoja de estilos global.
     * * @param currentScene La escena actual para identificar la ventana (Stage).
     * @param fxmlPath La ruta relativa al recurso FXML.
     */
    public static void cargarVista(Scene currentScene, String fxmlPath) {
        if (currentScene == null) return;

        try {
            Stage stage = (Stage) currentScene.getWindow();

            URL fxmlLocation = MainApp.class.getResource(fxmlPath);
            if (fxmlLocation == null) {
                throw new IOException("No se encontró el archivo FXML en: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            Scene newScene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);

            // Carga segura de CSS
            URL cssResource = MainApp.class.getResource(MAIN_CSS);
            if (cssResource != null) {
                newScene.getStylesheets().add(cssResource.toExternalForm());
            }

            stage.setScene(newScene);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            System.err.println("Error al navegar a la vista: " + fxmlPath);
            e.printStackTrace();
        }
    }
}
