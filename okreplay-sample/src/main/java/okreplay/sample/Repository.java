package okreplay.sample;

import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

@AutoValue abstract class Repository {
  abstract String name();
  @Nullable abstract String description();

  public static JsonAdapter<Repository> jsonAdapter(Moshi moshi) {
    return new AutoValue_Repository.MoshiJsonAdapter(moshi);
  }
}
