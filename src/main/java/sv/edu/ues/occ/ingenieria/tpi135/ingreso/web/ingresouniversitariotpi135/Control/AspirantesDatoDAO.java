package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.NoResultException;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato;

import java.io.Serializable;

@Stateless
@LocalBean
public class AspirantesDatoDAO extends IngresoDefaultDataAccess<AspirantesDato> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public AspirantesDatoDAO() {
        super(AspirantesDato.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public AspirantesDato findByDui(String dui) {
        if (dui == null || dui.isBlank()) {
            throw new IllegalArgumentException("dui must not be null or blank");
        }
        try {
            return em.createQuery(
                            "SELECT a FROM AspirantesDato a WHERE a.dui = :dui",
                            AspirantesDato.class)
                    .setParameter("dui", dui)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public AspirantesDato findByCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("correo must not be null or blank");
        }
        try {
            return em.createQuery(
                            "SELECT a FROM AspirantesDato a WHERE a.correo = :correo",
                            AspirantesDato.class)
                    .setParameter("correo", correo)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

}
