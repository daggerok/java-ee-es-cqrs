package daggerok;

import daggerok.app.EventConsumer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.function.Consumer;

@Slf4j
public class App {

  @SneakyThrows
  public static void main(String[] args) {
    log.info("yo!");

    final Consumer<ConsumerRecord<String, String>> eventConsumer = record -> {
      log.info("received value: {}", record.value());
      log.info("received key: {}", record.key());
    };

    new Thread(new EventConsumer(eventConsumer, "orders"))
        .start();

    Thread.currentThread().join();

    while (System.in.read() != -1)
      System.exit(0);
  }
}
