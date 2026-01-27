package org.dam.fcojavier.ecometrica.dao;

import org.dam.fcojavier.ecometrica.entities.Categoria;
import org.dam.fcojavier.ecometrica.entities.Recomendacion;
import org.dam.fcojavier.ecometrica.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

/**
 * DAO para obtener consejos ambientales basados en categorías.
 */
public class RecomendacionDAO extends GenericDAO<Recomendacion>{
    private static final String HQL_BUSCAR_POR_CATEGORIA = "FROM Recomendacion r WHERE r.idCategoria.id = :idCat";
    public RecomendacionDAO() {
        super(Recomendacion.class);
    }

    /**
     * Obtiene recomendaciones para una categoría específica.
     * @param categoria Categoría de interés.
     * @return Lista de recomendaciones.
     */
    public List<Recomendacion> findByCategoria(Categoria categoria) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Recomendacion> query = session.createQuery(HQL_BUSCAR_POR_CATEGORIA, Recomendacion.class);
            query.setParameter("idCat", categoria.getId());
            return query.list();
        }
    }
}
