package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.RespuestasExaman;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class RespuestasExamanDAO extends IngresoDefaultDataAccess<RespuestasExaman> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public RespuestasExamanDAO() {
        super(RespuestasExaman.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<RespuestasExaman> findByExamenId(UUID examenId) {
        if (examenId == null) {
            throw new IllegalArgumentException("examenId must not be null");
        }
        try {
            return em.createNamedQuery("RespuestasExaman.findByExamenId", RespuestasExaman.class)
                    .setParameter("idExamen", examenId)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot access db", e);
        }
    }

}
