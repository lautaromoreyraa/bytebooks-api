package com.bytebooks.api.domain;


import com.bytebooks.api.enumeration.EstadoLibroEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.*;

@AllArgsConstructor
@NoArgsConstructor

@Entity
public class Libro {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36, columnDefinition = "Varchar(36)", updatable = false, nullable = false)
    private UUID id;

    @Column (nullable = false, unique = true)
    private String titulo;

    @Column (nullable = false, length = 100)
    private String autor;

    @Column (length = 1000)
    private String descripcion;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    private String editorial;

    private Date anioPublicacion;

    @Enumerated(EnumType.STRING)
    private EstadoLibroEnum estadoLibro;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getEditorial() {
        return editorial;
    }

    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }

    public Date getAnioPublicacion() {
        return anioPublicacion;
    }

    public void setAnioPublicacion(Date anioPublicacion) {
        this.anioPublicacion = anioPublicacion;
    }

    public EstadoLibroEnum getEstadoLibro() {
        return estadoLibro;
    }

    public void setEstadoLibro(EstadoLibroEnum estadoLibro) {
        this.estadoLibro = estadoLibro;
    }

}
