package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenRealizado;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class ExamenRealizadoDAO extends IngresoDefaultDataAccess<ExamenRealizado> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public ExamenRealizadoDAO() {
        super(ExamenRealizado.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<ExamenRealizado> findByAspiranteId(UUID aspiranteId) {
        if (aspiranteId == null) {
            throw new IllegalArgumentException("aspiranteId must not be null");
        }
        try {
            return em.createNamedQuery("ExamenRealizado.findByAspiranteId", ExamenRealizado.class)
                    .setParameter("aspiranteId", aspiranteId)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot access db", e);
        }
    }

    public List<ExamenRealizado> findByPruebaId(UUID pruebaId) {
        if (pruebaId == null) {
            throw new IllegalArgumentException("pruebaId must not be null");
        }
        try {
            return em.createNamedQuery("ExamenRealizado.findByPruebaId", ExamenRealizado.class)
                    .setParameter("pruebaId", pruebaId)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot access db", e);
        }
    }

    public ExamenRealizado calificarExamen(UUID examenId) {
        if (examenId == null) {
            throw new IllegalArgumentException("examenId must not be null");
        }
        try {
            ExamenRealizado examen = em.find(ExamenRealizado.class, examenId);
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
                            "SELECT COUNT(DISTINCT r.idPreguntaOpcion.idPregunta.id) "
                                    + "FROM RespuestaExamen r "
                                    + "JOIN r.idPreguntaOpcion o "
                                    + "WHERE r.idExamen.id = :idExamen "
                                    + "AND o.esCorrecta = TRUE "
                                    + "AND o.idPregunta.id IN ("
                                    + "  SELECT p2.id.idPregunta FROM PreguntasPorClave p2 WHERE p2.id.idClave = :idClave"
                                    + ")",
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

            if (examen.getIdInscripcion() != null) {
                InscripcionesPrueba inscripcion = examen.getIdInscripcion();
                inscripcion.setEstado("CALIFICADO");
                em.merge(inscripcion);
            }

            ExamenRealizado actualizado = em.merge(examen);
            em.flush();
            em.refresh(actualizado);
            return actualizado;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot access db", e);
        }
    }
}
