package com.liga.futbol.controller;

import com.liga.futbol.model.Asociacion;
import com.liga.futbol.model.Club;
import com.liga.futbol.repository.AsociacionRepository;
import com.liga.futbol.service.ClubService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/asociaciones")
public class AsociacionController {

    private final AsociacionRepository repository;
    private final ClubService clubService;

    public AsociacionController(AsociacionRepository repository, ClubService clubService) {
        this.repository = repository;
        this.clubService = clubService;
    }

    @GetMapping
    public List<Asociacion> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Asociacion buscar(@PathVariable String id) {
        return repository.findById(id).orElseThrow(() -> noEncontrado(id));
    }

    /** Lado inverso de @ManyToOne: clubes afiliados a la asociacion. */
    @GetMapping("/{id}/clubes")
    public List<Club> clubes(@PathVariable String id) {
        if (!repository.existsById(id)) throw noEncontrado(id);
        return clubService.clubesDeAsociacion(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Asociacion crear(@Valid @RequestBody Asociacion asociacion) {
        asociacion.setId(null);
        return repository.save(asociacion);
    }

    @PutMapping("/{id}")
    public Asociacion actualizar(@PathVariable String id, @Valid @RequestBody Asociacion asociacion) {
        if (!repository.existsById(id)) throw noEncontrado(id);
        asociacion.setId(id);
        return repository.save(asociacion);
    }

    /** Si tiene clubes afiliados responde 409 (como ON DELETE NO ACTION). */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id) {
        if (!repository.existsById(id)) throw noEncontrado(id);
        clubService.validarBorradoAsociacion(id);
        repository.deleteById(id);
    }

    private ResponseStatusException noEncontrado(String id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Asociacion no encontrada: " + id);
    }
}
