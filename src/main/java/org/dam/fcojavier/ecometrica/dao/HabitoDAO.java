package org.dam.fcojavier.ecometrica.dao;

import org.dam.fcojavier.ecometrica.entities.Habito;
import org.dam.fcojavier.ecometrica.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * DAO para la gestión de hábitos (actividades frecuentes) de los usuarios.
 */
public class HabitoDAO extends GenericDAO<Habito>{
    private static final String HQL_BUSCAR_POR_USUARIO = "FROM Habito h " +
            "JOIN FETCH h.actividad a " +
            "JOIN FETCH a.categoria " +
            "WHERE h.usuario.id = :idUsuario";
    public HabitoDAO() {
        super(Habito.class);
    }

    /**
     * Obtiene los hábitos de un usuario con carga inmediata de relaciones.
     * @param idUsuario ID del usuario.
     * @return Lista de hábitos.
     */
    public List<Habito> findByUsuario(int idUsuario) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Habito> query = session.createQuery(HQL_BUSCAR_POR_USUARIO, Habito.class);
            query.setParameter("idUsuario", idUsuario);
            return query.list();
        }
    }

    /**
     * Persiste o actualiza un hábito usando merge para gestionar la clave compuesta.
     * @param habito Entidad hábito.
     */
    public void saveOrUpdate(Habito habito) {
        update(habito);
    }
}
