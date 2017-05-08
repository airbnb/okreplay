package okreplay;

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

/**
 * The configuration used by okreplay.
 *
 * `OkReplayConfig` instances are created with a builder. For example:
 *
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
  private final ImmutableCollection<String> ignoreHosts;
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
   *
   * You should **not** call this method yourself.
   */
  public void registerListeners(Collection<RecorderListener> listeners) {
    listeners.add(new ProxyServer(this, interceptor));
  }

  public static class Builder {
    TapeRoot tapeRoot = new DefaultTapeRoot(new File(OkReplayConfig.DEFAULT_TAPE_ROOT));
    TapeMode defaultMode = OkReplayConfig.DEFAULT_MODE;
    MatchRule defaultMatchRule = OkReplayConfig.DEFAULT_MATCH_RULE;
    ImmutableCollection<String> ignoreHosts = ImmutableList.of();
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
        List<MatchRule> rules = Lists.transform(Splitter.on(",").splitToList(properties
            .getProperty("okreplay.defaultMatchRules")), new Function<String, MatchRule>() {
          @Override public MatchRule apply(String input) {
            return MatchRules.valueOf(input);
          }
        });
        defaultMatchRule(ComposedMatchRule.of(rules));
      }

      if (properties.containsKey("okreplay.ignoreHosts")) {
        ignoreHosts(Splitter.on(",").splitToList(properties.getProperty("okreplay.ignoreHosts")));
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

    public OkReplayConfig build() {
      return new OkReplayConfig(this);
    }
  }
}
