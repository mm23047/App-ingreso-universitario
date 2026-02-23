package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExaman;

import java.io.Serializable;

public class ClavesExamanDAO extends IngresoDefaultDataAccess<ClavesExaman> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public ClavesExamanDAO() {
        super(ClavesExaman.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
