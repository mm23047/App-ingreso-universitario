package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ProcesoAdmisionAspirante;

import java.io.Serializable;

public class ProcesoAdmisionAspiranteDAO extends IngresoDefaultDataAccess<ProcesoAdmisionAspirante> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public ProcesoAdmisionAspiranteDAO() {
        super(ProcesoAdmisionAspirante.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
