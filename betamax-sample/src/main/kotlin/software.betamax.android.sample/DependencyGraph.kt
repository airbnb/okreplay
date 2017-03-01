package software.betamax.android.sample

import com.squareup.moshi.Moshi
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import software.betamax.proxy.BetamaxInterceptor

internal class DependencyGraph private constructor() {
  private val API_BASE_URL = "https://api.github.com"
  val betamaxInterceptor = BetamaxInterceptor()
  private val moshi = Moshi.Builder()
      .add(BetamaxAdapterFactory.create())
      .build()
  private val acceptHeaderInterceptor = Interceptor {
    it.proceed(it.request()
        .newBuilder()
        .addHeader("Accept", "application/vnd.github.v3+json")
        .build())
  }
  val okHttpClient: OkHttpClient = OkHttpClient.Builder()
      .addInterceptor(acceptHeaderInterceptor)
      .addInterceptor(betamaxInterceptor)
      .build()
  private val retrofit = Retrofit.Builder()
      .baseUrl(API_BASE_URL)
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
      .build()
  val service: GithubService = retrofit.create(GithubService::class.java)

  companion object {
    private var instance: DependencyGraph? = null

    fun instance(): DependencyGraph {
      if (instance == null) {
        instance = DependencyGraph()
      }
      return instance as DependencyGraph
    }
  }
}
