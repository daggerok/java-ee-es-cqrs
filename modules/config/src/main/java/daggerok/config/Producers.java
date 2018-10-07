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

  private static BiFunction<String, String, String> getEnvOrDefault = (key, defaultValue) -> {
    Objects.requireNonNull(key, "key.null");
    Objects.requireNonNull(defaultValue, "defaultValue.null");

    final String envValue = System.getenv().getOrDefault(key, defaultValue);
    if (!envValue.equals(defaultValue)) return envValue;

    final String systemValue = System.getProperty(key, defaultValue);
    if (!systemValue.equals(defaultValue)) return systemValue;

    return defaultValue;
  };

  private static BiFunction<String, Object, Object> getConfigOrDefault = (key, defaultValue) -> {
    final String string = Try.of(() -> (String) defaultValue).getOrElseGet(null);
    final String value = getEnvOrDefault.apply(key, string);
    return kafkaProperties.getProperty(key, value);
  };

  @Produces
  public Properties kafkaProperties() {
    return kafkaProperties;
  }

  @Produces
  public BiFunction<String, String, String> getEnvOrDefault() {
    return getEnvOrDefault;
  }

  @Produces
  public BiFunction<String, Object, Object> getConfigOrDefault() {
    return getConfigOrDefault;
  }
}
