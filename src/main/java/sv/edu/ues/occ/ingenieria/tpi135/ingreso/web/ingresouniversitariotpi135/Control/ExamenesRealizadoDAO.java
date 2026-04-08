package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenesRealizado;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class ExamenesRealizadoDAO extends IngresoDefaultDataAccess<ExamenesRealizado> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public ExamenesRealizadoDAO() {
        super(ExamenesRealizado.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<ExamenesRealizado> findByAspiranteId(UUID aspiranteId) {
        if (aspiranteId == null) {
            throw new IllegalArgumentException("aspiranteId must not be null");
        }
        try {
            return em.createNamedQuery("ExamenesRealizado.findByAspiranteId", ExamenesRealizado.class)
                    .setParameter("aspiranteId", aspiranteId)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot access db", e);
        }
    }

    public List<ExamenesRealizado> findByPruebaId(UUID pruebaId) {
        if (pruebaId == null) {
            throw new IllegalArgumentException("pruebaId must not be null");
        }
        try {
            return em.createNamedQuery("ExamenesRealizado.findByPruebaId", ExamenesRealizado.class)
                    .setParameter("pruebaId", pruebaId)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot access db", e);
        }
    }

}
