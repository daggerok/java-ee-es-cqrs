package daggerok.config;

import io.vavr.control.Try;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.json.JsonBuilderFactory;
import javax.json.bind.JsonbBuilder;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.function.BiFunction;

@ApplicationScoped
public class Producers {

  private static final Properties kafkaProperties = readKafkaProperties();

  private static Properties readKafkaProperties() {
    final Properties properties = new Properties();
    final ClassLoader classLoader = Thread.currentThread()
                                          .getContextClassLoader();
    return Try.of(() -> {
      try (final InputStream stream = classLoader.getResourceAsStream("kafka.properties")) {
        properties.load(stream);
        return properties;
      }
    }).getOrElseGet(throwable -> properties);
  }

  private static BiFunction<String, Object, Object> getConfigOrDefault = (key, defaultValue) -> {
    Objects.requireNonNull(key, "key.null");
    Objects.requireNonNull(defaultValue, "defaultValue.null");

    final Object envValue = System.getenv().getOrDefault(key, "" + defaultValue);
    if (!envValue.equals(defaultValue)) return envValue;

    final Object systemValue = System.getProperty(key, "" + defaultValue);
    if (!systemValue.equals(defaultValue)) return systemValue;

    return kafkaProperties.getProperty(key, "" + defaultValue);
  };

  public static <EVENT> String stringify(final EVENT event) {
    return JsonbBuilder.create().toJson(event);
  }

  @Produces
  public BiFunction<String, Object, Object> getConfigOrDefault() {
    return getConfigOrDefault;
  }

  @Produces
  public Properties kafkaProperties() {
    return kafkaProperties;
  }
}
