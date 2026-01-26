package org.dam.fcojavier.ecometrica.services;

import org.dam.fcojavier.ecometrica.dao.ActividadDAO;
import org.dam.fcojavier.ecometrica.dao.CategoriaDAO;
import org.dam.fcojavier.ecometrica.dao.HabitoDAO;
import org.dam.fcojavier.ecometrica.entities.*;

import java.time.LocalDate;
import java.util.List;

public class HabitoService {

    private final HabitoDAO habitoDAO;
    private final CategoriaDAO categoriaDAO;
    private final ActividadDAO actividadDAO;

    public HabitoService() {
        this.habitoDAO = new HabitoDAO();
        this.categoriaDAO = new CategoriaDAO();
        this.actividadDAO = new ActividadDAO();
    }

    // --- MÉTODOS DE LECTURA (Para rellenar la tabla y los combos) ---

    public List<Categoria> obtenerTodasCategorias() {
        return categoriaDAO.findAll();
    }

    public List<Actividad> obtenerActividadesPorCategoria(Categoria categoria) {
        return actividadDAO.findByCategoria(categoria);
    }

    /**
     * Recupera la lista de hábitos configurados por el usuario.
     * Se usará para mostrar la tabla "Mis Hábitos".
     */
    public List<Habito> obtenerHabitosDelUsuario(Usuario usuario) {
        return habitoDAO.findByUsuario(usuario.getId());
    }

    // --- MÉTODO DE ESCRITURA (Lógica Principal) ---

    /**
     * Crea o Actualiza un hábito para el usuario.
     * Según requisitos: Si ya existe (mismo usuario y actividad), actualiza frecuencia/tipo/fecha.
     */
    public void guardarOActualizarHabito(Usuario usuario, Actividad actividad,
                                         int frecuencia, String tipo, LocalDate ultimaFecha) {

        // 1. Crear el ID Compuesto (La clave del hábito)
        HabitoId id = new HabitoId();
        id.setIdUsuario(usuario.getId());
        id.setIdActividad(actividad.getId());

        // 2. Crear el objeto Hábito y asignar sus partes
        Habito habito = new Habito();
        habito.setId(id); // Asignamos la clave compuesta

        // Relaciones completas (necesarias para JPA)
        habito.setUsuario(usuario);
        habito.setActividad(actividad);

        // Datos configurables por el usuario [cite: 15, 74]
        habito.setFrecuencia(frecuencia);
        habito.setTipo(tipo); // Ej: "semanal", "diario"
        habito.setUltimaFecha(ultimaFecha);

        // 3. Delegar al DAO la decisión de Insertar o Actualizar
        habitoDAO.saveOrUpdate(habito);
    }

    public void eliminarHabito(Habito habito) {
        habitoDAO.delete(habito);
    }
}
