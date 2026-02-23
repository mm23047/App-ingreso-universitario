package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;

import java.io.Serializable;

public class EtapasAdmisionDAO extends IngresoDefaultDataAccess<EtapasAdmision> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public EtapasAdmisionDAO() {
        super(EtapasAdmision.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
