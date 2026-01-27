package org.dam.fcojavier.ecometrica.dao;

import org.dam.fcojavier.ecometrica.entities.Huella;
import org.dam.fcojavier.ecometrica.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.List;

/**
 * DAO avanzado para el cálculo de impacto ambiental y registros de huella.
 */
public class HuellaDAO extends GenericDAO<Huella>{
    // Consulta extraída a constante
    private static final String HQL_BUSCAR_POR_USUARIO = "FROM Huella h " +
            "JOIN FETCH h.id_actividad a " +
            "JOIN FETCH a.categoria " +
            "WHERE h.id_usuario.id = :idUsuario";
    private static final String HQL_IMPACTO_TOTAL = "SELECT SUM(h.valor * c.factorEmision) " +
            "FROM Huella h " +
            "JOIN h.id_actividad a " +
            "JOIN a.categoria c " +
            "WHERE h.id_usuario.id = :idUsuario";
    private static final String HQL_IMPACTO_POR_CATEGORIA = "SELECT c.nombre, SUM(h.valor * c.factorEmision) " +
            "FROM Huella h " +
            "JOIN h.id_actividad a " +
            "JOIN a.categoria c " +
            "WHERE h.id_usuario.id = :idUsuario " +
            "GROUP BY c.nombre";
    private static final String HQL_IMPACTO_RANGO_FECHAS = "SELECT SUM(h.valor * c.factorEmision) " +
            "FROM Huella h " +
            "JOIN h.id_actividad a " +
            "JOIN a.categoria c " +
            "WHERE h.id_usuario.id = :idUsuario " +
            "AND h.fecha BETWEEN :inicio AND :fin";
    private static final String HQL_MEDIA_POR_CATEGORIA = "SELECT c.nombre, AVG(h.valor * c.factorEmision) " +
            "FROM Huella h " +
            "JOIN h.id_actividad a " +
            "JOIN a.categoria c " +
            "WHERE h.id_usuario.id = :idUsuario " +
            "GROUP BY c.nombre";
    private static final String HQL_TOP3_ACTIVIDADES = "SELECT a.nombre, SUM(h.valor * c.factorEmision) as impacto, a.categoria " + // <-- Añadido a.categoria
            "FROM Huella h " +
            "JOIN h.id_actividad a " +
            "JOIN a.categoria c " +
            "WHERE h.id_usuario.id = :idUsuario " +
            "GROUP BY a.nombre, a.categoria " +
            "ORDER BY impacto DESC";
    private static final String HQL_IMPACTO_CATEGORIA_Y_RANGO = "SELECT c.nombre, SUM(h.valor * c.factorEmision) " +
            "FROM Huella h " +
            "JOIN h.id_actividad a " +
            "JOIN a.categoria c " +
            "WHERE h.id_usuario.id = :idUsuario " +
            "AND h.fecha BETWEEN :inicio AND :fin " +
            "GROUP BY c.nombre";
    private static final String HQL_CALCULO_MEDIA_GLOBAL =
            "SELECT SUM(h.valor * c.factorEmision), COUNT(DISTINCT h.id_usuario) " +
                    "FROM Huella h " +
                    "JOIN h.id_actividad a " +
                    "JOIN a.categoria c " +
                    "WHERE h.fecha BETWEEN :inicio AND :fin";

    public HuellaDAO() {
        super(Huella.class);
    }

    /**
     * Obtiene el historial de registros de un usuario con carga de relaciones.
     */
    public List<Huella> findByUsuario(int idUsuario) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Huella> query = session.createQuery(HQL_BUSCAR_POR_USUARIO, Huella.class);
            query.setParameter("idUsuario", idUsuario);
            return query.list();
        }
    }

    /**
     * Calcula la suma total de emisiones de CO2 de un usuario.
     */
    public Double obtenerImpactoTotal(int idUsuario) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Double> query = session.createQuery(HQL_IMPACTO_TOTAL, Double.class);
            query.setParameter("idUsuario", idUsuario);
            Double res = query.uniqueResult();
            return (res != null) ? res : 0.0;
        }
    }

    /**
     * Obtiene el impacto agrupado por categorías (Nombre e impacto total).
     */
    public List<Object[]> obtenerImpactoPorCategoria(int idUsuario) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Object[]> query = session.createQuery(HQL_IMPACTO_POR_CATEGORIA, Object[].class);
            query.setParameter("idUsuario", idUsuario);
            return query.list();
        }
    }

    /**
     * Calcula el impacto total generado en un periodo de tiempo determinado.
     */
    public Double obtenerImpactoPorRangoFechas(int idUsuario, LocalDate fechaInicio, LocalDate fechaFin) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Double> query = session.createQuery(HQL_IMPACTO_RANGO_FECHAS, Double.class);
            query.setParameter("idUsuario", idUsuario);
            query.setParameter("inicio", fechaInicio);
            query.setParameter("fin", fechaFin);
            Double res = query.uniqueResult();
            return (res != null) ? res : 0.0;
        }
    }

    /**
     * Obtiene el impacto promedio agrupado por categoría.
     */
    public List<Object[]> obtenerMediaImpactoPorCategoria(int idUsuario) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Object[]> query = session.createQuery(HQL_MEDIA_POR_CATEGORIA, Object[].class);
            query.setParameter("idUsuario", idUsuario);
            return query.list();
        }
    }

    /**
     * Obtiene las 3 actividades más contaminantes del usuario.
     */
    public List<Object[]> obtenerTop3Actividades(int idUsuario) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Object[]> query = session.createQuery(HQL_TOP3_ACTIVIDADES, Object[].class);
            query.setParameter("idUsuario", idUsuario);
            query.setMaxResults(3);
            return query.list();
        }
    }

    /**
     * Obtiene el impacto por categoría filtrado por un rango de fechas.
     */
    public List<Object[]> obtenerImpactoPorCategoriaYRango(int idUsuario, LocalDate inicio, LocalDate fin) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Object[]> query = session.createQuery(HQL_IMPACTO_CATEGORIA_Y_RANGO, Object[].class);
            query.setParameter("idUsuario", idUsuario);
            query.setParameter("inicio", inicio);
            query.setParameter("fin", fin);
            return query.list();
        }
    }

    /**
     * Calcula la media global comunitaria: (CO2 Total / Número de Usuarios) en un periodo.
     */
    public Double obtenerMediaMensualGlobal(LocalDate inicio, LocalDate fin) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Object[]> query = session.createQuery(HQL_CALCULO_MEDIA_GLOBAL, Object[].class);
            query.setParameter("inicio", inicio);
            query.setParameter("fin", fin);

            Object[] resultado = query.uniqueResult();
            if (resultado == null) return 0.0;

            Double sumaTotal = (Double) resultado[0];
            Long totalUsuarios = (Long) resultado[1];

            if (sumaTotal == null || totalUsuarios == null || totalUsuarios == 0) {
                return 0.0;
            }

            return sumaTotal / totalUsuarios;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
}
