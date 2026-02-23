package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato;

import java.io.Serializable;

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

}
