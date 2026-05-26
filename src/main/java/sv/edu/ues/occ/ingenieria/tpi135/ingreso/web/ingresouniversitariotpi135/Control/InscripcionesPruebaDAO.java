package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class InscripcionesPruebaDAO extends IngresoDefaultDataAccess<InscripcionesPrueba> implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public InscripcionesPruebaDAO() {
        super(InscripcionesPrueba.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void crear(InscripcionesPrueba entity) {
        validarReglasDeInscripcion(entity);

        // REGLA DE NEGOCIO: Bloqueo preventivo de doble registro activo por aspirante y prueba
        if (existsByAspiranteAndPrueba(entity.getAspiranteDato().getId(), entity.getPruebaAdmision().getIdPruebaAdmision())) {
            throw new IllegalArgumentException("Violación de integridad: El aspirante ya cuenta con una inscripción activa registrada para esta prueba.");
        }
        super.crear(entity);
    }

    @Override
    public InscripcionesPrueba actualizar(InscripcionesPrueba entity) {
        validarReglasDeInscripcion(entity);
        if (entity.getIdInscripcionPrueba() == null) {
            throw new IllegalArgumentException("No se puede actualizar un registro de inscripción carente de ID primario.");
        }

        // REGLA DE NEGOCIO: Impedir colisiones de duplicidad con registros de terceros durante modificaciones
        if (existsByAspiranteAndPruebaExcludingId(entity.getAspiranteDato().getId(), entity.getPruebaAdmision().getIdPruebaAdmision(), entity.getIdInscripcionPrueba())) {
            throw new IllegalArgumentException("Conflicto en actualización: Los cambios asignados colisionan con otra inscripción ya registrada en el sistema.");
        }
        return super.actualizar(entity);
    }

    private void validarReglasDeInscripcion(InscripcionesPrueba entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad de inscripción de pruebas no puede ser nula.");
        }
        if (entity.getAspiranteDato() == null || entity.getAspiranteDato().getId() == null) {
            throw new IllegalArgumentException("Toda inscripción requiere asociar una entidad Aspirante válida.");
        }
        if (entity.getPruebaAdmision() == null || entity.getPruebaAdmision().getIdPruebaAdmision() == null) {
            throw new IllegalArgumentException("Toda inscripción requiere asociar una entidad Prueba de Admisión válida.");
        }
    }

    public List<InscripcionesPrueba> findByAspiranteId(UUID aspiranteId) {
        if (aspiranteId == null) {
            throw new IllegalArgumentException("El identificador del aspirante es obligatorio.");
        }
        try {
            return em.createNamedQuery("InscripcionesPrueba.findByAspiranteId", InscripcionesPrueba.class)
                    .setParameter("idAspirante", aspiranteId)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error de infraestructura al obtener inscripciones por aspirante.", e);
        }
    }

    public List<InscripcionesPrueba> findByPruebaId(UUID pruebaId) {
        if (pruebaId == null) {
            throw new IllegalArgumentException("El identificador de la prueba es obligatorio.");
        }
        try {
            return em.createNamedQuery("InscripcionesPrueba.findByPruebaId", InscripcionesPrueba.class)
                    .setParameter("idPrueba", pruebaId)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error de infraestructura al obtener inscripciones por prueba.", e);
        }
    }

    public boolean existsByAspiranteAndPrueba(UUID aspiranteId, UUID pruebaId) {
        if (aspiranteId == null || pruebaId == null) {
            throw new IllegalArgumentException("Los identificadores de aspirante y prueba son estrictamente mandatorios.");
        }
        try {
            Long count = em.createNamedQuery("InscripcionesPrueba.countByAspiranteAndPrueba", Long.class)
                    .setParameter("idAspirante", aspiranteId)
                    .setParameter("idPrueba", pruebaId)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            throw new IllegalStateException("Error al verificar la existencia previa de la inscripción.", e);
        }
    }

    public boolean existsByAspiranteAndPruebaExcludingId(UUID aspiranteId, UUID pruebaId, UUID excludeId) {
        if (aspiranteId == null || pruebaId == null || excludeId == null) {
            throw new IllegalArgumentException("aspiranteId, pruebaId y excludeId NO deben ser null");
        }
        try {
            Long count = em.createNamedQuery("InscripcionesPrueba.countByAspiranteAndPruebaExcludingId", Long.class)
                    .setParameter("idAspirante", aspiranteId)
                    .setParameter("idPrueba", pruebaId)
                    .setParameter("excludeId", excludeId)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            throw new IllegalStateException("sin acceso a la BD", e);
        }
    }

    /**
     * MÉTODOS DE NEGOCIO (FASE 1 COMPLETADA)
     * Recupera las inscripciones pertenecientes a una prueba filtradas estrictamente bajo un estado operativo uniforme.
     */
    public List<InscripcionesPrueba> findByPruebaAndEstado(UUID idPrueba, String estado) {
        if (idPrueba == null) {
            throw new IllegalArgumentException("El ID de la prueba es requerido para segmentar el listado.");
        }
        if (estado == null || estado.isBlank()) {
            throw new IllegalArgumentException("El estado de búsqueda no puede ser nulo o vacío.");
        }
        try {
            return em.createNamedQuery("InscripcionesPrueba.findByPruebaAndEstado", InscripcionesPrueba.class)
                    .setParameter("idPrueba", idPrueba)
                    .setParameter("estado", estado)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error en la extracción filtrada de matrículas por estado.", e);
        }
    }

    /**
     * Se sobrescribe el método leer del padre para incluir JOIN FETCH
     * y cargar Aspirante y Prueba, previniendo LazyInitializationException en REST.
     */
    @Override
    public InscripcionesPrueba leer(Object id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        try {
            return em.createNamedQuery("InscripcionesPrueba.findByIdConRelaciones", InscripcionesPrueba.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
            return null; // Comportamiento estándar de em.find()
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Error al leer registro de InscripcionesPrueba con relaciones", ex);
        }
    }

    /**
     * Se sobrescribe el método findRange del padre para forzar el uso
     * de la consulta con JOIN FETCH, paginando correctamente los resultados
     * y evitando excepciones de Lazy Loading al serializar a JSON.
     */
    @Override
    public List<InscripcionesPrueba> findRange(int first, int max) {
        if (first < 0 || max < 1) {
            throw new IllegalArgumentException("Límites de paginación inválidos. 'first' debe ser >= 0 y 'max' >= 1.");
        }
        try {
            return em.createNamedQuery("InscripcionesPrueba.findAllConRelaciones", InscripcionesPrueba.class)
                    .setFirstResult(first)   // Equivalente al OFFSET
                    .setMaxResults(max)      // Equivalente al LIMIT
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error al obtener el listado paginado de inscripciones con relaciones.", e);
        }
    }

}