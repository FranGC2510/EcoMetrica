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
        if (!isBaseDeDatosVacia()) {
            System.out.println(">> DataSeeder: Datos ya presentes. Omitiendo.");
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
