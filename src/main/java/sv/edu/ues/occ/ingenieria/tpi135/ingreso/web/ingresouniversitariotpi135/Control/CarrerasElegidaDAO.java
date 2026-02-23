package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegida;

import java.io.Serializable;

public class CarrerasElegidaDAO extends IngresoDefaultDataAccess<CarrerasElegida> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public CarrerasElegidaDAO() {
        super(CarrerasElegida.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
