package daggerok.events;

import io.vavr.control.Try;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.Singleton;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Properties;
import java.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ejb.ConcurrencyManagementType.BEAN;

@Slf4j
@Singleton
@ApplicationScoped
@ConcurrencyManagement(BEAN)
public class EventProducer {

  @Resource
  ManagedExecutorService mes;

  @Inject
  Properties kafkaProperties;

  @SneakyThrows
  public void fire(final String topic, final String json) {
    mes.submit(() -> {
//      try (final KafkaProducer<Object, Object> kafkaProducer = new KafkaProducer<>(kafkaProperties)) {
//        Try.run(() -> kafkaProducer.send(new ProducerRecord<>(topic, json)))
//           .onFailure(throwable -> log.error("sending error: {}", throwable.getLocalizedMessage()));
//      }
      log.info("fuck, no...: {}", json);
      try (final KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(kafkaProperties)) {
        final ProducerRecord<String, String> record = new ProducerRecord<>(topic, topic, json);
        final Future<RecordMetadata> future = kafkaProducer.send(record);
        log.info("sending a record: {}", record);
        final Try<RecordMetadata> aTry = Try.of(() -> future.get(5, SECONDS));
        if (aTry.isFailure()) {
          log.error("nope... {}", aTry.getCause().getLocalizedMessage());
          return;
        }
        final RecordMetadata recordMetadata = aTry.get();
        log.info("record sent to partition: {} and offset: {}",
                 recordMetadata.partition(), recordMetadata.offset());
      }
    });
  }
}
