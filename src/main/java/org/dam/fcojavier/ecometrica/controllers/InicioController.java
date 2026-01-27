package org.dam.fcojavier.ecometrica.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.dam.fcojavier.ecometrica.dao.RecomendacionDAO;
import org.dam.fcojavier.ecometrica.entities.Categoria;
import org.dam.fcojavier.ecometrica.entities.Recomendacion;
import org.dam.fcojavier.ecometrica.entities.Usuario;
import org.dam.fcojavier.ecometrica.services.HuellaService;
import org.dam.fcojavier.ecometrica.utils.Sesion;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Random;

/**
 * Controlador para la vista de inicio (Dashboard).
 * Muestra un resumen de los KPIs principales, el ranking de impacto y consejos personalizados.
 */
public class InicioController {

    private final HuellaService huellaService = new HuellaService();
    private final RecomendacionDAO recomendacionDAO = new RecomendacionDAO();

    @FXML private Label lblSaludo;
    @FXML private Label lblHuellaMes;
    @FXML private Label lblHuellaTotal;
    @FXML private HBox boxPodium;
    @FXML private Label lblConsejo;

    @FXML private ProgressBar barraProgreso;
    @FXML private Label lblPorcentajeComparacion;
    @FXML private Label lblValoresAbsolutos;

    /**
     * Inicializa el controlador.
     * Carga los datos del usuario logueado y actualiza los componentes de la vista.
     */
    @FXML
    public void initialize() {
        Usuario usuario = Sesion.getInstancia().getUsuarioLogueado();
        if (usuario == null) return;

        lblSaludo.setText("Hola, " + usuario.getNombre());

        cargarKPIs(usuario);
        cargarTop3(usuario);
        cargarComparativa(usuario);
    }

    /**
     * Carga los Indicadores Clave de Desempeño (KPIs): Huella del mes actual y Huella histórica total.
     */
    private void cargarKPIs(Usuario usuario) {
        // KPI 1: Mes Actual
        LocalDate inicioMes = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate finMes = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        double totalMes = huellaService.calcularImpactoRango(usuario, inicioMes, finMes);
        lblHuellaMes.setText(String.format("%.2f kg", totalMes));

        // KPI 2: Total Histórico
        double totalHistorico = huellaService.calcularImpactoTotal(usuario);
        lblHuellaTotal.setText(String.format("%.2f kg", totalHistorico));
    }

    /**
     * Carga el ranking de las 3 actividades con mayor impacto y muestra un consejo basado en la nº1.
     */
    private void cargarTop3(Usuario usuario) {
        boxPodium.getChildren().clear();
        List<Object[]> top3 = huellaService.obtenerTop3Actividades(usuario);

        if (top3.isEmpty()) {
            boxPodium.getChildren().add(new Label("Registra actividades para ver el podio."));
            cargarConsejoAleatorio();
            return;
        }

        Object[] rank1 = (top3.size() > 0) ? top3.get(0) : null;
        Object[] rank2 = (top3.size() > 1) ? top3.get(1) : null;
        Object[] rank3 = (top3.size() > 2) ? top3.get(2) : null;

        if (rank2 != null) boxPodium.getChildren().add(crearEscalonPodio(rank2, 2));
        if (rank1 != null) boxPodium.getChildren().add(crearEscalonPodio(rank1, 1));
        if (rank3 != null) boxPodium.getChildren().add(crearEscalonPodio(rank3, 3));

        if (rank1 != null) {
            Categoria catTop1 = (Categoria) rank1[2];
            cargarConsejoPorCategoria(catTop1);
        }
    }

    /**
     * Compara la huella del usuario con la media de la comunidad y actualiza la barra de progreso.
     */
    private void cargarComparativa(Usuario usuario) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicio = hoy.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate fin = hoy.with(TemporalAdjusters.lastDayOfMonth());

        double miHuella = huellaService.calcularImpactoRango(usuario, inicio, fin);
        double mediaComunidad = huellaService.obtenerMediaComunidadMesActual();

        if (mediaComunidad == 0) mediaComunidad = 1.0;

        double porcentaje = miHuella / mediaComunidad;

        barraProgreso.setProgress(Math.min(porcentaje, 1.0));
        lblValoresAbsolutos.setText(String.format("Tú: %.1f kg / Media: %.1f kg", miHuella, mediaComunidad));

