package edu.kaist.mskers.toondra;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import edu.kaist.mskers.toondra.navermodule.webtoon.NaverWebtoonCrawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created by harrykim on 2016. 10. 14..
 */

public class EpisodeListPage extends AppCompatActivity implements ScrollViewListener {
  private int latest_episode = 0;
  private int next_page_episode = -1;
  private int current_page = 1;
  private String listViewUrlPrefix = null;
  private Thread pageThread = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.episode_list_page);

    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

    Toolbar toolbar = (Toolbar) findViewById(R.id.list_toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setTitle(getIntent().getStringExtra("webtoon_name"));
    getSupportActionBar().setHomeButtonEnabled(true);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    ScrollViewExt readScroll = (ScrollViewExt) findViewById(R.id.episodeListScroll);
    readScroll.setScrollViewListener(this);

    listViewUrlPrefix = getIntent().getStringExtra("listview_url");
    startListViewUrl(listViewUrlPrefix, current_page, true);
    /*
    if (pageThread != null) {
      try {
        pageThread.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    boolean loadNoMore = isScrollable();
    while (!loadNoMore) {
      if (next_page_episode - 10 >= 1) {
        Log.e("next_page_episode", "" + next_page_episode);
        if (pageThread != null) {
          try {
            pageThread.join();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        startListViewUrl(listViewUrlPrefix, current_page, false);
        loadNoMore = isScrollable();
      } else {
        break;
      }
    }
    */
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    //getMenuInflater().inflate(R.menu.read_main_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == android.R.id.home) {
      finish();
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Add the first episode to the right side of the main view by giving first episode url.
   */
  private void startListViewUrl(String listViewUrlPrefix, int pageNumber, final boolean checkLatest) {
    current_page++;
    if (!checkLatest) {
      next_page_episode -= 10;
    }
    final String listViewUrl = listViewUrlPrefix + "&page=" + pageNumber;
    final LinearLayout listLinear = (LinearLayout) findViewById(R.id.episodeListLinear);
    pageThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          Element wtPage;
          Log.d("listview_url", listViewUrl);
          wtPage = Jsoup.connect(listViewUrl)
              .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0)"
                  + "Gecko/20100101 Firefox/23.0")
              .get();

          //Webtoon name and Writer info parser
          Element comicInfo = wtPage.getElementsByClass("comicInfo").first()
              .getElementsByClass("detail").first();


          Element table = wtPage.select("table").get(0);
          Elements rows = table.select("tr");

          Log.e("table", table.toString());
          Log.e("rows", rows.toString());

          if (rows == null) {
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                Toast toast = Toast.makeText(getApplicationContext(),
                    "19세 웹툰 이용을 위해서는 성인 인증이 필요합니다.",
                    Toast.LENGTH_LONG);
                toast.show();
              }
            });
            return;
          }

          boolean is_latest = true;
          for (int i = 0; i < rows.size(); i++) {
            Element episodeLink = rows.get(i);
            Log.e("episodeLink", episodeLink.toString());
            Elements cols = episodeLink.select("td");
            Log.e("cols", cols.toString());
            if (cols.size() == 0) {
              continue;
            }

            Element nextLink = cols.get(0).select("a").first();
            Log.e("nextLink", nextLink.toString());
            String nextUrl = nextLink.absUrl("href");
            Log.e("nextUrl", nextLink.toString());
            Element imgLink = cols.get(0).select("a").first().select("img").first();
            if (imgLink == null) {
              continue;
            }
            String imgUrl = imgLink.absUrl("src");
            if (imgUrl == null) {
              continue;
            }
            String description = imgLink.attr("title");
            if (description == null) {
              continue;
            }
            /*
            Log.e("description", description.toString());
            Log.e("1", (!nextUrl.toString().contains("no=")) +"");
            Log.e("2", (imgUrl == null) +"");
            Log.e("3", (!imgUrl.endsWith(".JPG")) +"");
            Log.e("4", (!imgUrl.toString().contains("inst_thumbnail")) +"");
            */

            // Check whether imgURL is a valid image file
            if (!nextUrl.toString().toLowerCase().contains("no=") || imgUrl == null
                || !imgUrl.toLowerCase().endsWith(".jpg")
                || !imgUrl.toString().toLowerCase().contains("inst_thumbnail")) {
              continue;
            }

            Connection.Response wtRes;
            wtRes = Jsoup.connect(imgUrl)
                .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0)"
                    + "Gecko/20100101 Firefox/23.0")
                .referrer(listViewUrl)
                .ignoreContentType(true)
                .maxBodySize(NaverWebtoonCrawler.MAX_DOWNLOAD_SIZE_BYTES)
                .execute();

            byte[] bitmapData = wtRes.bodyAsBytes();
            /* size sampling option
            BitmapFactory.Options options = new BitmapFactory.Options();;
            options.inSampleSize = 1;
            */
            Bitmap bitmap = BitmapFactory
                .decodeByteArray(bitmapData, 0, bitmapData.length);

            Log.e("nextUrl", nextUrl);
            String[] st = nextUrl.split("no=");
            Log.e("st", st[0]);
            int episodeId = Integer.valueOf(st[1].split("&")[0]);
            if (is_latest) {
              if (checkLatest) {
                latest_episode = episodeId;
                next_page_episode = latest_episode;
                is_latest = false;
              }
            }
            final EpisodeListInstance instance = new EpisodeListInstance(EpisodeListPage.this);
            instance.initView(bitmap, description, nextUrl, episodeId);
            instance.episodeLayout.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                Intent intent = new Intent(EpisodeListPage.this, ReadEpisodePage.class);
                intent.putExtra("read_url", instance.getEpisodeUrl().split("no=")[0] + "no=");
                intent.putExtra("episode_id", instance.getEpisodeId());
                intent.putExtra("latest_id", latest_episode);
                startActivity(intent);
              }
            });


            /*
            Runnable uiRunnable = new Runnable() {
              @Override
              public void run() {
                listLinear.addView(instance);
                synchronized (this) {
                  this.notify();
                }
              }
            };
            synchronized (uiRunnable) {
              runOnUiThread(uiRunnable);
              Log.e("wait for uiRunnable", "dd");
              uiRunnable.wait();
            }
            */


            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                listLinear.addView(instance);
              }
            });

          }
        } catch (IOException ex) {
          ex.printStackTrace();
        }

      }
    });
    pageThread.start();
  }

  @Override
  public void onScrollChanged(ScrollViewExt scrollView, int x, int y, int oldx, int oldy) {
    // We take the last son in the scrollview
    View view = (View) scrollView.getChildAt(scrollView.getChildCount() - 1);
    int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));

    // if diff is zero, then the bottom has been reached
    if (diff == 0) {
      // When the next_page episode is on 10, then there would be no next page.
      if (next_page_episode - 10 >= 1) {
        Log.e("next_page_episode", "" + next_page_episode);
        if (pageThread != null) {
          try {
            pageThread.join();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        startListViewUrl(listViewUrlPrefix, current_page, false);
        /*
        try {
          Thread.sleep(1500);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        */
      }
    }
  }
  /*
  public boolean isScrollable() {
    ScrollViewExt readScroll = (ScrollViewExt) findViewById(R.id.episodeListScroll);
    startListViewUrl(listViewUrlPrefix, current_page, true);
    int childHeight = ((LinearLayout) findViewById(R.id.episodeListLinear)).getHeight();
    boolean result = readScroll.getHeight() < childHeight + readScroll.getPaddingTop() + readScroll.getPaddingBottom();
    return result;
  }
  */
}
