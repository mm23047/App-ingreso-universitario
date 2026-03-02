package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ExamenesRealizadoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDAOInterface;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenesRealizado;

import java.io.Serializable;

@Named("examenesRealizadosFrm")
@ViewScoped
public class ExamenesRealizados extends DefaultFrm<ExamenesRealizado> implements Serializable {
    //DAO para acceder a la BD y hacer CRUD
    @Inject
    ExamenesRealizadoDAO examenesRealizadoDAO;

    // FC para poder saber que esta haciendo el usuario
    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    /**
     * DAO manejado por el padre para poder hacer CRUD
     * @return La entidad actual
     */
    @Override
    protected IngresoDAOInterface<ExamenesRealizado> getDao() {
        return examenesRealizadoDAO;
    }

    /**
     * Creamos una entidad para que el usuario pueda hacer nuevo registro
     * @return entidad a utilizar
     */
    @Override
    protected ExamenesRealizado nuevoRegistro() {
        return createNewEntity();
    }

    /**
     * BUscamos la entidad en la BD para poder hacer CRUD
     * @param id id del registro de la entidad actual
     * @return entidad encontra actual
     */
    @Override
    protected ExamenesRealizado buscarRegistroPorId(Object id) {
        if(id == null && examenesRealizadoDAO!=null){
            return examenesRealizadoDAO.leer(id);
        }
        return null;
    }

    /**
     * COnvertimos el registro de la BD a texto para que sea utilizado el fronENde
     * @param r entidad actual
     * @return ID en string de ExamenesRealizados
     */
    @Override
    protected String getIdAsText(ExamenesRealizado r) {
        if(r!=null && r.getId() != null){
            return r.getId().toString();
        }

        return null;
    }

    /**
     * Convertimos el trxto a objeto para ser utilizado por java
     * @param id del registro que recivimos desde la vista
     * @return el obhjeto para ser utilizado en un CRUD
     */
    @Override
    protected ExamenesRealizado getIdByText(String id) {
        if(id==null && !id.isEmpty()){
            return examenesRealizadoDAO.leer(id);
        }
        return null;
    }

    /**
     * Creamos un registro vacio para que el usuario lo pueda llenar
     * @return nueva entidad actual
     */
    @Override
    protected ExamenesRealizado createNewEntity() {
        return new  ExamenesRealizado();
    }

    /**
     * Entidad actual para que el padre le pueda hacer CRUD
     * @param entity ENtidad actual ExamenesRealizados
     * @return el ID de la entidad actual
     */
    @Override
    protected Object getEntityId(ExamenesRealizado entity) {
        if(entity!=null){
            return entity.getId();
        }
        return null;
    }

    // EL nombre de la entidad que estamos utilizando actualemte
    @Override
    protected String getEntityName() {
        return "Examenes Realizados";
    }
}
