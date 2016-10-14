package edu.kaist.mskers.toondra;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import edu.kaist.mskers.toondra.navermodule.NaverToonInfo;
import edu.kaist.mskers.toondra.navermodule.webtoon.Day;
import edu.kaist.mskers.toondra.navermodule.webtoon.NaverWebtoonCrawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

  private ArrayList<Map.Entry<Day, Button>> dayButtonArray = new ArrayList<>();
  private Day currentDay;
  private Thumbnail currentWebtoon = null;
  private static final int MAX_DOWNLOAD_SIZE_BYTES = 1024 * 1024 * 10; // 10MiB

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        this, drawer, toolbar, R.string.navigation_drawer_open,
        R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

    setDayButtons();
    addThumbnailsToLeftByDay(currentDay);

  }

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

  private void addThumbnailsToLeftByDay(final Day day) {
    final LinearLayout leftLinear = (LinearLayout) findViewById(R.id.leftLinearLayout);
    final LinearLayout rightLinear = (LinearLayout) findViewById(R.id.rightLinearLayout);
    leftLinear.removeAllViews();

    /* Download webtoon list by day and set first preview link */
    Thread mThread = new Thread(new Runnable() {
      @Override
      public void run() {
        final NaverToonInfo webtoons[] = NaverWebtoonCrawler.downloadWebtoonListByDay(day);
        for (int i = 0; i < webtoons.length; i++) {
          final Thumbnail thumb = new Thumbnail(getApplicationContext());
          thumb.initView(webtoons[i]);

          final String firstEpisodeUrl = thumb.getFirstEpisodeUrl();

          View.OnClickListener mOnClickListener = new View.OnClickListener() {
            public void onClick(View view) {
              if (currentWebtoon != null) {
                currentWebtoon.thumbName.setBackgroundColor(ContextCompat
                    .getColor(getApplicationContext(), R.color.waitButtonColor));
              }
              thumb.thumbName.setBackgroundColor(ContextCompat
                  .getColor(getApplicationContext(), R.color.activeButtonColor));
              currentWebtoon = thumb;
              rightLinear.removeAllViews();
              startFirstEpisodePreview(firstEpisodeUrl);
            }
          };

          View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
              Log.e("longClick", "ok");
              Intent intent = new Intent(getApplicationContext(), EpisodeListPage.class);
              startActivity(intent);
              return false;
            }
          };

          thumb.thumbLayout.setOnClickListener(mOnClickListener);
          thumb.thumbLayout.setOnLongClickListener(mOnLongClickListener);

          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              leftLinear.addView(thumb);
            }
          });

        }
      }
    });
    mThread.start();
  }

  private void startFirstEpisodePreview(final String firstEpisodeUrl) {
    final LinearLayout rightLinear = (LinearLayout) findViewById(R.id.rightLinearLayout);
    final Thread pageThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          Element wtPage;
          wtPage = Jsoup.connect(firstEpisodeUrl)
              .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
              .get();
          Element wtViewer = wtPage.getElementsByClass("wt_viewer").first();


          for (Element imgLink : wtViewer.children()) {
            String imgUrl = imgLink.absUrl("src");

            // Check whether imgURL is a valid image file
            if (imgUrl == null || !imgUrl.endsWith(".jpg"))
              continue;


            Connection.Response wtRes;
            wtRes = Jsoup.connect(imgUrl)
                .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                .referrer(firstEpisodeUrl)
                .ignoreContentType(true)
                .maxBodySize(MAX_DOWNLOAD_SIZE_BYTES)
                .execute();

            byte[] bitmapData = wtRes.bodyAsBytes();
            BitmapFactory.Options options = new BitmapFactory.Options();;
            options.inSampleSize = 2;
            Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length, options);

            final ImageView pageImage = (ImageView) getLayoutInflater().inflate(R.layout.page_custom, null);

            pageImage.setImageBitmap(bitmap);
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                rightLinear.addView(pageImage);
              }
            });
          }
        } catch (IOException e) {
          e.printStackTrace();
        }

      }
    });
    pageThread.start();
  }

  private void setDayButtons() {
    Calendar calendar = Calendar.getInstance();
    int day = calendar.get(Calendar.DAY_OF_WEEK);
    switch (day) {
      case Calendar.MONDAY:
        currentDay = Day.MON;
      case Calendar.TUESDAY:
        currentDay = Day.TUES;
      case Calendar.WEDNESDAY:
        currentDay = Day.WEDS;
      case Calendar.THURSDAY:
        currentDay = Day.THURS;
      case Calendar.FRIDAY:
        currentDay = Day.FRI;
      case Calendar.SATURDAY:
        currentDay = Day.SAT;
      case Calendar.SUNDAY:
        currentDay = Day.SUN;
    }

    Button mon = (Button) findViewById(R.id.button_mon);
    Button tue = (Button) findViewById(R.id.button_tue);
    Button wed = (Button) findViewById(R.id.button_wed);
    Button thu = (Button) findViewById(R.id.button_thu);
    Button fri = (Button) findViewById(R.id.button_fri);
    Button sat = (Button) findViewById(R.id.button_sat);
    Button sun = (Button) findViewById(R.id.button_sun);

    dayButtonArray.add(new AbstractMap.SimpleEntry<Day, Button>(Day.MON, mon));
    dayButtonArray.add(new AbstractMap.SimpleEntry<Day, Button>(Day.TUES, tue));
    dayButtonArray.add(new AbstractMap.SimpleEntry<Day, Button>(Day.WEDS, wed));
    dayButtonArray.add(new AbstractMap.SimpleEntry<Day, Button>(Day.THURS, thu));
    dayButtonArray.add(new AbstractMap.SimpleEntry<Day, Button>(Day.FRI, fri));
    dayButtonArray.add(new AbstractMap.SimpleEntry<Day, Button>(Day.SAT, sat));
    dayButtonArray.add(new AbstractMap.SimpleEntry<Day, Button>(Day.SUN, sun));

    for (final Map.Entry<Day, Button> entry : dayButtonArray) {
      if (entry.getKey() == currentDay) {
        entry.getValue().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.activeButtonColor));
      }
      entry.getValue().setOnClickListener(new View.OnClickListener() {
        public void onClick(View view) {
          addThumbnailsToLeftByDay(entry.getKey());
          entry.getValue().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.activeButtonColor));
          for (final Map.Entry<Day, Button> currentEntry : dayButtonArray) {
            if (currentEntry.getKey() == currentDay) {
              currentEntry.getValue().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.waitButtonColor));
            }
          }
          currentDay = entry.getKey();
        }
      });
    }
  }
}


