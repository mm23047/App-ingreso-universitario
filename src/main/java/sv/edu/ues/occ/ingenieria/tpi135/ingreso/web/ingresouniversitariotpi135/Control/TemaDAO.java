package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.Tema;

import java.io.Serializable;

@Stateless
@LocalBean
public class TemaDAO extends IngresoDefaultDataAccess<Tema> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public TemaDAO() {
        super(Tema.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public Tema findByNombreTema(String nombreTema) {
        if (nombreTema == null || nombreTema.isBlank()) {
            throw new IllegalArgumentException("nombreTema must not be null or blank");
        }
        try {
            return em.createQuery(
                            "SELECT t FROM Tema t WHERE t.nombreTema = :nombreTema",
                            Tema.class)
                    .setParameter("nombreTema", nombreTema)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }
}