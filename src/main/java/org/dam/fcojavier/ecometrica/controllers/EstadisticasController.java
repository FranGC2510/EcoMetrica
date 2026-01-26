package org.dam.fcojavier.ecometrica.controllers;

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
import org.dam.fcojavier.ecometrica.entities.Categoria;
import org.dam.fcojavier.ecometrica.entities.Usuario;
import org.dam.fcojavier.ecometrica.services.HuellaService;
import org.dam.fcojavier.ecometrica.services.ReporteService;
import org.dam.fcojavier.ecometrica.utils.Sesion;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // NUEVO: Tabla de Desglose
    @FXML private TableView<FilaEstadistica> tablaDesglose;
    @FXML private TableColumn<FilaEstadistica, String> colCatNombre;
    @FXML private TableColumn<FilaEstadistica, String> colCatTotal;
    @FXML private TableColumn<FilaEstadistica, String> colCatPorcentaje;

    private boolean actualizandoFechas = false;

    @FXML
    public void initialize() {
        // 1. Configurar columnas de la tabla
        colCatNombre.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colCatTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colCatPorcentaje.setCellValueFactory(new PropertyValueFactory<>("porcentaje"));

        // 2. Llenar el Combo con las opciones
        cbFiltroRapido.setItems(FXCollections.observableArrayList(
                "Hoy",
                "Esta Semana",
                "Este Mes",
                "Este Año",
                "Personalizado"
        ));

        // 3. Configurar el Listener del Combo
        cbFiltroRapido.setOnAction(e -> {
            // Si estamos actualizando las fechas programáticamente, no hacemos nada
            if (actualizandoFechas) return;
            aplicarFiltroRapido(cbFiltroRapido.getValue());
        });

        // 4. Configurar Listeners de los DatePickers
        // Si el usuario toca las fechas manualmente, cambiamos el combo a "Personalizado"
        dpInicio.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!actualizandoFechas) cbFiltroRapido.setValue("Personalizado");
        });
        dpFin.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!actualizandoFechas) cbFiltroRapido.setValue("Personalizado");
        });

        // 5. CARGAR FILTRO POR DEFECTO (LA SOLUCIÓN)
        // Usamos Platform.runLater para esperar a que la interfaz esté lista.
        // Esto asegura que los listeners estén activos y el evento se dispare correctamente.
        javafx.application.Platform.runLater(() -> {
            // Esto dispara el evento 'onAction', que llama a 'aplicarFiltroRapido',
            // que pone las fechas y llama a 'cargarDatos()'.
            cbFiltroRapido.setValue("Este Mes");
        });
    }

    private void aplicarFiltroRapido(String opcion) {
        if (opcion == null || opcion.equals("Personalizado")) return;

        LocalDate hoy = LocalDate.now();
        LocalDate inicio = null;
        LocalDate fin = hoy; // Por defecto hasta hoy

        switch (opcion) {
            case "Hoy":
                inicio = hoy;
                break;
            case "Esta Semana":
                // Asumimos Lunes como primer día
                inicio = hoy.with(java.time.DayOfWeek.MONDAY);
                fin = hoy.with(java.time.DayOfWeek.SUNDAY);
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
            actualizandoFechas = true; // Bloqueamos para que no salte a "Personalizado"
            dpInicio.setValue(inicio);
            dpFin.setValue(fin);
            actualizandoFechas = false;

            // Opcional: Cargar datos automáticamente al cambiar el filtro rápido
            cargarDatos();
        }
    }

    @FXML
    public void onFiltrarClick() {
        cargarDatos();
    }

    private void cargarDatos() {
        Usuario usuario = Sesion.getInstancia().getUsuarioLogueado();
        if (usuario == null) return;

        LocalDate inicio = dpInicio.getValue();
        LocalDate fin = dpFin.getValue();

        if (inicio == null || fin == null) return;

        // ACTUALIZAR TÍTULO
        String filtroSeleccionado = cbFiltroRapido.getValue();
        if (filtroSeleccionado == null || filtroSeleccionado.equals("Personalizado")) {
            lblTituloGrafico.setText("Impacto (" +
                    inicio.format(DateTimeFormatter.ofPattern("dd/MM/yy")) + " - " +
                    fin.format(DateTimeFormatter.ofPattern("dd/MM/yy")) + ")");
        } else {
            lblTituloGrafico.setText("Impacto: " + filtroSeleccionado);
        }

        // 1. OBTENER DATOS
        List<Object[]> datosTarta = huellaService.obtenerEstadisticasPorCategoriaYRango(usuario, inicio, fin);

        // --- LÓGICA DE EMPTY STATE (Aquí está la magia) ---
        if (datosTarta.isEmpty()) {
            panelDatos.setVisible(false);
            panelSinDatos.setVisible(true);
            return; // Cortamos aquí, no hace falta procesar gráficos
        } else {
            panelDatos.setVisible(true);
            panelSinDatos.setVisible(false);
        }
        // --------------------------------------------------

        // 2. PROCESAR DATOS (Solo si hay contenido)
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        ObservableList<FilaEstadistica> tablaData = FXCollections.observableArrayList();

        double totalGlobal = 0;
        for (Object[] fila : datosTarta) totalGlobal += (Double) fila[1];

        for (Object[] fila : datosTarta) {
            String cat = (String) fila[0];
            Double val = (Double) fila[1];
            pieData.add(new PieChart.Data(cat, val));

            double pct = (totalGlobal > 0) ? (val / totalGlobal) * 100 : 0;
            tablaData.add(new FilaEstadistica(cat, String.format("%.2f kg", val), String.format("%.1f%%", pct)));
        }

        pieChart.setData(pieData);
        tablaDesglose.setItems(tablaData);

        // Cargar Barras
        cargarBarChartCategorias(usuario, inicio, fin);
    }

    @FXML
    public void onExportarPdfClick() {
        // 1. Validación de seguridad (Panel vacío)
        if (panelSinDatos.isVisible()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin datos", "No hay datos visibles para generar el reporte.");
            return;
        }

        // 2. Configurar el Selector de Archivos
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte de Impacto");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));

        // Nombre sugerido: "Reporte_Impacto_01-2026.pdf"
        String fechaStr = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-yyyy"));
        fileChooser.setInitialFileName("Reporte_Impacto_" + fechaStr + ".pdf");

        // 3. Abrir ventana de guardar
        // Usamos 'panelDatos' para obtener la ventana, ya que sabemos que está visible
        javafx.stage.Stage stage = (javafx.stage.Stage) panelDatos.getScene().getWindow();
        File archivoDestino = fileChooser.showSaveDialog(stage);

        if (archivoDestino != null) {
            try {
                // A) Recopilar los datos necesarios
                Usuario usuario = Sesion.getInstancia().getUsuarioLogueado();
                LocalDate inicio = dpInicio.getValue();
                LocalDate fin = dpFin.getValue();

                // Pedimos los datos frescos al servicio
                List<Object[]> datos = huellaService.obtenerEstadisticasPorCategoriaYRango(usuario, inicio, fin);

                // B) LLAMAR AL SERVICIO DE REPORTE (Aquí ocurre la magia)
                reporteService.generarReporteEstadisticas(archivoDestino, usuario, inicio, fin, datos);

                // C) Mensaje de Éxito
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "El reporte se ha guardado correctamente en:\n" + archivoDestino.getName());

            } catch (Exception e) {
                e.printStackTrace();
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo guardar el reporte.\nComprueba que el archivo no esté abierto.");
            }
        }
    }

    // Método auxiliar para mantener limpio el código anterior
    private void cargarBarChartCategorias(Usuario usuario, LocalDate inicio, LocalDate fin) {
        barChart.getData().clear();
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Impacto");

        // 1. Obtener los datos reales filtrados (Lo que el usuario ha gastado)
        // Devuelve: [ ["Transporte", 120.0], ["Energía", 50.0] ]
        List<Object[]> datosReales = huellaService.obtenerEstadisticasPorCategoriaYRango(usuario, inicio, fin);

        // 2. Convertir a un Mapa para búsqueda rápida (Clave: NombreCategoria -> Valor: Impacto)
        Map<String, Double> mapaValores = new HashMap<>();
        for (Object[] fila : datosReales) {
            mapaValores.put((String) fila[0], (Double) fila[1]);
        }

        // 3. Obtener TODAS las categorías del sistema (Transporte, Energía, Agua...)
        // Esto asegura que el gráfico siempre tenga todas las columnas, aunque estén a 0.
        List<Categoria> todasCategorias = huellaService.obtenerTodasCategorias();

        // 4. Construir la serie
        for (Categoria cat : todasCategorias) {
            String nombreCat = cat.getNombre();
            // Si hay datos, usamos el valor. Si no, ponemos 0.0
            Double valor = mapaValores.getOrDefault(nombreCat, 0.0);

            serie.getData().add(new XYChart.Data<>(nombreCat, valor));
        }

        barChart.getData().add(serie);
    }

    // --- CLASE INTERNA PARA EL MODELO DE LA TABLA ---
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

    // Método auxiliar mejorado para evitar repetir el código del initOwner
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        // Corrección del pantallazo negro: Asignamos el dueño
        if (panelDatos.getScene() != null) {
            javafx.stage.Stage stage = (javafx.stage.Stage) panelDatos.getScene().getWindow();
            alerta.initOwner(stage);
        }

        alerta.showAndWait();
    }
}
