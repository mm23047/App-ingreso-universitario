package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.RespuestaExamen;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class RespuestaExamenDAO extends IngresoDefaultDataAccess<RespuestaExamen> implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public RespuestaExamenDAO() {
        super(RespuestaExamen.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<RespuestaExamen> findByExamenId(UUID examenId) {
        if (examenId == null) {
            throw new IllegalArgumentException("examenId must not be null");
        }
        try {
            return em.createNamedQuery("RespuestaExamen.findByExamenId", RespuestaExamen.class)
                    .setParameter("idExamen", examenId)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot access db", e);
        }
    }

    /**
     * Se sobrescribe el método leer del padre para incluir el JOIN FETCH
     * y prevenir el LazyInitializationException al serializar en REST.
     */
    @Override
    public RespuestaExamen leer(Object id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        try {
            return em.createNamedQuery("RespuestaExamen.findById", RespuestaExamen.class)
                    .setParameter("idRespuestaExamen", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null; // Comportamiento esperado de em.find() si no existe
        } catch (Exception ex) {
            throw new IllegalStateException("Error al leer registro de RespuestaExamen con relaciones", ex);
        }
    }

    public boolean existsByExamenAndPregunta(UUID examenId, UUID preguntaId) {
        if (examenId == null || preguntaId == null) {
            throw new IllegalArgumentException("examenId and preguntaId must not be null");
        }
        try {
            Long count = em.createNamedQuery("RespuestaExamen.countByExamenAndPregunta", Long.class)
                    .setParameter("idExamen", examenId)
                    .setParameter("idPregunta", preguntaId)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot access db", e);
        }
    }


    /**
     * REGLA DE NEGOCIO (FASE DE EJECUCIÓN):
     * Busca la respuesta actual de un estudiante a una pregunta específica.
     * Útil para actualizar (UPDATE) la opción seleccionada si el estudiante cambia de opinión.
     */
    public RespuestaExamen findByExamenAndPregunta(UUID examenId, UUID preguntaId) {
        if (examenId == null || preguntaId == null) {
            throw new IllegalArgumentException("examenId y preguntaId no deben ser nulos");
        }
        try {
            return em.createNamedQuery("RespuestaExamen.findByExamenAndPregunta", RespuestaExamen.class)
                    .setParameter("idExamen", examenId)
                    .setParameter("idPregunta", preguntaId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null; // Si no ha respondido aún, devuelve null de forma controlada
        } catch (Exception e) {
            throw new IllegalStateException("Error al acceder a la base de datos", e);
        }
    }

    /**
     * REGLA DE NEGOCIO (FASE DE EJECUCIÓN):
     * Cuenta el total de respuestas guardadas de un examen.
     * Esencial para validar si el aspirante dejó preguntas en blanco antes de "Finalizar Examen".
     */
    public Long countRespuestasByExamen(UUID examenId) {
        if (examenId == null) {
            throw new IllegalArgumentException("examenId no debe ser nulo");
        }
        try {
            return em.createNamedQuery("RespuestaExamen.countRespuestasByExamen", Long.class)
                    .setParameter("idExamen", examenId)
                    .getSingleResult();
        } catch (Exception e) {
            throw new IllegalStateException("Error al acceder a la base de datos", e);
        }
    }
}