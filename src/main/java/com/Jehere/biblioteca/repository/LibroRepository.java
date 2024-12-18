package com.Jehere.biblioteca.repository;

import com.Jehere.biblioteca.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibroRepository extends JpaRepository<Libro,Long> {
}
