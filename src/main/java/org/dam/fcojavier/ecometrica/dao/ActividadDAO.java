package org.dam.fcojavier.ecometrica.dao;

import org.dam.fcojavier.ecometrica.entities.Actividad;
import org.dam.fcojavier.ecometrica.entities.Categoria;
import org.dam.fcojavier.ecometrica.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class ActividadDAO extends GenericDAO<Actividad>{

    private static final String HQL_BY_CATEGORIA = "FROM Actividad a JOIN FETCH a.categoria WHERE a.categoria.id = :idCat";

    public ActividadDAO() {
        super(Actividad.class);
    }

    public List<Actividad> findByCategoria(Categoria categoria) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Actividad> query = session.createQuery(HQL_BY_CATEGORIA, Actividad.class);
            query.setParameter("idCat", categoria.getId());
            return query.list();
        }
    }
}
