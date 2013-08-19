package co.freeside.betamax;

import co.freeside.betamax.message.Request;
import co.freeside.betamax.tape.MemoryTape;
import co.freeside.betamax.tape.StorableTape;
import co.freeside.betamax.tape.Tape;
import co.freeside.betamax.tape.TapeLoader;
import co.freeside.betamax.tape.yaml.YamlTape;
import co.freeside.betamax.tape.yaml.YamlTapeLoader;
import static co.freeside.betamax.MatchRule.*;
import static java.util.logging.Level.SEVERE;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Logger;

/**
 * This is the main interface to the Betamax proxy. It allows control of Betamax configuration and inserting and
 * ejecting `Tape` instances. The class can also be used as a _JUnit @Rule_ allowing tests annotated with `@Betamax` to
 * run with the Betamax HTTP proxy in the background.
 */
public class Recorder implements TestRule {
    public Recorder() {
        try {
            URL propertiesFile = Recorder.class.getResource("/betamax.properties");
            if (propertiesFile != null) {
                Properties properties = new Properties();
                properties.load(Files.newReader(new File(propertiesFile.getFile()), Charsets.UTF_8));
                configureFrom(properties);
            } else {
                configureWithDefaults();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Recorder(Properties properties) {
        configureFrom(properties);
    }

    public void start(String tapeName, Map arguments) {
        insertTape(tapeName, arguments);
    }

    public void start(String tapeName) {
        start(tapeName, new LinkedHashMap<Object, Object>());
    }

    public void stop() {
        ejectTape();
    }

    /**
     * Inserts a tape either creating a new one or loading an existing file from `tapeRoot`.
     *
     * @param name      the name of the _tape_.
     * @param arguments customize the behaviour of the tape.
     */
    @SuppressWarnings("unchecked")
    public void insertTape(String name, Map arguments) {
        tape = getTapeLoader().loadTape(name);
        if (tape instanceof MemoryTape) {
            MemoryTape memoryTape = (MemoryTape) tape;

            TapeMode mode = (TapeMode) arguments.get("mode");
            memoryTape.setMode(mode != null ? mode : defaultMode);

            List<Comparator<Request>> match = (List<Comparator<Request>>) arguments.get("match");
            Object[] array = match != null ? match.toArray() : null;
            Comparator[] matchArray = array != null ? Arrays.copyOf(array, array.length, Comparator[].class) : null;
            memoryTape.setMatchRules(matchArray != null ? matchArray : (Comparator[]) Arrays.asList(method, uri).toArray());
        }
    }

    /**
     * Inserts a tape either creating a new one or loading an existing file from `tapeRoot`.
     *
     * @param name      the name of the _tape_.
     */
    public void insertTape(String name) {
        insertTape(name, new LinkedHashMap<Object, Object>());
    }

    /**
     * Gets the current active _tape_.
     *
     * @return the active _tape_.
     */
    public Tape getTape() {
        return tape;
    }

    /**
     * 'Ejects' the current _tape_, writing its content to file. If the proxy is active after calling this method it
     * will no longer record or play back any HTTP traffic until another tape is inserted.
     */
    public void ejectTape() {
        if (tape != null) {
            getTapeLoader().writeTape(tape);
            tape = null;
        }

    }

    @Override
    public Statement apply(final Statement statement, Description description) {
        final Betamax annotation = description.getAnnotation(Betamax.class);
        if (annotation != null) {
            log.fine("found @Betamax annotation on '" + description.getDisplayName() + "'");
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    LinkedHashMap<String, Serializable> map = new LinkedHashMap<String, Serializable>(2);
                    map.put("mode", annotation.mode());
                    map.put("match", annotation.match());
                    try {
                        start(annotation.tape(), map);
                        statement.evaluate();
                    } catch (Exception e) {
                        log.log(SEVERE, "Caught exception starting Betamax", e);
                    } finally {
                        stop();
                    }
                }
            };
        } else {
            log.fine("no @Betamax annotation on '" + description.getDisplayName() + "'");
            return statement;
        }

    }

    public static boolean getBoolean(Properties properties, String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Boolean.valueOf(value) : defaultValue;
    }

    public static boolean getBoolean(Properties properties, String key) {
        return Recorder.getBoolean(properties, key, false);
    }

    public static int getInteger(Properties properties, String key, int defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Integer.getInteger(value) : defaultValue;
    }

    public static int getInteger(Properties properties, String key) {
        return Recorder.getInteger(properties, key, 0);
    }

    public static <T extends Enum<T>> T getEnum(Properties properties, String key, T defaultValue) {
        String value = properties.getProperty(key);
        T anEnum = Enum.valueOf((Class<T>)defaultValue.getClass(), value);
        return value != null ? anEnum : defaultValue;
    }

    protected void configureFrom(Properties properties) {
        tapeRoot = new File(properties.getProperty("betamax.tapeRoot", DEFAULT_TAPE_ROOT));
        defaultMode = getEnum(properties, "betamax.defaultMode", TapeMode.READ_WRITE);
        final List<String> tokenize = Lists.newArrayList(Splitter.on(",").split((String) properties.getProperty("betamax.ignoreHosts")));
        ignoreHosts = tokenize != null ? tokenize : new ArrayList<String>();
        ignoreLocalhost = getBoolean(properties, "betamax.ignoreLocalhost");
    }

    protected void configureWithDefaults() {
        tapeRoot = new File(DEFAULT_TAPE_ROOT);
        defaultMode = TapeMode.READ_WRITE;
        ignoreHosts = new ArrayList<String>();
        ignoreLocalhost = false;
    }

    /**
     * Not just a property as `tapeRoot` gets changed during constructor.
     */
    protected TapeLoader getTapeLoader() {
        return new YamlTapeLoader(tapeRoot);
    }

    public File getTapeRoot() {
        return tapeRoot;
    }

    public void setTapeRoot(File tapeRoot) {
        this.tapeRoot = tapeRoot;
    }

    public TapeMode getDefaultMode() {
        return defaultMode;
    }

    public void setDefaultMode(TapeMode defaultMode) {
        this.defaultMode = defaultMode;
    }

    public Collection<String> getIgnoreHosts() {
        return ignoreHosts;
    }

    public void setIgnoreHosts(Collection<String> ignoreHosts) {
        this.ignoreHosts = ignoreHosts;
    }

    public boolean getIgnoreLocalhost() {
        return ignoreLocalhost;
    }

    public boolean isIgnoreLocalhost() {
        return ignoreLocalhost;
    }

    public void setIgnoreLocalhost(boolean ignoreLocalhost) {
        this.ignoreLocalhost = ignoreLocalhost;
    }

    public static final String DEFAULT_TAPE_ROOT = "src/test/resources/betamax/tapes";
    protected final Logger log = Logger.getLogger(getClass().getName());
    /**
     * The base directory where tape files are stored.
     */
    private File tapeRoot = new File(DEFAULT_TAPE_ROOT);
    /**
     * The default mode for an inserted tape.
     */
    private TapeMode defaultMode = TapeMode.READ_ONLY;
    /**
     * Hosts that are ignored by the proxy. Any connections made will be allowed to proceed normally and not be
     * intercepted.
     */
    private Collection<String> ignoreHosts = new ArrayList<String>();
    /**
     * If set to true all connections to localhost addresses are ignored.
     * This is equivalent to setting `ignoreHosts` to `['localhost', '127.0.0.1', InetAddress.localHost.hostName,
     * InetAddress.localHost.hostAddress]`.
     */
    private boolean ignoreLocalhost = false;
    private StorableTape tape;
}
