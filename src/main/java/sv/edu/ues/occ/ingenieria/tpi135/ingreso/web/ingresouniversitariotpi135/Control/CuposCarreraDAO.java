package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CuposCarrera;

import java.io.Serializable;

public class CuposCarreraDAO extends IngresoDefaultDataAccess<CuposCarrera> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public CuposCarreraDAO() {
        super(CuposCarrera.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
