package daggerok.events;

import io.vavr.control.Try;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.function.BiFunction;

@ApplicationScoped
public class Producers {

  private static Properties props() {
    final Properties properties = new Properties();
    final ClassLoader classLoader = Thread.currentThread()
                                          .getContextClassLoader();
    return Try.of(() -> {
      try (final InputStream stream = classLoader.getResourceAsStream("application.properties")) {
        properties.load(stream);
        return properties;
      }
    }).getOrElseGet(throwable -> properties);
  }

  private static BiFunction<String, String, String> getConfigOrDefault = (key, defaultValue) -> {
    Objects.requireNonNull(key, "key.null");
    Objects.requireNonNull(defaultValue, "defaultValue.null");

    final String envValue = System.getenv().getOrDefault(key, defaultValue);
    if (!envValue.equals(defaultValue)) return envValue;

    final String systemValue = System.getProperty(key, defaultValue);
    if (!systemValue.equals(defaultValue)) return systemValue;

    return props().getProperty(key, defaultValue);
  };

  @Produces
  public BiFunction<String, String, String> getOrDefault() {
    return getConfigOrDefault;
  }

  @Produces
  public Properties properties() {
    return props();
  }
}
