package org.dam.fcojavier.ecometrica.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.dam.fcojavier.ecometrica.entities.Usuario;
import org.dam.fcojavier.ecometrica.services.UsuarioService;
import org.dam.fcojavier.ecometrica.utils.Sesion;

/**
 * Controlador para la vista de perfil de usuario.
 * Permite visualizar y editar los datos personales y cambiar la contraseña.
 */
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

    private final UsuarioService usuarioService;
    private Usuario usuarioLogueado;

    /**
     * Constructor por defecto. Inicializa el servicio de usuario.
     */
    public PerfilController() {
        this.usuarioService = new UsuarioService();
    }

    /**
     * Inicializa el controlador.
     * Obtiene el usuario de la sesión y carga sus datos en la vista.
     */
    @FXML
    public void initialize() {
        usuarioLogueado = Sesion.getInstancia().getUsuarioLogueado();
        if (usuarioLogueado != null) {
            cargarDatosEnVista();
        }
    }

    /**
     * Carga los datos del usuario en los campos de texto y etiquetas.
     */
    private void cargarDatosEnVista() {
        lblNombreCompleto.setText(usuarioLogueado.getNombre());
        lblEmailDisplay.setText(usuarioLogueado.getEmail());

        txtNombre.setText(usuarioLogueado.getNombre());
        txtEmail.setText(usuarioLogueado.getEmail());
    }

    /**
     * Acción ejecutada al hacer clic en "Guardar Cambios".
     * Valida y actualiza la información personal del usuario.
     */
    @FXML
    public void onGuardarDatosClick() {
        String nuevoNombre = txtNombre.getText();
        String nuevoEmail = txtEmail.getText();

        if (nuevoNombre.isEmpty() || nuevoEmail.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos vacíos", "Por favor, completa todos los datos personales.");
            return;
        }

        usuarioLogueado.setNombre(nuevoNombre);
        usuarioLogueado.setEmail(nuevoEmail);

        boolean exito = usuarioService.actualizarPerfil(usuarioLogueado);

        if (exito) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Perfil Actualizado", "Tus datos se han guardado correctamente.");
            cargarDatosEnVista();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudieron guardar los cambios.");
        }
    }

    /**
     * Acción ejecutada al hacer clic en "Actualizar Contraseña".
     * Valida la contraseña actual y la coincidencia de la nueva, y solicita el cambio al servicio.
     */
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

        boolean exito = usuarioService.cambiarContrasena(usuarioLogueado, passActual, passNueva);

        if (exito) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Contraseña actualizada correctamente.");
            limpiarCamposPass();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "La contraseña actual es incorrecta o hubo un error.");
        }
    }

    /**
     * Limpia los campos de contraseña del formulario.
     */
    private void limpiarCamposPass() {
        txtPassActual.clear();
        txtPassNueva.clear();
        txtPassConfirmar.clear();
    }

    /**
     * Muestra una alerta modal al usuario.
     *
     * @param tipo    El tipo de alerta (WARNING, ERROR, INFORMATION).
     * @param titulo  El título de la ventana de alerta.
     * @param mensaje El contenido del mensaje.
     */
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
