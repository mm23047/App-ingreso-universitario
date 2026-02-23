package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenesRealizado;

import java.io.Serializable;

public class ExamenesRealizadoDAO extends IngresoDefaultDataAccess<ExamenesRealizado> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public ExamenesRealizadoDAO() {
        super(ExamenesRealizado.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
