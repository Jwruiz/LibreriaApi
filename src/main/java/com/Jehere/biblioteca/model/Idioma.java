package com.Jehere.biblioteca.model;

public enum Idioma {
    ES,  // Español
    EN,  // Inglés
    FR;  // Francés

    // Método para convertir un string a un valor del enum
    public static Idioma fromString(String str) {
        switch (str.toLowerCase()) {
            case "es":
                return ES;
            case "en":
                return EN;
            case "fr":
                return FR;
            default:
                throw new IllegalArgumentException("Idioma no válido");
        }
    }
}

