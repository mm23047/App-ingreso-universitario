package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.EtapasAdmisionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDAOInterface;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;

import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class EtapasAdmisionFrm extends DefaultFrm<EtapasAdmision> {

    @Inject
    EtapasAdmisionDAO etapasAdmisionDAO;

    public EtapasAdmisionFrm() {
        this.nombreBean = "Etapas de Admisión";
    }

    @Override
    protected String getIdAsText(EtapasAdmision r) {
        if (r != null && r.getId() != null) {
            return r.getId().toString();
        }
        return null;
    }

    @Override
    protected EtapasAdmision getIdByText(String id) {
        if (id != null) {
            try {
                Short buscado = Short.parseShort(id);
                return etapasAdmisionDAO.leer(buscado);
            } catch (Exception e) {
                Logger.getLogger(EtapasAdmisionFrm.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return null;
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected IngresoDAOInterface<EtapasAdmision> getDao() {
        return etapasAdmisionDAO;
    }

    @Override
    protected EtapasAdmision nuevoRegistro() {
        return createNewEntity();
    }

    @Override
    protected EtapasAdmision buscarRegistroPorId(Object id) {
        if (id != null && etapasAdmisionDAO != null) {
            return etapasAdmisionDAO.leer(id);
        }
        return null;
    }

    @Override
    protected EtapasAdmision createNewEntity() {
        EtapasAdmision nueva = new EtapasAdmision();
        nueva.setNombre("");
        nueva.setDescripcion("");
        return nueva;
    }

    @Override
    public void seleccionarRegistro(EtapasAdmision registroSeleccionado) {
        if (registroSeleccionado != null) {
            this.registro = registroSeleccionado;
            this.estado = ESTADO_CRUD.MODIFICAR;
        }
    }

    @Override
    protected Object getEntityId(EtapasAdmision entity) {
        return entity != null ? entity.getId() : null;
    }

    @Override
    protected String getEntityName() {
        return this.nombreBean;
    }

    @Override
    protected void configurarNuevoRegistro() {
        // Configuración específica para EtapasAdmision si es necesaria
    }
}
