package com.Jehere.biblioteca.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(unique = true)
    private String titulo;

    private String autor;

    private LocalDate fechaPublicacion;  // Campo para la fecha de publicación

    private Idioma idioma;  // Agregamos el campo Idioma

    public Libro() {}

    // Constructor modificado para recibir tanto DatosLibro como Idioma
    public Libro(DatosLibro datosLibro, Idioma idioma) {
        this.titulo = datosLibro.titulo();
        this.autor = datosLibro.autor();
        this.fechaPublicacion = LocalDate.now();  // O una fecha específica si la tienes disponible
        this.idioma = idioma;  // Asignamos el idioma
    }

    @Override
    public String toString() {
        return "Libro{" +
                "titulo='" + titulo + '\'' +
                ", autor='" + autor + '\'' +
                ", fechaPublicacion=" + fechaPublicacion +
                ", idioma=" + idioma +  // También imprimimos el idioma
                '}';
    }

    // Getters y Setters

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
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

    public LocalDate getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDate fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public Idioma getIdioma() {
        return idioma;
    }

    public void setIdioma(Idioma idioma) {
        this.idioma = idioma;
    }
}
