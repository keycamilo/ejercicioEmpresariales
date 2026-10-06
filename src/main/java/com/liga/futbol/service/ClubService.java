package com.liga.futbol.service;

import com.liga.futbol.dto.ClubRequest;
import com.liga.futbol.model.*;
import com.liga.futbol.repository.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

/**
 * Logica de las relaciones del Club.
 *
 * MongoDB no tiene "foreign keys" ni ON DELETE CASCADE, asi que las reglas
 * que en JPA/SQL hace la base de datos aqui se hacen en el servicio:
 *  - @OneToOne: un entrenador no puede estar en dos clubes.
 *  - @OneToMany: un jugador no puede estar en dos clubes.
 *  - Borrar un club borra en cascada su entrenador y sus jugadores (como @OnDelete CASCADE).
 *  - Borrar una asociacion con clubes afiliados se rechaza (como "ON DELETE NO ACTION").
 *  - Borrar una competicion la saca de todos los clubes.
 */
@Service
public class ClubService {

    private static final String CLUB = "club";

    private final ClubRepository clubRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final JugadorRepository jugadorRepository;
    private final AsociacionRepository asociacionRepository;
    private final CompeticionRepository competicionRepository;
    private final MongoTemplate mongoTemplate;

    public ClubService(ClubRepository clubRepository,
                       EntrenadorRepository entrenadorRepository,
                       JugadorRepository jugadorRepository,
                       AsociacionRepository asociacionRepository,
                       CompeticionRepository competicionRepository,
                       MongoTemplate mongoTemplate) {
        this.clubRepository = clubRepository;
        this.entrenadorRepository = entrenadorRepository;
        this.jugadorRepository = jugadorRepository;
        this.asociacionRepository = asociacionRepository;
        this.competicionRepository = competicionRepository;
        this.mongoTemplate = mongoTemplate;
    }

    // ---------------------------------------------------------------- CRUD

    public List<Club> listar() {
        return clubRepository.findAll();
    }

    public Club buscar(String id) {
        return clubRepository.findById(id)
                .orElseThrow(() -> noEncontrado("Club", id));
    }

    public Club crear(ClubRequest request) {
        Club club = new Club();
        aplicar(club, request);
        return clubRepository.save(club);
    }

    public Club actualizar(String id, ClubRequest request) {
        Club club = buscar(id);
        aplicar(club, request);
        return clubRepository.save(club);
    }

    /** Borra el club y en cascada su entrenador y sus jugadores. */
    public void eliminar(String id) {
        Club club = buscar(id);
        if (club.getEntrenador() != null) {
            entrenadorRepository.deleteById(club.getEntrenador().getId());
        }
        for (Jugador j : club.getJugadores()) {
            jugadorRepository.deleteById(j.getId());
        }
        clubRepository.deleteById(id);
    }

    // ---------------------------------------------------- @OneToOne entrenador

    public Club asignarEntrenador(String clubId, String entrenadorId) {
        Club club = buscar(clubId);
        club.setEntrenador(resolverEntrenador(entrenadorId, clubId));
        return clubRepository.save(club);
    }

    public Club quitarEntrenador(String clubId) {
        Club club = buscar(clubId);
        club.setEntrenador(null);
        return clubRepository.save(club);
    }

    // ---------------------------------------------------- @OneToMany jugadores

    public Club agregarJugador(String clubId, String jugadorId) {

        Club club = buscar(clubId);

        club.setJugadores(
                new ArrayList<>(club.getJugadores())
        );

        boolean yaEsta = club.getJugadores()
                .stream()
                .anyMatch(j -> j != null && jugadorId.equals(j.getId()));

        if (!yaEsta) {
            club.getJugadores().add(
                    resolverJugador(jugadorId, clubId)
            );
        }

        return clubRepository.save(club);
    }

