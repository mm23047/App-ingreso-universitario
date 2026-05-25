package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.DisponibilidadAulaTurno;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class DisponibilidadAulaTurnoDAO extends IngresoDefaultDataAccess<DisponibilidadAulaTurno> implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public DisponibilidadAulaTurnoDAO() {
        super(DisponibilidadAulaTurno.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    /**
     * Se sobrescribe el método leer del padre para incluir los JOIN FETCH
     * de Aula y TurnoExamen, previniendo LazyInitializationException en REST.
     */
    @Override
    public DisponibilidadAulaTurno leer(Object id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        try {
            return em.createNamedQuery("DisponibilidadAulaTurno.findByIdConRelaciones", DisponibilidadAulaTurno.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
            return null; // Replicamos el comportamiento de em.find() de la clase padre
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Error al leer registro de DisponibilidadAulaTurno con relaciones", ex);
        }
    }

    public boolean existsByAulaAndTurno(UUID idAula, UUID idTurno) {
        if (idAula == null || idTurno == null) {
            throw new IllegalArgumentException("idAula and idTurno must not be null");
        }
        Long count = em.createNamedQuery("DisponibilidadAulaTurno.countByAulaAndTurno", Long.class)
                .setParameter("idAula", idAula)
                .setParameter("idTurno", idTurno)
                .getSingleResult();
        return count > 0;
    }

    /**
     * MÉTODOS DE NEGOCIO (FASE 1 COMPLETADA)
     * Obtiene todas las programaciones y asignaciones de aulas asociadas a un turno específico.
     * Permite que el sistema calcule capacidades de aforo de manera inmediata antes de citar aspirantes.
     */
    public List<DisponibilidadAulaTurno> findByTurno(UUID idTurno) {
        if (idTurno == null) {
            throw new IllegalArgumentException("El ID del turno es estrictamente obligatorio para listar la disponibilidad.");
        }
        try {
            // Este método NO necesita cambiar, automáticamente usa el NamedQuery actualizado con los JOIN FETCH
            return em.createNamedQuery("DisponibilidadAulaTurno.findByTurno", DisponibilidadAulaTurno.class)
                    .setParameter("idTurno", idTurno)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error al recuperar las aulas configuradas para el turno especificado.", e);
        }
    }

    public List<DisponibilidadAulaTurno> findFiltrado(UUID idAula, UUID idTurno) {
        try {
            // Construcción de consulta dinámica simplificada
            StringBuilder jpql = new StringBuilder("SELECT d FROM DisponibilidadAulaTurno d JOIN FETCH d.aula JOIN FETCH d.turnoExamen WHERE 1=1");
            if (idAula != null) jpql.append(" AND d.aula.idAula = :idAula");
            if (idTurno != null) jpql.append(" AND d.turnoExamen.idTurnoExamen = :idTurno");

            var query = em.createQuery(jpql.toString(), DisponibilidadAulaTurno.class);
            if (idAula != null) query.setParameter("idAula", idAula);
            if (idTurno != null) query.setParameter("idTurno", idTurno);

            return query.getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error al realizar la consulta filtrada de disponibilidad.", e);
        }
    }

}