package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.io.Serializable;

public class PruebasAdmisionDAO extends IngresoDefaultDataAccess<PruebasAdmision> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public PruebasAdmisionDAO() {
        super(PruebasAdmision.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
