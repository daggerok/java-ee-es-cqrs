package daggerok.order;

import daggerok.events.EventProducer;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.UUID;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Stateless
@Path("order")
public class OrderResource {

  @Inject
  EventProducer eventProducer;

  @Context
  UriInfo uriInfo;

  @POST
  @Path("")
  public Response createOrder(final List<String> items) {
    if (null == items || items.isEmpty())
      return Response.status(BAD_REQUEST)
                     .entity("No order items found.")
                     .build();

    final UUID uuid = UUID.randomUUID();
    eventProducer.fire(new CreateOrder(uuid, items));

    return Response.created(uriInfo.getBaseUriBuilder()
                                   .path("order/{uuid}")
                                   .build(uuid))
                   .build();
  }
}
