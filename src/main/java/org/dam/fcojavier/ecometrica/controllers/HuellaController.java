package org.dam.fcojavier.ecometrica.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.dam.fcojavier.ecometrica.entities.Actividad;
import org.dam.fcojavier.ecometrica.entities.Categoria;
import org.dam.fcojavier.ecometrica.entities.Usuario;
import org.dam.fcojavier.ecometrica.services.HuellaService;
import org.dam.fcojavier.ecometrica.utils.Sesion;

import java.time.LocalDate;
import java.util.List;

public class HuellaController {

    private final HuellaService huellaService = new HuellaService();

    @FXML private ComboBox<Categoria> cbCategoria;
    @FXML private ComboBox<Actividad> cbActividad;
    @FXML private TextField txtValor;
    @FXML private DatePicker dpFecha;
    @FXML private Label lblUnidad; // Para mostrar "Km", "kWh", etc.
    @FXML private Label lblMensaje;

    @FXML
    public void initialize() {
        dpFecha.setValue(LocalDate.now());
        cargarCategorias();

        // Listener: Cuando cambia la categoría...
        cbCategoria.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // 1. Limpiamos la selección anterior del segundo combo
                cbActividad.setValue(null);
                cbActividad.setPromptText("Selecciona una actividad...");

                // 2. Cargamos las nuevas actividades
                cargarActividades(newVal);

                // 3. Actualizamos la unidad y habilitamos
                lblUnidad.setText(newVal.getUnidad());
                cbActividad.setDisable(false);
            } else {
                // Si por alguna razón se deselecciona la categoría, bloqueamos todo
                cbActividad.getItems().clear();
                cbActividad.setDisable(true);
                lblUnidad.setText("-");
            }
        });
    }

    private void cargarCategorias() {
        List<Categoria> categorias = huellaService.obtenerTodasCategorias();
        cbCategoria.setItems(FXCollections.observableArrayList(categorias));
    }

    private void cargarActividades(Categoria categoria) {
        List<Actividad> actividades = huellaService.obtenerActividadesPorCategoria(categoria);
        cbActividad.setItems(FXCollections.observableArrayList(actividades));
    }

    @FXML
    public void onGuardarClick() {
        // 1. Recogemos datos
        Usuario usuario = Sesion.getInstancia().getUsuarioLogueado();
        Actividad actividad = cbActividad.getValue();
        LocalDate fecha = dpFecha.getValue();
        String valorTexto = txtValor.getText();

        // 2. Validaciones básicas
        if (actividad == null || valorTexto.isEmpty() || fecha == null) {
            mostrarMensaje("Rellena todos los campos.", true);
            return;
        }

        try {
            double valor = Double.parseDouble(valorTexto);

            double co2Generado = huellaService.registrarHuella(usuario, actividad, valor, fecha);

            // FEEDBACK MEJORADO: Mostramos al usuario cuánto ha contaminado
            // Usamos String.format para mostrar solo 2 decimales
            String mensaje = String.format("Registrado. Impacto: %.2f kg CO2", co2Generado);
            mostrarMensaje(mensaje, false);

            limpiarFormulario();

        } catch (NumberFormatException e) {
            mostrarMensaje("El valor debe ser un número (ej: 10.5)", true);
        } catch (Exception e) {
            mostrarMensaje("Error al guardar en base de datos.", true);
            e.printStackTrace();
        }
    }

    private void limpiarFormulario() {
        txtValor.clear();
        cbCategoria.getSelectionModel().clearSelection();
        cbActividad.getItems().clear();
        cbActividad.setDisable(true);
        lblUnidad.setText("Unidad");
    }

    private void mostrarMensaje(String texto, boolean esError) {
        lblMensaje.setText(texto);
        if (esError) {
            lblMensaje.setStyle("-fx-text-fill: -fx-color-error;");
        } else {
            lblMensaje.setStyle("-fx-text-fill: -fx-color-exito;");
        }
    }
}
