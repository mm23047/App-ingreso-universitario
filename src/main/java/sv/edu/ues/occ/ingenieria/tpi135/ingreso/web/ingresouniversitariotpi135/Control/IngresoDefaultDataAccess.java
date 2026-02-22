package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;

public abstract class IngresoDefaultDataAccess<T> implements IngresoDAOInterface  {

    private final Class<T> tipoDato;

    public IngresoDefaultDataAccess(Class<T> tipoDato) {
        this.tipoDato = tipoDato;
    }

    // Método abstracto que cada DAO debe implementar
    public abstract EntityManager getEntityManager();

}
