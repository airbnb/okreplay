package okreplay;

import java.util.Date;

import okreplay.Request;
import okreplay.Response;

public class RecordedInteraction {
  private final Date recorded;
  private final Request request;
  private final Response response;

  public RecordedInteraction(Date recorded, Request request, Response response) {
    this.recorded = recorded;
    this.request = request;
    this.response = response;
  }

  public Date recorded() {
    return recorded;
  }

  public Request request() {
    return request;
  }

  public Response response() {
    return response;
  }
}
