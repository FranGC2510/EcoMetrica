package org.dam.fcojavier.ecometrica;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.dam.fcojavier.ecometrica.utils.DataSeeder;
import org.dam.fcojavier.ecometrica.utils.HibernateUtil;

import javax.imageio.ImageIO;
import java.awt.Taskbar;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        System.out.println("Iniciando EcoMetrica...");

        HibernateUtil.getSessionFactory();

        DataSeeder seeder = new DataSeeder();
        seeder.sembrarDatos();
        //seeder.eliminarDatosDeUsuario(); // Descomentar solo si se quiere limpiar la BD
        seeder.sembrarUsuariosDePrueba(); // Carga los usuarios de prueba

        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("views/Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 750, 700);
        scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());

        // --- CONFIGURACIÓN DEL ICONO DE LA APLICACIÓN ---
        String rutaIcono = "/org/dam/fcojavier/ecometrica/views/images/isologo.png";

        // 1. Icono para la barra de título (Windows/Linux)
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream(rutaIcono)));
            stage.getIcons().add(new Image(getClass().getResourceAsStream(rutaIcono), 64, 64, true, true));
            stage.getIcons().add(new Image(getClass().getResourceAsStream(rutaIcono), 128, 128, true, true));
            stage.getIcons().add(new Image(getClass().getResourceAsStream(rutaIcono), 256, 256, true, true));
            stage.getIcons().add(new Image(getClass().getResourceAsStream(rutaIcono), 512, 512, true, true));
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono de la ventana: " + e.getMessage());
        }

        // 2. Icono para el Dock (macOS)
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("mac")) {
                URL iconURL = getClass().getResource(rutaIcono);
                if (iconURL != null) {
                    BufferedImage awtImage = ImageIO.read(iconURL);
                    if (Taskbar.isTaskbarSupported() && Taskbar.getTaskbar().isSupported(Taskbar.Feature.ICON_IMAGE)) {
                        Taskbar.getTaskbar().setIconImage(awtImage);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("No se pudo cambiar el icono del Dock en Mac: " + e.getMessage());
        }

        stage.setTitle("EcoMetrica - Login");
        stage.setScene(scene);
        stage.show();
    }

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
