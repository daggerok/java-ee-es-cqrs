package daggerok.app;

import daggerok.config.Producers;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.CLIENT_ID_CONFIG;

/**
 * see {@link org.apache.kafka.clients.consumer.KafkaConsumer} for details...
 */
@Slf4j
public class EventConsumer implements Runnable {

  private final AtomicBoolean running = new AtomicBoolean();
  private final Consumer<ConsumerRecord<String, String>> consumer;
  private final KafkaConsumer kafkaConsumer;
  private final Set<String> topics;

  public EventConsumer(final Consumer<ConsumerRecord<String, String>> consumer,
                       final String... topics) {

    final Producers producers = new Producers();
    final String bootstrapServers = producers.getEnvOrDefault()
                                             .apply(BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
    final Properties properties = producers.kafkaProperties();

    properties.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    properties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
    properties.put(CLIENT_ID_CONFIG, getClass().getName());
    this.kafkaConsumer = new KafkaConsumer(properties);
    this.topics = new HashSet<>(asList(topics));
    this.consumer = consumer;
  }

  public void run() {
    try {
//      final List<TopicPartition> topicPartitions = topics.stream()
//                                                 .map(t -> new TopicPartition(t, 0))
//                                                 .collect(toList());
//      kafkaConsumer.assign(topicPartitions);
      kafkaConsumer.subscribe(topics);
      running.set(true);
      while (running.get()) {
        final ConsumerRecords<String, String> records = kafkaConsumer.poll(10000);
        for (final ConsumerRecord<String, String> record : records) {
          consumer.accept(record);
        }
        kafkaConsumer.commitAsync();
      }
    } catch (WakeupException e) {
      // Ignore exception if closing
      if (running.get()) log.info("ignore wakeup...");
    } finally {
      log.info("closing consumer...");
      kafkaConsumer.close();
    }
  }

  // Shutdown hook which can be called from a separate thread
  public void shutdown() {
    log.info("shutdown...");
    running.set(false);
    kafkaConsumer.wakeup();
  }
}
