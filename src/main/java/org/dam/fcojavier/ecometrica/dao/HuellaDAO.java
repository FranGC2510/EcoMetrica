package org.dam.fcojavier.ecometrica.dao;

import org.dam.fcojavier.ecometrica.entities.Huella;
import org.dam.fcojavier.ecometrica.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.List;

public class HuellaDAO extends GenericDAO<Huella>{
    // Consulta extraída a constante
    private static final String HQL_BUSCAR_POR_USUARIO = "FROM Huella h " +
            "JOIN FETCH h.id_actividad a " +
            "JOIN FETCH a.categoria " +
            "WHERE h.id_usuario.id = :idUsuario";
    private static final String HQL_OBTENER_IMPACTO_TOTAL = "SELECT SUM(h.valor * c.factorEmision) " +
            "FROM Huella h " +
            "JOIN h.id_actividad a " +
            "JOIN a.categoria c " +
            "WHERE h.id_usuario.id = :idUsuario";
    private static final String HQL_OBTENER_IMPACTO_POR_CATEGORIA = "SELECT c.nombre, SUM(h.valor * c.factorEmision) " +
            "FROM Huella h " +
            "JOIN h.id_actividad a " +
            "JOIN a.categoria c " +
            "WHERE h.id_usuario.id = :idUsuario " +
            "GROUP BY c.nombre";
    private static final String HQL_OBTENER_IMPACTO_POR_RANGO_FECHAS = "SELECT SUM(h.valor * c.factorEmision) " +
            "FROM Huella h " +
            "JOIN h.id_actividad a " +
            "JOIN a.categoria c " +
            "WHERE h.id_usuario.id = :idUsuario " +
            "AND h.fecha BETWEEN :inicio AND :fin";
    private static final String HQL_OBTENER_MEDIA_COMUNIDAD = "SELECT c.nombre, AVG(h.valor * c.factorEmision) " +
            "FROM Huella h " +
            "JOIN h.id_actividad a " +
            "JOIN a.categoria c " +
            "GROUP BY c.nombre";
    private static final String HQL_OBTENER_MEDIA_POR_CATEGORIA = "SELECT c.nombre, AVG(h.valor * c.factorEmision) " +
            "FROM Huella h " +
            "JOIN h.id_actividad a " +
            "JOIN a.categoria c " +
            "WHERE h.id_usuario.id = :idUsuario " +
            "GROUP BY c.nombre";
    private static final String HQL_OBTENER_TOP3_ACTIVIDADES = "SELECT a.nombre, SUM(h.valor * c.factorEmision) as impacto, a.categoria " + // <-- Añadido a.categoria
            "FROM Huella h " +
            "JOIN h.id_actividad a " +
            "JOIN a.categoria c " +
            "WHERE h.id_usuario.id = :idUsuario " +
            "GROUP BY a.nombre, a.categoria " +
            "ORDER BY impacto DESC";
    private static final String HQL_OBTENER_IMPACTO_POR_RANGO_FECHAS_Y_CATEGORIA = "SELECT c.nombre, SUM(h.valor * c.factorEmision) " +
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

    public List<Huella> findByUsuario(int idUsuario) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Huella> query = session.createQuery(HQL_BUSCAR_POR_USUARIO, Huella.class);
            query.setParameter("idUsuario", idUsuario);
            return query.list();
        }
    }

    /**
     * Calcula la suma total de emisiones de CO2 de un usuario.
     * HQL: SUM(valor * factor)
     */
    public Double obtenerImpactoTotal(int idUsuario) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Double> query = session.createQuery(HQL_OBTENER_IMPACTO_TOTAL, Double.class);
            query.setParameter("idUsuario", idUsuario);

            // Si no hay registros devuelve null, así que lo controlamos
            Double resultado = query.uniqueResult();
            return (resultado != null) ? resultado : 0.0;
        }
    }

    /**
     * Obtiene el impacto agrupado por categorías para el gráfico de tarta.
     * Devuelve una lista de arrays de objetos: [NombreCategoria (String), ImpactoTotal (Double)]
     */
    public List<Object[]> obtenerImpactoPorCategoria(int idUsuario) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Object[]> query = session.createQuery(HQL_OBTENER_IMPACTO_POR_CATEGORIA, Object[].class);
            query.setParameter("idUsuario", idUsuario);
            return query.list();
        }
    }

    public Double obtenerImpactoPorRangoFechas(int idUsuario, LocalDate fechaInicio, LocalDate fechaFin) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Double> query = session.createQuery(HQL_OBTENER_IMPACTO_POR_RANGO_FECHAS, Double.class);
            query.setParameter("idUsuario", idUsuario);
            query.setParameter("inicio", fechaInicio);
            query.setParameter("fin", fechaFin);

            Double resultado = query.uniqueResult();
            return (resultado != null) ? resultado : 0.0;
        }
    }

    // 2. REQUISITO: Comparativa AVG (Media) de TODOS los usuarios por categoría
    public List<Object[]> obtenerMediaImpactoComunidad() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Object[]> query = session.createQuery(HQL_OBTENER_MEDIA_COMUNIDAD, Object[].class);
            return query.list();
        }
    }

    public List<Object[]> obtenerMediaImpactoPorCategoria(int idUsuario) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Object[]> query = session.createQuery(HQL_OBTENER_MEDIA_POR_CATEGORIA, Object[].class);
            query.setParameter("idUsuario", idUsuario);
            return query.list();
        }
    }

    public List<Object[]> obtenerTop3Actividades(int idUsuario) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Object[]> query = session.createQuery(HQL_OBTENER_TOP3_ACTIVIDADES, Object[].class);
            query.setParameter("idUsuario", idUsuario);
            query.setMaxResults(3);

            return query.list();
        }
    }

    /**
     * Obtiene el impacto por categoría pero FILTRADO por un rango de fechas.
     */
    public List<Object[]> obtenerImpactoPorCategoriaYRango(int idUsuario, LocalDate inicio, LocalDate fin) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Object[]> query = session.createQuery(HQL_OBTENER_IMPACTO_POR_RANGO_FECHAS_Y_CATEGORIA, Object[].class);
            query.setParameter("idUsuario", idUsuario);
            query.setParameter("inicio", inicio);
            query.setParameter("fin", fin);
            return query.list();
        }
    }

    /**
     * Calcula la media global en una sola consulta.
     * Devuelve: (Suma Total de CO2) / (Total de Usuarios distintos)
     */
    public Double obtenerMediaMensualGlobal(LocalDate inicio, LocalDate fin) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            // 1. Ejecutamos la consulta única que devuelve un array de objetos
            Query<Object[]> query = session.createQuery(HQL_CALCULO_MEDIA_GLOBAL, Object[].class);
            query.setParameter("inicio", inicio);
            query.setParameter("fin", fin);

            // 2. Obtenemos el resultado (Object[0] = SUMA, Object[1] = COUNT)
            Object[] resultado = query.uniqueResult();

            // 3. Validaciones de seguridad para evitar NullPointer
            if (resultado == null) return 0.0;

            Double sumaTotal = (Double) resultado[0];
            Long totalUsuarios = (Long) resultado[1];

            // 4. Validación matemática
            // Si la suma es null (no hay registros) o hay 0 usuarios, devolvemos 0
            if (sumaTotal == null || totalUsuarios == null || totalUsuarios == 0) {
                return 0.0;
            }

            // 5. Devolvemos la media real por persona
            return sumaTotal / totalUsuarios;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
}
