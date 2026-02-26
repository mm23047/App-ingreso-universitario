package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDAOInterface;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PruebasAdmisionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.time.Year;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class PruebasAdmisionFrm extends DefaultFrm<PruebasAdmision> {

    @Inject
    PruebasAdmisionDAO pruebasAdmisionDAO;

    public PruebasAdmisionFrm() {
        this.nombreBean = "Pruebas de Admisión";
    }

    @Override
    protected String getIdAsText(PruebasAdmision r) {
        if (r != null && r.getId() != null) {
            return r.getId().toString();
        }
        return null;
    }

    @Override
    protected PruebasAdmision getIdByText(String id) {
        if (id != null) {
            try {
                Integer buscado = Integer.parseInt(id);
                return pruebasAdmisionDAO.leer(buscado);
            } catch (Exception e) {
                Logger.getLogger(PruebasAdmisionFrm.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return null;
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected IngresoDAOInterface<PruebasAdmision> getDao() {
        return pruebasAdmisionDAO;
    }

    @Override
    protected PruebasAdmision nuevoRegistro() {
        return createNewEntity();
    }

    @Override
    protected PruebasAdmision buscarRegistroPorId(Object id) {
        if (id != null && pruebasAdmisionDAO != null) {
            return pruebasAdmisionDAO.leer(id);
        }
        return null;
    }

    @Override
    protected PruebasAdmision createNewEntity() {
        PruebasAdmision nueva = new PruebasAdmision();
        nueva.setNombrePrueba("");
        nueva.setAnio(Year.now().getValue());
        nueva.setActiva(false);
        return nueva;
    }

    @Override
    protected Object getEntityId(PruebasAdmision entity) {
        return entity != null ? entity.getId() : null;
    }

    @Override
    protected String getEntityName() {
        return this.nombreBean;
    }

    @Override
    protected void configurarNuevoRegistro() {
        // Sin configuración adicional para PruebasAdmision
    }
}
