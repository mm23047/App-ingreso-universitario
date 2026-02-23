package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.UsuariosSistema;

import java.io.Serializable;

public class UsuariosSistemaDAO extends IngresoDefaultDataAccess<UsuariosSistema> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public UsuariosSistemaDAO() {
        super(UsuariosSistema.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
