package org.dam.fcojavier.ecometrica.dao;

import org.dam.fcojavier.ecometrica.entities.Habito;
import org.dam.fcojavier.ecometrica.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class HabitoDAO extends GenericDAO<Habito>{
    private static final String HQL_BUSCAR_POR_USUARIO = "FROM Habito h " +
            "JOIN FETCH h.actividad a " +
            "JOIN FETCH a.categoria " +
            "WHERE h.usuario.id = :idUsuario";
    public HabitoDAO() {
        super(Habito.class);
    }

    /**
     * Recupera todos los hábitos de un usuario específico.
     * Usamos JOIN FETCH para traer la Actividad y su Categoría (evitando LazyInitializationException).
     */
    public List<Habito> findByUsuario(int idUsuario) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Habito> query = session.createQuery(HQL_BUSCAR_POR_USUARIO, Habito.class);
            query.setParameter("idUsuario", idUsuario);
            return query.list();
        }
    }

    /**
     * Guarda o actualiza un hábito.
     * Como usamos una clave compuesta, Hibernate a veces necesita ayuda para saber si es INSERT o UPDATE.
     * merge() se encarga de decidirlo automáticamente.
     */
    public void saveOrUpdate(Habito habito) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(habito); // merge es más seguro que saveOrUpdate para claves compuestas
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
}
