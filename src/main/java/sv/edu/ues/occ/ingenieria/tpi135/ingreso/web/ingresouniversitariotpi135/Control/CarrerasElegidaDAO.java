package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegida;

import java.io.Serializable;
import java.util.List;
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
            Long count = em.createNamedQuery("CarrerasElegida.countByInscripcionAndPrioridad", Long.class)
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
            return em.createNamedQuery("CarrerasElegida.findByInscripcionAndCarrera", CarrerasElegida.class)
                    .setParameter("idInscripcion", idInscripcion)
                    .setParameter("idCarrera", idCarrera)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            throw new IllegalStateException("sin acceso a la BD", e);
        }
    }

    // TODO: FASE 1 - MÉTODO DE NEGOCIO FALTANTE DETECTADO
    // Falta un método que devuelva todas las carreras seleccionadas por un aspirante, ordenadas de mayor a menor importancia.
    // Ejemplo: "public List<CarrerasElegida> findByInscripcionOrderByPrioridad(UUID idInscripcion);"
    // Justificación: Durante la fase de selección/calificación, cuando el algoritmo del sistema procesa los puntajes finales
    // obtenidos en 'ExamenRealizado', debe evaluar si la nota del aspirante alcanza para su primera prioridad (prioridad = 1).
    // Si la carrera ya no tiene cupos disponibles, el sistema salta automáticamente a procesar la carrera de prioridad 2.
    // Sin este listado ordenado, es imposible ejecutar el proceso automatizado de asignación de cupos.
}