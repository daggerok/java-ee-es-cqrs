package daggerok.order;

import daggerok.events.EventConsumer;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static javax.ejb.ConcurrencyManagementType.BEAN;

@Slf4j
@Startup
@Singleton
@ApplicationScoped
@ConcurrencyManagement(BEAN)
public class Orders {

  @Inject
  EventConsumer eventConsumer;

  private final Map<UUID, List<String>> events = new ConcurrentHashMap<>();

  @PostConstruct
  public void initProjections() {
    final BiConsumer<Object, Object> handler = (o, o2) -> {
      log.info("{} handling event: {}", o, o2);
      final String key = (String) o;
      final UUID aggregateId = UUID.fromString(key);
      final String event = String.class.cast(o2);
      add(aggregateId, event);
    };
    eventConsumer.handle(handler, "orders");
  }

  private void add(final UUID key, final String event) {
    events.putIfAbsent(key, new ArrayList<>());
    events.put(key, Stream.concat(events.get(key).stream(),
                                  Stream.of(event))
                          .collect(toList()));
  }

  public List<String> getEvent(final UUID uuid) {
    return events.getOrDefault(uuid, new ArrayList<>());
  }
}
