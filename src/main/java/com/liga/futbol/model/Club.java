package com.liga.futbol.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Club: es el propietario de todas las relaciones.
 *
 * En MongoDB no hay tablas ni FK. Con @DocumentReference el documento "club"
 * guarda solo el _id del documento relacionado (como si fuera la FK),
 * y Spring Data lo resuelve automaticamente al leer el club.
 *
 * Equivalencias con JPA:
 *   @OneToOne   -> entrenador     (un _id)
 *   @OneToMany  -> jugadores      (arreglo de _id; un jugador solo puede estar en un club)
 *   @ManyToOne  -> asociacion     (un _id; muchos clubes apuntan a la misma asociacion)
 *   @ManyToMany -> competiciones  (arreglo de _id; una competicion puede estar en muchos clubes)
 *
 * Ejemplo de como queda guardado en la coleccion "club":
 * {
 *   "_id": ObjectId("..."),
 *   "nombre": "Millonarios FC",
 *   "entrenador": ObjectId("..."),
 *   "jugadores": [ObjectId("..."), ObjectId("...")],
 *   "asociacion": ObjectId("..."),
 *   "competiciones": [ObjectId("..."), ObjectId("...")]
 * }
 */
@Document(collection = "club")
public class Club {

    @Id
    private String id;

    private String nombre;

    // @OneToOne: un club tiene un solo entrenador y un entrenador pertenece a un solo club
    @DocumentReference
    private Entrenador entrenador;

    // @OneToMany: un club tiene muchos jugadores; cada jugador pertenece a un solo club
    @DocumentReference
    private List<Jugador> jugadores = new ArrayList<>();

    // @ManyToOne: muchos clubes pertenecen a una asociacion
    @DocumentReference
    private Asociacion asociacion;

    // @ManyToMany: un club participa en muchas competiciones y una competicion tiene muchos clubes
    @DocumentReference
    private List<Competicion> competiciones = new ArrayList<>();

    public Club() {
    }

    public Club(String nombre) {
        this.nombre = nombre;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Entrenador getEntrenador() { return entrenador; }
    public void setEntrenador(Entrenador entrenador) { this.entrenador = entrenador; }

    public List<Jugador> getJugadores() { return jugadores; }
    public void setJugadores(List<Jugador> jugadores) {
        this.jugadores = jugadores != null ? jugadores : new ArrayList<>();
    }

    public Asociacion getAsociacion() { return asociacion; }
    public void setAsociacion(Asociacion asociacion) { this.asociacion = asociacion; }

    public List<Competicion> getCompeticiones() { return competiciones; }
    public void setCompeticiones(List<Competicion> competiciones) {
        this.competiciones = competiciones != null ? competiciones : new ArrayList<>();
    }
}
