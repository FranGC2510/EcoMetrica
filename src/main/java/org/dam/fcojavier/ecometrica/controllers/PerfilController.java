package org.dam.fcojavier.ecometrica.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.dam.fcojavier.ecometrica.entities.Usuario;
import org.dam.fcojavier.ecometrica.services.UsuarioService;
import org.dam.fcojavier.ecometrica.utils.Sesion;

public class PerfilController {

    // --- DATOS PERSONALES ---
    @FXML private Label lblNombreCompleto;
    @FXML private Label lblEmailDisplay;
    @FXML private TextField txtNombre;
    @FXML private TextField txtEmail;

    // --- SEGURIDAD ---
    @FXML private PasswordField txtPassActual;
    @FXML private PasswordField txtPassNueva;
    @FXML private PasswordField txtPassConfirmar;

    // Usamos el SERVICIO, no el DAO ni Hibernate directo
    private final UsuarioService usuarioService;
    private Usuario usuarioLogueado;

    public PerfilController() {
        this.usuarioService = new UsuarioService();
    }

    @FXML
    public void initialize() {
        usuarioLogueado = Sesion.getInstancia().getUsuarioLogueado();
        if (usuarioLogueado != null) {
            cargarDatosEnVista();
        }
    }

    private void cargarDatosEnVista() {
        lblNombreCompleto.setText(usuarioLogueado.getNombre());
        lblEmailDisplay.setText(usuarioLogueado.getEmail());

        txtNombre.setText(usuarioLogueado.getNombre());
        txtEmail.setText(usuarioLogueado.getEmail());
    }

    @FXML
    public void onGuardarDatosClick() {
        String nuevoNombre = txtNombre.getText();
        String nuevoEmail = txtEmail.getText();

        if (nuevoNombre.isEmpty() || nuevoEmail.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos vacíos", "Por favor, completa todos los datos personales.");
            return;
        }

        // Actualizamos el objeto en memoria
        usuarioLogueado.setNombre(nuevoNombre);
        usuarioLogueado.setEmail(nuevoEmail);

        // Delegamos al SERVICIO la persistencia
        boolean exito = usuarioService.actualizarPerfil(usuarioLogueado);

        if (exito) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Perfil Actualizado", "Tus datos se han guardado correctamente.");
            cargarDatosEnVista(); // Refrescar la cabecera
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudieron guardar los cambios.");
        }
    }

    @FXML
    public void onCambiarPassClick() {
        String passActual = txtPassActual.getText();
        String passNueva = txtPassNueva.getText();
        String passConfirm = txtPassConfirmar.getText();

        if (passActual.isEmpty() || passNueva.isEmpty() || passConfirm.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Faltan datos", "Rellena todos los campos de contraseña.");
            return;
        }

        if (!passNueva.equals(passConfirm)) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "La nueva contraseña y la confirmación no coinciden.");
            return;
        }

        // Llamamos al SERVICIO para verificar la actual y guardar la nueva
        boolean exito = usuarioService.cambiarContrasena(usuarioLogueado, passActual, passNueva);

        if (exito) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Contraseña actualizada correctamente.");
            limpiarCamposPass();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "La contraseña actual es incorrecta o hubo un error.");
        }
    }

    private void limpiarCamposPass() {
        txtPassActual.clear();
        txtPassNueva.clear();
        txtPassConfirmar.clear();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        if (txtNombre.getScene() != null) {
            alerta.initOwner(txtNombre.getScene().getWindow());
        }
        alerta.showAndWait();
    }
}
