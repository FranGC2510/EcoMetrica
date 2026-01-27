package org.dam.fcojavier.ecometrica.dao;

import org.dam.fcojavier.ecometrica.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

/**
 * Clase padre para gestionar la base de datos.
 * <T> representa la Entidad (Usuario, Huella, etc.) con la que vamos a trabajar.
 */
public abstract class GenericDAO<T> {
    protected final Class<T> entityClass;

    public GenericDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Guarda una nueva entidad en la base de datos.
     * @param entity Objeto a persistir.
     */
    public void save(T entity) {
        executeInsideTransaction(session -> session.persist(entity));
    }

    /**
     * Actualiza o inserta una entidad (Sincroniza el estado).
     * @param entity Objeto a actualizar.
     */
    public void update(T entity) {
        executeInsideTransaction(session -> session.merge(entity));
    }

    /**
     * Elimina una entidad de la base de datos.
     * @param entity Objeto a borrar.
     */
    public void delete(T entity) {
        executeInsideTransaction(session -> session.remove(session.contains(entity) ? entity : session.merge(entity)));
    }

    /**
     * Busca una entidad por su identificador único.
     * @param id Identificador de la entidad.
     * @return La entidad encontrada o null.
     */
    public T findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(entityClass, id);
        }
    }

    /**
     * Recupera todos los registros de la entidad en la base de datos.
     * @return Lista de todas las entidades.
     */
    public List<T> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM " + entityClass.getSimpleName(), entityClass).list();
        }
    }

    /**
     * Método utilitario para envolver operaciones en una transacción segura.
     */
    protected void executeInsideTransaction(java.util.function.Consumer<Session> action) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            action.accept(session);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Error en operación de base de datos", e);
        }
    }
}
