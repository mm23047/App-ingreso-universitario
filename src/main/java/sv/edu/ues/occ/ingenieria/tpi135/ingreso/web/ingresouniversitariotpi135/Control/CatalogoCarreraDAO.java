package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;

import java.io.Serializable;

@Stateless
@LocalBean
public class CatalogoCarreraDAO extends IngresoDefaultDataAccess<CatalogoCarrera> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public CatalogoCarreraDAO() {
        super(CatalogoCarrera.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
