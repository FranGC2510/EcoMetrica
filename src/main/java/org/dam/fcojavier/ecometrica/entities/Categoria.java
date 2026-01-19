package org.dam.fcojavier.ecometrica.entities;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "categoria")
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria", nullable = false)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "factor_emision", nullable = false)
    private Double factorEmision;

    @Column(name = "unidad", nullable = false, length = 20)
    private String unidad;

    @OneToMany(mappedBy = "categoria")
    private Set<Actividad> actividades = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idCategoria")
    private Set<Recomendacion> recomendaciones = new LinkedHashSet<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getFactorEmision() {
        return factorEmision;
    }

    public void setFactorEmision(Double factorEmision) {
        this.factorEmision = factorEmision;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public Set<Actividad> getActividades() {
        return actividades;
    }

    public void setActividades(Set<Actividad> actividads) {
        this.actividades = actividads;
    }

    public Set<Recomendacion> getRecomendaciones() {
        return recomendaciones;
    }

    public void setRecomendaciones(Set<Recomendacion> recomendacions) {
        this.recomendaciones = recomendacions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Categoria categoria = (Categoria) o;
        // Comparamos solo por el ID (que es único)
        return Objects.equals(id, categoria.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    @Override
    public String toString() {
        return nombre;
    }
}