package okreplay;

import okhttp3.MediaType;
import okhttp3.ResponseBody;

public class RecordedResponseJavabean extends RecordedMessageJavabean {
  private int status;

  public int code() {
    return status;
  }

  public void setCode(int status) {
    this.status = status;
  }

  Response toImmutable() {
    Object body = body();
    MediaType mediaType = MediaType.parse(contentType());
    ResponseBody responseBody = null;
    if (body != null) {
      responseBody = body instanceof String
          ? ResponseBody.create(mediaType, (String) body)
          : ResponseBody.create(mediaType, (byte[]) body);
    }
    return new RecordedResponse.Builder()
        .code(code())
        .headers(okhttp3.Headers.of(headers()))
        .body(responseBody)
        .build();
  }
}
