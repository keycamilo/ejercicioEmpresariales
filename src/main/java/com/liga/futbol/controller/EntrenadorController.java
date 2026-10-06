package com.liga.futbol.controller;

import com.liga.futbol.model.Entrenador;
import com.liga.futbol.repository.EntrenadorRepository;
import com.liga.futbol.service.ClubService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/entrenadores")
public class EntrenadorController {

    private final EntrenadorRepository repository;
    private final ClubService clubService;

    public EntrenadorController(EntrenadorRepository repository, ClubService clubService) {
        this.repository = repository;
        this.clubService = clubService;
    }

    @GetMapping
    public List<Entrenador> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Entrenador buscar(@PathVariable String id) {
        return repository.findById(id).orElseThrow(() -> noEncontrado(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Entrenador crear(@Valid @RequestBody Entrenador entrenador) {
        entrenador.setId(null);
        return repository.save(entrenador);
    }

    @PutMapping("/{id}")
    public Entrenador actualizar(@PathVariable String id, @Valid @RequestBody Entrenador entrenador) {
        if (!repository.existsById(id)) throw noEncontrado(id);
        entrenador.setId(id);
        return repository.save(entrenador);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id) {
        if (!repository.existsById(id)) throw noEncontrado(id);
        clubService.desvincularEntrenador(id);
        repository.deleteById(id);
    }

    private ResponseStatusException noEncontrado(String id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrenador no encontrado: " + id);
    }
}
