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
            return em.createNamedQuery("DisponibilidadAulaTurno.findByTurno", DisponibilidadAulaTurno.class)
                    .setParameter("idTurno", idTurno)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error al recuperar las aulas configuradas para el turno especificado.", e);
        }
    }
}