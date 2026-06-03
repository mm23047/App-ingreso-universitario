package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.CacheRetrieveMode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExamen;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
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
        UUID idEtapa  = entity.getEtapaAdmision().getIdEtapaAdmision();
        UUID idPrueba = entity.getPruebaAdmision().getIdPruebaAdmision();
        // Usar referencias JPA gestionadas para no meter proxies parciales en el caché L2.
        entity.setEtapaAdmision(em.getReference(EtapasAdmision.class,  idEtapa));
        entity.setPruebaAdmision(em.getReference(PruebasAdmision.class, idPrueba));
        super.crear(entity);
        // Después del flush, EclipseLink puede dejar una EtapasAdmision "hollow" (solo id)
        // en el caché L2. Evictarla obliga a que findByIdWithEtapa la lea siempre desde BD.
        em.getEntityManagerFactory().getCache().evict(EtapasAdmision.class, idEtapa);
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
        // NUEVO: Validar que venga la etapa
        if (entity.getEtapaAdmision() == null || entity.getEtapaAdmision().getIdEtapaAdmision() == null) {
            throw new IllegalArgumentException("La clave debe pertenecer a una etapa de admisión válida.");
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
    // 2. Agrega este nuevo método para uso exclusivo del Resource
    /**
     * Extrae la clave y carga proactivamente su Etapa de Admisión (JOIN FETCH)
     * para evitar LazyInitializationException al consultar sus límites.
     */
    public ClavesExamen findByIdWithEtapa(UUID idClave) {
        // Dos em.find() independientes con BYPASS, ambos dentro de la transacción EJB.
        // Garantiza que EtapasAdmision se carga desde BD y queda inicializada antes de
        // que el método retorne y la entidad sea desanclada del contexto de persistencia.
        Map<String, Object> bypass = Map.of(
                "jakarta.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS);
        ClavesExamen result = em.find(ClavesExamen.class, idClave, bypass);
        if (result == null) return null;
        if (result.getEtapaAdmision() != null) {
            UUID idEtapa = result.getEtapaAdmision().getIdEtapaAdmision();
            EtapasAdmision etapa = em.find(EtapasAdmision.class, idEtapa, bypass);
            result.setEtapaAdmision(etapa);
        }
        return result;
    }
}