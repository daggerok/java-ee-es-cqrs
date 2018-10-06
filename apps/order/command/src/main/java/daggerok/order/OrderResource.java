package daggerok.order;

import daggerok.events.EventProducer;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.UUID;

import static daggerok.config.Producers.stringify;

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
      throw new NotFoundException("No order items found.");

    final UUID uuid = UUID.randomUUID();
    final CreateOrder event = new CreateOrder(uuid, items);

    eventProducer.fire("orders", uuid.toString(), stringify(event));

    return Response.created(uriInfo.getBaseUriBuilder()
                                   .path("order/{uuid}")
                                   .build(uuid))
                   .build();
  }
}
