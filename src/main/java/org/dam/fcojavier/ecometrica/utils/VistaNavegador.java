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
     * Opción A: Usar este método cuando el cambio de pantalla lo provoca un clic (Botón o Label).
     * @param event El evento del ratón o teclado.
     * @param fxmlName Ruta del archivo FXML (ej: "views/Registro.fxml").
     */
    public static void cargarVista(Event event, String fxmlName) {
        Node node = (Node) event.getSource();
        cargarVista(node.getScene(), fxmlName);
    }

    /**
     * Opción B: Usar este método desde el código lógico (ej: LoginController tras validar pass).
     * @param currentScene La escena actual (se puede obtener de cualquier nodo con .getScene()).
     * @param fxmlName Ruta del archivo FXML.
     */
    public static void cargarVista(Scene currentScene, String fxmlName) {
        try {
            // 1. Obtenemos la ventana actual (Stage)
            Stage stage = (Stage) currentScene.getWindow();

            // 2. Cargamos el nuevo FXML
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxmlName));
            Parent root = loader.load();

            // 3. Creamos la escena nueva.
            // Ponemos un tamaño de 900x600 que es mejor para el Dashboard.
            Scene scene = new Scene(root, 900, 600);

            // 4. IMPORTANTE: Cargar el CSS global (para no perder tus colores Terracota)
            scene.getStylesheets().add(MainApp.class.getResource("/styles/application.css").toExternalForm());

            // 5. Mostrar la nueva escena y centrar la ventana
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            System.err.println("Error grave cargando la vista: " + fxmlName);
            e.printStackTrace();
        }
    }
}
