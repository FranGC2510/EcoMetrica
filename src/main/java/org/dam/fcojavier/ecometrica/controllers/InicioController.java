package org.dam.fcojavier.ecometrica.controllers;


import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.dam.fcojavier.ecometrica.dao.RecomendacionDAO;
import org.dam.fcojavier.ecometrica.entities.Huella;
import org.dam.fcojavier.ecometrica.entities.Recomendacion;
import org.dam.fcojavier.ecometrica.entities.Usuario;
import org.dam.fcojavier.ecometrica.services.HuellaService;
import org.dam.fcojavier.ecometrica.utils.Sesion;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class InicioController {

    private final HuellaService huellaService = new HuellaService();
    // Usamos el DAO directamente para el consejo aleatorio (lectura simple)
    private final RecomendacionDAO recomendacionDAO = new RecomendacionDAO();

    @FXML private Label lblSaludo;
    @FXML private Label lblHuellaMes;
    @FXML private Label lblHuellaTotal;
    @FXML private Label lblUltimaActividad;
    @FXML private Label lblUltimaFecha;
    @FXML private HBox boxPodium;
    @FXML private Label lblConsejo;

    @FXML
    public void initialize() {
        Usuario usuario = Sesion.getInstancia().getUsuarioLogueado();
        if (usuario == null) return;

        lblSaludo.setText("Hola, " + usuario.getNombre());

        cargarKPIs(usuario);
        cargarTop3(usuario);
    }

    private void cargarKPIs(Usuario usuario) {
        // KPI 1: Mes Actual
        LocalDate inicioMes = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate finMes = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        double totalMes = huellaService.calcularImpactoRango(usuario, inicioMes, finMes);
        lblHuellaMes.setText(String.format("%.2f kg", totalMes));

        // KPI 2: Total Histórico
        double totalHistorico = huellaService.calcularImpactoTotal(usuario);
        lblHuellaTotal.setText(String.format("%.2f kg", totalHistorico));

        // KPI 3: Última Actividad (Buscamos la última huella)
        List<Huella> huellas = huellaService.obtenerHuellasDelUsuario(usuario);
        if (!huellas.isEmpty()) {
            // Ordenamos por fecha descendente para coger la última
            huellas.sort(Comparator.comparing(Huella::getFecha).reversed());
            Huella ultima = huellas.get(0);
            lblUltimaActividad.setText(ultima.getId_actividad().getNombre());
            lblUltimaFecha.setText(ultima.getFecha().toString());
        } else {
            lblUltimaActividad.setText("-");
            lblUltimaFecha.setText("Sin registros");
        }
    }

    private void cargarTop3(Usuario usuario) {
        boxPodium.getChildren().clear();
        List<Object[]> top3 = huellaService.obtenerTop3Actividades(usuario);

        if (top3.isEmpty()) {
            boxPodium.getChildren().add(new Label("Registra actividades para ver el podio."));
            // Si no hay podio, mostramos un consejo genérico aleatorio
            cargarConsejoAleatorio();
            return;
        }

        // --- LÓGICA DEL PODIO (Mantenemos lo que tenías) ---
        Object[] rank1 = (top3.size() > 0) ? top3.get(0) : null;
        Object[] rank2 = (top3.size() > 1) ? top3.get(1) : null;
        Object[] rank3 = (top3.size() > 2) ? top3.get(2) : null;

        if (rank2 != null) boxPodium.getChildren().add(crearEscalonPodio(rank2, 2));
        if (rank1 != null) boxPodium.getChildren().add(crearEscalonPodio(rank1, 1));
        if (rank3 != null) boxPodium.getChildren().add(crearEscalonPodio(rank3, 3));

        // --- NUEVO: CARGAR CONSEJO DEL TOP 1 POR DEFECTO ---
        if (rank1 != null) {
            // Extraemos la categoría del nº1 (posición [2] del array)
            org.dam.fcojavier.ecometrica.entities.Categoria catTop1 =
                    (org.dam.fcojavier.ecometrica.entities.Categoria) rank1[2];

            cargarConsejoPorCategoria(catTop1);
        }
    }

    private VBox crearEscalonPodio(Object[] datos, int ranking) {
        String actividad = (String) datos[0];
        Double impacto = (Double) datos[1];
        // NUEVO: Recuperamos la categoría de la consulta
        org.dam.fcojavier.ecometrica.entities.Categoria categoria = (org.dam.fcojavier.ecometrica.entities.Categoria) datos[2];

        // --- CONFIGURACIÓN DE DIMENSIONES Y COLORES (Igual que antes) ---
        String colorFondo;
        int altura;
        String medalla;
        String bordeColor;

        switch (ranking) {
            case 1: // ORO
                colorFondo = "#FFF8E1";
                bordeColor = "#FFD54F";
                altura = 200;
                medalla = "🥇";
                break;
            case 2: // PLATA
                colorFondo = "#F5F5F5";
                bordeColor = "#BDBDBD";
                altura = 160;
                medalla = "🥈";
                break;
            default: // BRONCE
                colorFondo = "#EFEBE9";
                bordeColor = "#8D6E63";
                altura = 130;
                medalla = "🥉";
                break;
        }

        // --- CONSTRUCCIÓN DEL BLOQUE ---
        VBox escalon = new VBox(5);
        escalon.setAlignment(Pos.CENTER);
        escalon.setPadding(new javafx.geometry.Insets(10, 5, 10, 5));

        escalon.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(escalon, Priority.ALWAYS);

        escalon.setPrefHeight(altura);
        escalon.setMinHeight(altura);
        escalon.setMaxHeight(altura);

        escalon.setStyle("-fx-background-color: " + colorFondo + "; " +
                "-fx-background-radius: 15; " +
                "-fx-border-color: " + bordeColor + "; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // --- NUEVO: INTERACTIVIDAD ---
        // 1. Cambiar cursor al pasar por encima
        escalon.setCursor(javafx.scene.Cursor.HAND);

        // 2. Evento Clic
        escalon.setOnMouseClicked(event -> {
            cargarConsejoPorCategoria(categoria);
        });

        // 3. Efecto Hover (Opcional, para que se ilumine un poco)
        escalon.setOnMouseEntered(e -> escalon.setStyle(
                "-fx-background-color: " + colorFondo + "; " +
                        "-fx-background-radius: 15; " +
                        "-fx-border-color: -fx-color-primario; " + // Borde naranja al pasar ratón
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 15; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 4);" // Más sombra
        ));

        escalon.setOnMouseExited(e -> escalon.setStyle(
                "-fx-background-color: " + colorFondo + "; " +
                        "-fx-background-radius: 15; " +
                        "-fx-border-color: " + bordeColor + "; " + // Vuelve a su color original
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 15; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        ));

        // --- CONTENIDO (Igual que antes) ---
        Label lblMedalla = new Label(medalla);
        lblMedalla.setStyle("-fx-font-size: 36px; -fx-padding: -5 0 0 0;");

        Label lblNombre = new Label(actividad);
        lblNombre.setWrapText(true);
        lblNombre.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        lblNombre.setStyle("-fx-font-weight: bold; -fx-text-fill: #3E2723; -fx-font-size: 14px;");

        Label lblValor = new Label(String.format("%.0f kg", impacto));
        lblValor.setStyle("-fx-text-fill: -fx-color-primario; -fx-font-weight: bold; -fx-font-size: 20px;");

        escalon.getChildren().addAll(lblMedalla, lblNombre, lblValor);

        return escalon;
    }

    private void cargarConsejoAleatorio() {
        List<Recomendacion> todas = recomendacionDAO.findAll();
        if (!todas.isEmpty()) {
            int indice = new Random().nextInt(todas.size());
            lblConsejo.setText("\"" + todas.get(indice).getDescripcion() + "\"");
        } else {
            lblConsejo.setText("Registra actividades para ver consejos.");
        }
    }

    private void cargarConsejoPorCategoria(org.dam.fcojavier.ecometrica.entities.Categoria categoria) {
        if (categoria == null) return;

        List<Recomendacion> recomendaciones = recomendacionDAO.findByCategoria(categoria);

        if (recomendaciones != null && !recomendaciones.isEmpty()) {
            int indice = new Random().nextInt(recomendaciones.size());
            // Animación visual simple: cambiamos el texto
            lblConsejo.setText("\"" + recomendaciones.get(indice).getDescripcion() + "\"");
        } else {
            lblConsejo.setText("No hay consejos específicos disponibles para " + categoria.getNombre());
        }
    }
}
