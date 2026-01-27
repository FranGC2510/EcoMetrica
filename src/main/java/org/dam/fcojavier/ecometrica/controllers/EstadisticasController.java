package org.dam.fcojavier.ecometrica.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.dam.fcojavier.ecometrica.entities.Categoria;
import org.dam.fcojavier.ecometrica.entities.Usuario;
import org.dam.fcojavier.ecometrica.services.HuellaService;
import org.dam.fcojavier.ecometrica.services.ReporteService;
import org.dam.fcojavier.ecometrica.utils.Sesion;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para la vista de estadísticas de impacto ambiental.
 * Gestiona la visualización de gráficos, tablas de desglose y la exportación de reportes PDF.
 */
public class EstadisticasController {

    private final HuellaService huellaService = new HuellaService();
    private final ReporteService reporteService = new ReporteService();

    @FXML private HBox panelDatos;
    @FXML private VBox panelSinDatos;

    @FXML private ComboBox<String> cbFiltroRapido;
    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFin;

    // Gráficos
    @FXML private PieChart pieChart;
    @FXML private BarChart<String, Number> barChart;
    @FXML private Label lblTituloGrafico;

    // Tabla de Desglose
    @FXML private TableView<FilaEstadistica> tablaDesglose;
    @FXML private TableColumn<FilaEstadistica, String> colCatNombre;
    @FXML private TableColumn<FilaEstadistica, String> colCatTotal;
    @FXML private TableColumn<FilaEstadistica, String> colCatPorcentaje;

    private boolean actualizandoFechas = false;

    /**
     * Inicializa el controlador.
     * Configura las columnas de la tabla, los listeners de los controles y carga el filtro por defecto.
     */
    @FXML
    public void initialize() {
        configurarTabla();
        configurarFiltros();
        cargarFiltroPorDefecto();
    }

    /**
     * Configura las columnas de la tabla de desglose.
     */
    private void configurarTabla() {
        colCatNombre.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colCatTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colCatPorcentaje.setCellValueFactory(new PropertyValueFactory<>("porcentaje"));
    }

