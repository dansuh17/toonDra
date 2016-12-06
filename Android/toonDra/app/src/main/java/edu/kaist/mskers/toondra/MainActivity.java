package edu.kaist.mskers.toondra;

import static edu.kaist.mskers.toondra.navermodule.webtoon.Day.ALL;
import static edu.kaist.mskers.toondra.navermodule.webtoon.Day.FRI;
import static edu.kaist.mskers.toondra.navermodule.webtoon.Day.MON;
import static edu.kaist.mskers.toondra.navermodule.webtoon.Day.SAT;
import static edu.kaist.mskers.toondra.navermodule.webtoon.Day.SUN;
import static edu.kaist.mskers.toondra.navermodule.webtoon.Day.THURS;
import static edu.kaist.mskers.toondra.navermodule.webtoon.Day.TUES;
import static edu.kaist.mskers.toondra.navermodule.webtoon.Day.WEDS;
import static edu.kaist.mskers.toondra.navermodule.webtoon.NaverWebtoonCrawler.downloadWebtoonListByDay;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.SystemClock;
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

import edu.kaist.mskers.toondra.navermodule.NaverToonInfo;
import edu.kaist.mskers.toondra.navermodule.webtoon.Day;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

  private ArrayList<Map.Entry<Day, Button>> dayButtonArray = new ArrayList<>();
  private NaverToonInfo[][] webtoonInfoByDay = new NaverToonInfo[7][];
  private Day currentDay;

  /* 0 -> preViewModeFragment now, 1 -> GridViewModeFragment now */
  private int currentFragment = 0;
  private final int previewModeFlag = 0;
  private final int gridviewModeFlag = 1;

  private PreviewModeFragment previewModeFragment;
  private GridViewModeFragment gridViewModeFragment;

  private long lastClickTime = 0;


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

    //Hide Navigation bar now. It will be implemented in future.
    toolbar.setNavigationIcon(null);

    downloadAllWebtoons();

    setRealCurrentDay();
    setDayButtons();

    initializeFragments();
  }
  /**
   * Catch the back press button press on the screen.
   */
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

    if (id == R.id.change_fragment) {
      changeFragment(item);
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

  /**
   * Get the current day which is selected by the user, default is real-time current day.
   */
  public Day getCurrentDay() {
    return currentDay;
  }

  /**
   * Set the current day which is selected by the user, default is real-time current day.
   */
  public void setCurrentDay(Day day) {
    currentDay = day;
  }

  public ArrayList<Map.Entry<Day, Button>> getDayButtonArray() {
    return dayButtonArray;
  }


  /**
   * Change the fragment to either preview mode or gridView mode.
   * Icon on the top-right will be changed accordingly.
   */
  public void changeFragment(MenuItem item) {
    Log.e("ChangeFragment", "" + currentFragment);
    Fragment currentFragmentObj;
    Fragment nextFragmentObj;
    switch (currentFragment) {
      default:
      case previewModeFlag: {
        currentFragmentObj = previewModeFragment;
        nextFragmentObj = gridViewModeFragment;
        currentFragment = gridviewModeFlag;
        item.setIcon(R.drawable.preview_menu);
        break;
      }
      case gridviewModeFlag: {
        currentFragmentObj = gridViewModeFragment;
        nextFragmentObj = previewModeFragment;
        currentFragment = previewModeFlag;
        item.setIcon(R.drawable.gridview_menu);
        break;
      }
    }

    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    fragmentTransaction.hide(currentFragmentObj);
    fragmentTransaction.show(nextFragmentObj);
    addThumbnails();
    fragmentTransaction.commit();
  }

  /**
   * Get the current webtoon info list of the selected day.
   */
  public NaverToonInfo[] getCurrentWebtoonsByDay(Day day) {
    return webtoonInfoByDay[dayToIndex(day)];
  }

  /**
   * Change the Enum day type to corresponding integer index.
   */
  public int dayToIndex(Day day) {
    int index = -1;
    switch (day) {
      case MON:
        index = 0;
        break;
      case TUES:
        index = 1;
        break;
      case WEDS:
        index = 2;
        break;
      case THURS:
        index = 3;
        break;
      case FRI:
        index = 4;
        break;
      case SAT:
        index = 5;
        break;
      case SUN:
        index = 6;
        break;
      default:
        index = 0;
        break;
    }
    return index;
  }

  /**
   * Set the current day as the real-time current day.
   */
  private void setRealCurrentDay() {
    Calendar calendar = Calendar.getInstance();
    int day = calendar.get(Calendar.DAY_OF_WEEK);
    switch (day) {
      case Calendar.MONDAY:
        currentDay = MON;
        break;
      case Calendar.TUESDAY:
        currentDay = TUES;
        break;
      case Calendar.WEDNESDAY:
        currentDay = WEDS;
        break;
      case Calendar.THURSDAY:
        currentDay = THURS;
        break;
      case Calendar.FRIDAY:
        currentDay = FRI;
        break;
      case Calendar.SATURDAY:
        currentDay = SAT;
        break;
      case Calendar.SUNDAY:
        currentDay = SUN;
        break;
      default:
        currentDay = MON;
        break;
    }
  }

  /**
   * Set the day buttons with click detection.
   */
  private void setDayButtons() {
    Button mon = (Button) findViewById(R.id.button_mon);
    Button tue = (Button) findViewById(R.id.button_tue);
    Button wed = (Button) findViewById(R.id.button_wed);
    Button thu = (Button) findViewById(R.id.button_thu);
    Button fri = (Button) findViewById(R.id.button_fri);
    Button sat = (Button) findViewById(R.id.button_sat);
    Button sun = (Button) findViewById(R.id.button_sun);

    dayButtonArray.add(new AbstractMap.SimpleEntry<Day, Button>(MON, mon));
    dayButtonArray.add(new AbstractMap.SimpleEntry<Day, Button>(TUES, tue));
    dayButtonArray.add(new AbstractMap.SimpleEntry<Day, Button>(WEDS, wed));
    dayButtonArray.add(new AbstractMap.SimpleEntry<Day, Button>(THURS, thu));
    dayButtonArray.add(new AbstractMap.SimpleEntry<Day, Button>(FRI, fri));
    dayButtonArray.add(new AbstractMap.SimpleEntry<Day, Button>(SAT, sat));
    dayButtonArray.add(new AbstractMap.SimpleEntry<Day, Button>(SUN, sun));

    setDayButtonClick();
  }

  /**
   * When the day button is clicked, it should give corresponding webtoon list views.
   */
  private void setDayButtonClick() {
    for (final Map.Entry<Day, Button> entry : dayButtonArray) {
      if (entry.getKey() == currentDay) {
        entry.getValue().setBackgroundColor(ContextCompat.getColor(getApplicationContext(),
            R.color.activeButtonColor));
      }
      entry.getValue().setOnClickListener(new View.OnClickListener() {
        public void onClick(View view) {
          if (!checkElapsedTimeIsValid()) {
            return;
          }
          dayButtonArray.get(dayToIndex(currentDay)).getValue().setBackgroundColor(ContextCompat
              .getColor(getApplicationContext(), R.color.waitButtonColor));
          entry.getValue().setBackgroundColor(ContextCompat
              .getColor(getApplicationContext(), R.color.activeButtonColor));
          setCurrentDay(entry.getKey());
          Log.e("current_day", currentDay.toString());
          addThumbnails();
        }
      });
    }
  }

  /**
   * Add thumbnails to the main view, according to the current mode.
   */
  private void addThumbnails() {
    if (currentFragment == previewModeFlag) {
      previewModeFragment.addThumbnailsToLeftByDay(currentDay);
    } else if (currentFragment == gridviewModeFlag) {
      gridViewModeFragment.addThumbnailsToGrid(currentDay);
    } else {
      Log.e("flag", "Invalid flag error");
      finish();
    }
  }

  /**
   * Download all the webtoon information uploaded in the website.
   */
  private void downloadAllWebtoons() {
    for (final Day day : Day.values()) {
      if (day == ALL) {
        continue;
      }
      Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
          if (webtoonInfoByDay[dayToIndex(day)] == null) {
            NaverToonInfo[] webtoonInfo = downloadWebtoonListByDay(day);
            webtoonInfoByDay[dayToIndex(day)] = webtoonInfo;
          }
        }
      });
      thread.start();

      try {
        thread.join();
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }
    }
  }

  /**
   * Check the elapsed time after the click is occurred.
   */
  private boolean checkElapsedTimeIsValid() {
    // preventing double, using threshold of 300 ms
    if (SystemClock.elapsedRealtime() - lastClickTime < 300) {
      return false;
    }
    lastClickTime = SystemClock.elapsedRealtime();
    return true;
  }

  /**
   * Initialize the preview and gridView fragments to enable conversion between them.
   */
  private void initializeFragments() {
    previewModeFragment = new PreviewModeFragment();
    gridViewModeFragment = new GridViewModeFragment();

    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    fragmentTransaction.add(R.id.container, previewModeFragment);
    fragmentTransaction.add(R.id.container, gridViewModeFragment);
    fragmentTransaction.hide(gridViewModeFragment);
    fragmentTransaction.commit();
  }
}
