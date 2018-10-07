package daggerok.order;

import daggerok.events.EventProducer;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
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

    final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
    for (final String item : items) arrayBuilder.add(item);

    final String uuid = UUID.randomUUID().toString();
    final JsonObject jsonObject = Json.createObjectBuilder()
                                      .add("type", "CreateEvent")
                                      .add("id", uuid)
                                      .add("items", arrayBuilder.build())
                                      .build();

    eventProducer.fire("orders", jsonObject.toString());

    final URI uri = uriInfo.getBaseUri();
    return Response.created(UriBuilder.fromUri("{scheme}://{host}:{port}/app/order/{uuid}")
                                      .build(uri.getScheme(), uri.getHost(), uri.getPort() + 1, uuid))
                   .build();
  }
}
