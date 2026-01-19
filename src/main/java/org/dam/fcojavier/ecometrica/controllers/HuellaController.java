package org.dam.fcojavier.ecometrica.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.dam.fcojavier.ecometrica.entities.Actividad;
import org.dam.fcojavier.ecometrica.entities.Categoria;
import org.dam.fcojavier.ecometrica.entities.Huella;
import org.dam.fcojavier.ecometrica.entities.Usuario;
import org.dam.fcojavier.ecometrica.services.HuellaService;
import org.dam.fcojavier.ecometrica.utils.Sesion;

import java.time.LocalDate;
import java.util.List;

public class HuellaController {

    private final HuellaService huellaService = new HuellaService();

    // --- FORMULARIO ---
    @FXML private ComboBox<Categoria> cbCategoria;
    @FXML private ComboBox<Actividad> cbActividad;
    @FXML private TextField txtValor;
    @FXML private DatePicker dpFecha;
    @FXML private Label lblUnidad;
    @FXML private Label lblMensaje;
    @FXML private Button btnGuardar;
    @FXML private Button btnEliminar;

    // Variable para controlar si estamos editando
    private Huella huellaSeleccionada = null;

    // --- TABLA ---
    @FXML private TableView<Huella> tablaHuellas;
    @FXML private TableColumn<Huella, LocalDate> colFecha;
    @FXML private TableColumn<Huella, String> colActividad;
    @FXML private TableColumn<Huella, String> colCategoria;
    @FXML private TableColumn<Huella, Double> colValor;
    @FXML private TableColumn<Huella, String> colUnidad;
    @FXML private TableColumn<Huella, String> colImpacto; // Columna Calculada (String formateado)

