package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import static sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.RestHeaders.*;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AspirantesDatoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato;

import java.util.UUID;

/**
 * Recurso REST para la gestión de Datos del Aspirante.
 * Hereda el endpoint GET paginado de AbstractResource.
 * Expone operaciones CRUD completas bajo /resources/v1/aspirantes_datos
 */
@Path("aspirantes_datos")
public class AspirantesDatoResource extends AbstractResource<AspirantesDato> {

    @Inject
    AspirantesDatoDAO aspirantesDatoDAO;

    @QueryParam("dui")
    String duiParam;

    @Override
    protected IngresoDefaultDataAccess<AspirantesDato> getDAO() {
        return aspirantesDatoDAO;
    }

    @Override
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response findRange(
            @DefaultValue("0") @QueryParam("first") int first,
            @DefaultValue("50") @QueryParam("max") int max
    ) {
        if (duiParam != null) {
            if (duiParam.isBlank()) {
                return Response.status(422)
                        .header(MISSING_PARAMETER, "dui")
                        .build();
            }
            try {
                AspirantesDato encontrado = aspirantesDatoDAO.findByDui(duiParam.trim());
                if (encontrado != null) {
                    return Response.ok(encontrado).build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header(NOT_FOUND_ID, "Record with dui " + duiParam + " not found")
                        .build();
            } catch (IllegalArgumentException ex) {
                return Response.status(422)
                        .header(MISSING_PARAMETER, "dui")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }

        return super.findRange(first, max);
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findById(@PathParam("id") UUID id) {
        if (id != null) {
            try {
                AspirantesDato resp = aspirantesDatoDAO.leer(id);
                if (resp != null) {
                    return Response.ok(resp).build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header(NOT_FOUND_ID, "Record with id " + id + " not found")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header(MISSING_PARAMETER, "id")
                .build();
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response create(AspirantesDato entity, @Context UriInfo uriInfo) {
        if (entity != null && entity.getId() == null
                && entity.getNombres() != null
                && entity.getApellidos() != null
                && entity.getDui() != null
                && entity.getCorreo() != null
                && entity.getFechaNacimiento() != null) {
            try {
                if (aspirantesDatoDAO.findByDui(entity.getDui()) != null) {
                    return Response.status(Response.Status.CONFLICT)
                            .header(CONFLICT_REASON, "dui already exists")
                            .build();
                }
                if (aspirantesDatoDAO.findByCorreo(entity.getCorreo()) != null) {
                    return Response.status(Response.Status.CONFLICT)
                            .header(CONFLICT_REASON, "correo already exists")
                            .build();
                }

                AspirantesDato nuevo = new AspirantesDato();
                nuevo.setNombres(entity.getNombres());
                nuevo.setApellidos(entity.getApellidos());
                nuevo.setDui(entity.getDui());
                nuevo.setCorreo(entity.getCorreo());
                nuevo.setFechaNacimiento(entity.getFechaNacimiento());
                nuevo.setFechaCreacionPerfil(entity.getFechaCreacionPerfil());
                nuevo.setUsaSillaRuedas(entity.getUsaSillaRuedas() != null ? entity.getUsaSillaRuedas() : Boolean.FALSE);

                aspirantesDatoDAO.crear(nuevo);
                return Response.created(
                        uriInfo.getAbsolutePathBuilder()
                                .path(String.valueOf(nuevo.getId()))
                                .build())
                        .build();
            } catch (IllegalStateException ex) {
                if (ex.getMessage() != null && ex.getMessage().contains("duplicate")) {
                    return Response.status(Response.Status.CONFLICT)
                            .header(CONFLICT_REASON, "dui/correo already exists")
                            .build();
                }
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header(MISSING_PARAMETER, "entity must not be null; id must be null; nombres, apellidos, dui, correo, fechaNacimiento must not be null")
                .build();
    }

    @PUT
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response update(@PathParam("id") UUID id, AspirantesDato entity) {
        if (id != null && entity != null) {
            try {
                AspirantesDato existing = aspirantesDatoDAO.leer(id);
                if (existing != null) {
                    if (entity.getNombres() != null) {
                        existing.setNombres(entity.getNombres());
                    }
                    if (entity.getApellidos() != null) {
                        existing.setApellidos(entity.getApellidos());
                    }
                    if (entity.getDui() != null) {
                        AspirantesDato byDui = aspirantesDatoDAO.findByDui(entity.getDui());
                        if (byDui != null && !byDui.getId().equals(existing.getId())) {
                            return Response.status(Response.Status.CONFLICT)
                                    .header(CONFLICT_REASON, "dui already exists")
                                    .build();
                        }
                        existing.setDui(entity.getDui());
                    }
                    if (entity.getCorreo() != null) {
                        AspirantesDato byCorreo = aspirantesDatoDAO.findByCorreo(entity.getCorreo());
                        if (byCorreo != null && !byCorreo.getId().equals(existing.getId())) {
                            return Response.status(Response.Status.CONFLICT)
                                    .header(CONFLICT_REASON, "correo already exists")
                                    .build();
                        }
                        existing.setCorreo(entity.getCorreo());
                    }
                    if (entity.getFechaNacimiento() != null) {
                        existing.setFechaNacimiento(entity.getFechaNacimiento());
                    }
                    if (entity.getFechaCreacionPerfil() != null) {
                        existing.setFechaCreacionPerfil(entity.getFechaCreacionPerfil());
                    }
                    if (entity.getUsaSillaRuedas() != null) {
                        existing.setUsaSillaRuedas(entity.getUsaSillaRuedas());
                    }

                    AspirantesDato actualizado = aspirantesDatoDAO.actualizar(existing);
                    return Response.ok(actualizado).build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header(NOT_FOUND_ID, "Record with id " + id + " not found")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header(MISSING_PARAMETER, "id and entity must not be null")
                .build();
    }

    @DELETE
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response delete(@PathParam("id") UUID id) {
        if (id != null) {
            try {
                AspirantesDato existing = aspirantesDatoDAO.leer(id);
                if (existing != null) {
                    aspirantesDatoDAO.eliminar(existing);
                    return Response.noContent().build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header(NOT_FOUND_ID, "Record with id " + id + " not found")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header(MISSING_PARAMETER, "id")
                .build();
    }
}
