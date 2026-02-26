package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDAOInterface;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.UsuariosSistemaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.UsuariosSistema;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class UsuariosSistemaFrm extends DefaultFrm<UsuariosSistema> implements Serializable {

    @Inject
    UsuariosSistemaDAO usuariosSistemaDAO;

    public UsuariosSistemaFrm() {
        this.nombreBean = "Usuarios del Sistema";
    }

    @Override
    protected String getIdAsText(UsuariosSistema r) {
        if (r != null && r.getId() != null) {
            return r.getId().toString();
        }
        return null;
    }

    @Override
    protected UsuariosSistema getIdByText(String id) {
        if (id != null) {
            try {
                Integer buscado = Integer.parseInt(id);
                return usuariosSistemaDAO.leer(buscado);
            } catch (Exception e) {
                Logger.getLogger(UsuariosSistemaFrm.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return null;
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected IngresoDAOInterface<UsuariosSistema> getDao() {
        return usuariosSistemaDAO;
    }

    @Override
    protected UsuariosSistema nuevoRegistro() {
        return createNewEntity();
    }

    @Override
    protected UsuariosSistema buscarRegistroPorId(Object id) {
        if (id != null && usuariosSistemaDAO != null) {
            return usuariosSistemaDAO.leer(id);
        }
        return null;
    }

    @Override
    protected UsuariosSistema createNewEntity() {
        UsuariosSistema nuevo = new UsuariosSistema();
        nuevo.setNombreUsuario("");
        nuevo.setCorreo("");
        nuevo.setContrasenaHash("");
        nuevo.setRol("");
        return nuevo;
    }

    @Override
    protected Object getEntityId(UsuariosSistema entity) {
        return entity != null ? entity.getId() : null;
    }

    @Override
    protected String getEntityName() {
        return this.nombreBean;
    }

    @Override
    protected void configurarNuevoRegistro() {
        // Sin configuración adicional para UsuariosSistema
    }
}
