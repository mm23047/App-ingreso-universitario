package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegida;

import java.io.Serializable;
import java.util.UUID;

@Stateless
@LocalBean
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

    public boolean existsByInscripcionAndPrioridad(UUID idInscripcion, Short prioridad) {
        if (idInscripcion == null || prioridad == null) {
            throw new IllegalArgumentException("idInscripcion and prioridad must not be null");
        }
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(c) FROM CarrerasElegida c WHERE c.idInscripcion.id = :idInscripcion AND c.prioridad = :prioridad",
                            Long.class)
                    .setParameter("idInscripcion", idInscripcion)
                    .setParameter("prioridad", prioridad)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            throw new IllegalStateException("sin acceso a la BD", e);
        }
    }

    public CarrerasElegida findByInscripcionAndCarrera(UUID idInscripcion, String idCarrera) {
        if (idInscripcion == null || idCarrera == null || idCarrera.isBlank()) {
            throw new IllegalArgumentException("idInscripcion and idCarrera must not be null");
        }
        try {
            return em.createQuery(
                            "SELECT c FROM CarrerasElegida c WHERE c.idInscripcion.id = :idInscripcion AND c.idCarrera.idCarrera = :idCarrera",
                            CarrerasElegida.class)
                    .setParameter("idInscripcion", idInscripcion)
                    .setParameter("idCarrera", idCarrera)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            throw new IllegalStateException("sin acceso a la BD", e);
        }
    }

}
