package org.dam.fcojavier.ecometrica.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad que representa a un usuario del sistema EcoMetrica.
 */
@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario", nullable = false)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "`contraseña`", nullable = false)
    private String contraseña;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    @OneToMany(mappedBy = "usuario")
    private Set<Habito> habitos = new LinkedHashSet<>();

    @OneToMany(mappedBy = "id_usuario")
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
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getContraseña() {
        return contraseña;
    }
    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }
    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }
    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
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
        if (!(o instanceof Usuario usuario)) return false;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}