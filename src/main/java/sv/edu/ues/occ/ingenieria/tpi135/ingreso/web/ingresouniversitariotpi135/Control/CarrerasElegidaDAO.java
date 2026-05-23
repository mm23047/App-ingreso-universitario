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

    /**
     * Verifica si el aspirante ya tiene una carrera asignada a un nivel de prioridad específico.
     * * Regla de Negocio: Evita colisiones de prioridad en el formulario (por ejemplo,
     * que un alumno intente enviar dos carreras distintas marcadas como su "Opción 1").
     *
     * @param idInscripcion ID de la inscripción del aspirante.
     * @param prioridad     El nivel de prioridad a evaluar (ej. 1, 2, 3).
     * @return true si esa prioridad ya está ocupada en su lista, false si está libre.
     */
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

    /**
     * Obtiene la "Lista de Deseos" completa del aspirante, ordenada desde su opción más deseada a la menos deseada.
     * * Regla de Negocio: Este método es el motor para el Algoritmo de Asignación Automática.
     * Permite que el sistema evalúe secuencialmente las opciones del alumno (Prioridad 1, 2, 3...)
     * para intentar asignarle el primer cupo disponible según su calificación en la prueba.
     *
     * @param idInscripcion ID de la inscripción del aspirante.
     * @return Lista de entidades CarrerasElegida ordenadas por el campo 'prioridad' de forma ascendente.
     */
    public List<CarrerasElegida> findByInscripcionOrderByPrioridad(UUID idInscripcion) {
        if (idInscripcion == null) {
            throw new IllegalArgumentException("El idInscripcion no puede ser nulo");
        }
        try {
            return em.createNamedQuery("CarrerasElegida.findByInscripcionOrderByPrioridad", CarrerasElegida.class)
                    .setParameter("idInscripcion", idInscripcion)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error al consultar las prioridades en la BD", e);
        }
    }

    /**
     * Busca el registro exacto de una elección de carrera específica de un aspirante.
     * * Uso: Ideal para cargar la entidad completa cuando necesitas modificarla o eliminarla
     * (ej. si el alumno decide cambiar/borrar su opción de carrera antes de la fecha límite).
     *
     * @param idInscripcion ID de la inscripción del aspirante.
     * @param idCarrera     ID de la carrera elegida en el catálogo.
     * @return La entidad CarrerasElegida si fue encontrada, o null si el alumno no ha elegido esa carrera.
     */
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

    /**
     * Verifica si el aspirante ya seleccionó una carrera específica en cualquiera de sus prioridades.
     * * Regla de Negocio: Evita opciones redundantes o tramposas (por ejemplo, que el alumno elija
     * "Medicina" como Prioridad 1 y también intente asegurar "Medicina" como Prioridad 2 para
     * aumentar sus probabilidades erróneamente).
     *
     * @param idInscripcion ID de la inscripción del aspirante.
     * @param idCarrera     ID de la carrera a verificar.
     * @return true si la carrera ya está en su lista de opciones, false si es una carrera nueva para él.
     */
    public boolean existsByInscripcionAndCarrera(UUID idInscripcion, String idCarrera) {
        if (idInscripcion == null || idCarrera == null || idCarrera.isBlank()) {
            throw new IllegalArgumentException("Los parámetros no pueden ser nulos");
        }
        try {
            return !em.createNamedQuery("CarrerasElegida.findByInscripcionAndCarrera", CarrerasElegida.class)
                    .setParameter("idInscripcion", idInscripcion)
                    .setParameter("idCarrera", idCarrera)
                    .getResultList().isEmpty();
        } catch (Exception e) {
            throw new IllegalStateException("Error al verificar duplicidad de carrera", e);
        }
    }
}