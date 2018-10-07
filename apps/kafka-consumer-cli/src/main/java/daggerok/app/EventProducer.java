package daggerok.app;

import daggerok.config.Producers;
import io.vavr.control.Try;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public class EventProducer {

  private final String topic;

  public EventProducer(final String topic) {
    this.topic = topic;
  }

  public void send(final String message) {
    final Properties props = new Producers().kafkaProperties();
    try (final KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(props)) {
      final ProducerRecord<String, String> record = new ProducerRecord<>(topic, message);
      kafkaProducer.send(record, (metadata, exception) -> {
        if (null != metadata) {
          log.info("message {} sent to {} topic with {} offset",
                   record, metadata.topic(), metadata.offset());
        }
        if (null != exception) {
          log.error("oops.. {}", exception.getLocalizedMessage(), exception);
        }
      });
    }
  }

  @SneakyThrows
  public static void main(String[] args) {
    final EventProducer eventProducer = new EventProducer("orders");
    for (int i = 0; i < 10; i++) {
      TimeUnit.SECONDS.sleep(1);
      eventProducer.send(i + ") hello!");
    }
  }
}
