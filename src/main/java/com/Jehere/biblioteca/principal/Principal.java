package com.Jehere.biblioteca.principal;

import com.Jehere.biblioteca.model.DatosLibro;
import com.Jehere.biblioteca.model.Idioma;
import com.Jehere.biblioteca.model.Libro;
import com.Jehere.biblioteca.repository.LibroRepository;
import com.Jehere.biblioteca.service.ConsumoApi;
import com.Jehere.biblioteca.service.ConvierteDatos;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoApi consumoApi = new ConsumoApi();
    private final String URL_BASE = "https://gutendex.com/books?search=";

    private ConvierteDatos conversor = new ConvierteDatos();
    private LibroRepository repositorio;
    private List<Libro> libros;

    public Principal(LibroRepository repository) {
        this.repositorio = repository;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                Elije la opción a través del número:
                1 - Buscar libros por título
                2 - Mostrar libros buscados
                3 - Mostrar autores registrados
                4 - Mostrar autores desde una fecha
                5 - Mostrar libros por idioma
                6 - Guardar libros en archivo de texto
                0 - Salir
                """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarLibro();
                    break;
                case 2:
                    mostrarLibrosBuscados();
                    break;
                case 3:
                    mostrarAutoresRegistrados();
                    break;
                case 4:
                    filtrarAutoresPorFecha();
                    break;
                case 5:
                    mostrarLibroPorIdioma();
                    break;
                case 6:
                    guardarLibrosEnArchivo();
                    break;
                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }
    }

    public void buscarLibro() {
        // Pedir el nombre del libro
        System.out.println("Escribe el nombre del libro que deseas buscar");
        var nombreLibro = teclado.nextLine();

        // Verificar si el libro ya está en el archivo
        if (existeLibroEnArchivo(nombreLibro)) {
            System.out.println("El libro ya está registrado en el archivo.");
            return;  // Si el libro ya está, no se realiza la búsqueda en la API
        }

        // Si el libro no está en el archivo, proceder con la búsqueda en la API
        DatosLibro datos = getDatosLibro(nombreLibro);

        if (datos == null) {
            System.out.println("No se pudo obtener los datos del libro.");
            return;  // Si no se obtuvo un libro, no continuamos con el proceso.
        }

        // Asegúrate de que el idioma esté definido (puede venir de una opción del usuario)
        Idioma idiomaSeleccionado = Idioma.ES;  // Aquí seleccionas el idioma adecuado, por ejemplo 'ES'

        // Si los datos son válidos, proceder a crear el libro
        Libro libro = new Libro(datos, idiomaSeleccionado);
        repositorio.save(libro);
        System.out.println("Libro guardado: " + libro.getTitulo());

        // Guardar el libro en el archivo también
        guardarLibroEnArchivo(libro);
    }


    private DatosLibro getDatosLibro(String nombreLibro) {
        String url = URL_BASE + nombreLibro.replace(" ", "%20");
        System.out.println("URL generada: " + url);

        // Obtener la respuesta de la API
        String json = consumoApi.obtenerDatos(url);

        // Verificar si la respuesta es nula o vacía
        if (json == null || json.trim().isEmpty()) {
            System.out.println("La respuesta de la API está vacía o nula.");
            return null;
        }

        try {
            // Parsear el JSON a un objeto que pueda manejar la estructura
            JsonNode rootNode = new ObjectMapper().readTree(json);
            JsonNode resultsNode = rootNode.path("results");

            if (resultsNode.isArray() && resultsNode.size() > 0) {
                JsonNode bookNode = resultsNode.get(0);
                String titulo = bookNode.path("title").asText();
                String autor = bookNode.path("authors").get(0).path("name").asText();

                // Crear un objeto DatosLibro con la información extraída
                return new DatosLibro(titulo, autor);
            } else {
                System.out.println("No se encontraron libros con ese título.");
                return null;
            }
        } catch (Exception e) {
            System.out.println("Error al deserializar el JSON: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private boolean existeLibroEnArchivo(String nombreLibro) {
        File archivo = new File("libros.txt");

        // Verificamos si el archivo existe
        if (!archivo.exists()) {
            return false;  // Si no existe el archivo, significa que no hay libros registrados
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Comprobar si el título del libro se encuentra en el archivo
                if (line.contains("Título: " + nombreLibro)) {
                    return true;  // Si encontramos el título en el archivo, el libro ya existe
                }
            }
        } catch (IOException e) {
            System.out.println("Error al leer el archivo: " + e.getMessage());
        }

        return false;  // Si no encontramos el libro en el archivo
    }

    private void guardarLibroEnArchivo(Libro libro) {
        File archivo = new File("libros.txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo, true))) { // 'true' para agregar al final
            writer.write("Título: " + libro.getTitulo() + "\n");
            writer.write("Autor: " + libro.getAutor() + "\n");
            writer.write("Fecha de Publicación: " + libro.getFechaPublicacion() + "\n");
            writer.write("-----------------\n");  // Separador entre libros
        } catch (IOException e) {
            System.out.println("Ocurrió un error al guardar el libro en el archivo: " + e.getMessage());
        }
    }

    private void mostrarLibrosBuscados() {
        List<Libro> librosEncontrados = repositorio.findAll();

        librosEncontrados.stream()
                .sorted(Comparator.comparing(Libro::getTitulo))
                .forEach(System.out::println);
    }

    private void mostrarAutoresRegistrados() {
        Set<String> autores = new HashSet<>();
        List<Libro> librosEncontrados = repositorio.findAll();

        for (Libro libro : librosEncontrados) {
            String autor = libro.getAutor();
            if (autor != null && !autor.trim().isEmpty()) {
                autores.add(autor);
            }
        }

        if (autores.isEmpty()) {
            System.out.println("No hay autores registrados.");
        } else {
            System.out.println("Autores registrados:");
            autores.forEach(System.out::println);
        }
    }

    private void filtrarAutoresPorFecha() {
        // Pedir las fechas al usuario
        System.out.println("Ingresa la fecha de inicio (formato: yyyy-MM-dd):");
        String fechaInicioStr = teclado.nextLine();
        System.out.println("Ingresa la fecha de fin (formato: yyyy-MM-dd):");
        String fechaFinStr = teclado.nextLine();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate fechaInicio = null;
        LocalDate fechaFin = null;

        try {
            fechaInicio = LocalDate.parse(fechaInicioStr, formatter);
            fechaFin = LocalDate.parse(fechaFinStr, formatter);
        } catch (Exception e) {
            System.out.println("Formato de fecha inválido. Asegúrate de usar el formato yyyy-MM-dd.");
            return;
        }

        List<Libro> librosEncontrados = repositorio.findAll();
        Set<String> autoresFiltrados = new HashSet<>();

        for (Libro libro : librosEncontrados) {
            LocalDate fechaPublicacion = libro.getFechaPublicacion();
            if (fechaPublicacion != null) {
                if ((fechaPublicacion.isAfter(fechaInicio) || fechaPublicacion.isEqual(fechaInicio)) &&
                        (fechaPublicacion.isBefore(fechaFin) || fechaPublicacion.isEqual(fechaFin))) {
                    autoresFiltrados.add(libro.getAutor());
                }
            }
        }

        if (autoresFiltrados.isEmpty()) {
            System.out.println("No se encontraron autores dentro del rango de fechas.");
        } else {
            System.out.println("Autores registrados dentro del rango de fechas:");
            autoresFiltrados.forEach(System.out::println);
        }
    }

    private void mostrarLibroPorIdioma() {
        System.out.println("Ingresa el idioma del libro (es para Español, en para Inglés, fr para Francés):");
        String idiomaStr = teclado.nextLine();

        Idioma idiomaSeleccionado = null;
        try {
            idiomaSeleccionado = Idioma.fromString(String.valueOf(Idioma.valueOf(idiomaStr.toUpperCase())));
        } catch (IllegalArgumentException e) {
            System.out.println("Idioma no válido. Asegúrate de usar 'es', 'en' o 'fr'.");
            return;
        }

        List<Libro> librosEncontrados = repositorio.findAll();
        List<Libro> librosFiltrados = new ArrayList<>();

        for (Libro libro : librosEncontrados) {
            if (libro.getIdioma() == idiomaSeleccionado) {
                librosFiltrados.add(libro);
            }
        }

        if (librosFiltrados.isEmpty()) {
            System.out.println("No se encontraron libros en el idioma seleccionado.");
        } else {
            System.out.println("Libros encontrados en el idioma " + idiomaSeleccionado.name() + ":");
            for (Libro libro : librosFiltrados) {
                System.out.println(libro.getTitulo() + " - " + libro.getAutor());
            }
        }
    }

    public void guardarLibrosEnArchivo() {
        List<Libro> librosEncontrados = repositorio.findAll();
        File archivo = new File("libros.txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
            writer.write("Lista de Libros:\n");
            writer.write("-----------------\n");

            for (Libro libro : librosEncontrados) {
                writer.write("Título: " + libro.getTitulo() + "\n");
                writer.write("Autor: " + libro.getAutor() + "\n");
                writer.write("Fecha de Publicación: " + libro.getFechaPublicacion() + "\n");
                writer.write("-----------------\n");
            }

            System.out.println("Los libros han sido guardados en 'libros.txt'.");
        } catch (IOException e) {
            System.out.println("Ocurrió un error al guardar los libros en el archivo: " + e.getMessage());
        }
    }
}
