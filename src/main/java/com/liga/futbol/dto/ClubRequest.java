package com.liga.futbol.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

/**
 * Lo que se envia para crear/actualizar un club: solo los ids de los documentos relacionados.
 *
 * {
 *   "nombre": "Millonarios FC",
 *   "entrenadorId": "...",
 *   "jugadoresIds": ["...", "..."],
 *   "asociacionId": "...",
 *   "competicionesIds": ["...", "..."]
 * }
 */
public class ClubRequest {

    @NotBlank
    private String nombre;

    private String entrenadorId;

    private List<String> jugadoresIds = new ArrayList<>();

    private String asociacionId;

    private List<String> competicionesIds = new ArrayList<>();

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEntrenadorId() { return entrenadorId; }
    public void setEntrenadorId(String entrenadorId) { this.entrenadorId = entrenadorId; }

    public List<String> getJugadoresIds() { return jugadoresIds; }
    public void setJugadoresIds(List<String> jugadoresIds) {
        this.jugadoresIds = jugadoresIds != null ? jugadoresIds : new ArrayList<>();
    }

    public String getAsociacionId() { return asociacionId; }
    public void setAsociacionId(String asociacionId) { this.asociacionId = asociacionId; }

    public List<String> getCompeticionesIds() { return competicionesIds; }
    public void setCompeticionesIds(List<String> competicionesIds) {
        this.competicionesIds = competicionesIds != null ? competicionesIds : new ArrayList<>();
    }
}
