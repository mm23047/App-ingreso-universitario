package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExamen;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class ClavesExamanDAO extends IngresoDefaultDataAccess<ClavesExamen> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public ClavesExamanDAO() {
        super(ClavesExamen.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void crear(ClavesExamen entity) {
        validarCampos(entity);
        if (existsByPruebaAndNombre(entity.getPruebaAdmision().getIdPruebaAdmision(), entity.getNombreClave().trim())) {
            throw new IllegalArgumentException("Ya existe una clave con el nombre '" + entity.getNombreClave() + "' registrada para esta prueba de admisión.");
        }
        super.crear(entity);
    }

    @Override
    public ClavesExamen actualizar(ClavesExamen entity) {
        if (entity == null || entity.getIdClaveExaman() == null) {
            throw new IllegalArgumentException("Entidad no válida para actualización.");
        }
        validarCampos(entity);

        Long duplicados = em.createNamedQuery("ClavesExaman.countByPruebaAndNombreNotId", Long.class)
                .setParameter("idPrueba", entity.getPruebaAdmision().getIdPruebaAdmision())
                .setParameter("nombreClave", entity.getNombreClave().trim())
                .setParameter("idClave", entity.getIdClaveExaman())
                .getSingleResult();

        if (duplicados > 0) {
            throw new IllegalArgumentException("No se puede renombrar. El nombre de clave '" + entity.getNombreClave() + "' ya está ocupado en esta prueba.");
        }
        return super.actualizar(entity);
    }

    private void validarCampos(ClavesExamen entity) {
        if (entity == null || entity.getPruebaAdmision() == null || entity.getNombreClave() == null || entity.getNombreClave().isBlank()) {
            throw new IllegalArgumentException("La prueba asociada y el nombre de la clave son campos estrictamente obligatorios.");
        }
    }

    public List<ClavesExamen> findByPrueba(UUID idPrueba) {
        if (idPrueba == null) {
            throw new IllegalArgumentException("El idPrueba no puede ser nulo");
        }
        return em.createNamedQuery("ClavesExaman.findByPrueba", ClavesExamen.class)
                .setParameter("idPrueba", idPrueba)
                .getResultList();
    }

    /**
     * MÉTODOS DE NEGOCIO (FASE 1 COMPLETADA)
     * Devuelve la cantidad de claves parametrizadas para una prueba específica.
     */
    public long countByPrueba(UUID idPrueba) {
        if (idPrueba == null) {
            throw new IllegalArgumentException("El ID de la prueba no puede ser nulo.");
        }
        return em.createNamedQuery("ClavesExaman.countByPrueba", Long.class)
                .setParameter("idPrueba", idPrueba)
                .getSingleResult();
    }

    /**
     * MÉTODOS DE NEGOCIO (FASE 1 COMPLETADA)
     * Valida la existencia de un nombre de clave específico dentro del contexto de una prueba.
     */
    public boolean existsByPruebaAndNombre(UUID idPrueba, String nombreClave) {
        if (idPrueba == null || nombreClave == null || nombreClave.isBlank()) {
            throw new IllegalArgumentException("El ID de la prueba y el nombre de la clave son requeridos.");
        }
        Long count = em.createNamedQuery("ClavesExaman.countByPruebaAndNombre", Long.class)
                .setParameter("idPrueba", idPrueba)
                .setParameter("nombreClave", nombreClave.trim())
                .getSingleResult();
        return count > 0;
    }
}