package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AulasExaman;

import java.io.Serializable;

public class AulasExamanDAO extends IngresoDefaultDataAccess<AulasExaman> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public AulasExamanDAO() {
        super(AulasExaman.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
