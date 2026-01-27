package org.dam.fcojavier.ecometrica;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.dam.fcojavier.ecometrica.utils.DataSeeder;
import org.dam.fcojavier.ecometrica.utils.HibernateUtil;

import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        System.out.println("Iniciando EcoMetrica...");

        HibernateUtil.getSessionFactory();

        DataSeeder seeder = new DataSeeder();
        seeder.sembrarDatos();

        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("views/Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm()); // <--- IMPORTANTE

        stage.setTitle("EcoMetrica - Login");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Método del Ciclo de Vida de JavaFX.
     * Se ejecuta AUTOMÁTICAMENTE justo antes de que el proceso muera.
     */
    @Override
    public void stop() throws Exception {
        System.out.println("PARANDO APLICACIÓN...");
        try{
            HibernateUtil.shutdown();
            System.out.println("Conexión a Base de Datos cerrada con éxito.");
        }catch (Exception e){
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }

        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}
