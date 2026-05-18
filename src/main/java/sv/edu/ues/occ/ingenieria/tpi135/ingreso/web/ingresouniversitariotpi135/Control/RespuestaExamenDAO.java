package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.RespuestaExamen;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class RespuestaExamenDAO extends IngresoDefaultDataAccess<RespuestaExamen> implements Serializable {

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

    public boolean existsByExamenAndPregunta(UUID examenId, UUID preguntaId) {
        if (examenId == null || preguntaId == null) {
            throw new IllegalArgumentException("examenId and preguntaId must not be null");
        }
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(r) FROM RespuestaExamen r " +
                                    "WHERE r.idExamen.id = :idExamen " +
                                    "AND r.idPreguntaOpcion.idPregunta.id = :idPregunta",
                            Long.class)
                    .setParameter("idExamen", examenId)
                    .setParameter("idPregunta", preguntaId)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot access db", e);
        }
    }
}
