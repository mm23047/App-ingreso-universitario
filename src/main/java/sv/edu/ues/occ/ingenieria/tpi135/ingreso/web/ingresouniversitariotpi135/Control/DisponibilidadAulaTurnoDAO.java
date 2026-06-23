package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.DisponibilidadAulaTurno;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.DisponibilidadAulaTurnoId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class DisponibilidadAulaTurnoDAO extends IngresoDefaultDataAccess<DisponibilidadAulaTurno> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String PARAM_ID_TURNO = "idTurno";

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
        if (!(id instanceof DisponibilidadAulaTurnoId)) {
            throw new IllegalArgumentException("El id debe ser del tipo DisponibilidadAulaTurnoId");
        }
        DisponibilidadAulaTurnoId idCompuesto = (DisponibilidadAulaTurnoId) id;

        try {
            return em.createNamedQuery("DisponibilidadAulaTurno.findByIdConRelaciones", DisponibilidadAulaTurno.class)
                    .setParameter("idAula", idCompuesto.getIdAula())
                    .setParameter(PARAM_ID_TURNO, idCompuesto.getIdTurno())
                    .getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
            return null;
        }
    }

    public boolean existsByAulaAndTurno(UUID idAula, UUID idTurno) {
        if (idAula == null || idTurno == null) {
            throw new IllegalArgumentException("idAula and idTurno must not be null");
        }
        Long count = em.createNamedQuery("DisponibilidadAulaTurno.countByAulaAndTurno", Long.class)
                .setParameter("idAula", idAula)
                .setParameter(PARAM_ID_TURNO, idTurno)
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
                    .setParameter(PARAM_ID_TURNO, idTurno)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error al recuperar las aulas configuradas para el turno especificado.", e);
        }
    }

    // Tu nuevo método robusto con paginación
    public List<DisponibilidadAulaTurno> findFiltrado(UUID idAula, UUID idTurno, int first, int max) {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<DisponibilidadAulaTurno> cq = cb.createQuery(DisponibilidadAulaTurno.class);
            Root<DisponibilidadAulaTurno> root = cq.from(DisponibilidadAulaTurno.class);

            // 1. Configurar los JOIN FETCH para evitar LazyInitializationException en el Resource
            root.fetch("aula", JoinType.INNER);
            root.fetch("turnoExamen", JoinType.INNER);

            // 2. Construir los filtros dinámicamente
            List<Predicate> predicados = new ArrayList<>();

            if (idAula != null) {
                predicados.add(cb.equal(root.get("aula").get("idAula"), idAula));
            }
            if (idTurno != null) {
                predicados.add(cb.equal(root.get("turnoExamen").get("idTurnoExamen"), idTurno));
            }

            // 3. Aplicar los filtros si existen
            if (!predicados.isEmpty()) {
                cq.where(cb.and(predicados.toArray(new Predicate[0])));
            }

            // 4. Crear el query, aplicar paginación y retornar
            var query = em.createQuery(cq);
            query.setFirstResult(first);
            query.setMaxResults(max);

            return query.getResultList();

        } catch (Exception e) {
            throw new IllegalStateException("Error al realizar la consulta filtrada de disponibilidad mediante Criteria API.", e);
        }
    }
}