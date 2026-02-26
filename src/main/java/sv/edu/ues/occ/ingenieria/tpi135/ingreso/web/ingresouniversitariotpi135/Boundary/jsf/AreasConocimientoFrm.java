package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AreasConocimientoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDAOInterface;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AreasConocimiento;

import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class AreasConocimientoFrm extends DefaultFrm<AreasConocimiento> {

    @Inject
    AreasConocimientoDAO areasConocimientoDAO;

    public AreasConocimientoFrm() {
        this.nombreBean = "Áreas de Conocimiento";
    }

    @Override
    protected String getIdAsText(AreasConocimiento r) {
        if (r != null && r.getId() != null) {
            return r.getId().toString();
        }
        return null;
    }

    @Override
    protected AreasConocimiento getIdByText(String id) {
        if (id != null) {
            try {
                Integer buscado = Integer.parseInt(id);
                return areasConocimientoDAO.leer(buscado);
            } catch (Exception e) {
                Logger.getLogger(AreasConocimientoFrm.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return null;
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected IngresoDAOInterface<AreasConocimiento> getDao() {
        return areasConocimientoDAO;
    }

    @Override
    protected AreasConocimiento nuevoRegistro() {
        return createNewEntity();
    }

    @Override
    protected AreasConocimiento buscarRegistroPorId(Object id) {
        if (id != null && areasConocimientoDAO != null) {
            return areasConocimientoDAO.leer(id);
        }
        return null;
    }

    @Override
    protected AreasConocimiento createNewEntity() {
        AreasConocimiento nueva = new AreasConocimiento();
        nueva.setNombreArea("");
        return nueva;
    }

    @Override
    public void seleccionarRegistro(AreasConocimiento registroSeleccionado) {
        if (registroSeleccionado != null) {
            this.registro = registroSeleccionado;
            this.estado = ESTADO_CRUD.MODIFICAR;
        }
    }

    @Override
    protected Object getEntityId(AreasConocimiento entity) {
        return entity != null ? entity.getId() : null;
    }

    @Override
    protected String getEntityName() {
        return this.nombreBean;
    }

    @Override
    protected void configurarNuevoRegistro() {
        // Sin configuración adicional necesaria para AreasConocimiento
    }
}
