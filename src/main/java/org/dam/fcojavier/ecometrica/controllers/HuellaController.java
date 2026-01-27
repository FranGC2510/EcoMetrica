package org.dam.fcojavier.ecometrica.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.dam.fcojavier.ecometrica.entities.Actividad;
import org.dam.fcojavier.ecometrica.entities.Categoria;
import org.dam.fcojavier.ecometrica.entities.Huella;
import org.dam.fcojavier.ecometrica.entities.Usuario;
import org.dam.fcojavier.ecometrica.services.HuellaService;
import org.dam.fcojavier.ecometrica.utils.Sesion;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador para la gestión de huellas de carbono.
 * Permite registrar, consultar, actualizar y eliminar (CRUD) registros de impacto ambiental.
 */
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
    @FXML private TableColumn<Huella, Categoria> colCategoria;
    @FXML private TableColumn<Huella, Double> colValor;
    @FXML private TableColumn<Huella, String> colUnidad;
    @FXML private TableColumn<Huella, String> colImpacto; // Columna Calculada (String formateado)

    /**
     * Inicializa el controlador.
     * Configura la tabla, los listeners del formulario y carga los datos iniciales.
     */
    @FXML
    public void initialize() {
        configurarTabla();
        configurarFormulario();
        configurarListeners();
        refrescarTabla();
    }

    /**
     * Configura las columnas de la tabla de huellas.
     */
    private void configurarTabla() {
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colValor.setCellValueFactory(new PropertyValueFactory<>("valor"));
        colUnidad.setCellValueFactory(new PropertyValueFactory<>("unidad"));

        colActividad.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getId_actividad().getNombre()));

        colCategoria.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getId_actividad().getCategoria())
        );

        colCategoria.setCellFactory(column -> new TableCell<Huella, Categoria>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setGraphic(crearBadgeCategoria(item));
                    setText(null);
                }
            }
        });

        // COLUMNA CALCULADA: Impacto = Valor * Factor Emisión
        colImpacto.setCellValueFactory(cellData -> {
            Huella h = cellData.getValue();
            double factor = h.getId_actividad().getCategoria().getFactorEmision();
            double totalCo2 = h.getValor() * factor;
            return new SimpleStringProperty(String.format("%.2f", totalCo2));
        });
    }

    /**
     * Crea un componente gráfico (Badge) para mostrar la categoría con estilo.
     *
     * @param item La categoría a mostrar.
     * @return Un contenedor HBox con el Label estilizado.
     */
    private HBox crearBadgeCategoria(Categoria item) {
        Label lblBadge = new Label(item.getNombre());
        lblBadge.getStyleClass().add("badge-base");

        String nombreCat = item.getNombre().toLowerCase();
        if (nombreCat.contains("transporte")) lblBadge.getStyleClass().add("badge-transporte");
        else if (nombreCat.contains("energía") || nombreCat.contains("energia")) lblBadge.getStyleClass().add("badge-energia");
        else if (nombreCat.contains("alimentación") || nombreCat.contains("comida")) lblBadge.getStyleClass().add("badge-alimentacion");
        else if (nombreCat.contains("agua")) lblBadge.getStyleClass().add("badge-agua");
        else if (nombreCat.contains("residuos") || nombreCat.contains("basura")) lblBadge.getStyleClass().add("badge-residuos");
        else lblBadge.getStyleClass().add("badge-default");

        HBox container = new HBox(lblBadge);
        container.setAlignment(Pos.CENTER);
        return container;
    }

    /**
     * Configura el estado inicial del formulario y los DatePickers.
     */
    private void configurarFormulario() {
        resetearCombo(cbCategoria);
        resetearCombo(cbActividad);
        cbActividad.setPromptText("Primero elige categoría...");

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
    }

    /**
     * Configura los listeners para los componentes de la interfaz.
     */
    private void configurarListeners() {
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

        // Listener de selección en la tabla
        tablaHuellas.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                huellaSeleccionada = newSelection;
                cargarHuellaEnFormulario(newSelection);
            }
        });
    }

    private void cargarCategorias() {
        cbCategoria.setItems(FXCollections.observableArrayList(huellaService.obtenerTodasCategorias()));
    }

    private void cargarActividades(Categoria categoria) {
        cbActividad.setItems(FXCollections.observableArrayList(huellaService.obtenerActividadesPorCategoria(categoria)));
    }

    /**
     * Recarga la tabla con las huellas del usuario actual.
     */
    private void refrescarTabla() {
        Usuario usuario = Sesion.getInstancia().getUsuarioLogueado();
        if (usuario != null) {
            List<Huella> huellas = huellaService.obtenerHuellasDelUsuario(usuario);
            tablaHuellas.setItems(FXCollections.observableArrayList(huellas));
        }
    }

    /**
     * Carga los datos de una huella seleccionada en el formulario para su edición.
     *
     * @param huella La huella seleccionada.
     */
    private void cargarHuellaEnFormulario(Huella huella) {
        dpFecha.setValue(huella.getFecha());
        txtValor.setText(String.valueOf(huella.getValor()));

        Categoria cat = huella.getId_actividad().getCategoria();
        cbCategoria.setValue(cat);

        cbActividad.setValue(huella.getId_actividad());
        cbActividad.setDisable(false);

        btnGuardar.setText("ACTUALIZAR");
        btnEliminar.setDisable(false);
        mostrarMensaje("Editando registro del " + huella.getFecha(), false);
    }

    /**
     * Acción ejecutada al hacer clic en "Guardar".
     * Registra una nueva huella o actualiza una existente.
     */
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
            double co2;

            if (huellaSeleccionada == null) {
                co2 = huellaService.registrarHuella(usuario, actividad, valor, fecha);
                mostrarMensaje(String.format("Registrado. Impacto: %.2f kg CO2", co2), false);
            } else {
                huellaSeleccionada.setId_actividad(actividad);
                huellaSeleccionada.setValor(valor);
                huellaSeleccionada.setFecha(fecha);
                co2 = huellaService.actualizarHuella(huellaSeleccionada);
                mostrarMensaje(String.format("Actualizado. Impacto: %.2f kg CO2", co2), false);
            }

            refrescarTabla();
            onLimpiarClick();

        } catch (NumberFormatException e) {
            mostrarMensaje("El valor debe ser un número.", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Acción ejecutada al hacer clic en "Limpiar" o "Cancelar".
     * Resetea el formulario y el estado de selección.
     */
    @FXML
    public void onLimpiarClick() {
        limpiarFormulario();
        huellaSeleccionada = null;
        tablaHuellas.getSelectionModel().clearSelection();
        btnGuardar.setText("REGISTRAR IMPACTO");
        btnEliminar.setDisable(true);
        lblMensaje.setText("");
    }

    /**
     * Acción ejecutada al hacer clic en "Eliminar".
     * Elimina la huella seleccionada.
     */
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
        resetearCombo(cbCategoria);
        resetearCombo(cbActividad);
        cbActividad.setDisable(true);
        cbActividad.setPromptText("Primero elige categoría...");
        lblUnidad.setText("-");
    }

    /**
     * Resetea un ComboBox para mostrar el PromptText correctamente.
     */
    private <T> void resetearCombo(ComboBox<T> combo) {
        combo.setValue(null);
        combo.setButtonCell(new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });
    }

    /**
     * Muestra un mensaje de feedback en la interfaz.
     *
     * @param texto   El mensaje a mostrar.
     * @param esError Si es true, el mensaje se muestra en rojo; si no, en verde.
     */
    private void mostrarMensaje(String texto, boolean esError) {
        lblMensaje.setText(texto);
        lblMensaje.getStyleClass().removeAll("mensaje-error", "mensaje-exito");
        lblMensaje.getStyleClass().add("mensaje-base");
        if (esError) {
            lblMensaje.getStyleClass().add("mensaje-error");
        } else {
            lblMensaje.getStyleClass().add("mensaje-exito");
        }
    }
}
