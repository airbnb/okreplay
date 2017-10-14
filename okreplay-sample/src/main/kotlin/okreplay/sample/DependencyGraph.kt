package okreplay.sample

import com.squareup.moshi.Moshi
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okreplay.OkReplayInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

internal class DependencyGraph private constructor() {
  val okReplayInterceptor = OkReplayInterceptor()
  private val moshi = Moshi.Builder()
      .add(OkReplayAdapterFactory.create())
      .build()
  private val acceptHeaderInterceptor = Interceptor {
    it.proceed(it.request()
        .newBuilder()
        .addHeader("Accept", "application/vnd.github.v3+json")
        .build())
  }
  val okHttpClient: OkHttpClient = OkHttpClient.Builder()
      .addInterceptor(acceptHeaderInterceptor)
      .addInterceptor(okReplayInterceptor)
      .build()
  private val retrofit = Retrofit.Builder()
      .baseUrl(API_BASE_URL)
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
      .build()
  val service: GithubService = retrofit.create(GithubService::class.java)

  companion object {
    private const val API_BASE_URL = "https://api.github.com"
    private var instance: DependencyGraph? = null

    fun instance(): DependencyGraph {
      if (instance == null) {
        instance = DependencyGraph()
      }
      return instance as DependencyGraph
    }
  }
}
