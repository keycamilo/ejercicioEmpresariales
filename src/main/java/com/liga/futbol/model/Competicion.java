package com.liga.futbol.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "competicion")
public class Competicion {

    @Id
    private String id;

    @NotBlank(message = "El nombre de la competición es obligatorio")
    private String nombre;

    @NotNull(message = "El monto del premio es obligatorio")
    @Min(value = 0, message = "El monto del premio no puede ser negativo")
    private Integer montoPremio;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    public Competicion() {
    }

    public Competicion(
            String nombre,
            Integer montoPremio,
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        this.nombre = nombre;
        this.montoPremio = montoPremio;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
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

    public Integer getMontoPremio() {
        return montoPremio;
    }

    public void setMontoPremio(Integer montoPremio) {
        this.montoPremio = montoPremio;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }
}