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

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = "MainActivity";
  private static final String USERNAME = "felipecsl";
  private TextView textMessage;
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
    SampleApplication application = (SampleApplication) getApplication();
    application.graph.service.repos(USERNAME)
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
