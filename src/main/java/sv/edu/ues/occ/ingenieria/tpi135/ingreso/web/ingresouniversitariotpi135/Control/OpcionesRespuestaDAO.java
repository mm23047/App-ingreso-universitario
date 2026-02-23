package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.OpcionesRespuesta;

import java.io.Serializable;

public class OpcionesRespuestaDAO extends IngresoDefaultDataAccess<OpcionesRespuesta> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public OpcionesRespuestaDAO() {
        super(OpcionesRespuesta.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
