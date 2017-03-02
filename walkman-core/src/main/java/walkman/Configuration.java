package walkman;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import okhttp3.Interceptor;

/**
 * The configuration used by walkman.
 *
 * `Configuration` instances are created with a builder returned by the
 * {@link #builder()} factory method. For example:
 *
 * [source,java]
 * ----
 * Configuration configuration = Configuration.builder()
 * .tapeRoot(tapeRoot)
 * .ignoreLocalhost(true)
 * .build();
 * ----
 *
 * @see Builder
 */
public class Configuration {
  public static final String DEFAULT_TAPE_ROOT = "src/test/resources/walkman/tapes";
  public static final TapeMode DEFAULT_MODE = TapeMode.READ_ONLY;
  public static final MatchRule DEFAULT_MATCH_RULE = ComposedMatchRule.of(MatchRules.method,
      MatchRules.uri);

  private final File tapeRoot;
  private final TapeMode defaultMode;
  private final ImmutableCollection<String> ignoreHosts;
  private final boolean ignoreLocalhost;
  private final MatchRule defaultMatchRule;
  private final boolean sslEnabled;
  private final WalkmanInterceptor interceptor;

  protected Configuration(Builder builder) {
    this.tapeRoot = builder.tapeRoot;
    this.defaultMode = builder.defaultMode;
    this.defaultMatchRule = builder.defaultMatchRule;
    this.ignoreHosts = builder.ignoreHosts;
    this.ignoreLocalhost = builder.ignoreLocalhost;
    this.sslEnabled = builder.sslEnabled;
    this.interceptor = new WalkmanInterceptor();
  }

  public static Builder builder() {
    try {
      Builder builder = new Builder();
      URL propertiesFile = Configuration.class.getResource("/walkman.properties");
      if (propertiesFile != null) {
        Properties properties = new Properties();
        properties.load(propertiesFile.openStream());
        return builder.withProperties(properties);
      } else {
        return builder;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * The base directory where tape files are stored.
   */
  public File getTapeRoot() {
    return tapeRoot;
  }

  /**
   * The default mode for an inserted tape.
   */
  public TapeMode getDefaultMode() {
    return defaultMode;
  }

  public MatchRule getDefaultMatchRule() {
    return defaultMatchRule;
  }

  /**
   * Hosts that are ignored by walkman. Any connections made will be allowed to proceed
   * normally and not be intercepted.
   */
  public Collection<String> getIgnoreHosts() {
    if (isIgnoreLocalhost()) {
      return new ImmutableSet.Builder<String>()
          .addAll(ignoreHosts)
          .addAll(Network.getLocalAddresses())
          .build();
    } else {
      return ignoreHosts;
    }
  }

  /**
   * If `true` then all connections to localhost addresses are ignored.
   *
   * This is equivalent to including the following in the collection returned by {@link
   * #getIgnoreHosts()}: * `"localhost"` * `"127.0.0.1"` * `InetAddress.getLocalHost()
   * .getHostName()`
   * * `InetAddress.getLocalHost().getHostAddress()`
   */
  public boolean isIgnoreLocalhost() {
    return ignoreLocalhost;
  }

  public Interceptor interceptor() {
    return interceptor;
  }

  /**
   * If set to true add support for proxying SSL (disable certificate
   * checking).
   */
  public boolean isSslEnabled() {
    return sslEnabled;
  }

  /**
   * Called by the `Recorder` instance so that the configuration can add listeners.
   *
   * You should **not** call this method yourself.
   */
  public void registerListeners(Collection<RecorderListener> listeners) {
    listeners.add(new ProxyServer(this, interceptor));
  }

  public static class Builder {
    File tapeRoot = new File(Configuration.DEFAULT_TAPE_ROOT);
    TapeMode defaultMode = Configuration.DEFAULT_MODE;
    MatchRule defaultMatchRule = Configuration.DEFAULT_MATCH_RULE;
    ImmutableCollection<String> ignoreHosts = ImmutableList.of();
    boolean ignoreLocalhost;
    boolean sslEnabled;

    public Builder withProperties(Properties properties) {
      if (properties.containsKey("walkman.tapeRoot")) {
        tapeRoot(new File(properties.getProperty("walkman.tapeRoot")));
      }

      if (properties.containsKey("walkman.defaultMode")) {
        defaultMode(TapeMode.valueOf(properties.getProperty("walkman.defaultMode")));
      }

      if (properties.containsKey("walkman.defaultMatchRules")) {
        List<MatchRule> rules = Lists.transform(Splitter.on(",").splitToList(properties
            .getProperty("walkman.defaultMatchRules")), new Function<String, MatchRule>() {
          @Override public MatchRule apply(String input) {
            return MatchRules.valueOf(input);
          }
        });
        defaultMatchRule(ComposedMatchRule.of(rules));
      }

      if (properties.containsKey("walkman.ignoreHosts")) {
        ignoreHosts(Splitter.on(",").splitToList(properties.getProperty("walkman.ignoreHosts")));
      }

      if (properties.containsKey("walkman.ignoreLocalhost")) {
        ignoreLocalhost(Boolean.valueOf(properties.getProperty("walkman.ignoreLocalhost")));
      }

      if (properties.containsKey("walkman.sslEnabled")) {
        sslEnabled(TypedProperties.getBoolean(properties, "walkman.sslEnabled"));
      }

      return this;
    }

    public Builder tapeRoot(File tapeRoot) {
      this.tapeRoot = tapeRoot;
      return this;
    }

    public Builder defaultMode(TapeMode defaultMode) {
      this.defaultMode = defaultMode;
      return this;
    }

    public Builder defaultMatchRule(MatchRule defaultMatchRule) {
      this.defaultMatchRule = defaultMatchRule;
      return this;
    }

    public Builder defaultMatchRules(MatchRule... defaultMatchRules) {
      this.defaultMatchRule = ComposedMatchRule.of(defaultMatchRules);
      return this;
    }

    public Builder ignoreHosts(Iterable<String> ignoreHosts) {
      this.ignoreHosts = ImmutableList.copyOf(ignoreHosts);
      return this;
    }

    public Builder ignoreLocalhost(boolean ignoreLocalhost) {
      this.ignoreLocalhost = ignoreLocalhost;
      return this;
    }

    public Builder sslEnabled(boolean sslEnabled) {
      this.sslEnabled = sslEnabled;
      return this;
    }

    public Configuration build() {
      return new Configuration(this);
    }
  }
}
