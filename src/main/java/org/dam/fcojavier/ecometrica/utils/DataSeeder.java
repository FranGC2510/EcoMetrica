package org.dam.fcojavier.ecometrica.utils;

import org.dam.fcojavier.ecometrica.dao.ActividadDAO;
import org.dam.fcojavier.ecometrica.dao.CategoriaDAO;
import org.dam.fcojavier.ecometrica.dao.RecomendacionDAO;
import org.dam.fcojavier.ecometrica.dao.UsuarioDAO;
import org.dam.fcojavier.ecometrica.entities.*;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.util.List;

/**
 * Clase encargada de precargar los datos iniciales de la aplicación
 * tal como se especifica en el documento del proyecto.
 */
public class DataSeeder {

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ActividadDAO actividadDAO = new ActividadDAO();
    private final RecomendacionDAO recomendacionDAO = new RecomendacionDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public void sembrarDatos() {
        if (!isBaseDeDatosVacia()) {
            System.out.println(">> DataSeeder: Datos maestros ya presentes. Omitiendo.");
            return;
        }

        System.out.println(">> DataSeeder: Cargando datos maestros...");

        // 1. Categorías
        Categoria transporte = crearCat("Transporte", 0.21, "Km");
        Categoria energia = crearCat("Energía", 0.233, "KWh");
        Categoria alimentacion = crearCat("Alimentación", 2.5, "Kg");
        Categoria residuos = crearCat("Residuos", 0.41, "Kg");
        Categoria agua = crearCat("Agua", 0.35, "m3");

        // 2. Actividades
        crearAct("Conducir coche", transporte);
        crearAct("Usar transporte público", transporte);
        crearAct("Viajar en avión", transporte);
        crearAct("Consumo eléctrico", energia);
        crearAct("Consumo de gas", energia);
        crearAct("Comer carne de res", alimentacion);
        crearAct("Comer alimentos vegetarianos", alimentacion);
        crearAct("Generar residuos domésticos", residuos);
        crearAct("Consumo de agua potable", agua);

        // 3. Recomendaciones
        crearRec("Usa bicicleta o camina en distancias cortas", 30.0, transporte);
        crearRec("Opta por el transporte público en vez del coche", 45.0, transporte);
        crearRec("Apaga dispositivos eléctricos cuando no los uses", 10.0, energia);
        crearRec("Reduce el consumo de carne de res", 50.0, alimentacion);
        crearRec("Recicla residuos para disminuir emisiones", 25.0, residuos);
        crearRec("Reduce el tiempo de ducha", 5.0, agua);

        System.out.println(">> DataSeeder: Proceso finalizado.");
    }

