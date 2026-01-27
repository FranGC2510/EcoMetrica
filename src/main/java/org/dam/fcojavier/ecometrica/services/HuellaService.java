package org.dam.fcojavier.ecometrica.services;

import org.dam.fcojavier.ecometrica.dao.ActividadDAO;
import org.dam.fcojavier.ecometrica.dao.CategoriaDAO;
import org.dam.fcojavier.ecometrica.dao.HuellaDAO;
import org.dam.fcojavier.ecometrica.entities.Actividad;
import org.dam.fcojavier.ecometrica.entities.Categoria;
import org.dam.fcojavier.ecometrica.entities.Huella;
import org.dam.fcojavier.ecometrica.entities.Usuario;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
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

    /**
     * Recupera el historial de huellas de un usuario.
     */
    public List<Huella> obtenerHuellasDelUsuario(Usuario usuario) {
        // Delegamos al DAO que ya tiene el método findByUsuario implementado
        return huellaDAO.findByUsuario(usuario.getId());
    }

    public void eliminarHuella(Huella huella) {
        huellaDAO.delete(huella);
    }

    /**
     * Actualiza una huella existente y recalcula su impacto si cambiaron los valores.
     */
    public double actualizarHuella(Huella huella) {
        // Recalculamos la unidad y el valor por seguridad, por si cambió la actividad
        huella.setUnidad(huella.getId_actividad().getCategoria().getUnidad());

        huellaDAO.update(huella);

        // Devolvemos el impacto recalculado
        return huella.getValor() * huella.getId_actividad().getCategoria().getFactorEmision();
    }

    public double calcularImpactoTotal(Usuario usuario) {
        return huellaDAO.obtenerImpactoTotal(usuario.getId());
    }

    /**
     * Devuelve los datos listos para un PieChart (Nombre -> Valor)
     * JavaFX usa 'PieChart.Data', pero para no mezclar UI con Service,
     * devolvemos la lista cruda del DAO o un Map. Por sencillez, pasamos la lista del DAO.
     */
    public List<Object[]> obtenerEstadisticasPorCategoria(Usuario usuario) {
        return huellaDAO.obtenerImpactoPorCategoria(usuario.getId());
    }

    /**
     * Calcula el impacto en un rango de fechas personalizado.
     */
    public double calcularImpactoRango(Usuario usuario, LocalDate inicio, LocalDate fin) {
        return huellaDAO.obtenerImpactoPorRangoFechas(usuario.getId(), inicio, fin);
    }

    /**
     * Obtiene la media de la comunidad para comparar.
     * @return Lista de [Categoría, Media]
     */
    public Double obtenerMediaComunidadMesActual() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicio = hoy.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate fin = hoy.with(TemporalAdjusters.lastDayOfMonth());

        return huellaDAO.obtenerMediaMensualGlobal(inicio, fin);
    }

    /**
     * Obtiene la media de impacto del usuario por categoría.
     * Útil para comparar con la media global.
     */
    public List<Object[]> obtenerMediaImpactoUsuario(Usuario usuario) {
        return huellaDAO.obtenerMediaImpactoPorCategoria(usuario.getId());
    }

    public List<Object[]> obtenerTop3Actividades(Usuario usuario) {
        return huellaDAO.obtenerTop3Actividades(usuario.getId());
    }

    public List<Object[]> obtenerEstadisticasPorCategoriaYRango(Usuario usuario, LocalDate inicio, LocalDate fin) {
        return huellaDAO.obtenerImpactoPorCategoriaYRango(usuario.getId(), inicio, fin);
    }
}
