package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import java.util.List;

public interface IngresoDAOInterface<T> {

    public void crear(T registro) throws IllegalArgumentException, IllegalAccessException;

    public void eliminar(T registro) throws IllegalArgumentException, IllegalAccessException;

    public T actualizar(T registro) throws IllegalStateException;

    public T leer(Object id) throws IllegalStateException;

    public List<T> findRange(int first, int max) throws IllegalArgumentException;

    public int count() throws IllegalArgumentException;

}
