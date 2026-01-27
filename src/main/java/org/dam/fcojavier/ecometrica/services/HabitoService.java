package org.dam.fcojavier.ecometrica.services;

import org.dam.fcojavier.ecometrica.dao.ActividadDAO;
import org.dam.fcojavier.ecometrica.dao.CategoriaDAO;
import org.dam.fcojavier.ecometrica.dao.HabitoDAO;
import org.dam.fcojavier.ecometrica.entities.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio para la gestión de hábitos recurrentes del usuario.
 */
public class HabitoService {

    private final HabitoDAO habitoDAO;
    private final CategoriaDAO categoriaDAO;
    private final ActividadDAO actividadDAO;

    /**
     * Constructor por defecto. Inicializa los DAOs necesarios.
     */
    public HabitoService() {
        this.habitoDAO = new HabitoDAO();
        this.categoriaDAO = new CategoriaDAO();
        this.actividadDAO = new ActividadDAO();
    }

    // MÉTODOS DE LECTURA

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
     * Recupera la lista de hábitos configurados por el usuario.
     * Se usará para mostrar la tabla "Mis Hábitos".
     *
     * @param usuario El usuario del cual se quieren obtener los hábitos.
     * @return Lista de hábitos del usuario.
     */
    public List<Habito> obtenerHabitosDelUsuario(Usuario usuario) {
        return habitoDAO.findByUsuario(usuario.getId());
    }

    // MÉTODO DE ESCRITURA

    /**
     * Crea o Actualiza un hábito para el usuario.
     * Según requisitos: Si ya existe (mismo usuario y actividad), actualiza frecuencia/tipo/fecha.
     *
     * @param usuario     El usuario propietario del hábito.
     * @param actividad   La actividad asociada al hábito.
     * @param frecuencia  Frecuencia con la que se realiza el hábito.
     * @param tipo        Tipo de hábito (ej: "Diario", "Semanal").
     * @param ultimaFecha Última fecha en la que se registró el hábito.
     */
    public void guardarOActualizarHabito(Usuario usuario, Actividad actividad,
                                         int frecuencia, String tipo, LocalDate ultimaFecha) {

        HabitoId id = new HabitoId();
        id.setIdUsuario(usuario.getId());
        id.setIdActividad(actividad.getId());

        Habito habito = new Habito();
        habito.setId(id);

        habito.setUsuario(usuario);
        habito.setActividad(actividad);

        habito.setFrecuencia(frecuencia);
        habito.setTipo(tipo);
        habito.setUltimaFecha(ultimaFecha);

        habitoDAO.saveOrUpdate(habito);
    }

    /**
     * Elimina un hábito específico.
     *
     * @param habito El hábito a eliminar.
     */
    public void eliminarHabito(Habito habito) {
        habitoDAO.delete(habito);
    }
}
