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
            throw new IllegalArgumentException("aspiranteId NO debe ser null");
        }
        try {
            return em.createNamedQuery("InscripcionesPrueba.findByAspiranteId", InscripcionesPrueba.class)
                    .setParameter("idAspirante", aspiranteId)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("sin acceso a la BD", e);
        }
    }

    public List<InscripcionesPrueba> findByPruebaId(UUID pruebaId) {
        if (pruebaId == null) {
            throw new IllegalArgumentException("pruebaId NO debe ser null");
        }
        try {
            return em.createNamedQuery("InscripcionesPrueba.findByPruebaId", InscripcionesPrueba.class)
                    .setParameter("idPrueba", pruebaId)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("sin acceso a la BD", e);
        }
    }

    public boolean existsByAspiranteAndPrueba(UUID aspiranteId, UUID pruebaId) {
        if (aspiranteId == null || pruebaId == null) {
            throw new IllegalArgumentException("aspiranteId y pruebaId NO deben ser null");
        }
        try {
            Long count = em.createNamedQuery("InscripcionesPrueba.countByAspiranteAndPrueba", Long.class)
                    .setParameter("idAspirante", aspiranteId)
                    .setParameter("idPrueba", pruebaId)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            throw new IllegalStateException("sin acceso a la BD", e);
        }
    }

    public boolean existsByAspiranteAndPruebaExcludingId(UUID aspiranteId, UUID pruebaId, UUID excludeId) {
        if (aspiranteId == null || pruebaId == null || excludeId == null) {
            throw new IllegalArgumentException("aspiranteId, pruebaId y excludeId NO deben ser null");
        }
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(i) FROM InscripcionesPrueba i " +
                                    "WHERE i.idAspirante.id = :idAspirante " +
                                    "AND i.idPrueba.id = :idPrueba " +
                                    "AND i.id <> :excludeId",
                            Long.class)
                    .setParameter("idAspirante", aspiranteId)
                    .setParameter("idPrueba", pruebaId)
                    .setParameter("excludeId", excludeId)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            throw new IllegalStateException("sin acceso a la BD", e);
        }
    }

}
