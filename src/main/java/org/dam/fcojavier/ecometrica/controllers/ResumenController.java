package org.dam.fcojavier.ecometrica.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.dam.fcojavier.ecometrica.entities.Usuario;
import org.dam.fcojavier.ecometrica.services.HuellaService;
import org.dam.fcojavier.ecometrica.utils.Sesion;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

public class ResumenController {
    private final HuellaService huellaService = new HuellaService();

    // Filtros
    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFin;

    // KPIs
    @FXML private Label lblHuellaPeriodo;
    @FXML private Label lblHuellaHistorica;
    @FXML private Label lblMediaComunidad;

    // Gráficos
    @FXML private PieChart pieChart;
    @FXML private BarChart<String, Number> barChart;

    // Inferior
    @FXML private VBox boxTop3;
    @FXML private Label lblRecomendacion;

    @FXML
    public void initialize() {
        // Por defecto: Mes actual
        LocalDate hoy = LocalDate.now();
        dpInicio.setValue(hoy.with(TemporalAdjusters.firstDayOfMonth()));
        dpFin.setValue(hoy.with(TemporalAdjusters.lastDayOfMonth()));

        cargarDatos();
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

        // 1. CARGAR TARJETAS KPI
        double totalPeriodo = huellaService.calcularImpactoRango(usuario, inicio, fin);
        double totalHistorico = huellaService.calcularImpactoTotal(usuario);

        lblHuellaPeriodo.setText(String.format("%.2f kg CO₂", totalPeriodo));
        lblHuellaHistorica.setText(String.format("%.2f kg CO₂", totalHistorico));

        // Dato dummy para comunidad (hasta que tengas muchos usuarios reales) o cálculo real si lo tienes
        // lblMediaComunidad.setText("450.00 kg"); // Ejemplo estático
        // O si implementaste el cálculo en servicio:
        // double mediaComunidad = huellaService.obtenerMediaGlobal(); // (Si lo hiciste)

        // 2. CARGAR GRÁFICO TARTA (Distribución Mis Emisiones)
        List<Object[]> datosTarta = huellaService.obtenerEstadisticasPorCategoria(usuario);
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        for (Object[] fila : datosTarta) {
            String categoria = (String) fila[0];
            Double valor = (Double) fila[1];
            pieData.add(new PieChart.Data(categoria, valor));
        }
        pieChart.setData(pieData);

        // 3. CARGAR GRÁFICO BARRAS (Comparativa)
        cargarGraficoComparativo(usuario);

        // 4. CARGAR TOP 3
        cargarTop3(usuario);
    }

    private void cargarGraficoComparativo(Usuario usuario) {
        barChart.getData().clear();

        // Serie 1: Usuario
        XYChart.Series<String, Number> serieUsuario = new XYChart.Series<>();
        serieUsuario.setName("Tú");
        List<Object[]> mediaUsuario = huellaService.obtenerMediaImpactoUsuario(usuario);
        for (Object[] fila : mediaUsuario) {
            serieUsuario.getData().add(new XYChart.Data<>((String) fila[0], (Number) fila[1]));
        }

        // Serie 2: Comunidad
        XYChart.Series<String, Number> serieComunidad = new XYChart.Series<>();
        serieComunidad.setName("Comunidad");
        List<Object[]> mediaComunidad = huellaService.obtenerComparativaComunidad();
        for (Object[] fila : mediaComunidad) {
            serieComunidad.getData().add(new XYChart.Data<>((String) fila[0], (Number) fila[1]));
        }

        barChart.getData().addAll(serieUsuario, serieComunidad);
    }

    private void cargarTop3(Usuario usuario) {
        boxTop3.getChildren().clear();
        List<Object[]> top3 = huellaService.obtenerTop3Actividades(usuario);

        if (top3.isEmpty()) {
            boxTop3.getChildren().add(new Label("No hay registros suficientes."));
            return;
        }

        int rank = 1;
        String[] medallas = {"🥇", "🥈", "🥉"};

        for (Object[] fila : top3) {
            String actividad = (String) fila[0];
            Double impacto = (Double) fila[1];
            String medalla = (rank <= 3) ? medallas[rank-1] : "#" + rank;

            Label lbl = new Label(String.format("%s %s - %.2f kg CO₂", medalla, actividad, impacto));
            lbl.setStyle("-fx-font-size: 14px; -fx-padding: 2;");
            boxTop3.getChildren().add(lbl);
            rank++;
        }
    }
}
