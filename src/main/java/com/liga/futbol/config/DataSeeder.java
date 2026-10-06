package com.liga.futbol.config;

import com.liga.futbol.model.*;
import com.liga.futbol.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Carga datos de ejemplo la primera vez que arranca (si la coleccion "club" esta vacia).
 * Se desactiva con app.seed.enabled=false
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final ClubRepository clubRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final JugadorRepository jugadorRepository;
    private final AsociacionRepository asociacionRepository;
    private final CompeticionRepository competicionRepository;

    public DataSeeder(ClubRepository clubRepository,
                      EntrenadorRepository entrenadorRepository,
                      JugadorRepository jugadorRepository,
                      AsociacionRepository asociacionRepository,
                      CompeticionRepository competicionRepository) {
        this.clubRepository = clubRepository;
        this.entrenadorRepository = entrenadorRepository;
        this.jugadorRepository = jugadorRepository;
        this.asociacionRepository = asociacionRepository;
        this.competicionRepository = competicionRepository;
    }

    @Override
    public void run(String... args) {
        if (clubRepository.count() > 0) {
            log.info("Ya hay clubes en la base de datos, no se cargan datos de ejemplo");
            return;
        }

        // @ManyToOne -> una asociacion para varios clubes
        Asociacion dimayor = asociacionRepository.save(
                new Asociacion("Division Mayor del Futbol Colombiano", "Colombia", "Fernando Jaramillo"));

        // @ManyToMany -> competiciones compartidas
        Competicion liga = competicionRepository.save(new Competicion(
                "Liga BetPlay", 3000000, LocalDate.of(2026, 7, 12), LocalDate.of(2026, 12, 20)));
        Competicion copa = competicionRepository.save(new Competicion(
                "Copa BetPlay", 1200000, LocalDate.of(2026, 3, 4), LocalDate.of(2026, 11, 25)));
        Competicion sudamericana = competicionRepository.save(new Competicion(
                "Copa Sudamericana", 6000000, LocalDate.of(2026, 3, 3), LocalDate.of(2026, 11, 21)));

        // @OneToOne -> un entrenador por club
        Entrenador entNacional = entrenadorRepository.save(new Entrenador("Ricardo", "Mendoza", 55, "Colombiana"));
        Entrenador entJunior = entrenadorRepository.save(new Entrenador("Pablo", "Acosta", 44, "Uruguaya"));
        entrenadorRepository.save(new Entrenador("Hernan", "Quintero", 50, "Colombiana")); // libre

        // @OneToMany -> plantilla de cada club
        List<Jugador> jugNacional = jugadorRepository.saveAll(List.of(
                new Jugador("Santiago", "Rios", 1, "Arquero"),
                new Jugador("Daniel", "Cardona", 2, "Lateral"),
                new Jugador("Esteban", "Velez", 8, "Volante"),
                new Jugador("Nicolas", "Arango", 19, "Delantero")));
        List<Jugador> jugJunior = jugadorRepository.saveAll(List.of(
                new Jugador("Alejandro", "Pacheco", 12, "Arquero"),
                new Jugador("Brayan", "Cassiani", 6, "Defensa"),
                new Jugador("Jhon", "Barrios", 10, "Volante"),
                new Jugador("Oscar", "Fontalvo", 7, "Extremo")));
        jugadorRepository.saveAll(List.of(
                new Jugador("Miguel", "Zapata", 14, "Defensa"),
                new Jugador("Tomas", "Restrepo", 21, "Delantero"))); // libres

        Club nacional = new Club("Atletico Nacional");
        nacional.setEntrenador(entNacional);
        nacional.setJugadores(jugNacional);
        nacional.setAsociacion(dimayor);
        nacional.setCompeticiones(List.of(liga, copa, sudamericana));

        Club junior = new Club("Junior de Barranquilla");
        junior.setEntrenador(entJunior);
        junior.setJugadores(jugJunior);
        junior.setAsociacion(dimayor);
        junior.setCompeticiones(List.of(liga, sudamericana));

        clubRepository.saveAll(List.of(nacional, junior));
        log.info("Datos de ejemplo cargados: 2 clubes, 3 entrenadores, 10 jugadores, 1 asociacion, 3 competiciones");
    }
}
