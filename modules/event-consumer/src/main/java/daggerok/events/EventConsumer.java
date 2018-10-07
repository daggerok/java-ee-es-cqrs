package daggerok.events;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
@ApplicationScoped
public class EventConsumer {

  @Resource
  ManagedExecutorService mes;

  @Inject
  Properties kafkaProperties;

  public void handle(final BiConsumer<Object, Object> handler, final String... topic) {

    final AtomicBoolean running = new AtomicBoolean(true);

    mes.submit(() -> {
      try (KafkaConsumer<Object, Object> kafkaConsumer = new KafkaConsumer<>(kafkaProperties)) {
        try {
          kafkaConsumer.subscribe(Arrays.asList(topic));

          while (running.get()) {
            for (ConsumerRecord<Object, Object> record : kafkaConsumer.poll(Long.MAX_VALUE)) {
              final Object key = record.key();
              final Object value = record.value();
              handler.accept(key, value);
            }
          }

          kafkaConsumer.commitAsync((offsets, exception) -> {
            if (nonNull(offsets))
              log.debug("commit offsets: {}", offsets);
            if (nonNull(exception))
              log.error("error: {}", exception.getLocalizedMessage(), exception);
          });

          running.set(false);
        }

        finally {
          kafkaConsumer.close(5, SECONDS);
        }
      }
    });
  }
}
