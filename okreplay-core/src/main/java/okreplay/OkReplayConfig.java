package okreplay;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * The configuration used by okreplay.
 * <p>
 * `OkReplayConfig` instances are created with a builder. For example:
 * <p>
 * [source,java]
 * ----
 * OkReplayConfig configuration = new OkReplayConfig.Builder()
 * .tapeRoot(tapeRoot)
 * .ignoreLocalhost(true)
 * .build();
 * ----
 *
 * @see Builder
 */
@SuppressWarnings("WeakerAccess")
public class OkReplayConfig {
  public static final String DEFAULT_TAPE_ROOT = "src/test/resources/okreplay/tapes";
  public static final TapeMode DEFAULT_MODE = TapeMode.READ_ONLY;
  public static final MatchRule DEFAULT_MATCH_RULE = ComposedMatchRule.of(MatchRules.method,
      MatchRules.uri);

  private final TapeRoot tapeRoot;
  private final TapeMode defaultMode;
  private final Collection<String> ignoreHosts;
  private final boolean ignoreLocalhost;
  private final MatchRule defaultMatchRule;
  private final boolean sslEnabled;
  private final OkReplayInterceptor interceptor;

  protected OkReplayConfig(Builder builder) {
    this.tapeRoot = builder.tapeRoot;
    this.defaultMode = builder.defaultMode;
    this.defaultMatchRule = builder.defaultMatchRule;
    this.ignoreHosts = builder.ignoreHosts;
    this.ignoreLocalhost = builder.ignoreLocalhost;
    this.sslEnabled = builder.sslEnabled;
    this.interceptor = builder.interceptor;
  }

  /**
   * The base directory where tape files are stored.
   */
  public TapeRoot getTapeRoot() {
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
   * Hosts that are ignored by okreplay. Any connections made will be allowed to proceed
   * normally and not be intercepted.
   */
  public Collection<String> getIgnoreHosts() {
    if (isIgnoreLocalhost()) {
      Set<String> set = new LinkedHashSet<>(ignoreHosts);
      set.addAll(Network.getLocalAddresses());
      return Collections.unmodifiableSet(set);
    } else {
      return ignoreHosts;
    }
  }

  /**
   * If `true` then all connections to localhost addresses are ignored.
   * <p>
   * This is equivalent to including the following in the collection returned by {@link
   * #getIgnoreHosts()}: * `"localhost"` * `"127.0.0.1"` * `InetAddress.getLocalHost()
   * .getHostName()`
   * * `InetAddress.getLocalHost().getHostAddress()`
   */
  public boolean isIgnoreLocalhost() {
    return ignoreLocalhost;
  }

  public OkReplayInterceptor interceptor() {
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
   * <p>
   * You should **not** call this method yourself.
   */
  public void registerListeners(Collection<RecorderListener> listeners) {
    listeners.add(new ProxyServer(this, interceptor));
  }

  public static class Builder {
    TapeRoot tapeRoot = new DefaultTapeRoot(new File(OkReplayConfig.DEFAULT_TAPE_ROOT));
    TapeMode defaultMode = OkReplayConfig.DEFAULT_MODE;
    MatchRule defaultMatchRule = OkReplayConfig.DEFAULT_MATCH_RULE;
    List<String> ignoreHosts = Collections.emptyList();
    boolean ignoreLocalhost;
    boolean sslEnabled;
    OkReplayInterceptor interceptor;

    public Builder() {
      try {
        URL propertiesFile = OkReplayConfig.class.getResource("/okreplay.properties");
        if (propertiesFile != null) {
          Properties properties = new Properties();
          properties.load(propertiesFile.openStream());
          withProperties(properties);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    public Builder withProperties(Properties properties) {
      if (properties.containsKey("okreplay.tapeRoot")) {
        tapeRoot(new File(properties.getProperty("okreplay.tapeRoot")));
      }

      if (properties.containsKey("okreplay.defaultMode")) {
        defaultMode(TapeMode.valueOf(properties.getProperty("okreplay.defaultMode")));
      }

      if (properties.containsKey("okreplay.defaultMatchRules")) {
        String property = properties.getProperty("okreplay.defaultMatchRules");
        List<MatchRule> rules = new ArrayList<>();
        for (String s : Arrays.asList(property.split(","))) {
          rules.add(MatchRules.valueOf(s));
        }
        defaultMatchRule(ComposedMatchRule.of(rules));
      }

      if (properties.containsKey("okreplay.ignoreHosts")) {
        ignoreHosts(Arrays.asList(properties.getProperty("okreplay.ignoreHosts").split(",")));
      }

      if (properties.containsKey("okreplay.ignoreLocalhost")) {
        ignoreLocalhost(Boolean.valueOf(properties.getProperty("okreplay.ignoreLocalhost")));
      }

      if (properties.containsKey("okreplay.sslEnabled")) {
        sslEnabled(TypedProperties.getBoolean(properties, "okreplay.sslEnabled"));
      }

      return this;
    }

    public Builder tapeRoot(File tapeRoot) {
      return tapeRoot(new DefaultTapeRoot(tapeRoot));
    }

    public Builder tapeRoot(TapeRoot tapeRoot) {
      this.tapeRoot = tapeRoot;
      return this;
    }

    public Builder defaultMode(TapeMode defaultMode) {
      this.defaultMode = defaultMode;
      return this;
    }

    public Builder interceptor(OkReplayInterceptor interceptor) {
      this.interceptor = interceptor;
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

    public Builder ignoreHosts(Collection<String> ignoreHosts) {
      this.ignoreHosts = new ArrayList<>(ignoreHosts);
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

    public OkReplayConfig build() {
      return new OkReplayConfig(this);
    }
  }
}
