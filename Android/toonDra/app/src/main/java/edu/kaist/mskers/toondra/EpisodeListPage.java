package edu.kaist.mskers.toondra;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

public class EpisodeListPage extends AppCompatActivity {
  private int latest_episode = 0;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.episode_list_page);

    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
    getSupportActionBar().setTitle("Sample listview from Naver Webtoon");

    String listViewUrl = getIntent().getStringExtra("listview_url");
    startListViewUrl(listViewUrl);
  }

  /**
   * Add the first episode to the right side of the main view by giving first episode url.
   */
  private void startListViewUrl(final String listViewUrl) {
    final LinearLayout listLinear = (LinearLayout) findViewById(R.id.episodeListLinear);
    Thread pageThread = new Thread(new Runnable() {
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
                Toast toast =  Toast.makeText(getApplicationContext(),
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
            String nextUrl = nextLink.absUrl("href");
            Element imgLink = cols.get(0).select("a").first().select("img").first();
            if (imgLink == null) {
              continue;
            }

            String imgUrl = imgLink.absUrl("src");
            String description = imgLink.attr("title");

            // Check whether imgURL is a valid image file
            if (!nextUrl.toString().contains("no=") || imgUrl == null || !imgUrl.endsWith(".jpg")
                || !imgUrl.toString().contains("inst_thumbnail")) {
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
            int episodeId =  Integer.valueOf(st[1].split("&")[0]);
            if (is_latest) {
              latest_episode = episodeId;
              is_latest = false;
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
}
