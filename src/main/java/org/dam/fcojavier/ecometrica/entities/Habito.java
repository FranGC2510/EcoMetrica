package org.dam.fcojavier.ecometrica.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "habito")
public class Habito {
    @EmbeddedId
    private HabitoId id;

    @MapsId("idUsuario")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @MapsId("idActividad")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_actividad", nullable = false)
    private Actividad actividad;

    @Column(name = "frecuencia", nullable = false)
    private Integer frecuencia;

    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    @Column(name = "ultima_fecha", nullable = false)
    private LocalDate ultimaFecha;

    public HabitoId getId() {
        return id;
    }

    public void setId(HabitoId id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario idUsuario) {
        this.usuario = idUsuario;
    }

    public Actividad getActividad() {
        return actividad;
    }

    public void setActividad(Actividad idActividad) {
        this.actividad = idActividad;
    }

    public Integer getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(Integer frecuencia) {
        this.frecuencia = frecuencia;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public LocalDate getUltimaFecha() {
        return ultimaFecha;
    }

    public void setUltimaFecha(LocalDate ultimaFecha) {
        this.ultimaFecha = ultimaFecha;
    }

}