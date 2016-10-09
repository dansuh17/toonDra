package edu.kaist.mskers.toondra;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import edu.kaist.mskers.toondra.navermodule.NaverToonInfo;
import edu.kaist.mskers.toondra.navermodule.webtoon.Day;
import edu.kaist.mskers.toondra.navermodule.webtoon.NaverWebtoonCrawler;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show();
      }
    });

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        this, drawer, toolbar, R.string.navigation_drawer_open,
        R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

    LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayout1);
    linearLayout.setOnClickListener(mClickListener);

    addThumbnailsToLeft(Day.MON);
    /*
    Thread mThread = new Thread(new Runnable() {
      @Override
      public void run() {
        final NaverToonInfo webtoons[] = downloadWebtoons(Day.MON);
        try {
          URL url = new URL(webtoons[0].getthumbUrl());
          final Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
          final Thumbnail thumb = new Thumbnail(getApplicationContext());

          thumb.initView(webtoons[1].getthumbUrl(), webtoons[1].getTitleName());
          Log.e("uploaded toon", url.toString());
          runOnUiThread(new Runnable() {
            @Override
            public void run() {

              LinearLayout leftLinear = (LinearLayout) findViewById(R.id.leftLinearLayout);
              leftLinear.addView(thumb, 0);
              ImageView imageview = (ImageView) findViewById(R.id.title_image1);
              imageview.setImageBitmap(bitmap);
              imageview.getLayoutParams().height = 300;
              imageview.getLayoutParams().width = 300;
              Log.e("ui result?", "done");
            }
          });
        } catch (IOException e) {
          e.printStackTrace();
        }

      }
    });
    mThread.start();
    */


  }

  private int flag = 1;
  private final LinearLayout.OnClickListener mClickListener = new View.OnClickListener() {
    public void onClick(View v) {
      TextView textFruit = (TextView) findViewById(R.id.title_text1);
      if ((flag++) % 2 == 1) {
        textFruit.setText("changed");
      } else {
        textFruit.setText("returned");
      }
    }
  };

  @Override
  public void onBackPressed() {
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @SuppressWarnings("StatementWithEmptyBody")
  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    // Handle navigation view item clicks here.
    int id = item.getItemId();

    if (id == R.id.nav_camera) {
      // Handle the camera action
    } else if (id == R.id.nav_gallery) {

    } else if (id == R.id.nav_slideshow) {

    } else if (id == R.id.nav_manage) {

    } else if (id == R.id.nav_share) {

    } else if (id == R.id.nav_send) {

    }

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  private NaverToonInfo[] downloadWebtoons(Day day) {
    NaverToonInfo toons[] = NaverWebtoonCrawler.downloadWebtoonListByDay(day);
    Log.e("toon name", toons[0].getTitleName());
    return toons;
  }

  private void addThumbnailsToLeft(final Day day) {

    Thread mThread = new Thread(new Runnable() {
      @Override
      public void run() {
        NaverToonInfo webtoons[] = NaverWebtoonCrawler.downloadWebtoonListByDay(day);
        for (int i = 0; i < webtoons.length; i++) {
          try {
            URL url = new URL(webtoons[0].getthumbUrl());
            final Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            final Thumbnail thumb = new Thumbnail(getApplicationContext());
            thumb.initView(webtoons[i].getthumbUrl(), webtoons[i].getTitleName());
            Log.e("uploaded toon", url.toString());
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                LinearLayout leftLinear = (LinearLayout) findViewById(R.id.leftLinearLayout);
                leftLinear.addView(thumb, 0);

                ImageView imageview = (ImageView) findViewById(R.id.title_image1);
                imageview.setImageBitmap(bitmap);

                App mApp = (App)getApplicationContext();
                imageview.getLayoutParams().height = mApp.dpToPixel(120);
                imageview.getLayoutParams().width = mApp.dpToPixel(120);
                Log.e("ui result?", "done");
              }
            });
          } catch (IOException e) {
            e.printStackTrace();
          }

        }
      }
    });
    mThread.start();
  }

}
