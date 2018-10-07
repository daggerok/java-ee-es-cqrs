package daggerok.order;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.UUID;

@Slf4j
@Stateless
@Path("order")
public class OrderResource {

  @Inject
  Orders orders;

  @Context
  UriInfo uriInfo;

  @GET
  @Path("{uuid}")
  public Response findOrder(@PathParam("uuid") final String uuid) {

    final Try<UUID> aTry = Try.of(() -> UUID.fromString(uuid));
    if (aTry.isFailure()) throw new IllegalArgumentException("Incorrect UUID format.");

    final UUID id = aTry.get();
    final List<String> events = orders.getEvent(id);

    if (events.isEmpty()) throw new NotFoundException("Events not found.");
    return Response.ok(events).build();
  }
}
