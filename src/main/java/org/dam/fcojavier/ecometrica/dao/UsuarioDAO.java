package org.dam.fcojavier.ecometrica.dao;

import org.dam.fcojavier.ecometrica.entities.Usuario;
import org.dam.fcojavier.ecometrica.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class UsuarioDAO extends GenericDAO<Usuario>{
    // Definimos la consulta como constante
    private static final String HQL_BUSCAR_POR_EMAIL = "FROM Usuario WHERE email = :email";

    public UsuarioDAO() {
        super(Usuario.class);
    }

    /**
     * Busca un usuario mediante su correo electrónico.
     * @param email Correo a buscar.
     * @return El Usuario encontrado o null.
     */
    public Usuario findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Usuario> query = session.createQuery(HQL_BUSCAR_POR_EMAIL, Usuario.class);
            query.setParameter("email", email);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
