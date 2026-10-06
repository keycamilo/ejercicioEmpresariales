package com.liga.futbol.controller;

import com.liga.futbol.model.Jugador;
import com.liga.futbol.repository.JugadorRepository;
import com.liga.futbol.service.ClubService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/jugadores")
public class JugadorController {

    private final JugadorRepository repository;
    private final ClubService clubService;

    public JugadorController(JugadorRepository repository, ClubService clubService) {
        this.repository = repository;
        this.clubService = clubService;
    }

    @GetMapping
    public List<Jugador> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Jugador buscar(@PathVariable String id) {
        return repository.findById(id).orElseThrow(() -> noEncontrado(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Jugador crear(@Valid @RequestBody Jugador jugador) {
        jugador.setId(null);
        return repository.save(jugador);
    }

    @PutMapping("/{id}")
    public Jugador actualizar(@PathVariable String id, @Valid @RequestBody Jugador jugador) {
        if (!repository.existsById(id)) throw noEncontrado(id);
        jugador.setId(id);
        return repository.save(jugador);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id) {
        if (!repository.existsById(id)) throw noEncontrado(id);
        clubService.desvincularJugador(id);
        repository.deleteById(id);
    }

    private ResponseStatusException noEncontrado(String id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Jugador no encontrado: " + id);
    }
}
