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
import org.dam.fcojavier.ecometrica.entities.Habito;
import org.dam.fcojavier.ecometrica.entities.Usuario;
import org.dam.fcojavier.ecometrica.services.HabitoService;
import org.dam.fcojavier.ecometrica.utils.Sesion;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador para la gestión de hábitos de usuario.
 * Permite crear, leer, actualizar y eliminar (CRUD) hábitos recurrentes.
 */
public class HabitosController {

    private final HabitoService habitoService = new HabitoService();

    // --- FORMULARIO ---
    @FXML private ComboBox<Categoria> cbCategoria;
    @FXML private ComboBox<Actividad> cbActividad;
    @FXML private TextField txtFrecuencia;
    @FXML private ComboBox<String> cbTipo; // diario, semanal...
    @FXML private DatePicker dpUltimaFecha;
    @FXML private Label lblMensaje;
    @FXML private Button btnGuardar;
    @FXML private Button btnEliminar;

    private Habito habitoSeleccionado = null;

    // --- TABLA ---
    @FXML private TableView<Habito> tablaHabitos;
    @FXML private TableColumn<Habito, String> colActividad;
    @FXML private TableColumn<Habito, Categoria> colCategoria;
    @FXML private TableColumn<Habito, Integer> colFrecuencia;
    @FXML private TableColumn<Habito, String> colTipo;
    @FXML private TableColumn<Habito, LocalDate> colFecha;

    /**
     * Inicializa el controlador.
     * Configura las columnas de la tabla, los listeners de los controles y carga los datos iniciales.
     */
    @FXML
    public void initialize() {
        configurarColumnasTabla();
        configurarListeners();
        cargarDatosIniciales();
    }

