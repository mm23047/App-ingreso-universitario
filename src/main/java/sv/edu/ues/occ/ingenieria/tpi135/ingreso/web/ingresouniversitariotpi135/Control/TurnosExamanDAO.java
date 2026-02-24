package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.TurnosExaman;

import java.io.Serializable;

@Stateless
@LocalBean
public class TurnosExamanDAO extends IngresoDefaultDataAccess<TurnosExaman> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public TurnosExamanDAO() {
        super(TurnosExaman.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
