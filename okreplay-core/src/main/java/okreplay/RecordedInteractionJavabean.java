package okreplay;

import java.util.Date;

public class RecordedInteractionJavabean {
  private final Date recorded;
  private final RecordedRequestJavabean request;
  private final RecordedResponseJavabean response;

  public RecordedInteractionJavabean(Date recorded, RecordedRequestJavabean request,
      RecordedResponseJavabean response) {
    this.recorded = recorded;
    this.request = request;
    this.response = response;
  }

  /** For SnakeYAML */
  @SuppressWarnings("unused") public RecordedInteractionJavabean() {
    this(null, null, null);
  }

  public Date getRecorded() {
    return recorded;
  }

  public RecordedRequestJavabean getRequest() {
    return request;
  }

  public RecordedResponseJavabean getResponse() {
    return response;
  }

  RecordedInteraction toImmutable() {
    return new RecordedInteraction(recorded, request.toImmutable(), response.toImmutable());
  }
}
