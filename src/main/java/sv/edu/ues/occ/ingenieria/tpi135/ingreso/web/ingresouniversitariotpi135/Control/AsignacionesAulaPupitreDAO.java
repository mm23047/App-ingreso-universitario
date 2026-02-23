package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AsignacionesAulaPupitre;

import java.io.Serializable;

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

}
