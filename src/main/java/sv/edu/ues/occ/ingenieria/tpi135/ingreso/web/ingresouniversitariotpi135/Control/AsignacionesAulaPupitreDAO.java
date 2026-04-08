package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AsignacionesAulaPupitre;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class AsignacionesAulaPupitreDAO extends IngresoDefaultDataAccess<AsignacionesAulaPupitre> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public AsignacionesAulaPupitreDAO() {
        super(AsignacionesAulaPupitre.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<AsignacionesAulaPupitre> findByInscripcionId(UUID inscripcionId) {
        if (inscripcionId == null) {
            throw new IllegalArgumentException("inscripcionId must not be null");
        }
        try {
            return em.createNamedQuery("AsignacionesAulaPupitre.findByInscripcionId", AsignacionesAulaPupitre.class)
                    .setParameter("idInscripcion", inscripcionId)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot access db", e);
        }
    }

    public List<AsignacionesAulaPupitre> findByAspiranteId(UUID aspiranteId) {
        if (aspiranteId == null) {
            throw new IllegalArgumentException("aspiranteId must not be null");
        }
        try {
            return em.createNamedQuery("AsignacionesAulaPupitre.findByAspiranteId", AsignacionesAulaPupitre.class)
                    .setParameter("idAspirante", aspiranteId)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot access db", e);
        }
    }

}
