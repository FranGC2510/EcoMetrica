package org.dam.fcojavier.ecometrica.entities;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Representa una actividad específica vinculada a una categoría de CO2.
 */
@Entity
@Table(name = "actividad")
public class Actividad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_actividad", nullable = false)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @OneToMany(mappedBy = "actividad")
    private Set<Habito> habitos = new LinkedHashSet<>();

    @OneToMany(mappedBy = "id_actividad")
    private Set<Huella> huellas = new LinkedHashSet<>();

    // Getters y Setters
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
    public Categoria getCategoria() {
        return categoria;
    }
    public void setCategoria(Categoria idCategoria) {
        this.categoria = idCategoria;
    }
    public Set<Habito> getHabitos() {
        return habitos;
    }
    public void setHabitos(Set<Habito> habitos) {
        this.habitos = habitos;
    }
    public Set<Huella> getHuellas() {
        return huellas;
    }
    public void setHuellas(Set<Huella> huellas) {
        this.huellas = huellas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Actividad actividad = (Actividad) o;
        return Objects.equals(id, actividad.id);
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