package org.dam.fcojavier.ecometrica.dao;

import org.dam.fcojavier.ecometrica.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * Clase padre para gestionar la base de datos.
 * <T> representa la Entidad (Usuario, Huella, etc.) con la que vamos a trabajar.
 */
public abstract class GenericDAO<T> {
    // Necesitamos saber qué clase es T para hacer las consultas
    private Class<T> entityClass;

    public GenericDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    // 1. GUARDAR (Create)
    public void save(T entity) {
        Transaction transaction = null;
        // Abrimos sesión. El try(...) asegura que se cierre sola al terminar.
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(entity); // persist = guardar nuevo
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback(); // Si falla, deshacer cambios
            e.printStackTrace();
        }
    }

    // 2. ACTUALIZAR (Update)
    public void update(T entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(entity); // merge = actualiza si existe
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    // 3. BORRAR (Delete)
    public void delete(T entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(entity); // remove = borrar
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    // 4. BUSCAR POR ID (Read One)
    public T findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(entityClass, id);
        }
    }

    // 5. BUSCAR TODOS (Read All)
    public List<T> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // HQL: "FROM Usuario", "FROM Huella", etc.
            String hql = "FROM " + entityClass.getSimpleName();
            Query<T> query = session.createQuery(hql, entityClass);
            return query.list();
        }
    }
}
