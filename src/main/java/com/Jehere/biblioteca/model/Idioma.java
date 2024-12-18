package com.Jehere.biblioteca.model;

public enum Idioma {


        ESPANOL("es"),
        INGLES("en"),
        FRANCES("fr");

        private String idiomaLibro;
        Idioma (String idiomaLibro){
            this.idiomaLibro = idiomaLibro;
        }

        public static Idioma fromString(String text) {
            for (Idioma idioma : Idioma.values()) {
                if (idioma.idiomaLibro.equalsIgnoreCase(text)) {
                    return idioma;
                }
            }
            throw new IllegalArgumentException("Ningun idioma encontrado: " + text);
        }

    }


