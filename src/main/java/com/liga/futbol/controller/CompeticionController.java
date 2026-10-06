package com.liga.futbol.controller;

import com.liga.futbol.model.Club;
import com.liga.futbol.model.Competicion;
import com.liga.futbol.repository.CompeticionRepository;
import com.liga.futbol.service.ClubService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/competiciones")
public class CompeticionController {

    private final CompeticionRepository repository;
    private final ClubService clubService;

    public CompeticionController(CompeticionRepository repository, ClubService clubService) {
        this.repository = repository;
        this.clubService = clubService;
    }

    @GetMapping
    public List<Competicion> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Competicion buscar(@PathVariable String id) {
        return repository.findById(id).orElseThrow(() -> noEncontrado(id));
    }

    /** Lado inverso de @ManyToMany: clubes que participan en la competicion. */
    @GetMapping("/{id}/clubes")
    public List<Club> clubes(@PathVariable String id) {
        if (!repository.existsById(id)) throw noEncontrado(id);
        return clubService.clubesDeCompeticion(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Competicion crear(@Valid @RequestBody Competicion competicion) {
        competicion.setId(null);
        return repository.save(competicion);
    }

    @PutMapping("/{id}")
    public Competicion actualizar(@PathVariable String id, @Valid @RequestBody Competicion competicion) {
        if (!repository.existsById(id)) throw noEncontrado(id);
        competicion.setId(id);
        return repository.save(competicion);
    }

    /** Se borra y se saca de todos los clubes que la tenian. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id) {
        if (!repository.existsById(id)) throw noEncontrado(id);
        clubService.desvincularCompeticion(id);
        repository.deleteById(id);
    }

    private ResponseStatusException noEncontrado(String id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Competicion no encontrada: " + id);
    }
}