    /**
     * Configura el ComboBox de filtro rápido y los DatePickers.
     */
    private void configurarFiltros() {
        cbFiltroRapido.setItems(FXCollections.observableArrayList(
                "Hoy",
                "Esta Semana",
                "Este Mes",
                "Este Año",
                "Personalizado"
        ));

        cbFiltroRapido.setOnAction(e -> {
            if (!actualizandoFechas) {
                aplicarFiltroRapido(cbFiltroRapido.getValue());
            }
        });

        // Si el usuario toca las fechas manualmente, cambiamos el combo a "Personalizado"
        dpInicio.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!actualizandoFechas) cbFiltroRapido.setValue("Personalizado");
        });
        dpFin.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!actualizandoFechas) cbFiltroRapido.setValue("Personalizado");
        });
    }

    /**
     * Carga el filtro "Este Mes" por defecto al iniciar la vista.
     */
    private void cargarFiltroPorDefecto() {
        Platform.runLater(() -> cbFiltroRapido.setValue("Este Mes"));
    }

    /**
     * Aplica un rango de fechas predefinido según la opción seleccionada.
     *
     * @param opcion La opción de filtro seleccionada (ej: "Esta Semana").
     */
    private void aplicarFiltroRapido(String opcion) {
        if (opcion == null || opcion.equals("Personalizado")) return;

        LocalDate hoy = LocalDate.now();
        LocalDate inicio = null;
        LocalDate fin = hoy;

        switch (opcion) {
            case "Hoy":
                inicio = hoy;
                break;
            case "Esta Semana":
                inicio = hoy.with(DayOfWeek.MONDAY);
                fin = hoy.with(DayOfWeek.SUNDAY);
                break;
            case "Este Mes":
                inicio = hoy.with(TemporalAdjusters.firstDayOfMonth());
                fin = hoy.with(TemporalAdjusters.lastDayOfMonth());
                break;
            case "Este Año":
                inicio = hoy.with(TemporalAdjusters.firstDayOfYear());
                fin = hoy.with(TemporalAdjusters.lastDayOfYear());
                break;
        }

        if (inicio != null) {
            actualizandoFechas = true;
            dpInicio.setValue(inicio);
            dpFin.setValue(fin);
            actualizandoFechas = false;
            cargarDatos();
        }
    }

    /**
     * Acción ejecutada al hacer clic en el botón "Filtrar".
     * Recarga los datos según el rango de fechas actual.
     */
    @FXML
    public void onFiltrarClick() {
        cargarDatos();
    }

    /**
     * Carga y procesa los datos estadísticos para el usuario y rango de fechas seleccionados.
     * Actualiza los gráficos y la tabla, o muestra el panel "Sin Datos" si no hay registros.
     */
    private void cargarDatos() {
        Usuario usuario = Sesion.getInstancia().getUsuarioLogueado();
        if (usuario == null) return;

        LocalDate inicio = dpInicio.getValue();
        LocalDate fin = dpFin.getValue();

        if (inicio == null || fin == null) return;

        actualizarTituloGrafico(inicio, fin);

        List<Object[]> datosTarta = huellaService.obtenerEstadisticasPorCategoriaYRango(usuario, inicio, fin);

        if (datosTarta.isEmpty()) {
            mostrarPanelSinDatos();
            return;
        }

        mostrarPanelConDatos();
        actualizarGraficosYTabla(datosTarta, usuario, inicio, fin);
    }

    /**
     * Actualiza el título del gráfico principal según el filtro aplicado.
     */
    private void actualizarTituloGrafico(LocalDate inicio, LocalDate fin) {
        String filtroSeleccionado = cbFiltroRapido.getValue();
        if (filtroSeleccionado == null || filtroSeleccionado.equals("Personalizado")) {
            lblTituloGrafico.setText("Impacto (" +
                    inicio.format(DateTimeFormatter.ofPattern("dd/MM/yy")) + " - " +
                    fin.format(DateTimeFormatter.ofPattern("dd/MM/yy")) + ")");
        } else {
            lblTituloGrafico.setText("Impacto: " + filtroSeleccionado);
        }
    }

    private void mostrarPanelSinDatos() {
        panelDatos.setVisible(false);
        panelSinDatos.setVisible(true);
    }

    private void mostrarPanelConDatos() {
        panelDatos.setVisible(true);
        panelSinDatos.setVisible(false);
    }

    /**
     * Actualiza el PieChart, la tabla de desglose y el BarChart con los datos obtenidos.
     */
    private void actualizarGraficosYTabla(List<Object[]> datosTarta, Usuario usuario, LocalDate inicio, LocalDate fin) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        ObservableList<FilaEstadistica> tablaData = FXCollections.observableArrayList();

        double totalGlobal = datosTarta.stream().mapToDouble(fila -> (Double) fila[1]).sum();

        for (Object[] fila : datosTarta) {
            String cat = (String) fila[0];
            Double val = (Double) fila[1];
            pieData.add(new PieChart.Data(cat, val));

            double pct = (totalGlobal > 0) ? (val / totalGlobal) * 100 : 0;
            tablaData.add(new FilaEstadistica(cat, String.format("%.2f kg", val), String.format("%.1f%%", pct)));
        }

        pieChart.setData(pieData);
        tablaDesglose.setItems(tablaData);

        cargarBarChartCategorias(usuario, inicio, fin);
    }

    /**
     * Acción ejecutada al hacer clic en "Exportar PDF".
     * Genera un reporte PDF con los datos actuales.
     */
    @FXML
    public void onExportarPdfClick() {
        if (panelSinDatos.isVisible()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin datos", "No hay datos visibles para generar el reporte.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte de Impacto");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));

        String fechaStr = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-yyyy"));
        fileChooser.setInitialFileName("Reporte_Impacto_" + fechaStr + ".pdf");

        Stage stage = (Stage) panelDatos.getScene().getWindow();
        File archivoDestino = fileChooser.showSaveDialog(stage);

        if (archivoDestino != null) {
            generarReporte(archivoDestino);
        }
    }

    /**
     * Llama al servicio de reportes para generar el PDF.
     */
    private void generarReporte(File archivoDestino) {
        try {
            Usuario usuario = Sesion.getInstancia().getUsuarioLogueado();
            LocalDate inicio = dpInicio.getValue();
            LocalDate fin = dpFin.getValue();

            List<Object[]> datos = huellaService.obtenerEstadisticasPorCategoriaYRango(usuario, inicio, fin);
            reporteService.generarReporteEstadisticas(archivoDestino, usuario, inicio, fin, datos);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "El reporte se ha guardado correctamente en:\n" + archivoDestino.getName());

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo guardar el reporte.\nComprueba que el archivo no esté abierto.");
        }
    }

    /**
     * Carga los datos en el gráfico de barras, asegurando que todas las categorías aparezcan (incluso con valor 0).
     */
    private void cargarBarChartCategorias(Usuario usuario, LocalDate inicio, LocalDate fin) {
        barChart.getData().clear();
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Impacto");

        List<Object[]> datosReales = huellaService.obtenerEstadisticasPorCategoriaYRango(usuario, inicio, fin);
        Map<String, Double> mapaValores = new HashMap<>();
        for (Object[] fila : datosReales) {
            mapaValores.put((String) fila[0], (Double) fila[1]);
        }

        List<Categoria> todasCategorias = huellaService.obtenerTodasCategorias();

        for (Categoria cat : todasCategorias) {
            String nombreCat = cat.getNombre();
            Double valor = mapaValores.getOrDefault(nombreCat, 0.0);
            serie.getData().add(new XYChart.Data<>(nombreCat, valor));
        }

        barChart.getData().add(serie);
    }

    /**
     * Muestra una alerta modal al usuario.
     */
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        if (panelDatos.getScene() != null) {
            Stage stage = (Stage) panelDatos.getScene().getWindow();
            alerta.initOwner(stage);
        }

        alerta.showAndWait();
    }

    /**
     * Clase interna para representar una fila en la tabla de desglose estadístico.
     */
    public static class FilaEstadistica {
        private final SimpleStringProperty categoria;
        private final SimpleStringProperty total;
        private final SimpleStringProperty porcentaje;

        public FilaEstadistica(String categoria, String total, String porcentaje) {
            this.categoria = new SimpleStringProperty(categoria);
            this.total = new SimpleStringProperty(total);
            this.porcentaje = new SimpleStringProperty(porcentaje);
        }

        public String getCategoria() { return categoria.get(); }
        public String getTotal() { return total.get(); }
        public String getPorcentaje() { return porcentaje.get(); }
    }
}
