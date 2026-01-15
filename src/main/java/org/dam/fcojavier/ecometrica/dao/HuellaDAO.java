package org.dam.fcojavier.ecometrica.dao;

import org.dam.fcojavier.ecometrica.entities.Huella;
import org.dam.fcojavier.ecometrica.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class HuellaDAO extends GenericDAO<Huella>{
    // Consulta extraída a constante
    private static final String HQL_BUSCAR_POR_USUARIO = "FROM Huella h WHERE h.id_usuario.id = :idUsuario";

    public HuellaDAO() {
        super(Huella.class);
    }

    public List<Huella> findByUsuario(int idUsuario) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Huella> query = session.createQuery(HQL_BUSCAR_POR_USUARIO, Huella.class);
            query.setParameter("idUsuario", idUsuario);
            return query.list();
        }
    }
}
