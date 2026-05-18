package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntasPorClave;

import java.io.Serializable;
import java.util.UUID;

@Stateless
@LocalBean
public class PreguntasPorClaveDAO extends IngresoDefaultDataAccess<PreguntasPorClave> implements Serializable {

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
        Long count = em.createQuery(
                        "SELECT COUNT(p) FROM PreguntasPorClave p " +
                                "WHERE p.id.idClave = :idClave AND p.id.idPregunta = :idPregunta",
                        Long.class)
                .setParameter("idClave", idClave)
                .setParameter("idPregunta", idPregunta)
                .getSingleResult();
        return count > 0;
    }

}
