package edu.kaist.mskers.toondra;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import edu.kaist.mskers.toondra.navermodule.webtoon.NaverWebtoonCrawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;

/**
 * Created by harrykim on 2016. 10. 14..
 */

public class ReadEpisodePage extends AppCompatActivity implements ScrollViewListener{
  private BottomNavigationView bottomNavigationView = null;
  private String read_url = null;
  private int latest_id = 0;
  private int episode_id = 0;
  private LinearLayout readLinear = null;
  private boolean isAutoScroll = false;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.episode_read_page);

    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

    Toolbar toolbar = (Toolbar) findViewById(R.id.read_toolbar);
    toolbar.setTitle("");
    setSupportActionBar(toolbar);
    getSupportActionBar().setHomeButtonEnabled(true);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().hide();
    findViewById(R.id.eye_screen_center).setVisibility(View.INVISIBLE);

    readLinear = (LinearLayout) findViewById(R.id.readLinear);
    read_url = getIntent().getStringExtra("read_url");
    latest_id = getIntent().getIntExtra("latest_id", 0);
    episode_id = getIntent().getIntExtra("episode_id", 0);
    Log.d("readUrl", getIntent().getStringExtra("read_url"));
    readEpisode(read_url + episode_id);
    ScrollViewExt readScroll = (ScrollViewExt) findViewById(R.id.readScroll);
    readScroll.setScrollViewListener(this);

    View.OnClickListener onClickListener = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        getSupportActionBar().show();
        bottomNavigationView.setVisibility(View.VISIBLE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
          @Override
          public void run() {
            getSupportActionBar().hide();
            bottomNavigationView.setVisibility(View.INVISIBLE);
          }
        }, 1000);
      }
    };
    readLinear.setOnClickListener(onClickListener);

    bottomNavigationView = (BottomNavigationView)
        findViewById(R.id.bottom_navigation);
    bottomNavigationView.setVisibility(View.INVISIBLE);

    // resize the text on the bottom
    SpannableString spanString = new SpannableString(String.valueOf(episode_id));
    int end = spanString.length();
    spanString.setSpan(new RelativeSizeSpan(2.0f), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    BottomNavigationItemView titleItem = (BottomNavigationItemView) findViewById(R.id.bottom_title_text);
    titleItem.setTitle(spanString);

    bottomNavigationView.setOnNavigationItemSelectedListener(
        new BottomNavigationView.OnNavigationItemSelectedListener() {
          @Override
          public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
              case R.id.prev_episode:
                if (episode_id - 1 <= 0) {
                  Toast toast =  Toast.makeText(getApplicationContext(),
                      "이전화가 존재하지 않습니다.", Toast.LENGTH_LONG);
                  toast.show();
                } else {
                  episode_id -= 1;
                  readEpisode(read_url + episode_id);
                }
                break;
              case R.id.bottom_title_text:
                break;
              case R.id.next_episode:
                if (latest_id == episode_id) {
                  Toast toast =  Toast.makeText(getApplicationContext(),
                      "현재 페이지가 가장 최신화입니다.", Toast.LENGTH_LONG);
                  toast.show();
                } else {
                  episode_id += 1;
                  readEpisode(read_url + episode_id);
                }
                break;
            }
            return false;
          }
        });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.read_main_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    if (id == R.id.read_eye) {
      if (isAutoScroll == false) {
        isAutoScroll = true;
        findViewById(R.id.eye_screen_center).setVisibility(View.VISIBLE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
          @Override
          public void run() {
            findViewById(R.id.eye_screen_center).setVisibility(View.INVISIBLE);
          }
        }, 1000);
      }
      else {
        isAutoScroll = false;
      }
        return true;
    } else if (id == android.R.id.home) {
      finish();
    }

    return super.onOptionsItemSelected(item);
  }

  private void readEpisode(final String firstEpisodeUrl) {
    readLinear.removeAllViews();
    Thread pageThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          Element wtPage;
          wtPage = Jsoup.connect(firstEpisodeUrl)
              .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0)"
                  + "Gecko/20100101 Firefox/23.0")
              .get();
          Element wtViewer = wtPage.getElementsByClass("wt_viewer").first();

          if (wtViewer == null) {
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                Toast toast =  Toast.makeText(getApplicationContext(),
                    "19세 웹툰 이용을 위해서는 성인 인증이 필요합니다.",
                    Toast.LENGTH_LONG);
                toast.show();
              }
            });
            return;
          }

          for (Element imgLink : wtViewer.children()) {
            Log.d("ThumbImgLink", imgLink.toString());
            String imgUrl = imgLink.absUrl("src");

            // Check whether imgURL is a valid image file
            if (imgUrl == null || !imgUrl.endsWith(".jpg")) {
              continue;
            }

            Connection.Response wtRes;
            wtRes = Jsoup.connect(imgUrl)
                .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0)"
                    + "Gecko/20100101 Firefox/23.0")
                .referrer(firstEpisodeUrl)
                .ignoreContentType(true)
                .maxBodySize(NaverWebtoonCrawler.MAX_DOWNLOAD_SIZE_BYTES)
                .execute();

            byte[] bitmapData = wtRes.bodyAsBytes();
            /*
            BitmapFactory.Options options = new BitmapFactory.Options();;
            options.inSampleSize = 2;
            */
            Bitmap bitmap = BitmapFactory
                .decodeByteArray(bitmapData, 0, bitmapData.length);

            final ImageView pageImage = (ImageView)getLayoutInflater().inflate(R.layout.page_custom, null);

            pageImage.setImageBitmap(bitmap);
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                readLinear.addView(pageImage);
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
      bottomNavigationView.setVisibility(View.VISIBLE);
      getSupportActionBar().show();
    } else {
      if (bottomNavigationView.getVisibility() == View.VISIBLE) {
        bottomNavigationView.setVisibility(View.INVISIBLE);
      }
      if (getSupportActionBar().isShowing()) {
        getSupportActionBar().hide();
      }
    }
  }
}
