package edu.kaist.mskers.toondra;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

/**
 * Created by harrykim on 2016. 10. 9..
 * This class is used to maintain global variables or contexts thorugh out all the activities.
 */

public class App extends MultiDexApplication {

  private static Context mContext;

  @Override
  public void onCreate() {
    super.onCreate();
    mContext = getApplicationContext();
  }

  public static Context getAppContext() {
    return App.mContext;
  }

  public int dpToPixel(int dp) {
    float scale = getResources().getDisplayMetrics().density;
    return (int) (dp * scale + 0.5f);
  }

  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    MultiDex.install(getBaseContext());
  }
}