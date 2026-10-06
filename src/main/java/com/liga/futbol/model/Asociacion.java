package com.liga.futbol.model;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "asociacion")
public class Asociacion {

    @Id
    private String id;

    @NotBlank(message = "El nombre de la asociación es obligatorio")
    private String nombre;

    private String pais;

    private String presidente;

    public Asociacion() {
    }

    public Asociacion(String nombre, String pais, String presidente) {
        this.nombre = nombre;
        this.pais = pais;
        this.presidente = presidente;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getPresidente() {
        return presidente;
    }

    public void setPresidente(String presidente) {
        this.presidente = presidente;
    }
}