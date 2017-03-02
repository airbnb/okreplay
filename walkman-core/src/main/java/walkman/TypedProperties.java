package walkman;

import java.util.Properties;

class TypedProperties extends Properties {
  private static boolean getBoolean(Properties properties, String key, boolean defaultValue) {
    String value = properties.getProperty(key);
    return value != null ? Boolean.valueOf(value) : defaultValue;
  }

  static boolean getBoolean(Properties properties, String key) {
    return getBoolean(properties, key, false);
  }

  private static int getInteger(Properties properties, String key, int defaultValue) {
    String value = properties.getProperty(key);
    return value != null ? Integer.parseInt(value) : defaultValue;
  }

  public static int getInteger(Properties properties, String key) {
    return getInteger(properties, key, 0);
  }
}
