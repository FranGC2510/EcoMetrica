package org.dam.fcojavier.ecometrica.entities;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
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

    @OneToMany(mappedBy = "idCategoria")
    private Set<Actividad> actividads = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idCategoria")
    private Set<Recomendacion> recomendacions = new LinkedHashSet<>();

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

    public Set<Actividad> getActividads() {
        return actividads;
    }

    public void setActividads(Set<Actividad> actividads) {
        this.actividads = actividads;
    }

    public Set<Recomendacion> getRecomendacions() {
        return recomendacions;
    }

    public void setRecomendacions(Set<Recomendacion> recomendacions) {
        this.recomendacions = recomendacions;
    }

}