package okreplay.sample

import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

internal interface GithubService {
  @GET("/repos/{owner}/{repo}") fun repo(
      @Path("owner") owner: String, @Path("repo") repo: String): Observable<Response<Repository>>

  @GET("/users/{owner}/repos") fun repos(
      @Path("owner") owner: String): Observable<Response<List<Repository>>>
}
