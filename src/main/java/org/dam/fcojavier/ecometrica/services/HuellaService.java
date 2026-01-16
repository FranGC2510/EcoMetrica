package org.dam.fcojavier.ecometrica.services;

import org.dam.fcojavier.ecometrica.dao.ActividadDAO;
import org.dam.fcojavier.ecometrica.dao.CategoriaDAO;
import org.dam.fcojavier.ecometrica.dao.HuellaDAO;
import org.dam.fcojavier.ecometrica.entities.Actividad;
import org.dam.fcojavier.ecometrica.entities.Categoria;
import org.dam.fcojavier.ecometrica.entities.Huella;
import org.dam.fcojavier.ecometrica.entities.Usuario;

import java.time.LocalDate;
import java.util.List;

public class HuellaService {

    private final HuellaDAO huellaDAO;
    private final CategoriaDAO categoriaDAO;
    private final ActividadDAO actividadDAO;

    public HuellaService() {
        this.huellaDAO = new HuellaDAO();
        this.categoriaDAO = new CategoriaDAO();
        this.actividadDAO = new ActividadDAO();
    }

    // --- MÉTODOS DE LECTURA (Para llenar los ComboBox) ---

    public List<Categoria> obtenerTodasCategorias() {
        return categoriaDAO.findAll();
    }

    public List<Actividad> obtenerActividadesPorCategoria(Categoria categoria) {
        return actividadDAO.findByCategoria(categoria);
    }

    // --- MÉTODO DE ESCRITURA (Lógica principal) ---

    /**
     * Calcula el impacto y guarda el registro de huella.
     *
     * @param usuario   El usuario que está en sesión.
     * @param actividad La actividad seleccionada (ej: Conducir).
     * @param valor     El dato numérico introducido (ej: 50 km).
     * @param fecha     La fecha seleccionada.
     */
    public double registrarHuella(Usuario usuario, Actividad actividad, double valor, LocalDate fecha) {
        Huella nuevaHuella = new Huella();
        nuevaHuella.setId_usuario(usuario);
        nuevaHuella.setId_actividad(actividad);

        // CORRECCIÓN IMPORTANTE: Guardamos el input del usuario (ej: 150), no el CO2.
        nuevaHuella.setValor(valor);

        // CORRECCIÓN IMPORTANTE: Guardamos la unidad de la categoría (ej: "km", "kWh").
        nuevaHuella.setUnidad(actividad.getCategoria().getUnidad());

        nuevaHuella.setFecha(fecha);

        // 2. Guardamos en base de datos
        huellaDAO.save(nuevaHuella);

        // 3. Calculamos y devolvemos el impacto SOLO para informar al usuario (Feedback)
        // Fórmula: Valor * Factor de Emisión
        return valor * actividad.getCategoria().getFactorEmision();
    }
}
