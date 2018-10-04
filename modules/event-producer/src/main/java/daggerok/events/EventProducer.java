package daggerok.events;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Properties;

@Slf4j
@ApplicationScoped
public class EventProducer {

  @Inject Properties properties;

  public boolean fire(final Object event) {
    return Try.of(() -> {
      log.info("ololo: {}", properties);
      return true;
    }).getOrElse(false);
  }
}
