package daggerok.events;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.Singleton;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Properties;

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

  public void fire(final String topic, final String key, final String json) {
    mes.submit(() -> {
      try (final KafkaProducer<Object, Object> kafkaProducer = new KafkaProducer<>(kafkaProperties)) {
        Try.run(() -> kafkaProducer.send(new ProducerRecord<>(topic, key, json)))
           .onFailure(throwable -> log.error("sending error: {}", throwable.getLocalizedMessage()));
      }
    });
  }
}
