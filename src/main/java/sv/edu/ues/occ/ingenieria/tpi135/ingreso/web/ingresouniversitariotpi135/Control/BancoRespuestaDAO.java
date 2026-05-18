package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoRespuesta;

import java.io.Serializable;

@Stateless
@LocalBean
public class BancoRespuestaDAO extends IngresoDefaultDataAccess<BancoRespuesta> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public BancoRespuestaDAO() {
        super(BancoRespuesta.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public BancoRespuesta findByTextoRespuesta(String textoRespuesta) {
        if (textoRespuesta == null || textoRespuesta.isBlank()) {
            throw new IllegalArgumentException("textoRespuesta must not be null or blank");
        }
        try {
            return em.createQuery(
                            "SELECT b FROM BancoRespuesta b WHERE b.textoRespuesta = :textoRespuesta",
                            BancoRespuesta.class)
                    .setParameter("textoRespuesta", textoRespuesta)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }
}