package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AsignacionAulaAspirante;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.DisponibilidadAulaTurno;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;
@Stateless
@LocalBean
public class AsignacionAulaAspiranteDAO extends IngresoDefaultDataAccess<AsignacionAulaAspirante> implements Serializable {
    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public AsignacionAulaAspiranteDAO() {
        super(AsignacionAulaAspirante.class);
    }

    @Override
    public void crear(AsignacionAulaAspirante entity) {
        if (entity == null || entity.getInscripcionPrueba() == null || entity.getDisponibilidad() == null) {
            throw new IllegalArgumentException("La asignación, la inscripción y la disponibilidad de aula son requeridas.");
        }

        // Asegurar que disponibilidad no venga con relaciones internas nulas desde el controlador REST
        DisponibilidadAulaTurno disp = entity.getDisponibilidad();
        if (disp.getAula() == null || disp.getTurnoExamen() == null) {
            throw new IllegalArgumentException("La disponibilidad debe contener un aula y un turno válidos.");
        }

        UUID idInscripcion = entity.getInscripcionPrueba().getIdInscripcionPrueba();
        UUID idAula = disp.getAula().getIdAula();
        UUID idTurno = disp.getTurnoExamen().getIdTurnoExamen();

        // REGLA 1: Evitar choques de horario del aspirante
        if (existsByInscripcionAndTurno(idInscripcion, idTurno)) {
            throw new IllegalStateException("El aspirante ya cuenta con un aula asignada para este turno de examen.");
        }

        // REGLA 2: Controlar la capacidad física del aula (Overbooking)
        long estudiantesAsignados = countByAulaAndTurno(idAula, idTurno);
        int capacidadMaxima = entity.getDisponibilidad().getAula().getCapacidadFisica();

        if (estudiantesAsignados >= capacidadMaxima) {
            throw new IllegalStateException("No se puede completar la operación: El aula seleccionada ha alcanzado su capacidad máxima (" + capacidadMaxima + ").");
        }

        super.crear(entity);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public long countByAulaAndTurno(UUID idAula, UUID idTurno) {
        if (idAula == null || idTurno == null) {
            throw new IllegalArgumentException("idAula and idTurno must not be null");
        }
        return em.createNamedQuery("AsignacionAulaAspirante.countByAulaAndTurno", Long.class)
                .setParameter("idAula", idAula)
                .setParameter("idTurno", idTurno)
                .getSingleResult();
    }

    public boolean existsByInscripcionAndTurno(UUID idInscripcion, UUID idTurno) {
        if (idInscripcion == null || idTurno == null) {
            throw new IllegalArgumentException("idInscripcion and idTurno must not be null");
        }
        Long count = em.createNamedQuery("AsignacionAulaAspirante.countByInscripcionAndTurno", Long.class)
                .setParameter("idInscripcion", idInscripcion)
                .setParameter("idTurno", idTurno)
                .getSingleResult();
        return count > 0;
    }

    public List<AsignacionAulaAspirante> findByInscripcion(UUID idInscripcion) {
        if (idInscripcion == null) {
            throw new IllegalArgumentException("idInscripcion must not be null");
        }
        return em.createNamedQuery("AsignacionAulaAspirante.findByInscripcion", AsignacionAulaAspirante.class)
                .setParameter("idInscripcion", idInscripcion)
                .getResultList();
    }

    /**
     * Sobrescribimos el método leer para usar JOIN FETCH y evitar LazyInitializationException
     * al consultar asignaciones individuales desde el API REST.
     */
    @Override
    public AsignacionAulaAspirante leer(Object id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        try {
            return em.createNamedQuery("AsignacionAulaAspirante.findByIdConRelaciones", AsignacionAulaAspirante.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
            return null; // Mismo comportamiento que em.find() de la clase padre
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Error al leer registro de AsignacionAulaAspirante con relaciones", ex);
        }
    }


    public List<AsignacionAulaAspirante> findByAulaAndTurno(UUID idAula, UUID idTurno, int first, int max) {
        if (idAula == null || idTurno == null) {
            throw new IllegalArgumentException("El idAula y el idTurno son requeridos.");
        }
        return em.createNamedQuery("AsignacionAulaAspirante.findByAulaAndTurno", AsignacionAulaAspirante.class)
                .setParameter("idAula", idAula)
                .setParameter("idTurno", idTurno)
                .setFirstResult(first) // Delega la paginación a la Base de Datos
                .setMaxResults(max)
                .getResultList();
    }

}