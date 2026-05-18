package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.DisponibilidadAulaTurno;

import java.io.Serializable;
import java.util.UUID;

@Stateless
@LocalBean
public class DisponibilidadAulaTurnoDAO extends IngresoDefaultDataAccess<DisponibilidadAulaTurno> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public DisponibilidadAulaTurnoDAO() {
        super(DisponibilidadAulaTurno.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public boolean existsByAulaAndTurno(UUID idAula, UUID idTurno) {
        if (idAula == null || idTurno == null) {
            throw new IllegalArgumentException("idAula and idTurno must not be null");
        }
        Long count = em.createQuery(
                        "SELECT COUNT(d) FROM DisponibilidadAulaTurno d WHERE d.idAula.id = :idAula AND d.idTurno.id = :idTurno",
                        Long.class)
                .setParameter("idAula", idAula)
                .setParameter("idTurno", idTurno)
                .getSingleResult();
        return count > 0;
    }
}