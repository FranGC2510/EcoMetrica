package org.dam.fcojavier.ecometrica.services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.dam.fcojavier.ecometrica.entities.Usuario;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servicio de exportación de datos a PDF utilizando la librería OpenPDF.
 * Mantiene la identidad visual corporativa de EcoMetrica.
 */
public class ReporteService {

    // --- Constantes de Diseño ---
    private static final Color COLOR_PRIMARIO = new Color(216, 125, 74); // #D87D4A
    private static final Color COLOR_GRIS_FONDO = new Color(248, 248, 248); // #F8F8F8
    private static final Color COLOR_BORDE = new Color(230, 230, 230); // #E6E6E6

    private static final Font F_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new Color(62, 39, 35));
    private static final Font F_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.GRAY);
    private static final Font F_HEADER_TABLA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
    private static final Font F_CELDA = FontFactory.getFont(FontFactory.HELVETICA, 11, new Color(60, 60, 60));
    private static final Font F_TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, COLOR_PRIMARIO);
    private static final Font F_METADATO_LABEL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
    private static final Font F_FOOTER = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.ITALIC, Color.GRAY);

    private static final String RUTA_LOGO = "/org/dam/fcojavier/ecometrica/views/images/isologo.png";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Genera un reporte detallado de impacto ambiental en formato PDF.
     *
     * @param archivoDestino Archivo donde se guardará el PDF.
     * @param usuario        Usuario del cual se genera el reporte.
     * @param inicio         Fecha de inicio del periodo del reporte.
     * @param fin            Fecha de fin del periodo del reporte.
     * @param datos          Lista de datos estadísticos a incluir en el reporte.
     * @throws DocumentException Si ocurre un error al generar el documento PDF.
     * @throws IOException       Si ocurre un error de entrada/salida al escribir el archivo.
     */
    public void generarReporteEstadisticas(File archivoDestino, Usuario usuario, LocalDate inicio, LocalDate fin, List<Object[]> datos) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, new FileOutputStream(archivoDestino));
        document.open();

        agregarCabecera(document);
        agregarInformacionUsuario(document, usuario, inicio, fin);
        double totalGlobal = agregarTablaDatos(document, datos);
        agregarResumenFinal(document, totalGlobal);
        agregarPiePagina(document);

        document.close();
    }

    // --- Métodos de Construcción del Documento ---

    /**
     * Agrega la cabecera del documento, incluyendo el logo y el título.
     *
     * @param document Documento PDF al que se agregará la cabecera.
     * @throws DocumentException Si ocurre un error al agregar elementos al documento.
     */
    private void agregarCabecera(Document document) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1f, 2f});
        headerTable.setSpacingAfter(20);

        // Columna Logo
        PdfPCell cellLogo = new PdfPCell();
        cellLogo.setBorder(Rectangle.NO_BORDER);
        cellLogo.setVerticalAlignment(Element.ALIGN_MIDDLE);

        try {
            URL urlImagen = getClass().getResource(RUTA_LOGO);
            if (urlImagen != null) {
                Image logo = Image.getInstance(urlImagen);
                logo.scaleToFit(200, 100);
                logo.setAlignment(Element.ALIGN_LEFT);
                cellLogo.addElement(logo);
            } else {
                cellLogo.addElement(new Phrase("EcoMetrica", F_SUBTITULO));
            }
        } catch (Exception e) {
            cellLogo.addElement(new Phrase("EcoMetrica", F_SUBTITULO));
        }
        headerTable.addCell(cellLogo);

        // Columna Título
        PdfPCell cellTitulo = new PdfPCell();
        cellTitulo.setBorder(Rectangle.NO_BORDER);
        cellTitulo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellTitulo.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph pTitulo = new Paragraph("Reporte de Impacto Ambiental", F_TITULO);
        pTitulo.setAlignment(Element.ALIGN_RIGHT);
        cellTitulo.addElement(pTitulo);

        Paragraph pLinea = new Paragraph("________________________________",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_PRIMARIO));
        pLinea.setAlignment(Element.ALIGN_RIGHT);
        pLinea.setLeading(10);
        cellTitulo.addElement(pLinea);

        headerTable.addCell(cellTitulo);
        document.add(headerTable);
    }

    /**
     * Agrega la información del usuario y el periodo del reporte al documento.
     *
     * @param document Documento PDF.
     * @param usuario  Usuario del reporte.
     * @param inicio   Fecha de inicio.
     * @param fin      Fecha de fin.
     * @throws DocumentException Si ocurre un error al agregar elementos.
     */
    private void agregarInformacionUsuario(Document document, Usuario usuario, LocalDate inicio, LocalDate fin) throws DocumentException {
        Paragraph datosMeta = new Paragraph();
        datosMeta.setSpacingAfter(20);

        datosMeta.add(new Chunk("Usuario: ", F_METADATO_LABEL));
        datosMeta.add(new Chunk(usuario.getNombre()));

        datosMeta.add(new Chunk("\nPeriodo:  ", F_METADATO_LABEL));
        datosMeta.add(new Chunk(inicio.format(DATE_FORMATTER) + " - " + fin.format(DATE_FORMATTER), F_SUBTITULO));

        document.add(datosMeta);
    }

    /**
     * Agrega la tabla de datos estadísticos al documento.
     *
     * @param document Documento PDF.
     * @param datos    Lista de datos a mostrar en la tabla.
     * @return El valor total global calculado a partir de los datos.
     * @throws DocumentException Si ocurre un error al agregar la tabla.
     */
    private double agregarTablaDatos(Document document, List<Object[]> datos) throws DocumentException {
        PdfPTable tabla = new PdfPTable(3);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{4f, 3f, 3f});
        tabla.setSpacingBefore(10f);

        crearEncabezadosTabla(tabla);

        double totalGlobal = datos.stream()
                .mapToDouble(fila -> (Double) fila[1])
                .sum();

        boolean par = true;
        for (Object[] fila : datos) {
            String categoria = (String) fila[0];
            Double valor = (Double) fila[1];
            double porcentaje = (totalGlobal > 0) ? (valor / totalGlobal) * 100 : 0;

            Color background = par ? Color.WHITE : COLOR_GRIS_FONDO;
            agregarFilaTabla(tabla, categoria, valor, porcentaje, background);
            par = !par;
        }

        document.add(tabla);
        return totalGlobal;
    }

    /**
     * Crea los encabezados de la tabla de datos.
     *
     * @param tabla Tabla PDF a la que se agregarán los encabezados.
     */
    private void crearEncabezadosTabla(PdfPTable tabla) {
        agregarCeldaHeader(tabla, "CATEGORÍA");
        agregarCeldaHeader(tabla, "IMPACTO (kg CO₂)");
        agregarCeldaHeader(tabla, "% TOTAL");
    }

    /**
     * Agrega una fila de datos a la tabla.
     *
     * @param tabla      Tabla PDF.
     * @param categoria  Nombre de la categoría.
     * @param valor      Valor del impacto.
     * @param porcentaje Porcentaje respecto al total.
     * @param background Color de fondo de la fila.
     */
    private void agregarFilaTabla(PdfPTable tabla, String categoria, Double valor, double porcentaje, Color background) {
        agregarCeldaDato(tabla, categoria, background, Element.ALIGN_LEFT);
        agregarCeldaDato(tabla, String.format("%.2f kg", valor), background, Element.ALIGN_CENTER);
        agregarCeldaDato(tabla, String.format("%.1f%%", porcentaje), background, Element.ALIGN_CENTER);
    }

    /**
     * Agrega el resumen final con el total del periodo al documento.
     *
     * @param document    Documento PDF.
     * @param totalGlobal Valor total del impacto en el periodo.
     * @throws DocumentException Si ocurre un error al agregar elementos.
     */
    private void agregarResumenFinal(Document document, double totalGlobal) throws DocumentException {
        document.add(Chunk.NEWLINE);

        PdfPTable tablaTotal = new PdfPTable(2);
        tablaTotal.setWidthPercentage(40);
        tablaTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);

        PdfPCell labelTotal = new PdfPCell(new Phrase("TOTAL PERIODO:", F_SUBTITULO));
        labelTotal.setBorder(Rectangle.NO_BORDER);
        labelTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelTotal.setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPCell valorTotal = new PdfPCell(new Phrase(String.format("%.2f kg", totalGlobal), F_TOTAL));
        valorTotal.setBorder(Rectangle.NO_BORDER);
        valorTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);

        tablaTotal.addCell(labelTotal);
        tablaTotal.addCell(valorTotal);
        document.add(tablaTotal);
    }

    /**
     * Agrega el pie de página al documento.
     *
     * @param document Documento PDF.
     * @throws DocumentException Si ocurre un error al agregar elementos.
     */
    private void agregarPiePagina(Document document) throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);
        Paragraph footer = new Paragraph("EcoMetrica App - Cuidando el planeta dato a dato.", F_FOOTER);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    // --- Métodos Auxiliares de Estilo ---

    /**
     * Agrega una celda de encabezado con estilo predefinido.
     *
     * @param tabla Tabla PDF.
     * @param texto Texto del encabezado.
     */
    private void agregarCeldaHeader(PdfPTable tabla, String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, F_HEADER_TABLA));
        cell.setBackgroundColor(COLOR_PRIMARIO);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(10);
        cell.setPaddingBottom(10);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.addCell(cell);
    }

    /**
     * Agrega una celda de datos con estilo predefinido.
     *
     * @param tabla Tabla PDF.
     * @param texto Texto de la celda.
     * @param fondo Color de fondo de la celda.
     * @param align Alineación del texto.
     */
    private void agregarCeldaDato(PdfPTable tabla, String texto, Color fondo, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, F_CELDA));
        cell.setBackgroundColor(fondo);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColorBottom(COLOR_BORDE);
        cell.setBorderWidthBottom(1f);
        cell.setBorderWidthLeft(0);
        cell.setBorderWidthRight(0);
        cell.setBorderWidthTop(0);
        cell.setPaddingTop(8);
        cell.setPaddingBottom(8);
        cell.setPaddingLeft(10);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tabla.addCell(cell);
    }
}
