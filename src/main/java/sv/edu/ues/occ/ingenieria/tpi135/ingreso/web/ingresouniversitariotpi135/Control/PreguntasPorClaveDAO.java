package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntasPorClave;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class PreguntasPorClaveDAO extends IngresoDefaultDataAccess<PreguntasPorClave> implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public PreguntasPorClaveDAO() {
        super(PreguntasPorClave.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public boolean existsByClaveAndPregunta(UUID idClave, UUID idPregunta) {
        if (idClave == null || idPregunta == null) {
            throw new IllegalArgumentException("idClave and idPregunta must not be null");
        }
        Long count = em.createNamedQuery("PreguntasPorClave.countByClaveAndPregunta", Long.class)
                .setParameter("idClave", idClave)
                .setParameter("idPregunta", idPregunta)
                .getSingleResult();
        return count > 0;
    }

    /**
     * REGLA DE NEGOCIO: Obtiene la estructura completa de preguntas asignadas a un examen específico (Clave).
     * Utiliza un JOIN FETCH implícito en la consulta para cargar las preguntas eficientemente en un solo viaje.
     */
    public List<PreguntasPorClave> findPreguntasByClave(UUID idClave) {
        if (idClave == null) {
            return Collections.emptyList();
        }
        return em.createNamedQuery("PreguntasPorClave.findPreguntasByClave", PreguntasPorClave.class)
                .setParameter("idClave", idClave)
                .getResultList();
    }

    @Override
    public PreguntasPorClave leer(Object id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        try {
            // Nota: Aquí necesitarías crear un NamedQuery llamado "PreguntasPorClave.findById"
            // en tu entidad con su respectivo JOIN FETCH.
            return em.createNamedQuery("PreguntasPorClave.findById", PreguntasPorClave.class)
                    .setParameter("idPreguntaPorClave", id)
                    .getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
            return null;
        } catch (Exception ex) {
            throw new IllegalStateException("Error al leer registro con relaciones", ex);
        }
    }
}