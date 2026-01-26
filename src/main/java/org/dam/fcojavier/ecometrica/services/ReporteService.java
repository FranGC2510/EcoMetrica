package org.dam.fcojavier.ecometrica.services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.dam.fcojavier.ecometrica.entities.Usuario;

import java.awt.Color; // Para colores personalizados
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReporteService {

    // COLORES CORPORATIVOS
    private static final Color COLOR_PRIMARIO = new Color(216, 125, 74); // #D87D4A (Naranja)
    private static final Color COLOR_GRIS_FONDO = new Color(248, 248, 248); // #F8F8F8 (Gris muy suave)
    private static final Color COLOR_BORDE = new Color(230, 230, 230); // #E6E6E6 (Líneas sutiles)

    // FUENTES
    private static final Font F_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new Color(62, 39, 35));
    private static final Font F_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.GRAY);
    private static final Font F_HEADER_TABLA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
    private static final Font F_CELDA = FontFactory.getFont(FontFactory.HELVETICA, 11, new Color(60, 60, 60));
    private static final Font F_TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, COLOR_PRIMARIO);

    public void generarReporteEstadisticas(File archivoDestino, Usuario usuario, LocalDate inicio, LocalDate fin, List<Object[]> datos) throws DocumentException, IOException {

        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(archivoDestino));

        // Evento para pintar cabeceras/pies de página si quisieras (opcional)
        document.open();

        // --- 1. CABECERA MAQUETADA (Tabla invisible 2 columnas) ---
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        // Proporción: 1 parte para logo, 2 partes para el título
        headerTable.setWidths(new float[]{1f, 2f});
        headerTable.setSpacingAfter(20);

        // -- CELDA IZQUIERDA: LOGO --
        PdfPCell cellLogo = new PdfPCell();
        cellLogo.setBorder(Rectangle.NO_BORDER);
        cellLogo.setVerticalAlignment(Element.ALIGN_MIDDLE); // Centrado verticalmente respecto al título

        try {
            // Usamos la ruta que ya te ha funcionado
            String rutaLogo = "/org/dam/fcojavier/ecometrica/views/images/isologo.png";
            URL urlImagen = getClass().getResource(rutaLogo);

            if (urlImagen != null) {
                Image logo = Image.getInstance(urlImagen);

                // CAMBIO: Aumentamos el tamaño (antes 120,60 -> ahora 200,100)
                logo.scaleToFit(200, 100);
                logo.setAlignment(Element.ALIGN_LEFT);
                cellLogo.addElement(logo);
            } else {
                cellLogo.addElement(new Phrase("EcoMetrica", F_SUBTITULO));
            }
        } catch (Exception e) {
            // Si falla, no rompemos el PDF
            cellLogo.addElement(new Phrase("", F_SUBTITULO));
        }
        headerTable.addCell(cellLogo);

        // -- CELDA DERECHA: TÍTULO --
        PdfPCell cellTitulo = new PdfPCell();
        cellTitulo.setBorder(Rectangle.NO_BORDER);
        cellTitulo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellTitulo.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph pTitulo = new Paragraph("Reporte de Impacto Ambiental", F_TITULO);
        pTitulo.setAlignment(Element.ALIGN_RIGHT);
        cellTitulo.addElement(pTitulo);

        // Línea decorativa naranja DEBAJO del título
        Paragraph pLinea = new Paragraph("________________________________",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_PRIMARIO));
        pLinea.setAlignment(Element.ALIGN_RIGHT);
        // Ajustamos el interlineado para que la línea suba y pegue al texto
        pLinea.setLeading(10);
        cellTitulo.addElement(pLinea);

        headerTable.addCell(cellTitulo);

        // Añadir la tabla de cabecera al documento
        document.add(headerTable);

        // --- 2. DATOS DEL USUARIO (Debajo de la cabecera) ---
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Usamos un párrafo simple con algo de margen izquierdo o directo
        Paragraph datosMeta = new Paragraph();
        datosMeta.setSpacingAfter(20);
        datosMeta.add(new Chunk("Usuario: ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK)));
        datosMeta.add(new Chunk(usuario.getNombre()));

        datosMeta.add(new Chunk("\nPeriodo:  ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK)));
        datosMeta.add(new Chunk(inicio.format(fmt) + " - " + fin.format(fmt), F_SUBTITULO));

        document.add(datosMeta);

        // --- 3. TABLA MODERNA ---
        PdfPTable tabla = new PdfPTable(3);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{4f, 3f, 3f});
        tabla.setSpacingBefore(10f);

        // Cabecera Estilizada
        agregarHeader(tabla, "CATEGORÍA");
        agregarHeader(tabla, "IMPACTO (kg CO₂)");
        agregarHeader(tabla, "% TOTAL");

        // Datos
        double totalGlobal = 0;
        for (Object[] fila : datos) totalGlobal += (Double) fila[1];

        boolean par = true; // Para alternar colores
        for (Object[] fila : datos) {
            String cat = (String) fila[0];
            Double val = (Double) fila[1];
            double pct = (totalGlobal > 0) ? (val / totalGlobal) * 100 : 0;

            Color background = par ? Color.WHITE : COLOR_GRIS_FONDO;

            agregarCelda(tabla, cat, background, Element.ALIGN_LEFT);
            agregarCelda(tabla, String.format("%.2f kg", val), background, Element.ALIGN_CENTER);
            agregarCelda(tabla, String.format("%.1f%%", pct), background, Element.ALIGN_CENTER);

            par = !par;
        }
        document.add(tabla);

        // --- 4. TOTAL Y PIE ---
        document.add(Chunk.NEWLINE);

        // Panel de Total (Un pequeño cuadro alineado a la derecha)
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

        // Pie de página
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);
        Paragraph footer = new Paragraph("EcoMetrica App - Cuidando el planeta dato a dato.",
                FontFactory.getFont(FontFactory.HELVETICA, 9, Font.ITALIC, Color.GRAY));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
    }

    // --- MÉTODOS DE ESTILO ---

    private Paragraph crearParrafoDato(String etiqueta, String valor) {
        Phrase p = new Phrase();
        p.add(new Chunk(etiqueta + " ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK)));
        p.add(new Chunk(valor, F_SUBTITULO));
        return new Paragraph(p);
    }

    private void agregarHeader(PdfPTable tabla, String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, F_HEADER_TABLA));
        cell.setBackgroundColor(COLOR_PRIMARIO);
        cell.setBorder(Rectangle.NO_BORDER); // Sin bordes negros
        cell.setPaddingTop(10);
        cell.setPaddingBottom(10);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.addCell(cell);
    }

    private void agregarCelda(PdfPTable tabla, String texto, Color fondo, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, F_CELDA));
        cell.setBackgroundColor(fondo);

        // ESTILO MODERNO: Solo borde inferior gris suave
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