    public Club quitarJugador(String clubId, String jugadorId) {

        Club club = buscar(clubId);

        boolean existe = club.getJugadores()
                .stream()
                .anyMatch(j -> j != null && jugadorId.equals(j.getId()));

        if (!existe) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "El jugador no está vinculado a este club"
            );
        }

        club.setJugadores(new ArrayList<>(club.getJugadores()));

        club.getJugadores().removeIf(
                j -> j != null && jugadorId.equals(j.getId())
        );

        return clubRepository.save(club);
    }
    // ---------------------------------------------------- @ManyToOne asociacion

    public Club asignarAsociacion(String clubId, String asociacionId) {
        Club club = buscar(clubId);
        club.setAsociacion(resolverAsociacion(asociacionId));
        return clubRepository.save(club);
    }

    /** Lado inverso de @ManyToOne: todos los clubes de una asociacion. */
    public List<Club> clubesDeAsociacion(String asociacionId) {
        return mongoTemplate.find(query(Criteria.where("asociacion").in(refs(asociacionId))), Club.class);
    }

    // ------------------------------------------------- @ManyToMany competiciones

    public Club inscribirEnCompeticion(String clubId, String competicionId) {
        Club club = buscar(clubId);
        club.setCompeticiones(new ArrayList<>(club.getCompeticiones()));
        boolean yaEsta = club.getCompeticiones().stream().anyMatch(c -> c.getId().equals(competicionId));
        if (!yaEsta) {
            club.getCompeticiones().add(resolverCompeticion(competicionId));
        }
        return clubRepository.save(club);
    }

    public Club retirarDeCompeticion(String clubId, String competicionId) {

        Club club = buscar(clubId);

        boolean existe = club.getCompeticiones()
                .stream()
                .anyMatch(c -> c != null && competicionId.equals(c.getId()));

        if (!existe) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "La competición no está vinculada a este club"
            );
        }

        club.setCompeticiones(
                new ArrayList<>(club.getCompeticiones())
        );

        club.getCompeticiones().removeIf(
                c -> c != null && competicionId.equals(c.getId())
        );

        return clubRepository.save(club);
    }

    /** Lado inverso de @ManyToMany: todos los clubes que participan en una competicion. */
    public List<Club> clubesDeCompeticion(String competicionId) {
        return mongoTemplate.find(query(Criteria.where("competiciones").in(refs(competicionId))), Club.class);
    }

    // ---------------------------------- "integridad referencial" al borrar hijos

    /** Al borrar un entrenador se desvincula de su club. */
    public void desvincularEntrenador(String entrenadorId) {
        mongoTemplate.updateMulti(query(Criteria.where("entrenador").in(refs(entrenadorId))),
                new Update().unset("entrenador"), CLUB);
    }

    /** Al borrar un jugador se saca de la plantilla de su club. */
    public void desvincularJugador(String jugadorId) {
        mongoTemplate.updateMulti(new Query(),
                new Update().pullAll("jugadores", refs(jugadorId)), CLUB);
    }

    /** Al borrar una competicion se saca de todos los clubes. */
    public void desvincularCompeticion(String competicionId) {
        mongoTemplate.updateMulti(new Query(),
                new Update().pullAll("competiciones", refs(competicionId)), CLUB);
    }

    /** Equivalente a "ON DELETE NO ACTION": no se puede borrar una asociacion con clubes. */
    public void validarBorradoAsociacion(String asociacionId) {
        if (!clubesDeAsociacion(asociacionId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar la asociacion: tiene clubes afiliados");
        }
    }

    // ----------------------------------------------------------- auxiliares

    private void aplicar(Club club, ClubRequest req) {
        club.setNombre(req.getNombre());

        club.setEntrenador(req.getEntrenadorId() == null || req.getEntrenadorId().isBlank()
                ? null : resolverEntrenador(req.getEntrenadorId(), club.getId()));

        List<Jugador> jugadores = new ArrayList<>();
        for (String jugadorId : req.getJugadoresIds().stream().distinct().toList()) {
            jugadores.add(resolverJugador(jugadorId, club.getId()));
        }
        club.setJugadores(jugadores);

        club.setAsociacion(req.getAsociacionId() == null || req.getAsociacionId().isBlank()
                ? null : resolverAsociacion(req.getAsociacionId()));

        List<Competicion> competiciones = new ArrayList<>();
        for (String competicionId : req.getCompeticionesIds().stream().distinct().toList()) {
            competiciones.add(resolverCompeticion(competicionId));
        }
        club.setCompeticiones(competiciones);
    }

    private Entrenador resolverEntrenador(String entrenadorId, String clubIdActual) {
        Entrenador entrenador = entrenadorRepository.findById(entrenadorId)
                .orElseThrow(() -> noEncontrado("Entrenador", entrenadorId));
        if (existeEnOtroClub("entrenador", entrenadorId, clubIdActual)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El entrenador " + entrenadorId + " ya pertenece a otro club (@OneToOne)");
        }
        return entrenador;
    }

    private Jugador resolverJugador(String jugadorId, String clubIdActual) {
        Jugador jugador = jugadorRepository.findById(jugadorId)
                .orElseThrow(() -> noEncontrado("Jugador", jugadorId));
        if (existeEnOtroClub("jugadores", jugadorId, clubIdActual)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El jugador " + jugadorId + " ya pertenece a otro club (@OneToMany)");
        }
        return jugador;
    }

    private Asociacion resolverAsociacion(String asociacionId) {
        return asociacionRepository.findById(asociacionId)
                .orElseThrow(() -> noEncontrado("Asociacion", asociacionId));
    }

    private Competicion resolverCompeticion(String competicionId) {
        return competicionRepository.findById(competicionId)
                .orElseThrow(() -> noEncontrado("Competicion", competicionId));
    }

    /** Revisa si el documento referenciado ya esta usado por un club distinto al actual. */
    private boolean existeEnOtroClub(String campo, String refId, String clubIdActual) {
        Criteria criteria = Criteria.where(campo).in(refs(refId));
        if (clubIdActual != null) {
            criteria = criteria.and("_id").nin(refs(clubIdActual));
        }
        return mongoTemplate.exists(query(criteria), CLUB);
    }

    /**
     * @DocumentReference guarda el _id como ObjectId. Se busca por ObjectId y por String
     * para que funcione en ambos casos.
     */
    private static Object[] refs(String id) {
        return ObjectId.isValid(id) ? new Object[]{new ObjectId(id), id} : new Object[]{id};
    }

    private static Query query(Criteria criteria) {
        return new Query(criteria);
    }

    private static ResponseStatusException noEncontrado(String entidad, String id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, entidad + " no encontrado: " + id);
    }
}