    /**
     * Configura las columnas de la tabla de hábitos, incluyendo la renderización personalizada de categorías (badges).
     */
    private void configurarColumnasTabla() {
        colActividad.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getActividad().getNombre()));

        colCategoria.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getActividad().getCategoria())
        );

        colCategoria.setCellFactory(column -> new TableCell<Habito, Categoria>() {
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

        colFrecuencia.setCellValueFactory(new PropertyValueFactory<>("frecuencia"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("ultimaFecha"));
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
     * Configura los listeners para los componentes de la interfaz.
     */
    private void configurarListeners() {
        // Listener para filtrado de actividades según categoría
        cbCategoria.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                cbActividad.setValue(null);
                cargarActividades(newVal);
                if (!cbActividad.getItems().isEmpty()) {
                    cbActividad.getSelectionModel().selectFirst();
                }
                cbActividad.setDisable(false);
            } else {
                cbActividad.getItems().clear();
                cbActividad.setDisable(true);
            }
        });

        // Listener de selección en la tabla para editar
        tablaHabitos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                habitoSeleccionado = newSelection;
                cargarHabitoEnFormulario(newSelection);
            }
        });

        // Configuración de celdas de fecha (deshabilitar futuro)
        dpUltimaFecha.setDayCellFactory(param -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                getStyleClass().remove("fecha-futura");
                setDisable(false);
                if (date.isAfter(LocalDate.now())) {
                    setDisable(true);
                    getStyleClass().add("fecha-futura");
                }
            }
        });
    }

    /**
     * Carga los datos iniciales en los combos y la tabla.
     */
    private void cargarDatosIniciales() {
        cargarCategorias();
        cbTipo.setItems(FXCollections.observableArrayList("diaria", "semanal", "mensual", "anual"));
        cbTipo.setValue("semanal");
        dpUltimaFecha.setValue(LocalDate.now());
        refrescarTabla();
    }

    private void cargarCategorias() {
        cbCategoria.setItems(FXCollections.observableArrayList(habitoService.obtenerTodasCategorias()));
    }

    private void cargarActividades(Categoria categoria) {
        cbActividad.setItems(FXCollections.observableArrayList(habitoService.obtenerActividadesPorCategoria(categoria)));
    }

    /**
     * Recarga la tabla con los hábitos del usuario actual.
     */
    private void refrescarTabla() {
        Usuario usuario = Sesion.getInstancia().getUsuarioLogueado();
        List<Habito> habitos = habitoService.obtenerHabitosDelUsuario(usuario);
        tablaHabitos.setItems(FXCollections.observableArrayList(habitos));
    }

    /**
     * Carga los datos de un hábito seleccionado en el formulario para su edición.
     *
     * @param habito El hábito seleccionado.
     */
    private void cargarHabitoEnFormulario(Habito habito) {
        cbCategoria.setValue(habito.getActividad().getCategoria());
        cbActividad.setValue(habito.getActividad());
        txtFrecuencia.setText(String.valueOf(habito.getFrecuencia()));
        cbTipo.setValue(habito.getTipo());
        dpUltimaFecha.setValue(habito.getUltimaFecha());

        cbCategoria.setDisable(true);
        cbActividad.setDisable(true);

        btnGuardar.setText("ACTUALIZAR DATOS");
        btnEliminar.setDisable(false);
        mostrarMensaje("Editando hábito: " + habito.getActividad().getNombre(), "mensaje-info");
    }

    /**
     * Acción ejecutada al hacer clic en "Guardar".
     * Crea o actualiza un hábito tras validar los datos.
     */
    @FXML
    public void onGuardarClick() {
        Usuario usuario = Sesion.getInstancia().getUsuarioLogueado();
        Actividad actividad = cbActividad.getValue();
        String frecTexto = txtFrecuencia.getText();
        String tipo = cbTipo.getValue();
        LocalDate fecha = dpUltimaFecha.getValue();

        if (actividad == null || frecTexto.isEmpty() || tipo == null || fecha == null) {
            mostrarMensaje("Por favor, completa todos los campos.", "mensaje-error");
            return;
        }

        if (fecha.isAfter(LocalDate.now())) {
            mostrarMensaje("No puedes registrar hábitos en el futuro.", "mensaje-error");
            return;
        }

        try {
            int frecuencia = Integer.parseInt(frecTexto);
            habitoService.guardarOActualizarHabito(usuario, actividad, frecuencia, tipo, fecha);
            mostrarMensaje("Hábito guardado correctamente.", "mensaje-exito");
            tablaHabitos.getSelectionModel().clearSelection();
            refrescarTabla();
            limpiarFormulario();

        } catch (NumberFormatException e) {
            mostrarMensaje("La frecuencia debe ser un número entero.", "mensaje-error");
        } catch (Exception e) {
            mostrarMensaje("Error al guardar en base de datos.", "mensaje-error");
            e.printStackTrace();
        }
    }

    /**
     * Acción ejecutada al hacer clic en "Eliminar".
     * Elimina el hábito seleccionado.
     */
    @FXML
    public void onEliminarClick() {
        if (habitoSeleccionado != null) {
            habitoService.eliminarHabito(habitoSeleccionado);
            mostrarMensaje("Hábito eliminado correctamente.", "mensaje-exito");
            refrescarTabla();
            onLimpiarClick();
        }
    }

    /**
     * Acción ejecutada al hacer clic en "Cancelar" o "Limpiar".
     * Resetea el formulario y el estado de selección.
     */
    @FXML
    public void onLimpiarClick() {
        limpiarFormulario();
        tablaHabitos.getSelectionModel().clearSelection();
        habitoSeleccionado = null;
        btnGuardar.setText("GUARDAR CONFIGURACIÓN");
        btnEliminar.setDisable(true);
        cbCategoria.setDisable(false);
        lblMensaje.setText("");
    }

    private void limpiarFormulario() {
        resetearCombo(cbCategoria);
        resetearCombo(cbActividad);
        cbActividad.setDisable(true);
        txtFrecuencia.clear();
        cbTipo.setValue("semanal");
        dpUltimaFecha.setValue(LocalDate.now());
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
     * @param texto     El mensaje a mostrar.
     * @param tipoClase La clase CSS para el estilo del mensaje (ej: "mensaje-error").
     */
    private void mostrarMensaje(String texto, String tipoClase) {
        lblMensaje.setText(texto);
        lblMensaje.getStyleClass().removeAll("mensaje-error", "mensaje-exito", "mensaje-info");
        lblMensaje.getStyleClass().add("mensaje-base");
        lblMensaje.getStyleClass().add(tipoClase);
    }
}
