package org.dam.fcojavier.ecometrica.controllers;

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

    @FXML
    public void initialize() {
        // 1. Configurar columnas de la tabla
        // Usamos 'SimpleStringProperty' para acceder a propiedades anidadas (Actividad -> Nombre)
        colActividad.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getActividad().getNombre()));

        colCategoria.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getActividad().getCategoria())
        );
        colCategoria.setCellFactory(column -> new TableCell<Habito, Categoria>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    // Creamos el "Chip" (un Label estilizado)
                    Label lblBadge = new Label(item.getNombre());
                    lblBadge.getStyleClass().add("badge-base");

                    // Asignamos clase según el nombre de la categoría
                    String nombreCat = item.getNombre().toLowerCase();
                    if (nombreCat.contains("transporte")) {
                        lblBadge.getStyleClass().add("badge-transporte");
                    } else if (nombreCat.contains("energía") || nombreCat.contains("energia")) {
                        lblBadge.getStyleClass().add("badge-energia");
                    } else if (nombreCat.contains("alimentación") || nombreCat.contains("comida")) {
                        lblBadge.getStyleClass().add("badge-alimentacion");
                    } else if (nombreCat.contains("agua")) {
                        lblBadge.getStyleClass().add("badge-agua");
                    } else if (nombreCat.contains("residuos") || nombreCat.contains("basura")) {
                        lblBadge.getStyleClass().add("badge-residuos");
                    } else {
                        lblBadge.getStyleClass().add("badge-default");
                    }

                    // Centramos el chip en la celda
                    HBox container = new HBox(lblBadge);
                    container.setAlignment(Pos.CENTER);

                    setGraphic(container);
                    setText(null); // Borramos el texto plano
                }
            }
        });

        colFrecuencia.setCellValueFactory(new PropertyValueFactory<>("frecuencia"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("ultimaFecha"));

        // 2. Cargar datos iniciales en los combos
        cargarCategorias();
        cbTipo.setItems(FXCollections.observableArrayList("diaria", "semanal", "mensual", "anual"));
        cbTipo.setValue("semanal");

        dpUltimaFecha.setValue(LocalDate.now());
        dpUltimaFecha.setDayCellFactory(param -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                // Limpieza inicial: Quitar la clase por si la celda se reutiliza
                getStyleClass().remove("fecha-futura");
                setDisable(false);

                if (date.isAfter(LocalDate.now())) {
                    setDisable(true);
                    // AÑADIR CLASE CSS en lugar de setStyle
                    getStyleClass().add("fecha-futura");
                }
            }
        });
        // 3. Listener para filtrado de actividades (Igual que en Huella)
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

        // 4. Listener DE LA TABLA: Para editar al hacer clic
        tablaHabitos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                habitoSeleccionado = newSelection; // <--- IMPORTANTE: Guardamos la referencia
                cargarHabitoEnFormulario(newSelection);
            }
        });

        // 5. Cargar la tabla con los datos del usuario actual
        refrescarTabla();
    }

    private void cargarCategorias() {
        cbCategoria.setItems(FXCollections.observableArrayList(habitoService.obtenerTodasCategorias()));
    }

    private void cargarActividades(Categoria categoria) {
        cbActividad.setItems(FXCollections.observableArrayList(habitoService.obtenerActividadesPorCategoria(categoria)));
    }

    private void refrescarTabla() {
        Usuario usuario = Sesion.getInstancia().getUsuarioLogueado();
        List<Habito> habitos = habitoService.obtenerHabitosDelUsuario(usuario);
        tablaHabitos.setItems(FXCollections.observableArrayList(habitos));
    }

    private void cargarHabitoEnFormulario(Habito habito) {
        // Rellenamos el formulario con los datos de la fila seleccionada
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

    @FXML
    public void onGuardarClick() {
        Usuario usuario = Sesion.getInstancia().getUsuarioLogueado();
        Actividad actividad = cbActividad.getValue();
        String frecTexto = txtFrecuencia.getText();
        String tipo = cbTipo.getValue();
        LocalDate fecha = dpUltimaFecha.getValue();

        // Validación de campos vacíos
        if (actividad == null || frecTexto.isEmpty() || tipo == null || fecha == null) {
            mostrarMensaje("Por favor, completa todos los campos.", "mensaje-error");
            return;
        }

        // Validación de fecha futura
        if (fecha.isAfter(LocalDate.now())) {
            mostrarMensaje("No puedes registrar hábitos en el futuro.", "mensaje-error");
            return;
        }

        try {
            int frecuencia = Integer.parseInt(frecTexto);

            // Llamamos al servicio (Crear o Actualizar)
            habitoService.guardarOActualizarHabito(usuario, actividad, frecuencia, tipo, fecha);

            mostrarMensaje("Hábito guardado correctamente.", "mensaje-exito");

            // Limpiamos selección y recargamos tabla
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

    @FXML
    public void onEliminarClick() {
        if (habitoSeleccionado != null) {
            // Pregunta de seguridad opcional (recomendado en UX real, pero aquí directo por sencillez)
            habitoService.eliminarHabito(habitoSeleccionado);

            mostrarMensaje("Hábito eliminado correctamente.", "mensaje-exito");

            refrescarTabla();
            onLimpiarClick(); // Limpiamos selección
        }
    }

    @FXML
    public void onLimpiarClick() {
        limpiarFormulario();
        tablaHabitos.getSelectionModel().clearSelection();

        // Resetear estado
        habitoSeleccionado = null;
        btnGuardar.setText("GUARDAR CONFIGURACIÓN");
        btnEliminar.setDisable(true);

        // Reactivar combos por si estaban bloqueados por edición
        cbCategoria.setDisable(false);
        // cbActividad se gestiona solo según la categoría
        lblMensaje.setText("");
    }

    private void limpiarFormulario() {
        resetearCombo(cbCategoria);

        resetearCombo(cbActividad);
        cbActividad.setDisable(true);

        txtFrecuencia.clear();

        // El tipo NO lo reseteamos con el método especial porque tiene un valor por defecto
        cbTipo.setValue("semanal");

        dpUltimaFecha.setValue(LocalDate.now());
    }

    /**
     * Método auxiliar para resetear un ComboBox correctamente.
     * Restaura el PromptText cuando está vacío y muestra el nombre cuando seleccionas algo.
     */
    private <T> void resetearCombo(ComboBox<T> combo) {
        combo.setValue(null);
        combo.setButtonCell(new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); // Deja ver el PromptText
                } else {
                    setText(item.toString()); // Muestra el nombre (Categoría/Actividad)
                }
            }
        });
    }

    /**
     * Muestra un mensaje en la etiqueta aplicando estilos CSS y eliminando los hardcoded.
     * @param texto El mensaje a mostrar.
     * @param tipoClase El nombre de la clase CSS (ej: "mensaje-error", "mensaje-exito").
     */
    private void mostrarMensaje(String texto, String tipoClase) {
        lblMensaje.setText(texto);
        // Limpiamos estilos anteriores para no mezclar (ej: error + exito)
        lblMensaje.getStyleClass().removeAll("mensaje-error", "mensaje-exito", "mensaje-info");
        // Añadimos la clase base (si quieres negrita) y la específica
        lblMensaje.getStyleClass().add("mensaje-base");
        lblMensaje.getStyleClass().add(tipoClase);
    }
}
