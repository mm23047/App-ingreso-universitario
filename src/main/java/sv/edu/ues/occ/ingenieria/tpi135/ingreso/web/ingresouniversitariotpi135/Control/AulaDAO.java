package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.Aula;

import java.io.Serializable;

@Stateless
@LocalBean
public class AulaDAO extends IngresoDefaultDataAccess<Aula> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public AulaDAO() {
        super(Aula.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public Aula findByCodigoAulaApi(String codigoAulaApi) {
        if (codigoAulaApi == null || codigoAulaApi.isBlank()) {
            throw new IllegalArgumentException("codigoAulaApi must not be null or blank");
        }
        try {
            return em.createQuery(
                            "SELECT a FROM Aula a WHERE a.codigoAulaApi = :codigoAulaApi",
                            Aula.class)
                    .setParameter("codigoAulaApi", codigoAulaApi)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }
}