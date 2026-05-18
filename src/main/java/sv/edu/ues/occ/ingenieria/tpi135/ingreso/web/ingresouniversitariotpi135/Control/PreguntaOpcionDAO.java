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

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public PreguntaOpcionDAO() {
        super(PreguntaOpcion.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<PreguntaOpcion> findByPregunta(UUID idPregunta) {
        if (idPregunta == null) {
            throw new IllegalArgumentException("idPregunta must not be null");
        }
        return em.createQuery(
                        "SELECT p FROM PreguntaOpcion p WHERE p.idPregunta.id = :idPregunta ORDER BY p.id",
                        PreguntaOpcion.class)
                .setParameter("idPregunta", idPregunta)
                .getResultList();
    }

    public boolean existsByPreguntaAndRespuesta(UUID idPregunta, UUID idRespuestaGlobal) {
        if (idPregunta == null || idRespuestaGlobal == null) {
            throw new IllegalArgumentException("idPregunta and idRespuestaGlobal must not be null");
        }
        Long count = em.createQuery(
                        "SELECT COUNT(p) FROM PreguntaOpcion p WHERE p.idPregunta.id = :idPregunta AND p.idRespuestaGlobal.id = :idRespuestaGlobal",
                        Long.class)
                .setParameter("idPregunta", idPregunta)
                .setParameter("idRespuestaGlobal", idRespuestaGlobal)
                .getSingleResult();
        return count > 0;
    }
}