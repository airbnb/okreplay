package walkman;

public class ProxyServer implements RecorderListener {
  private final Configuration configuration;
  private final WalkmanInterceptor interceptor;
  private boolean running;

  public ProxyServer(Configuration configuration, WalkmanInterceptor interceptor) {
    this.configuration = configuration;
    this.interceptor = interceptor;
  }

  @Override public void onRecorderStart(Tape tape) {
    if (!isRunning()) {
      start(tape);
    }
  }

  @Override public void onRecorderStop() {
    if (isRunning()) {
      stop();
    }
  }

  private boolean isRunning() {
    return running;
  }

  public void start(final Tape tape) {
    if (isRunning()) {
      throw new IllegalStateException("Walkman proxy server is already running");
    }
    interceptor.start(configuration, tape);
    running = true;
  }

  public void stop() {
    if (!isRunning()) {
      throw new IllegalStateException("Walkman proxy server is already stopped");
    }
    interceptor.stop();
    running = false;
  }
}