        actualizarEstiloBarraProgreso(porcentaje);
    }

    /**
     * Actualiza el color y el texto de la barra de progreso según el porcentaje de impacto.
     */
    private void actualizarEstiloBarraProgreso(double porcentaje) {
        // Limpiamos estilos anteriores
        barraProgreso.getStyleClass().removeAll("progress-bar-success", "progress-bar-warning", "progress-bar-danger", "progress-bar-critical");

        if (porcentaje < 0.5) {
            barraProgreso.getStyleClass().add("progress-bar-success");
            lblPorcentajeComparacion.setText("¡Excelente! Solo llevas el " + String.format("%.0f%%", porcentaje * 100));
        } else if (porcentaje < 0.9) {
            barraProgreso.getStyleClass().add("progress-bar-warning");
            lblPorcentajeComparacion.setText("Atención, estás al " + String.format("%.0f%%", porcentaje * 100));
        } else if (porcentaje <= 1.0) {
            barraProgreso.getStyleClass().add("progress-bar-danger");
            lblPorcentajeComparacion.setText("Estás justamente al (" + String.format("%.0f%%", porcentaje * 100) + ")");
        } else {
            barraProgreso.getStyleClass().add("progress-bar-critical");
            lblPorcentajeComparacion.setText("Has superado la media (" + String.format("%.0f%%", porcentaje * 100) + ")");
        }
    }

    /**
     * Crea un componente visual (VBox) que representa un escalón del podio.
     *
     * @param datos   Array con [NombreActividad, Impacto, Categoria].
     * @param ranking Posición en el ranking (1, 2 o 3).
     * @return El componente VBox configurado.
     */
    private VBox crearEscalonPodio(Object[] datos, int ranking) {
        String actividad = (String) datos[0];
        Double impacto = (Double) datos[1];
        Categoria categoria = (Categoria) datos[2];

        PodiumConfig config = obtenerConfiguracionPodio(ranking);

        VBox escalon = new VBox(5);
        escalon.setAlignment(Pos.CENTER);
        escalon.setPadding(new javafx.geometry.Insets(10, 5, 10, 5));
        escalon.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(escalon, Priority.ALWAYS);
        escalon.setPrefHeight(config.altura);
        escalon.setMinHeight(config.altura);
        escalon.setMaxHeight(config.altura);

        // Aplicamos clases CSS base y específica
        escalon.getStyleClass().add("podium-step");
        escalon.getStyleClass().add(config.styleClass);

        // Configurar interacción
        escalon.setOnMouseClicked(event -> cargarConsejoPorCategoria(categoria));

        FontIcon iconoMedalla = new FontIcon(config.iconLiteral);
        iconoMedalla.getStyleClass().add(config.iconStyleClass);

        Label lblNombre = new Label(actividad);
        lblNombre.setWrapText(true);
        lblNombre.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        lblNombre.getStyleClass().add("podium-label-name");

        Label lblValor = new Label(String.format("%.0f kg", impacto));
        lblValor.getStyleClass().add("podium-label-value");

        escalon.getChildren().addAll(iconoMedalla, lblNombre, lblValor);

        return escalon;
    }

    /**
     * Obtiene la configuración visual (clases CSS, iconos, altura) para un puesto del ranking.
     */
    private PodiumConfig obtenerConfiguracionPodio(int ranking) {
        switch (ranking) {
            case 1: return new PodiumConfig("podium-step-gold", 200, "mdal-emoji_events", "podium-icon-gold");
            case 2: return new PodiumConfig("podium-step-silver", 160, "mdmz-military_tech", "podium-icon-silver");
            default: return new PodiumConfig("podium-step-bronze", 130, "mdmz-military_tech", "podium-icon-bronze");
        }
    }

    /**
     * Carga un consejo aleatorio de la base de datos.
     */
    private void cargarConsejoAleatorio() {
        List<Recomendacion> todas = recomendacionDAO.findAll();
        if (!todas.isEmpty()) {
            int indice = new Random().nextInt(todas.size());
            lblConsejo.setText("\"" + todas.get(indice).getDescripcion() + "\"");
        } else {
            lblConsejo.setText("Registra actividades para ver consejos.");
        }
    }

    /**
     * Carga un consejo aleatorio filtrado por una categoría específica.
     */
    private void cargarConsejoPorCategoria(Categoria categoria) {
        if (categoria == null) return;

        List<Recomendacion> recomendaciones = recomendacionDAO.findByCategoria(categoria);

        if (recomendaciones != null && !recomendaciones.isEmpty()) {
            int indice = new Random().nextInt(recomendaciones.size());
            lblConsejo.setText("\"" + recomendaciones.get(indice).getDescripcion() + "\"");
        } else {
            lblConsejo.setText("No hay consejos específicos disponibles para " + categoria.getNombre());
        }
    }

    // Clase auxiliar interna para configuración del podio
    private static class PodiumConfig {
        String styleClass;
        int altura;
        String iconLiteral;
        String iconStyleClass;

        public PodiumConfig(String styleClass, int altura, String iconLiteral, String iconStyleClass) {
            this.styleClass = styleClass;
            this.altura = altura;
            this.iconLiteral = iconLiteral;
            this.iconStyleClass = iconStyleClass;
        }
    }
}
