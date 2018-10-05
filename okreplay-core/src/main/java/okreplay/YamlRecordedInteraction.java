package okreplay;

import java.util.Date;

public class YamlRecordedInteraction {
  private final Date recorded;
  private final YamlRecordedRequest request;
  private final YamlRecordedResponse response;
  private transient RecordedInteraction immutableInteraction;

  YamlRecordedInteraction(Date recorded, YamlRecordedRequest request,
      YamlRecordedResponse response) {
    this.recorded = recorded;
    this.request = request;
    this.response = response;
  }

  /** For SnakeYAML */
  @SuppressWarnings("unused") public YamlRecordedInteraction() {
    this(null, null, null);
  }

  RecordedInteraction toImmutable() {
    if (immutableInteraction == null) {
      immutableInteraction = createImmutable();
    }
    return immutableInteraction;
  }

  private RecordedInteraction createImmutable() {
      return new RecordedInteraction(recorded, request.toImmutable(), response.toImmutable());
  }
}
