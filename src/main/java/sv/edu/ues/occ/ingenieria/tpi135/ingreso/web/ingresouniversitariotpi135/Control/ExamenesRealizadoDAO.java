package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenesRealizado;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class ExamenesRealizadoDAO extends IngresoDefaultDataAccess<ExamenesRealizado> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public ExamenesRealizadoDAO() {
        super(ExamenesRealizado.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<ExamenesRealizado> findByAspiranteId(UUID aspiranteId) {
        if (aspiranteId == null) {
            throw new IllegalArgumentException("aspiranteId must not be null");
        }
        try {
            return em.createNamedQuery("ExamenesRealizado.findByAspiranteId", ExamenesRealizado.class)
                    .setParameter("aspiranteId", aspiranteId)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot access db", e);
        }
    }

    public List<ExamenesRealizado> findByPruebaId(UUID pruebaId) {
        if (pruebaId == null) {
            throw new IllegalArgumentException("pruebaId must not be null");
        }
        try {
            return em.createNamedQuery("ExamenesRealizado.findByPruebaId", ExamenesRealizado.class)
                    .setParameter("pruebaId", pruebaId)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot access db", e);
        }
    }

    public ExamenesRealizado calificarExamen(UUID examenId) {
        if (examenId == null) {
            throw new IllegalArgumentException("examenId must not be null");
        }
        try {
            ExamenesRealizado examen = em.find(ExamenesRealizado.class, examenId);
            if (examen == null) {
                return null;
            }

            UUID claveId = examen.getIdClave().getId();

            Long totalPreguntasClave = em.createQuery(
                            "SELECT COUNT(DISTINCT p.id.idPregunta) "
                                    + "FROM PreguntasPorClave p "
                                    + "WHERE p.id.idClave = :idClave",
                            Long.class)
                    .setParameter("idClave", claveId)
                    .getSingleResult();

            Long preguntasCorrectas = em.createQuery(
                            "SELECT COUNT(DISTINCT r.idPregunta.id) "
                                    + "FROM RespuestasExaman r "
                                    + "JOIN r.idOpcionSeleccionada o "
                                    + "WHERE r.idExamen.id = :idExamen "
                                    + "AND r.idPregunta.id IN ("
                                    + "  SELECT p2.id.idPregunta FROM PreguntasPorClave p2 WHERE p2.id.idClave = :idClave"
                                    + ") "
                                    + "AND o.esCorrecta = TRUE "
                                    + "AND o.idPregunta.id = r.idPregunta.id",
                            Long.class)
                    .setParameter("idExamen", examenId)
                    .setParameter("idClave", claveId)
                    .getSingleResult();

            BigDecimal puntajeMaximo = examen.getIdEtapa() != null && examen.getIdEtapa().getPuntajeMaximo() != null
                    ? examen.getIdEtapa().getPuntajeMaximo()
                    : BigDecimal.TEN;

            BigDecimal puntajeCalculado = BigDecimal.ZERO;
            if (totalPreguntasClave != null && totalPreguntasClave > 0) {
                puntajeCalculado = puntajeMaximo
                        .multiply(BigDecimal.valueOf(preguntasCorrectas != null ? preguntasCorrectas : 0L))
                        .divide(BigDecimal.valueOf(totalPreguntasClave), 2, RoundingMode.HALF_UP);
            }

            examen.setPuntajeFinal(puntajeCalculado);

            if (examen.getIdAsignacion() != null && examen.getIdAsignacion().getIdInscripcion() != null) {
                InscripcionesPrueba inscripcion = examen.getIdAsignacion().getIdInscripcion();
                inscripcion.setEstado("CALIFICADO");
                em.merge(inscripcion);
            }

            ExamenesRealizado actualizado = em.merge(examen);
            em.flush();
            em.refresh(actualizado);
            return actualizado;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot access db", e);
        }
    }

}