    @FXML
    public void initialize() {
        // 1. Configuración de Columnas
        configurarTabla();

        // Configuración inicial de combos para que usen la celda inteligente
        resetearCombo(cbCategoria);
        resetearCombo(cbActividad);
        cbActividad.setPromptText("Primero elige categoría...");
        // 2. Configuración del Formulario
        dpFecha.setValue(LocalDate.now());
        dpFecha.setDayCellFactory(param -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(false);
                getStyleClass().remove("fecha-futura");
                if (date.isAfter(LocalDate.now())) {
                    setDisable(true);
                    getStyleClass().add("fecha-futura");
                }
            }
        });

        cargarCategorias();

        // Listener para cascada Categoría -> Actividad
        cbCategoria.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                cbActividad.setValue(null);
                cbActividad.setPromptText("Selecciona una actividad...");
                cargarActividades(newVal);
                lblUnidad.setText(newVal.getUnidad());
                cbActividad.setDisable(false);
            } else {
                cbActividad.getItems().clear();
                cbActividad.setDisable(true);
                lblUnidad.setText("-");
            }
        });

        tablaHuellas.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                huellaSeleccionada = newSelection;
                cargarHuellaEnFormulario(newSelection);
            }
        });

        // 3. Cargar datos iniciales en la tabla
        refrescarTabla();
    }

    private void configurarTabla() {
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colValor.setCellValueFactory(new PropertyValueFactory<>("valor"));
        colUnidad.setCellValueFactory(new PropertyValueFactory<>("unidad"));

        // Propiedades anidadas (Huella -> Actividad -> Nombre)
        // NOTA: Usamos getId_actividad() porque así se llama el getter en tu entidad Huella.java
        colActividad.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getId_actividad().getNombre()));

        colCategoria.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getId_actividad().getCategoria().getNombre()));

        // COLUMNA CALCULADA: Impacto = Valor * Factor Emisión
        colImpacto.setCellValueFactory(cellData -> {
            Huella h = cellData.getValue();
            double factor = h.getId_actividad().getCategoria().getFactorEmision();
            double totalCo2 = h.getValor() * factor;
            return new SimpleStringProperty(String.format("%.2f", totalCo2));
        });
    }

    private void cargarCategorias() {
        cbCategoria.setItems(FXCollections.observableArrayList(huellaService.obtenerTodasCategorias()));
    }

    private void cargarActividades(Categoria categoria) {
        cbActividad.setItems(FXCollections.observableArrayList(huellaService.obtenerActividadesPorCategoria(categoria)));
    }

    private void refrescarTabla() {
        Usuario usuario = Sesion.getInstancia().getUsuarioLogueado();
        if (usuario != null) {
            List<Huella> huellas = huellaService.obtenerHuellasDelUsuario(usuario);
            tablaHuellas.setItems(FXCollections.observableArrayList(huellas));
        }
    }

    private void cargarHuellaEnFormulario(Huella huella) {
        // 1. Cargamos datos simples
        dpFecha.setValue(huella.getFecha());
        txtValor.setText(String.valueOf(huella.getValor()));

        // 2. Cargamos los Combos (Cascada inversa)
        // Primero seleccionamos la categoría de la actividad de la huella
        Categoria cat = huella.getId_actividad().getCategoria();
        cbCategoria.setValue(cat);

        // Al seleccionar categoría, el listener carga las actividades.
        // Ahora seleccionamos la actividad específica.
        cbActividad.setValue(huella.getId_actividad());
        cbActividad.setDisable(false);

        // 3. Cambiamos estado de botones
        btnGuardar.setText("ACTUALIZAR");
        btnEliminar.setDisable(false);
        mostrarMensaje("Editando registro del " + huella.getFecha(), false);
    }

    @FXML
    public void onGuardarClick() {
        Usuario usuario = Sesion.getInstancia().getUsuarioLogueado();
        Actividad actividad = cbActividad.getValue();
        LocalDate fecha = dpFecha.getValue();
        String valorTexto = txtValor.getText();

        if (actividad == null || valorTexto.isEmpty() || fecha == null) {
            mostrarMensaje("Rellena todos los campos.", true);
            return;
        }

        if (fecha.isAfter(LocalDate.now())) {
            mostrarMensaje("La fecha no puede ser futura.", true);
            return;
        }

        try {
            double valor = Double.parseDouble(valorTexto);
            double co2 = 0;

            if (huellaSeleccionada == null) {
                // --- MODO CREAR ---
                co2 = huellaService.registrarHuella(usuario, actividad, valor, fecha);
                mostrarMensaje(String.format("Registrado. Impacto: %.2f kg CO2", co2), false);
            } else {
                // --- MODO EDICIÓN ---
                huellaSeleccionada.setId_actividad(actividad);
                huellaSeleccionada.setValor(valor);
                huellaSeleccionada.setFecha(fecha);

                co2 = huellaService.actualizarHuella(huellaSeleccionada);
                mostrarMensaje(String.format("Actualizado. Impacto: %.2f kg CO2", co2), false);
            }

            refrescarTabla();
            onLimpiarClick(); // Limpiamos para volver al estado "Nuevo Registro"

        } catch (NumberFormatException e) {
            mostrarMensaje("El valor debe ser un número.", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onLimpiarClick() {
        limpiarFormulario();
        huellaSeleccionada = null;
        tablaHuellas.getSelectionModel().clearSelection();
        btnGuardar.setText("REGISTRAR IMPACTO");
        btnEliminar.setDisable(true);
        lblMensaje.setText("");
    }

    @FXML
    public void onEliminarClick() {
        if (huellaSeleccionada != null) {
            huellaService.eliminarHuella(huellaSeleccionada);
            mostrarMensaje("Registro eliminado.", false);
            refrescarTabla();
            onLimpiarClick();
        }
    }

    private void limpiarFormulario() {
        txtValor.clear();
        dpFecha.setValue(LocalDate.now());

        // 1. Resetear Categoría (Esto disparará el listener que limpia Actividad)
        resetearCombo(cbCategoria);

        // 2. Aseguramos que Actividad también quede limpia visualmente y bloqueada
        resetearCombo(cbActividad);
        cbActividad.setDisable(true);
        cbActividad.setPromptText("Primero elige categoría..."); // Restauramos el texto inicial

        lblUnidad.setText("-");
    }

    /**
     * Método auxiliar para resetear correctamente un ComboBox y que se vea el PromptText.
     */
    private <T> void resetearCombo(ComboBox<T> combo) {
        combo.setValue(null);
        combo.setButtonCell(new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); // Esto hace que se vea el PromptText
                } else {
                    setText(item.toString()); // Muestra el nombre normal
                }
            }
        });
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
