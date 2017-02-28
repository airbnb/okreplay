package software.betamax.android.sample;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;

interface GithubService {
  @GET("/users/{owner}/repos") Observable<Response<List<Repository>>> repos(
      @Path("owner") String owner);
}
