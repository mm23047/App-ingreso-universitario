package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.*;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.*;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("cuposCarreraFrm")
@ViewScoped
public class CuposCarreraFrm extends DefaultFrm<CuposCarrera > implements Serializable {

    //DAO para acceder a la BD y hacer CRUD
    @Inject
    CuposCarreraDAO cuposCarreraDAO;

    // INYECTAMOS LOS DAOS DE LAS 3 TABLAS FORÁNEAS (Igual que en ExamenesRealizados)
    @Inject
    PruebasAdmisionDAO pruebasAdmisionDAO;
    @Inject
    CatalogoCarreraDAO catalogoCarrerasDAO;
    @Inject
    EtapasAdmisionDAO etapasAdmisionDAO;

    //CREAMOS LISTAS PARA LLENAR LOS COMBOBOX DEL HTML
    private List<PruebasAdmision> listPruebas;
    private List<CatalogoCarrera> listCarreras;
    private List<EtapasAdmision> listEtapas;

    @Override
    public void inicializarListas() {
        super.inicializarListas();
        if (pruebasAdmisionDAO != null) this.listPruebas = pruebasAdmisionDAO.findRange(0, 100);
        if (catalogoCarrerasDAO != null) this.listCarreras = catalogoCarrerasDAO.findRange(0, 100);
        if (etapasAdmisionDAO != null) this.listEtapas = etapasAdmisionDAO.findRange(0, 100);
    }

    public CuposCarreraFrm() {
        this.nombreBean = "cuposCarreraFrm";
    }

    /**
     * Para poder saber en que html se esta trabajando
     * @return FC
     */
    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    /**
     * DAO que utiliza el padre para hacer CRUD
     * @return DAO de la clase actual
     */
    @Override
    protected IngresoDAOInterface<CuposCarrera> getDao() {
        return cuposCarreraDAO;
    }

    /**
     * Para que el usuario pueda crear un registro vacio
     * @return La entidad a utilizar
     */
    @Override
    protected CuposCarrera nuevoRegistro() {
        return createNewEntity();
    }

    /**
     * BUscamos la entidad en la BD para hacer CRUD
     * @param id de la entidad actual
     * @return entidad encontrada
     */
    @Override
    protected CuposCarrera buscarRegistroPorId(Object id) {
        if (id != null && cuposCarreraDAO != null) {
            return cuposCarreraDAO.leer(id);
        }
        return null;
    }

    /**
     * COnvertimos el ID a String para poder mostrarlo como texto
     * @param r entidad actual de cupos
     * @return ID registro/entity en formato texto
     */
    @Override
    protected String getIdAsText(CuposCarrera r) {
        if (r != null && r.getId() != null) {
            return r.getId().getIdPrueba() + "_" +
                    r.getId().getIdCarrera() + "_" +
                    r.getId().getIdEtapa();
        }
        return null;
    }

    /**
     * COnvertimos el texto recibido desde el html a un tipo objeto para poder ser utilizado por java
     * @param idText del registro recibido desde el frontEnd
     * @return EL objeto para poder utilizarlo en el crud
     */
    @Override
    protected CuposCarrera getIdByText(String idText) {
        if (idText != null && !idText.isEmpty()) {
            try {
                // Separamos el string usando el guion bajo como límite
                String[] partes = idText.split("_");
                if (partes.length == 3) {
                    CuposCarreraId idCompuesto = new CuposCarreraId();
                    idCompuesto.setIdPrueba(UUID.fromString(partes[0]));
                    idCompuesto.setIdCarrera(partes[1]);
                    idCompuesto.setIdEtapa(UUID.fromString(partes[2]));

                    return cuposCarreraDAO.leer(idCompuesto);
                }
            } catch (Exception e) {
                Logger.getLogger(CuposCarreraFrm.class.getName()).log(Level.SEVERE, "Error al reconstruir ID compuesto", e);
                return null;
            }
        }
        return null;
    }

    /**
     * Creamos una entidad para poder llenarlo en el formulario
     * @return la entidad actual
     */
    @Override
    protected CuposCarrera createNewEntity() {
        CuposCarrera nuevo = new CuposCarrera();

        // Inicializamos el objeto ID
        nuevo.setId(new CuposCarreraId());

        // Inicializamos las entidades relacionadas para los ComboBox
        nuevo.setIdPrueba(new PruebasAdmision());
        nuevo.setIdCarrera(new CatalogoCarrera());
        nuevo.setIdEtapa(new EtapasAdmision());

        return nuevo;
    }

    /**
     * ENtidad actual para que el padre pueda hacer CRUD
     * @param entity ENtidad actual en clase
     * @return ID de la entity actual
     */
    @Override
    protected Object getEntityId(CuposCarrera entity) {
        if (entity != null) {
            return entity.getId();
        }
        return null;
    }

    //NOmbre de la entidad utilizada
    @Override
    protected String getEntityName() {
        return this.nombreBean;
    }

    // Getters para las listas
    public List<PruebasAdmision> getListPruebas() { return listPruebas; }
    public List<CatalogoCarrera> getListCarreras() { return listCarreras; }
    public List<EtapasAdmision> getListEtapas() { return listEtapas; }
}
