package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AsignacionesAulaPupitre;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AulasExaman;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class AsignacionesAulaPupitreDAO extends IngresoDefaultDataAccess<AsignacionesAulaPupitre> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public AsignacionesAulaPupitreDAO() {
        super(AsignacionesAulaPupitre.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<AsignacionesAulaPupitre> findByInscripcionId(UUID inscripcionId) {
        if (inscripcionId == null) {
            throw new IllegalArgumentException("inscripcionId must not be null");
        }
        try {
            return em.createNamedQuery("AsignacionesAulaPupitre.findByInscripcionId", AsignacionesAulaPupitre.class)
                    .setParameter("idInscripcion", inscripcionId)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot access db", e);
        }
    }

    public List<AsignacionesAulaPupitre> findByAspiranteId(UUID aspiranteId) {
        if (aspiranteId == null) {
            throw new IllegalArgumentException("aspiranteId must not be null");
        }
        try {
            return em.createNamedQuery("AsignacionesAulaPupitre.findByAspiranteId", AsignacionesAulaPupitre.class)
                    .setParameter("idAspirante", aspiranteId)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot access db", e);
        }
    }

    public AsignacionesAulaPupitre crearConCupo(AsignacionesAulaPupitre entity) {
        if (entity == null || entity.getIdAula() == null || entity.getIdAula().getId() == null) {
            throw new IllegalArgumentException("entity.idAula must not be null");
        }

        try {
            AulasExaman aula = em.find(AulasExaman.class, entity.getIdAula().getId(), LockModeType.PESSIMISTIC_WRITE);
            if (aula == null) {
                throw new IllegalStateException("AULA_NO_ENCONTRADA");
            }

            Integer capacidad = aula.getCapacidad();
            int ocupados = aula.getCuposOcupados() == null ? 0 : aula.getCuposOcupados();

            if (capacidad == null || capacidad < 1) {
                throw new IllegalStateException("AULA_CAPACIDAD_INVALIDA");
            }

            if (ocupados >= capacidad) {
                throw new IllegalStateException("AULA_SIN_CUPO");
            }

            aula.setCuposOcupados(ocupados + 1);
            entity.setIdAula(aula);
            em.persist(entity);
            em.flush();
            return entity;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error creating assignment: " + e.getMessage(), e);
        }
    }

}
