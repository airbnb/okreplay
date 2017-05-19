package okreplay;

import java.util.Date;

public class RecordedInteractionJavabean {
  private Date recorded;
  private RecordedRequestJavabean request;
  private RecordedResponseJavabean response;

  public Date getRecorded() {
    return recorded;
  }

  public void setRecorded(Date recorded) {
    this.recorded = recorded;
  }

  public RecordedRequestJavabean getRequest() {
    return request;
  }

  public void setRequest(RecordedRequestJavabean request) {
    this.request = request;
  }

  public RecordedResponseJavabean getResponse() {
    return response;
  }

  public void setResponse(RecordedResponseJavabean response) {
    this.response = response;
  }

  RecordedInteraction toImmutable() {
    return new RecordedInteraction(recorded, request.toImmutable(), response.toImmutable());
  }
}
