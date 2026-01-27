package org.dam.fcojavier.ecometrica.utils;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Utilidad para la gestión del ciclo de vida de Hibernate.
 * Proporciona acceso centralizado al {@link SessionFactory} para la persistencia de datos.
 * * @author EcoMetrica
 * @version 1.1
 */
public class HibernateUtil {
    /** Instancia única de SessionFactory para toda la aplicación. */
    private static final SessionFactory SESSION_FACTORY = buildSessionFactory();

    /**
     * Configura e inicializa la factoría de sesiones de Hibernate.
     * Lee la configuración desde el archivo hibernate.cfg.xml.
     * * @return Una instancia configurada de SessionFactory.
     * @throws ExceptionInInitializerError Si ocurre un error crítico en la configuración o conexión a la BBDD.
     */
    private static SessionFactory buildSessionFactory() {
        try {
            return new Configuration().configure().buildSessionFactory();
        } catch (HibernateException ex) {
            System.err.println("Error crítico en la inicialización de Hibernate: " + ex.getMessage());
            throw new ExceptionInInitializerError("No se pudo crear SessionFactory: " + ex);
        }
    }

    /**
     * Retorna la factoría de sesiones activa.
     * @return El SessionFactory configurado.
     */
    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

    /**
     * Cierra de forma segura el SessionFactory y libera todos los recursos.
     * Debe llamarse al finalizar la ejecución de la aplicación.
     */
    public static void shutdown() {
        if (SESSION_FACTORY != null && !SESSION_FACTORY.isClosed()) {
            SESSION_FACTORY.close();
        }
    }
}
