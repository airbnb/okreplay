package okreplay;

import java.util.Date;

class RecordedInteraction {
  private final Date recorded;
  private final Request request;
  private final Response response;

  RecordedInteraction(Date recorded, Request request, Response response) {
    this.recorded = recorded;
    this.request = request;
    this.response = response;
  }

  Date recorded() {
    return recorded;
  }

  Request request() {
    return request;
  }

  Response response() {
    return response;
  }

  YamlRecordedInteraction toJavaBean() {
    return new YamlRecordedInteraction(recorded, request.toYaml(), response.toJavaBean());
  }
}
