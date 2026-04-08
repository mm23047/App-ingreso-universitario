package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class InscripcionesPruebaDAO extends IngresoDefaultDataAccess<InscripcionesPrueba> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public InscripcionesPruebaDAO() {
        super(InscripcionesPrueba.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<InscripcionesPrueba> findByAspiranteId(UUID aspiranteId) {
        if (aspiranteId == null) {
            throw new IllegalArgumentException("aspiranteId must not be null");
        }
        try {
            return em.createNamedQuery("InscripcionesPrueba.findByAspiranteId", InscripcionesPrueba.class)
                    .setParameter("idAspirante", aspiranteId)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot access db", e);
        }
    }

    public List<InscripcionesPrueba> findByPruebaId(UUID pruebaId) {
        if (pruebaId == null) {
            throw new IllegalArgumentException("pruebaId must not be null");
        }
        try {
            return em.createNamedQuery("InscripcionesPrueba.findByPruebaId", InscripcionesPrueba.class)
                    .setParameter("idPrueba", pruebaId)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot access db", e);
        }
    }

}
