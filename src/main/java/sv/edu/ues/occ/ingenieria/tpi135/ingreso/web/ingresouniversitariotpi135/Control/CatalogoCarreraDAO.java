package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
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

    public CatalogoCarrera findByNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede ser nulo o estar en blanco");
        }
        try {
            return em.createNamedQuery("CatalogoCarrera.findByNombre", CatalogoCarrera.class)
                    .setParameter("nombre", nombre)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

}