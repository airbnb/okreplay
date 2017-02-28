package software.betamax.android.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;
import software.betamax.proxy.BetamaxInterceptor;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = "MainActivity";
  private static final String API_BASE_URL = "https://api.github.com";
  private static final String USERNAME = "felipecsl";
  private TextView textMessage;
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
  private final OkHttpClient client = new OkHttpClient.Builder()
      .addInterceptor(acceptHeaderInterceptor)
      .addInterceptor(new BetamaxInterceptor())
      .build();
  private final Retrofit retrofit = new Retrofit.Builder()
      .baseUrl(API_BASE_URL)
      .client(client)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
      .build();
  private final GithubService service = retrofit.create(GithubService.class);

  private final BottomNavigationView.OnNavigationItemSelectedListener itemSelectedListener
      = new BottomNavigationView.OnNavigationItemSelectedListener() {
    @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
      switch (item.getItemId()) {
        case R.id.navigation_activity:
          return goToActivity();
        case R.id.navigation_repositories:
          return goToRepositories();
        case R.id.navigation_organizations:
          return goToOrganizations();
      }
      return false;
    }
  };

  private boolean goToOrganizations() {
    textMessage.setText(R.string.title_organizations);
    return true;
  }

  private boolean goToRepositories() {
    textMessage.setText(R.string.title_repositories);
    service.repos(USERNAME)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<retrofit2.Response<List<Repository>>>() {
          @Override public void accept(retrofit2.Response<List<Repository>> listResponse)
              throws Exception {
            textMessage.setText(getString(R.string.title_repositories) + ": \n"
                + reposToString(listResponse.body()));
          }
        }, new Consumer<Throwable>() {
          @Override public void accept(Throwable throwable) throws Exception {
            Log.e(TAG, "Request failed", throwable);
          }
        });
    return true;
  }

  private String reposToString(List<Repository> repositories) {
    return Joiner.on("\n\n").join(FluentIterable.from(repositories)
        .transform(new Function<Repository, Object>() {
          @Nullable @Override public Object apply(Repository r) {
            return r.name() + ": " + r.description();
          }
        })
        .toList());
  }

  private boolean goToActivity() {
    textMessage.setText(R.string.title_activity);
    return true;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    textMessage = (TextView) findViewById(R.id.message);
    BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
    navigation.setOnNavigationItemSelectedListener(itemSelectedListener);
  }
}
