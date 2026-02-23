package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.RespuestasExaman;

import java.io.Serializable;

public class RespuestasExamanDAO extends IngresoDefaultDataAccess<RespuestasExaman> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public RespuestasExamanDAO() {
        super(RespuestasExaman.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
