package okreplay;

public class ProxyServer implements RecorderListener {
  private final OkReplayConfig configuration;
  private final OkReplayInterceptor interceptor;
  private boolean running;

  public ProxyServer(OkReplayConfig configuration, OkReplayInterceptor interceptor) {
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
      throw new IllegalStateException("OkReplay proxy server is already running");
    }
    interceptor.start(configuration, tape);
    running = true;
  }

  public void stop() {
    if (!isRunning()) {
      throw new IllegalStateException("OkReplay proxy server is already stopped");
    }
    interceptor.stop();
    running = false;
  }
}
