package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import java.util.List;

public interface IngresoDAOInterface<T> {

    void crear(T registro) throws IllegalArgumentException, IllegalStateException;

    void eliminar(T registro) throws IllegalArgumentException, IllegalStateException;

    T actualizar(T registro) throws IllegalArgumentException, IllegalStateException;

    T leer(Object id) throws IllegalArgumentException, IllegalStateException;

    List<T> findRange(int first, int max) throws IllegalArgumentException, IllegalStateException;

    int count() throws IllegalStateException;

}
