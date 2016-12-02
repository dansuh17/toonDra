package edu.kaist.mskers.toondra.navermodule.webtoon;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import edu.kaist.mskers.toondra.navermodule.NaverToonCategory;
import edu.kaist.mskers.toondra.navermodule.NaverToonInfo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;


/**
 * This class provides utility methods to crawl naver webtoon.
 */
public class NaverWebtoonCrawler {
  public static final int MAX_DOWNLOAD_SIZE_BYTES = 1024 * 1024 * 10; // 10MiB
  /**
   * Download available webtoon information for given day.
   *
   * @param day the day you want to download webtoon list from
   * @return Available webtoon information for the given day
   */
  public static NaverToonInfo[] downloadWebtoonListByDay(Day day) {
    // Create necessary references
    NaverToonInfo[] info;
    String url = "";
    String thumbUrl;
    Document doc;
    Element img;
    Element link;
    Matcher mat;

    // Try connect to url.
    try {
      url = NaverWebtoonUrl.getDayListUrl(day);
      doc = Jsoup.connect(url)
          .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
          .get();
    } catch (IOException ex) {
      ex.printStackTrace();
      return null;
    }


    // Grab available webtoon lists
    Element content = doc.getElementById("content");
    Elements imgList = content.getElementsByClass("img_list").first().children();
    int webtoonTotal = imgList.size(); // total number of webtoons for this day

    info = new NaverToonInfo[webtoonTotal];

    String href;
    for (int i = 0; i < webtoonTotal; i++) {
      img = imgList.get(i);
      link = img.getElementsByClass("thumb").first()
          .getElementsByTag("a").first();
      href = link.attr("href");

      thumbUrl = link.child(0).absUrl("src");
      Log.e("thumbUrl:", thumbUrl);
      try {
        URL imgUrl = new URL(thumbUrl);
        Bitmap bitmap = BitmapFactory.decodeStream(imgUrl.openConnection().getInputStream());
        // Use Regex to pull title id from the href link
        mat = NaverWebtoonUrl.titleIdPat.matcher(href);
        mat.find();
        info[i] = new NaverToonInfo(mat.group(1), link.attr("title"),
            bitmap,
            NaverToonCategory.WEBTOON);
      } catch (IOException ex) {
        ex.printStackTrace();
      }

    }

    return info;
  }

}
