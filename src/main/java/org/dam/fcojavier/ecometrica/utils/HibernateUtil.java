package org.dam.fcojavier.ecometrica.utils;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // Intenta leer el archivo hibernate.cfg.xml y crear la conexión
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            // Si falla aquí, es que no encuentra el archivo o la BBDD está mal configurada
            System.err.println("Error crítico creando SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        // Cierra la conexión al cerrar la aplicación
        getSessionFactory().close();
    }
}
