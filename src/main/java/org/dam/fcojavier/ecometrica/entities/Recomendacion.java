package org.dam.fcojavier.ecometrica.entities;

import jakarta.persistence.*;

import java.util.Objects;

/**
 * Entidad que almacena consejos para reducir la huella de carbono.
 */
@Entity
@Table(name = "recomendacion")
public class Recomendacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recomendacion", nullable = false)
    private Integer id;

    @Lob
    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "impacto_estimado", nullable = false)
    private Double impactoEstimado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria idCategoria;

    // Getters y Setters
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    public Double getImpactoEstimado() {
        return impactoEstimado;
    }
    public void setImpactoEstimado(Double impactoEstimado) {
        this.impactoEstimado = impactoEstimado;
    }
    public Categoria getIdCategoria() {
        return idCategoria;
    }
    public void setIdCategoria(Categoria idCategoria) {
        this.idCategoria = idCategoria;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recomendacion that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}