package org.dam.fcojavier.ecometrica.utils;

import org.dam.fcojavier.ecometrica.dao.ActividadDAO;
import org.dam.fcojavier.ecometrica.dao.CategoriaDAO;
import org.dam.fcojavier.ecometrica.dao.RecomendacionDAO;
import org.dam.fcojavier.ecometrica.entities.Actividad;
import org.dam.fcojavier.ecometrica.entities.Categoria;
import org.dam.fcojavier.ecometrica.entities.Recomendacion;

import java.util.List;

/**
 * Clase encargada de precargar los datos iniciales de la aplicación
 * tal como se especifica en el documento del proyecto.
 */
public class DataSeeder {

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ActividadDAO actividadDAO = new ActividadDAO();
    private final RecomendacionDAO recomendacionDAO = new RecomendacionDAO();

    public void sembrarDatos() {
        List<Categoria> categoriasExistentes = categoriaDAO.findAll();
        if (!categoriasExistentes.isEmpty()) {
            System.out.println(">> DataSeeder: La base de datos ya contiene datos. No se requiere inicialización.");
            return;
        }

        System.out.println(">> DataSeeder: Base de datos vacía. Iniciando carga de datos maestros...");

        // 1. CREACIÓN DE CATEGORÍAS

        Categoria catTransporte = createCategoria("Transporte", 0.21, "Km");
        Categoria catEnergia = createCategoria("Energía", 0.233, "KWh");
        Categoria catAlimentacion = createCategoria("Alimentación", 2.5, "Kg");
        Categoria catResiduos = createCategoria("Residuos", 0.41, "Kg");
        Categoria catAgua = createCategoria("Agua", 0.35, "m3");

        // 2. CREACIÓN DE ACTIVIDADES

        createActividad("Conducir coche", catTransporte);
        createActividad("Usar transporte público", catTransporte);
        createActividad("Viajar en avión", catTransporte);

        createActividad("Consumo eléctrico", catEnergia);
        createActividad("Consumo de gas", catEnergia);

        createActividad("Comer carne de res", catAlimentacion);
        createActividad("Comer alimentos vegetarianos", catAlimentacion);

        createActividad("Generar residuos domésticos", catResiduos);

        createActividad("Consumo de agua potable", catAgua);

        // 3. CREACIÓN DE RECOMENDACIONES

        createRecomendacion("Usa bicicleta o camina en distancias cortas", 30.0, catTransporte);
        createRecomendacion("Opta por el transporte público en vez del coche", 45.0, catTransporte);
        createRecomendacion("Compartir coche con compañeros reduce emisiones", 20.0, catTransporte);

        createRecomendacion("Apaga dispositivos eléctricos cuando no los uses", 10.0, catEnergia);
        createRecomendacion("Usa bombillas LED en lugar de incandescentes", 15.0, catEnergia);

        createRecomendacion("Reduce el consumo de carne de res y opta por vegetales", 50.0, catAlimentacion);
        createRecomendacion("Compra productos locales y de temporada", 20.0, catAlimentacion);

        createRecomendacion("Recicla residuos para disminuir emisiones", 25.0, catResiduos);
        createRecomendacion("Reduce el uso de plásticos desechables", 10.0, catResiduos);

        createRecomendacion("Reduce el tiempo de ducha y ahorra agua", 5.0, catAgua);

        System.out.println(">> DataSeeder: Carga de datos completada con éxito.");
    }

    // --- Métodos Auxiliares para no repetir código ---

    private Categoria createCategoria(String nombre, double factor, String unidad) {
        Categoria c = new Categoria();
        c.setNombre(nombre);
        c.setFactorEmision(factor);
        c.setUnidad(unidad);
        categoriaDAO.save(c);
        return c; // Devolvemos el objeto persistido (con ID) para usarlo en las relaciones
    }

    private void createActividad(String nombre, Categoria cat) {
        Actividad a = new Actividad();
        a.setNombre(nombre);
        a.setCategoria(cat);
        actividadDAO.save(a);
    }

    private void createRecomendacion(String descripcion, double impacto, Categoria cat) {
        Recomendacion r = new Recomendacion();
        r.setDescripcion(descripcion);
        r.setImpactoEstimado(impacto);
        r.setIdCategoria(cat);
        recomendacionDAO.save(r);
    }
}
