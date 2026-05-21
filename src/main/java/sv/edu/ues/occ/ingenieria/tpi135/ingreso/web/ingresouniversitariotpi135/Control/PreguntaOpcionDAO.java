package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntaOpcion;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class PreguntaOpcionDAO extends IngresoDefaultDataAccess<PreguntaOpcion> implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public PreguntaOpcionDAO() {
        super(PreguntaOpcion.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void crear(PreguntaOpcion entity) {
        validarConsistenciaOpcion(entity);

        // REGLA DE NEGOCIO: Evitar duplicar la misma respuesta en la misma pregunta
        if (existsByPreguntaAndRespuesta(entity.getIdPregunta().getIdBancoPregunta(), entity.getIdRespuestaGlobal().getIdBancoRespuesta())) {
            throw new IllegalArgumentException("Esta respuesta ya está vinculada a la pregunta actual.");
        }

        // REGLA DE NEGOCIO: Validar si ya existe una respuesta correcta para esta pregunta
        if (entity.getEsCorrecta() && !findOpcionesCorrectasByPregunta(entity.getIdPregunta().getIdBancoPregunta()).isEmpty()) {
            throw new IllegalArgumentException("Regla de negocio rota: La pregunta elegida ya posee una opción marcada como correcta.");
        }

        super.crear(entity);
    }

    @Override
    public PreguntaOpcion actualizar(PreguntaOpcion entity) {
        validarConsistenciaOpcion(entity);

        if (entity.getEsCorrecta()) {
            List<PreguntaOpcion> correctas = findOpcionesCorrectasByPregunta(entity.getIdPregunta().getIdBancoPregunta());
            // Si hay una correcta y no es la misma entidad que estamos editando, lanzamos error
            if (!correctas.isEmpty() && !correctas.get(0).getIdPreguntaOpcion().equals(entity.getIdPreguntaOpcion())) {
                throw new IllegalArgumentException("No se puede actualizar; ya existe otra opción correcta asignada a esta pregunta.");
            }
        }

        return super.actualizar(entity);
    }


    private void validarConsistenciaOpcion(PreguntaOpcion entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad PreguntaOpcion no puede ser nula.");
        }
        if (entity.getIdPregunta() == null || entity.getIdPregunta().getIdBancoPregunta() == null) {
            throw new IllegalArgumentException("La opción debe estar obligatoriamente vinculada a una pregunta del banco.");
        }
        if (entity.getIdRespuestaGlobal() == null || entity.getIdRespuestaGlobal().getIdBancoRespuesta() == null) {
            throw new IllegalArgumentException("La opción debe mapear obligatoriamente a una respuesta global.");
        }
    }

    public List<PreguntaOpcion> findByPregunta(UUID idPregunta) {
        if (idPregunta == null) {
            throw new IllegalArgumentException("El ID de la pregunta no puede ser nulo.");
        }
        try {
            return em.createNamedQuery("PreguntaOpcion.findByPregunta", PreguntaOpcion.class)
                    .setParameter("idPregunta", idPregunta)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error al recuperar las opciones de la pregunta.", e);
        }
    }

    public boolean existsByPreguntaAndRespuesta(UUID idPregunta, UUID idRespuestaGlobal) {
        if (idPregunta == null || idRespuestaGlobal == null) {
            throw new IllegalArgumentException("Los IDs de pregunta y respuesta son obligatorios.");
        }
        try {
            Long count = em.createNamedQuery("PreguntaOpcion.countByPreguntaAndRespuesta", Long.class)
                    .setParameter("idPregunta", idPregunta)
                    .setParameter("idRespuestaGlobal", idRespuestaGlobal)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            throw new IllegalStateException("Error al verificar la relación pregunta-respuesta.", e);
        }
    }

    /**
     * MÉTODOS DE NEGOCIO (FASE 1 COMPLETADA)
     * Extrae de forma directa las opciones correctas/llaves de respuesta asignadas a una pregunta del banco.
     */
    public List<PreguntaOpcion> findOpcionesCorrectasByPregunta(UUID idPregunta) {
        if (idPregunta == null) {
            throw new IllegalArgumentException("El ID de la pregunta es mandatorio para extraer la opción correcta.");
        }
        try {
            return em.createNamedQuery("PreguntaOpcion.findOpcionesCorrectasByPregunta", PreguntaOpcion.class)
                    .setParameter("idPregunta", idPregunta)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error de infraestructura al consultar la opción correcta.", e);
        }
    }

}