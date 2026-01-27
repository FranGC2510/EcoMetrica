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

/**
 * Servicio que orquestra el cálculo de emisiones y estadísticas de huella de carbono.
 */
public class HuellaService {

    private final HuellaDAO huellaDAO;
    private final CategoriaDAO categoriaDAO;
    private final ActividadDAO actividadDAO;

    /**
     * Constructor por defecto. Inicializa los DAOs necesarios.
     */
    public HuellaService() {
        this.huellaDAO = new HuellaDAO();
        this.categoriaDAO = new CategoriaDAO();
        this.actividadDAO = new ActividadDAO();
    }

    // Métodos de Consulta Maestra

    /**
     * Obtiene todas las categorías disponibles.
     *
     * @return Lista de todas las categorías.
     */
    public List<Categoria> obtenerTodasCategorias() {
        return categoriaDAO.findAll();
    }

    /**
     * Obtiene las actividades asociadas a una categoría específica.
     *
     * @param categoria La categoría de la cual se quieren obtener las actividades.
     * @return Lista de actividades de la categoría dada.
     */
    public List<Actividad> obtenerActividadesPorCategoria(Categoria categoria) {
        return actividadDAO.findByCategoria(categoria);
    }

    /**
     * Registra un nuevo impacto ambiental.
     * Automáticamente asigna la unidad basada en la categoría de la actividad.
     *
     * @param usuario   El usuario que está en sesión.
     * @param actividad La actividad seleccionada (ej: Conducir).
     * @param valor     El dato numérico introducido (ej: 50 km).
     * @param fecha     La fecha seleccionada.
     * @return El valor calculado de la huella de carbono.
     */
    public double registrarHuella(Usuario usuario, Actividad actividad, double valor, LocalDate fecha) {
        Huella nuevaHuella = new Huella();
        nuevaHuella.setId_usuario(usuario);
        nuevaHuella.setId_actividad(actividad);
        nuevaHuella.setValor(valor);

        nuevaHuella.setUnidad(actividad.getCategoria().getUnidad());
        nuevaHuella.setFecha(fecha);

        huellaDAO.save(nuevaHuella);

        return valor * actividad.getCategoria().getFactorEmision();
    }

    /**
     * Recupera el historial de huellas de un usuario.
     *
     * @param usuario El usuario del cual se quieren obtener las huellas.
     * @return Lista de huellas asociadas al usuario.
     */
    public List<Huella> obtenerHuellasDelUsuario(Usuario usuario) {
        return huellaDAO.findByUsuario(usuario.getId());
    }

    /**
     * Elimina una huella específica.
     *
     * @param huella La huella a eliminar.
     */
    public void eliminarHuella(Huella huella) {
        huellaDAO.delete(huella);
    }

    /**
     * Actualiza una huella existente y recalcula su impacto si cambiaron los valores.
     *
     * @param huella La huella con los datos actualizados.
     * @return El nuevo valor calculado de la huella de carbono.
     */
    public double actualizarHuella(Huella huella) {
        huella.setUnidad(huella.getId_actividad().getCategoria().getUnidad());
        huellaDAO.update(huella);
        return huella.getValor() * huella.getId_actividad().getCategoria().getFactorEmision();
    }

    // --- Métodos de Estadística

    /**
     * Calcula el impacto total de un usuario.
     *
     * @param usuario El usuario para el cual calcular el impacto total.
     * @return El impacto total de huella de carbono del usuario.
     */
    public double calcularImpactoTotal(Usuario usuario) {
        return huellaDAO.obtenerImpactoTotal(usuario.getId());
    }

    /**
     * Devuelve los datos listos para un PieChart (Nombre -> Valor).
     * JavaFX usa 'PieChart.Data', pero para no mezclar UI con Service,
     * devolvemos la lista cruda del DAO o un Map. Por sencillez, pasamos la lista del DAO.
     *
     * @param usuario El usuario para el cual obtener las estadísticas.
     * @return Lista de objetos con los datos estadísticos por categoría.
     */
    public List<Object[]> obtenerEstadisticasPorCategoria(Usuario usuario) {
        return huellaDAO.obtenerImpactoPorCategoria(usuario.getId());
    }

    /**
     * Calcula el impacto en un rango de fechas personalizado.
     *
     * @param usuario El usuario para el cual calcular el impacto.
     * @param inicio  Fecha de inicio del rango.
     * @param fin     Fecha de fin del rango.
     * @return El impacto total en el rango de fechas especificado.
     */
    public double calcularImpactoRango(Usuario usuario, LocalDate inicio, LocalDate fin) {
        return huellaDAO.obtenerImpactoPorRangoFechas(usuario.getId(), inicio, fin);
    }

    /**
     * Obtiene la media de la comunidad para comparar.
     *
     * @return Valor medio de la huella de carbono de la comunidad en el mes actual.
     */
    public Double obtenerMediaComunidadMesActual() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicio = hoy.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate fin = hoy.with(TemporalAdjusters.lastDayOfMonth());

        return huellaDAO.obtenerMediaMensualGlobal(inicio, fin);
    }

    /**
     * Obtiene las 3 actividades con mayor impacto para un usuario.
     *
     * @param usuario El usuario para el cual obtener el top 3 de actividades.
     * @return Lista de objetos con las 3 actividades principales y sus impactos.
     */
    public List<Object[]> obtenerTop3Actividades(Usuario usuario) {
        return huellaDAO.obtenerTop3Actividades(usuario.getId());
    }

    /**
     * Obtiene estadísticas por categoría dentro de un rango de fechas específico.
     *
     * @param usuario El usuario para el cual obtener las estadísticas.
     * @param inicio  Fecha de inicio del rango.
     * @param fin     Fecha de fin del rango.
     * @return Lista de objetos con estadísticas por categoría en el rango dado.
     */
    public List<Object[]> obtenerEstadisticasPorCategoriaYRango(Usuario usuario, LocalDate inicio, LocalDate fin) {
        return huellaDAO.obtenerImpactoPorCategoriaYRango(usuario.getId(), inicio, fin);
    }
}
