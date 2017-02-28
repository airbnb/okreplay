package software.betamax.android.sample;

import com.squareup.moshi.Moshi;

import java.io.IOException;

import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;
import software.betamax.proxy.BetamaxInterceptor;

class DependencyGraph {
  private static DependencyGraph instance;
  private final String API_BASE_URL = "https://api.github.com";
  final BetamaxInterceptor betamaxInterceptor = new BetamaxInterceptor();
  private final Moshi moshi = new Moshi.Builder()
      .add(BetamaxAdapterFactory.create())
      .build();
  private final Interceptor acceptHeaderInterceptor = new Interceptor() {
    @Override public Response intercept(Chain chain) throws IOException {
      return chain.proceed(chain.request()
          .newBuilder()
          .addHeader("Accept", "application/vnd.github.v3+json")
          .build());
    }
  };
  final OkHttpClient okHttpClient = new OkHttpClient.Builder()
      .addInterceptor(acceptHeaderInterceptor)
      .addInterceptor(betamaxInterceptor)
      .build();
  private final Retrofit retrofit = new Retrofit.Builder()
      .baseUrl(API_BASE_URL)
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
      .build();
  final GithubService service = retrofit.create(GithubService.class);

  private DependencyGraph() {
  }

  static DependencyGraph instance() {
    if (instance == null) {
      instance = new DependencyGraph();
    }
    return instance;
  }
}
