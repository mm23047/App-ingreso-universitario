package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AsignacionAulaAspirante;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class AsignacionAulaAspiranteDAO extends IngresoDefaultDataAccess<AsignacionAulaAspirante> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public AsignacionAulaAspiranteDAO() {
        super(AsignacionAulaAspirante.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public long countByAulaAndTurno(UUID idAula, UUID idTurno) {
        if (idAula == null || idTurno == null) {
            throw new IllegalArgumentException("idAula and idTurno must not be null");
        }
        return em.createQuery(
                        "SELECT COUNT(a) FROM AsignacionAulaAspirante a WHERE a.disponibilidad.idAula.id = :idAula AND a.disponibilidad.idTurno.id = :idTurno",
                        Long.class)
                .setParameter("idAula", idAula)
                .setParameter("idTurno", idTurno)
                .getSingleResult();
    }

    public boolean existsByInscripcionAndTurno(UUID idInscripcion, UUID idTurno) {
        if (idInscripcion == null || idTurno == null) {
            throw new IllegalArgumentException("idInscripcion and idTurno must not be null");
        }
        Long count = em.createQuery(
                        "SELECT COUNT(a) FROM AsignacionAulaAspirante a WHERE a.idInscripcion.id = :idInscripcion AND a.disponibilidad.idTurno.id = :idTurno",
                        Long.class)
                .setParameter("idInscripcion", idInscripcion)
                .setParameter("idTurno", idTurno)
                .getSingleResult();
        return count > 0;
    }

    public List<AsignacionAulaAspirante> findByInscripcion(UUID idInscripcion) {
        if (idInscripcion == null) {
            throw new IllegalArgumentException("idInscripcion must not be null");
        }
        return em.createQuery(
                        "SELECT a FROM AsignacionAulaAspirante a WHERE a.idInscripcion.id = :idInscripcion",
                        AsignacionAulaAspirante.class)
                .setParameter("idInscripcion", idInscripcion)
                .getResultList();
    }
}