    /**
     * Método específico para sembrar usuarios, huellas y hábitos de prueba.
     * Se ejecuta de forma independiente para no interferir con los datos maestros.
     */
    public void sembrarUsuariosDePrueba() {
        if (usuarioDAO.findByEmail("ana@gmail.com") != null) {
            System.out.println(">> DataSeeder: Usuarios de prueba ya existen. Omitiendo.");
            return;
        }

        System.out.println(">> DataSeeder: Creando usuarios de prueba...");

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Hash para "123"
            String passHash = "$2a$12$R9h/cIPz0gi.URNNXRfx.O83.v.m.Kx.n.L.q.w.z.y.";

            // --- 1. Crear Usuarios ---
            Usuario ana = crearUsuario(session, "Ana García", "ana@gmail.com", passHash, LocalDate.now().minusMonths(6));
            Usuario carlos = crearUsuario(session, "Carlos Ruiz", "carlos@example.com", passHash, LocalDate.now().minusMonths(5));
            Usuario elena = crearUsuario(session, "Elena Torres", "elena@example.com", passHash, LocalDate.now().minusMonths(4));
            Usuario david = crearUsuario(session, "David Mola", "david@example.com", passHash, LocalDate.now().minusMonths(3));

            // --- 2. Recuperar Actividades (Necesarias para Huellas y Hábitos) ---
            // Asumimos que los nombres coinciden con los creados en sembrarDatos()
            Actividad coche = buscarActividad(session, "Conducir coche");
            Actividad bus = buscarActividad(session, "Usar transporte público");
            Actividad electricidad = buscarActividad(session, "Consumo eléctrico");
            Actividad gas = buscarActividad(session, "Consumo de gas");
            Actividad carne = buscarActividad(session, "Comer carne de res");
            Actividad veggie = buscarActividad(session, "Comer alimentos vegetarianos");
            Actividad ducha = buscarActividad(session, "Consumo de agua potable"); // Usamos agua para ducha
            Actividad residuos = buscarActividad(session, "Generar residuos domésticos");

            // --- 3. Crear Huellas (Fechas dinámicas en el último mes) ---
            LocalDate hoy = LocalDate.now();
            
            // Ana
            crearHuella(session, ana, coche, 487.0, "km", hoy.minusDays(2));
            crearHuella(session, ana, electricidad, 780.0, "kWh", hoy.minusDays(5));
            crearHuella(session, ana, carne, 5.0, "kg", hoy.minusDays(10));
            crearHuella(session, ana, ducha, 100.0, "m3", hoy.minusDays(15)); 

            // Carlos
            crearHuella(session, carlos, coche, 687.0, "km", hoy.minusDays(3));
            crearHuella(session, carlos, gas, 50.0, "kWh", hoy.minusDays(12)); 
            crearHuella(session, carlos, carne, 8.0, "kg", hoy.minusDays(18));

            // Elena
            crearHuella(session, elena, bus, 20.0, "km", hoy.minusDays(4));
            crearHuella(session, elena, veggie, 15.0, "kg", hoy.minusDays(7));
            crearHuella(session, elena, electricidad, 276.0, "kWh", hoy.minusDays(14));

            // David
            crearHuella(session, david, electricidad, 350.0, "kWh", hoy.minusDays(6));
            crearHuella(session, david, ducha, 291.0, "m3", hoy.minusDays(9));
            crearHuella(session, david, residuos, 79.0, "kg", hoy.minusDays(22));

            // --- 4. Crear Hábitos (Fechas dinámicas) ---
            // Ana
            crearHabito(session, ana, coche, 5, "semanal", hoy.minusDays(1));
            crearHabito(session, ana, residuos, 1, "semanal", hoy.minusDays(3));

            // Carlos
            crearHabito(session, carlos, coche, 2, "diaria", hoy);
            crearHabito(session, carlos, carne, 4, "semanal", hoy.minusDays(2));

            // Elena
            crearHabito(session, elena, bus, 10, "semanal", hoy.minusDays(4));
            crearHabito(session, elena, veggie, 7, "diaria", hoy);

            // David
            crearHabito(session, david, ducha, 1, "diaria", hoy); 
            crearHabito(session, david, residuos, 1, "diaria", hoy);

            transaction.commit();
            System.out.println(">> DataSeeder: Usuarios de prueba creados correctamente.");

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            System.err.println(">> DataSeeder: Error al crear usuarios de prueba.");
        }
    }

    // --- Métodos Auxiliares para Usuarios ---

    private Usuario crearUsuario(Session session, String nombre, String email, String pass, LocalDate fecha) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setEmail(email);
        u.setContraseña(pass);
        u.setFechaRegistro(fecha);
        session.persist(u);
        return u;
    }

    private Actividad buscarActividad(Session session, String nombre) {
        return session.createQuery("FROM Actividad WHERE nombre = :n", Actividad.class)
                .setParameter("n", nombre)
                .uniqueResult();
    }

    private void crearHuella(Session session, Usuario u, Actividad a, double valor, String unidad, LocalDate fecha) {
        if (a == null) return; // Seguridad por si no existe la actividad
        Huella h = new Huella();
        h.setId_usuario(u);
        h.setId_actividad(a);
        h.setValor(valor);
        h.setUnidad(unidad);
        h.setFecha(fecha);
        session.persist(h);
    }

    private void crearHabito(Session session, Usuario u, Actividad a, int frec, String tipo, LocalDate fecha) {
        if (a == null) return;
        Habito h = new Habito();
        
        // Configurar ID Compuesto
        HabitoId id = new HabitoId();
        id.setIdUsuario(u.getId());
        id.setIdActividad(a.getId());
        h.setId(id);

        h.setUsuario(u);
        h.setActividad(a);
        h.setFrecuencia(frec);
        h.setTipo(tipo);
        h.setUltimaFecha(fecha);
        session.persist(h);
    }

    public void eliminarDatosDeUsuario() {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            System.out.println("⚠️ Iniciando limpieza de datos de usuario...");

            // 1. Desactivar comprobación de claves foráneas (MySQL/MariaDB)
            session.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();

            // 2. Truncar las tablas solicitadas (Orden irrelevante al desactivar FK)
            session.createNativeQuery("TRUNCATE TABLE huella").executeUpdate();
            session.createNativeQuery("TRUNCATE TABLE habito").executeUpdate(); // Asegúrate que la tabla se llama 'habito'
            session.createNativeQuery("TRUNCATE TABLE usuario").executeUpdate();

            // 3. Reactivar comprobación de claves foráneas
            session.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();

            transaction.commit();
            System.out.println("✅ Datos de Usuario, Huella y Hábito eliminados correctamente.");

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            System.err.println("❌ Error al eliminar los datos.");
        }
    }

    private boolean isBaseDeDatosVacia() {
        return categoriaDAO.findAll().isEmpty();
    }

    private Categoria crearCat(String n, double f, String u) {
        Categoria c = new Categoria();
        c.setNombre(n);
        c.setFactorEmision(f);
        c.setUnidad(u);
        categoriaDAO.save(c);
        return c;
    }

    private void crearAct(String n, Categoria c) {
        Actividad a = new Actividad();
        a.setNombre(n);
        a.setCategoria(c);
        actividadDAO.save(a);
    }

    private void crearRec(String d, double i, Categoria c) {
        Recomendacion r = new Recomendacion();
        r.setDescripcion(d);
        r.setImpactoEstimado(i);
        r.setIdCategoria(c);
        recomendacionDAO.save(r);
    }
}
