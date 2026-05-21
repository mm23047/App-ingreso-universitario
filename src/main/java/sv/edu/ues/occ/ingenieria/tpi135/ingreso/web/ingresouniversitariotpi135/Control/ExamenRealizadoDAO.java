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

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public ExamenRealizadoDAO() {
        super(ExamenRealizado.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void crear(ExamenRealizado entity) {
        validarConsistenciaEntidad(entity);
        super.crear(entity);
    }

    @Override
    public ExamenRealizado actualizar(ExamenRealizado entity) {
        validarConsistenciaEntidad(entity);
        return super.actualizar(entity);
    }

    private void validarConsistenciaEntidad(ExamenRealizado entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad ExamenRealizado no puede ser nula.");
        }
        if (entity.getPuntajeFinal() != null && entity.getPuntajeFinal().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El puntaje final calculado no puede poseer valores negativos.");
        }
    }

    public List<ExamenRealizado> findByAspiranteId(UUID aspiranteId) {
        if (aspiranteId == null) {
            throw new IllegalArgumentException("El ID del aspirante no puede ser nulo.");
        }
        try {
            return em.createNamedQuery("ExamenRealizado.findByAspiranteId", ExamenRealizado.class)
                    .setParameter("aspiranteId", aspiranteId)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error al consultar los exámenes del aspirante.", e);
        }
    }

    public List<ExamenRealizado> findByPruebaId(UUID pruebaId) {
        if (pruebaId == null) {
            throw new IllegalArgumentException("El ID de la prueba no puede ser nulo.");
        }
        try {
            return em.createNamedQuery("ExamenRealizado.findByPruebaId", ExamenRealizado.class)
                    .setParameter("pruebaId", pruebaId)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error al recuperar los exámenes por ID de prueba.", e);
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

            // CORRECCIÓN: Extracción correcta del ID de la clave
            UUID claveId = examen.getClaveExamen().getIdClaveExaman();

            // 1. Llamada al NamedQuery para el total de preguntas
            Long totalPreguntasClave = em.createNamedQuery("ExamenRealizado.countPreguntasByClave", Long.class)
                    .setParameter("idClave", claveId)
                    .getSingleResult();

            // 2. Llamada al NamedQuery para las respuestas correctas
            Long preguntasCorrectas = em.createNamedQuery("ExamenRealizado.countRespuestasCorrectas", Long.class)
                    .setParameter("idExamen", examenId)
                    .setParameter("idClave", claveId)
                    .getSingleResult();

            BigDecimal puntajeMaximo = examen.getEtapaAdmision() != null && examen.getEtapaAdmision().getPuntajeMaximo() != null
                    ? examen.getEtapaAdmision().getPuntajeMaximo()
                    : BigDecimal.TEN;

            BigDecimal puntajeCalculado = BigDecimal.ZERO;
            if (totalPreguntasClave != null && totalPreguntasClave > 0) {
                puntajeCalculado = puntajeMaximo
                        .multiply(BigDecimal.valueOf(preguntasCorrectas != null ? preguntasCorrectas : 0L))
                        .divide(BigDecimal.valueOf(totalPreguntasClave), 2, RoundingMode.HALF_UP);
            }

            examen.setPuntajeFinal(puntajeCalculado);

            if (examen.getInscripcionesPrueba() != null) {
                InscripcionesPrueba inscripcion = examen.getInscripcionesPrueba();
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

    /**
     * MÉTODOS DE NEGOCIO (FASE 1 COMPLETADA)
     * Retorna el listado de calificaciones en orden descendente para la asignación competitiva de plazas físicas.
     */
    public List<ExamenRealizado> findRankingByPruebaAndEtapa(UUID idPrueba, UUID idEtapa, int maxResults) {
        if (idPrueba == null || idEtapa == null) {
            throw new IllegalArgumentException("Los IDs de prueba y etapa son mandatorios para generar la clasificación.");
        }
        if (maxResults < 1) {
            throw new IllegalArgumentException("El límite de registros del ranking debe ser como mínimo 1.");
        }
        try {
            return em.createNamedQuery("ExamenRealizado.findRankingByPruebaAndEtapa", ExamenRealizado.class)
                    .setParameter("idPrueba", idPrueba)
                    .setParameter("idEtapa", idEtapa)
                    .setMaxResults(maxResults)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error al calcular el orden de mérito de los exámenes.", e);
        }
    }

}