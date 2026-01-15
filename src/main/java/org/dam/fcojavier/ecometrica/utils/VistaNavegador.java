package org.dam.fcojavier.ecometrica.utils;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.dam.fcojavier.ecometrica.MainApp;

import java.io.IOException;

public class VistaNavegador {

    /**
     * Carga una nueva vista FXML en la ventana actual.
     *
     * @param event El evento que disparó la acción (necesario para obtener la ventana actual).
     * @param fxmlName El nombre del archivo FXML (ej: "views/Registro.fxml").
     */
    public static void cargarVista(Event event, String fxmlName) {
        try {
            // 1. Obtener el Stage (ventana) desde el elemento que disparó el evento
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();

            // 2. Cargar el nuevo FXML
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxmlName));
            Parent root = loader.load();

            // 3. Crear la nueva escena
            Scene scene = new Scene(root, 800, 600);

            // 4. Cargar los estilos CSS (Importante para no perder los colores)
            scene.getStylesheets().add(MainApp.class.getResource("/styles/application.css").toExternalForm());

            // 5. Mostrar la nueva escena
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.err.println("Error al cargar la vista: " + fxmlName);
            e.printStackTrace();
        }
    }
}
