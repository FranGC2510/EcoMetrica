package org.dam.fcojavier.ecometrica.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "huella")
public class Huella {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_registro", nullable = false)
    private Integer id_registro;

    @Column(name = "valor", nullable = false)
    private Double valor;

    @Column(name = "unidad", nullable = false, length = 20)
    private String unidad;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario id_usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_actividad", nullable = false)
    private Actividad id_actividad;

    public Integer getId_registro() {
        return id_registro;
    }

    public void setId_registro(Integer id) {
        this.id_registro = id;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Usuario getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(Usuario idUsuario) {
        this.id_usuario = idUsuario;
    }

    public Actividad getId_actividad() {
        return id_actividad;
    }

    public void setId_actividad(Actividad idActividad) {
        this.id_actividad = idActividad;
    }

}