package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.CatalogoCarreraDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDAOInterface;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;

import java.io.Serializable;

@Named("catalogoCarreraFrm")
@ViewScoped
public class CatalogoCarreraFrm extends DefaultFrm<CatalogoCarrera> implements Serializable{

    //DAO con el que vamos a trabajar
    @Inject
    CatalogoCarreraDAO catalogoCarreraDAO;
    @Inject
    FacesContext facesContext;

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected IngresoDAOInterface<CatalogoCarrera> getDao() {
        return catalogoCarreraDAO;
    }

    @Override
    protected CatalogoCarrera nuevoRegistro() {
        return null;
    }

    @Override
    protected CatalogoCarrera buscarRegistroPorId(Object id) {
        if(id != null && catalogoCarreraDAO!=null){
            return catalogoCarreraDAO.leer(id);
        }
        return null;
    }

    @Override
    protected String getIdAsText(CatalogoCarrera r) {
        return "";
    }

    @Override
    protected CatalogoCarrera getIdByText(String id) {
        return null;
    }

    @Override
    protected CatalogoCarrera createNewEntity() {
        return null;
    }

    @Override
    protected Object getEntityId(CatalogoCarrera entity) {
        return null;
    }

    @Override
    protected String getEntityName() {
        return "";
    }


}
