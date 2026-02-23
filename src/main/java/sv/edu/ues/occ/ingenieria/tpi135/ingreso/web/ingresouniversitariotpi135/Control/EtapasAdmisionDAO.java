package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@LocalBean
public class EtapasAdmisionDAO extends IngresoDefaultDataAccess<EtapasAdmision> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public EtapasAdmisionDAO() {
        super(EtapasAdmision.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public EtapasAdmision buscarRegistroPorId(Short id) {
        try {
            if (id != null) {
                return em.find(EtapasAdmision.class, id);
            }
        } catch (Exception ex) {
            Logger.getLogger(EtapasAdmisionDAO.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return null;
    }

}
