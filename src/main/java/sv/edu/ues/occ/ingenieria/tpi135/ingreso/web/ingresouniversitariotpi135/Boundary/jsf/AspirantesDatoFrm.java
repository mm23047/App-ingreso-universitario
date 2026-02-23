package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AspirantesDatoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDAOInterface;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.UsuariosSistemaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.UsuariosSistema;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class AspirantesDatoFrm extends DefaultFrm<AspirantesDato> implements Serializable {

    @Inject
    AspirantesDatoDAO aspirantesDatoDAO;

    @Inject
    UsuariosSistemaDAO usuariosSistemaDAO;

    private List<UsuariosSistema> usuariosDisponibles = Collections.emptyList();

    public AspirantesDatoFrm() {
        this.nombreBean = "Datos de Aspirantes";
    }

    /**
     * Carga la lista de usuarios disponibles para el dropdown de {@code idUsuario}.
     * Se invoca automáticamente desde {@code inicializar()} vía la clase base.
     */
    @Override
    public void inicializarListas() {
        try {
            this.usuariosDisponibles = usuariosSistemaDAO.findRange(0, Integer.MAX_VALUE);
        } catch (Exception e) {
            this.usuariosDisponibles = Collections.emptyList();
            Logger.getLogger(AspirantesDatoFrm.class.getName())
                    .log(Level.SEVERE, "Error al cargar usuarios disponibles", e);
        }
    }

    @Override
    protected String getIdAsText(AspirantesDato r) {
        if (r != null && r.getId() != null) {
            return r.getId().toString();
        }
        return null;
    }

    @Override
    protected AspirantesDato getIdByText(String id) {
        if (id != null) {
            try {
                Integer buscado = Integer.parseInt(id);
                return aspirantesDatoDAO.leer(buscado);
            } catch (Exception e) {
                Logger.getLogger(AspirantesDatoFrm.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return null;
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected IngresoDAOInterface<AspirantesDato> getDao() {
        return aspirantesDatoDAO;
    }

    @Override
    protected AspirantesDato nuevoRegistro() {
        return createNewEntity();
    }

    @Override
    protected AspirantesDato buscarRegistroPorId(Object id) {
        if (id != null && aspirantesDatoDAO != null) {
            return aspirantesDatoDAO.leer(id);
        }
        return null;
    }

    @Override
    protected AspirantesDato createNewEntity() {
        AspirantesDato nuevo = new AspirantesDato();
        nuevo.setNombres("");
        nuevo.setApellidos("");
        nuevo.setDui("");
        nuevo.setUsaSillaRuedas(false);
        return nuevo;
    }

    @Override
    protected Object getEntityId(AspirantesDato entity) {
        return entity != null ? entity.getId() : null;
    }

    @Override
    protected String getEntityName() {
        return this.nombreBean;
    }

    @Override
    protected void configurarNuevoRegistro() {
        // La relación idUsuario la elige el usuario desde el dropdown
    }

    // ==================== Lista auxiliar para la vista ====================

    public List<UsuariosSistema> getUsuariosDisponibles() {
        return usuariosDisponibles;
    }
}
